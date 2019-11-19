package connection;

import events.*;
import model.Tile.FieldType;
import model.Tile.FieldEffect;
import model.Tile.Direction;
import model.Ship.ActorType;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePackException;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.ArrayBufferInput;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import zmq.ZError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;


public class ClientConnection<E> implements AutoCloseable {
    static final class Events {
        private Events() {}

        static final int REGISTRATIONABORTED = 0;
        static final int GAMESTARTED = 1;
        static final int MOVED = 2;
        static final int MAPUPDATE = 3;
        static final int ACTNOW = 4;
        static final int GAMEEND = 5;
        static final int NEXTCYCLE = 6;
        static final int DAMAGED = 7;
        static final int ROBBED = 8;
        static final int REPAIRED = 9;
        static final int PICKEDUP = 10;
        static final int HIT = 11;
        static final int SOLD = 12;
        static final int DROPPED = 13;
        static final int RELOADED = 14;
        static final int RESTOCKED = 15;
        static final int SWIRLEFFECT = 16;
        static final int STORMEFFECT = 17;
        static final int COMMANDFAILED = 18;
    }

    private final ZContext context;
    private final ZMQ.Socket socket;
    private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
    private final ArrayBufferInput inputBuffer = new ArrayBufferInput(new byte[0]);
    private final MessagePacker packer;
    private final MessageUnpacker unpacker;
    private final EventFactory<? extends E> eventFactory;
    private boolean closed;

    /**
     * Nutzen Sie eine Instanz dieser Klasse, um eine Verbindung zum Server aufzubauen.
     * @param host der hostname (oder IP-Adresse) des Spielservers.
     * @param port die Portnummer des Spielservers.
     * @param timeout gibt in ms an, wie lange auf ein Event vom Server gewartet werden darf, bevor eine TimeoutException geworfen wird.
     *                Der besondere Wert -1 bedeutet, dass ewig gewartet werden darf.
     * @param eventFactory Ihre Implementierung einer EventFactory, mit welcher Sie Ihre eigenen Events erstellen können.
     */
    public ClientConnection(String host, int port, int timeout, EventFactory<? extends E> eventFactory) {
        Objects.requireNonNull(host);
        if (port <= 1023) throw new IllegalArgumentException("Portnummer zu niedrig! (Siehe https://de.wikipedia.org/wiki/Transmission_Control_Protocol#Allgemeines)");
        if (port >= 65535) throw new IllegalArgumentException("Portnummer zu groß! (Siehe https://de.wikipedia.org/wiki/Transmission_Control_Protocol#Allgemeines");
        if (timeout == 0) throw new IllegalArgumentException("Timeout darf nicht 0 sein!");
        if (timeout < -1 ) throw new IllegalArgumentException("Timeout darf nicht kleiner als -1 sein!");
        this.eventFactory = Objects.requireNonNull(eventFactory);

        unpacker = MessagePack.newDefaultUnpacker(inputBuffer);
        packer = MessagePack.newDefaultPacker(outputBuffer);

        context = new ZContext();
        socket = context.createSocket(ZMQ.DEALER);
        socket.setReceiveTimeOut(timeout);
        socket.connect(String.format("tcp://%s:%d", host, port));
    }

    @Override
    public void close() {
        if (!closed) {
            context.destroy();
            closed = true;
        }
    }

    /**
     * Empfängt das nächste Event vom Spielserver und übersetzt es mittels Ihrer Factory in Ihre Implementierung des Events.
     */
    public final E nextEvent() throws TimeoutException {
        byte[] data; // receive payload (a ROUTER socket does not send its identity)
        try {
            data = socket.recv();
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        }
        if (data == null) throw new TimeoutException();
        inputBuffer.reset(data); // wrap unpacker around payload
        try {
            int type = unpacker.unpackInt();
            switch (type) {
                case Events.REGISTRATIONABORTED: {
                    return eventFactory.createRegistrationAborted();
                }
                case Events.GAMESTARTED: {
                    int x = unpacker.unpackInt();
                    int y = unpacker.unpackInt();
                    int maxCycles = unpacker.unpackInt();
                    int numTreasures = unpacker.unpackInt();
                    int numPirates = unpacker.unpackInt();
                    int numHarbors = unpacker.unpackInt();
                    return eventFactory.createGameStarted(x, y, maxCycles, numTreasures, numPirates, numHarbors);
                }
                case Events.MOVED: {
                    int x = unpacker.unpackInt();
                    int y = unpacker.unpackInt();
                    return eventFactory.createMoved(x, y);
                }
                case Events.MAPUPDATE: {
                    int x = unpacker.unpackInt();
                    int y = unpacker.unpackInt();
                    boolean pirate = unpacker.unpackBoolean();
                    int treasure = unpacker.unpackInt();
                    boolean north = unpacker.unpackBoolean();
                    boolean east = unpacker.unpackBoolean();
                    boolean south = unpacker.unpackBoolean();
                    boolean west = unpacker.unpackBoolean();
                    FieldType fieldType = FieldType.valueOf(unpacker.unpackString());
                    FieldEffect fieldEffect = FieldEffect.valueOf(unpacker.unpackString());
                    return eventFactory.createMapUpdate(x, y, pirate, treasure, north, east, south, west, fieldType, fieldEffect);
                }
                case Events.ACTNOW: {
                    int actionsLeft = unpacker.unpackInt();
                    return eventFactory.createActNow(actionsLeft);
                }
                case Events.GAMEEND: {
                    int score = unpacker.unpackInt();
                    return eventFactory.createGameEnd(score);
                }
                case Events.NEXTCYCLE: {
                    int cyclesLeft = unpacker.unpackInt();
                    return eventFactory.createNextCycle(cyclesLeft);
                }
                case Events.DAMAGED: {
                    int damage = unpacker.unpackInt();
                    return eventFactory.createDamaged(damage);
                }
                case Events.ROBBED: {
                    return eventFactory.createRobbed();
                }
                case Events.REPAIRED: {
                    int cost = unpacker.unpackInt();
                    return eventFactory.createRepaired(cost);
                }
                case Events.PICKEDUP: {
                    int value = unpacker.unpackInt();
                    return eventFactory.createPickedUp(value);
                }
                case Events.HIT: {
                    return eventFactory.createHit();
                }
                case Events.SOLD: {
                    int gold = unpacker.unpackInt();
                    return eventFactory.createSold(gold);
                }
                case Events.DROPPED: {
                    int value = unpacker.unpackInt();
                    return eventFactory.createDropped(value);
                }
                case Events.RELOADED: {
                    return eventFactory.createReloaded();
                }
                case Events.RESTOCKED: {
                    int amount = unpacker.unpackInt();
                    return eventFactory.createRestocked(amount);
                }
                case Events.SWIRLEFFECT: {
                    int x = unpacker.unpackInt();
                    int y = unpacker.unpackInt();
                    return eventFactory.createSwirlEffect(x, y);
                }
                case Events.STORMEFFECT: {
                    return eventFactory.createStormEffect();
                }
                case Events.COMMANDFAILED: {
                    String message = unpacker.unpackString();
                    return eventFactory.createCommandFailed(message);
                }
                default: throw new CommException("Unbekannter Eventtyp!");
            }
        } catch (IOException | MessagePackException e) {
            throw new CommException("Fehler beim Lesen des nächsten Events!", e);
        }
    }


    public final void sendRegister(String name, ActorType actorType) {
        try {
            packer.packInt(ServerConnection.Commands.REGISTER);
            packer.packString(name);
            packer.packString(actorType.name());
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Register' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendMove(Direction direction) {
        try {
            packer.packInt(ServerConnection.Commands.MOVE);
            packer.packString(direction.name());
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Move' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendEndTurn() {
        try {
            packer.packInt(ServerConnection.Commands.ENDTURN);
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'EndTurn' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendRepair() {
        try {
            packer.packInt(ServerConnection.Commands.REPAIR);
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Repair' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendFire(Direction direction) {
        try {
            packer.packInt(ServerConnection.Commands.FIRE);
            packer.packString(direction.name());
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Fire' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendPickup() {
        try {
            packer.packInt(ServerConnection.Commands.PICKUP);
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Pickup' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendSell() {
        try {
            packer.packInt(ServerConnection.Commands.SELL);
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Sell' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendDrop(int index) {
        try {
            packer.packInt(ServerConnection.Commands.DROP);
            packer.packInt(index);
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Drop' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendReload() {
        try {
            packer.packInt(ServerConnection.Commands.RELOAD);
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Reload' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendRestock(int amount) {
        try {
            packer.packInt(ServerConnection.Commands.RESTOCK);
            packer.packInt(amount);
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Restock' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendLeave() {
        try {
            packer.packInt(ServerConnection.Commands.LEAVE);
            packer.flush();
            socket.send(String.valueOf(outputBuffer.toByteArray()));
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Leave' Command konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Clientseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }
}
