package connection;

import commands.CommandFactory;
import model.Ship.ActorType;
import model.Tile.Direction;
import model.Tile.FieldEffect;
import model.Tile.FieldType;
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
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class ServerConnection<C> implements AutoCloseable {
    static final class Commands {
        private Commands() {}

        static final int REGISTER = 0;
        static final int MOVE = 1;
        static final int ENDTURN = 2;
        static final int REPAIR = 3;
        static final int FIRE = 4;
        static final int PICKUP = 5;
        static final int SELL = 6;
        static final int DROP = 7;
        static final int RELOAD = 8;
        static final int RESTOCK = 9;
        static final int LEAVE = 10;
    }

    private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
    private final ArrayBufferInput inputBuffer = new ArrayBufferInput(new byte[0]);
    private final ZContext context;
    private final ZMQ.Socket socket;
    private final MessagePacker packer;
    private final MessageUnpacker unpacker;
    private final CommandFactory<? extends C> commandFactory;
    private boolean closed;
    private boolean idSet = false;
    private int id;

    /**
     * Nutzen Sie diese Klasse, um eine Verbindung zu einem Client aufzubauen.
     * @param port die Portnummer des Client.
     * @param timeout gibt in ms an, wie lange auf ein Event vom Server gewartet werden darf, bevor eine TimeoutException geworfen wird.
     *                Der besondere Wert -1 bedeutet, dass ewig gewartet werden darf.
     * @param commandFactory Ihre Implementierung einer CommandFactory, mit welcher Sie Ihre eigenen Commands erstellen können.
     */
    public ServerConnection(int port, int timeout, CommandFactory<? extends C> commandFactory) {
        if (port <= 1023) throw new IllegalArgumentException("Portnummer zu niedrig! (Siehe https://de.wikipedia.org/wiki/Transmission_Control_Protocol#Allgemeines)");
        if (port >= 65535) throw new IllegalArgumentException("Portnummer zu groß! (Siehe https://de.wikipedia.org/wiki/Transmission_Control_Protocol#Allgemeines");
        this.commandFactory = Objects.requireNonNull(commandFactory);

        unpacker = MessagePack.newDefaultUnpacker(inputBuffer);
        packer = MessagePack.newDefaultPacker(outputBuffer);

        context = new ZContext();
        socket = context.createSocket(ZMQ.ROUTER);
        socket.setReceiveTimeOut(timeout);
        socket.setRouterMandatory(true); // ensure errors from the commlib are caught
        socket.bind(String.format("tcp://*:%d", port));
    }

    @Override
    public void close() {
        if (!closed) {
            context.destroy();
            closed = true;
        }
    }

    private static int commId(byte[] identity) {
        if (identity.length != 5) throw new IllegalArgumentException("Wrong ZMQ Identity!");
        ByteBuffer buffer = ByteBuffer.wrap(identity, 1, 4);
        return buffer.getInt();
    }

    private static byte[] zmqId(int value) {
        return new byte[] {
                (byte)0,
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public final C nextCommand() throws TimeoutException {
        byte[] identity; // receive sender identity
        try {
            identity = socket.recv();
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        }
        if (identity == null) throw new TimeoutException();
        int tempId = commId(identity);
        if (!idSet) {
            id = tempId;
            idSet = true;
        } else if (id != tempId) {
            return nextCommand();
        }
        byte[] data = socket.recv(); // receive payload
        inputBuffer.reset(data); // wrap unpacker around payload
        try {
            int type = unpacker.unpackInt();
            switch (type) {
                case Commands.REGISTER: {
                    String name = unpacker.unpackString();
                    ActorType actorType = ActorType.valueOf(unpacker.unpackString());
                    return commandFactory.createRegister(name, actorType);
                }
                case Commands.MOVE: {
                    Direction direction = Direction.valueOf(unpacker.unpackString());
                    return commandFactory.createMove(direction);
                }
                case Commands.ENDTURN: {
                    return commandFactory.createEndTurn();
                }
                case Commands.REPAIR: {
                    return commandFactory.createRepair();
                }
                case Commands.FIRE: {
                    Direction direction = Direction.valueOf(unpacker.unpackString());
                    return commandFactory.createFire(direction);
                }
                case Commands.PICKUP: {
                    return commandFactory.createPickup();
                }
                case Commands.SELL: {
                    return commandFactory.createSell();
                }
                case Commands.DROP: {
                    int index = unpacker.unpackInt();
                    return commandFactory.createDrop(index);
                }
                case Commands.RELOAD: {
                    return commandFactory.createReload();
                }
                case Commands.RESTOCK: {
                    int amount = unpacker.unpackInt();
                    return commandFactory.createRestock(amount);
                }
                case Commands.LEAVE: {
                    return commandFactory.createLeave();
                }
                default: throw new CommException("Unbekannter Commandtyp!");
            }
        } catch (IOException | MessagePackException e) {
            throw new CommException("Fehler beim Lesen des nächsten Commands!", e);
        }
    }


    public final void sendRegistrationAborted() {
        try {
            packer.packInt(ClientConnection.Events.REGISTRATIONABORTED);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2 );
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'RegistrationAborted' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendGameStarted(int x, int y, int maxCycles, int numTreasures, int numPirates, int numHarbors) {
        try {
            packer.packInt(ClientConnection.Events.GAMESTARTED);
            packer.packInt(x);
            packer.packInt(y);
            packer.packInt(maxCycles);
            packer.packInt(numTreasures);
            packer.packInt(numPirates);
            packer.packInt(numHarbors);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2 );
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'GameStarted' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendMoved(int x, int y) {
        try {
            packer.packInt(ClientConnection.Events.MOVED);
            packer.packInt(x);
            packer.packInt(y);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Moved' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendMapUpdate(int x, int y, boolean pirate, int treasure, boolean north, boolean east, boolean south, boolean west, FieldType fieldType, FieldEffect fieldEffect) {
        try {
            packer.packInt(ClientConnection.Events.MAPUPDATE);
            packer.packInt(x);
            packer.packInt(y);
            packer.packBoolean(pirate);
            packer.packInt(treasure);
            packer.packBoolean(north);
            packer.packBoolean(east);
            packer.packBoolean(south);
            packer.packBoolean(west);
            packer.packString(fieldType.name());
            packer.packString(fieldEffect.name());
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'MapUpdate' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendActNow(int actionsLeft) {
        try {
            packer.packInt(ClientConnection.Events.ACTNOW);
            packer.packInt(actionsLeft);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'ActNow' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendGameEnd(int score) {
        try {
            packer.packInt(ClientConnection.Events.GAMEEND);
            packer.packInt(score);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'GameEnd' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendNextCycle(int cyclesLeft) {
        try {
            packer.packInt(ClientConnection.Events.NEXTCYCLE);
            packer.packInt(cyclesLeft);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'NextCycle' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendDamaged(int damage) {
        try {
            packer.packInt(ClientConnection.Events.DAMAGED);
            packer.packInt(damage);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Damaged' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendRobbed() {
        try {
            packer.packInt(ClientConnection.Events.ROBBED);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Robbed' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendRepaired(int cost) {
        try {
            packer.packInt(ClientConnection.Events.REPAIRED);
            packer.packInt(cost);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Repaired' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendPickedUp(int value) {
        try {
            packer.packInt(ClientConnection.Events.PICKEDUP);
            packer.packInt(value);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'PickedUp' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendHit() {
        try {
            packer.packInt(ClientConnection.Events.HIT);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Hit' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendSold(int gold) {
        try {
            packer.packInt(ClientConnection.Events.SOLD);
            packer.packInt(gold);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Sold' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendDropped(int value) {
        try {
            packer.packInt(ClientConnection.Events.DROPPED);
            packer.packInt(value);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Dropped' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendReloaded() {
        try {
            packer.packInt(ClientConnection.Events.RELOADED);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Reloaded' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendRestocked(int amount) {
        try {
            packer.packInt(ClientConnection.Events.RESTOCKED);
            packer.packInt(amount);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'Restocked' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendSwirlEffect(int x, int y) {
        try {
            packer.packInt(ClientConnection.Events.SWIRLEFFECT);
            packer.packInt(x);
            packer.packInt(y);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'SwirlEffect' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendStormEffect() {
        try {
            packer.packInt(ClientConnection.Events.STORMEFFECT);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'StormEffect' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }

    public final void sendCommandFailed(String message) {
        try {
            packer.packInt(ClientConnection.Events.COMMANDFAILED);
            packer.packString(message);
            packer.flush();
            socket.send(zmqId(id), 0, zmqId(id).length, 2) ;
            socket.send(outputBuffer.toByteArray(), 0);
        } catch (IOException | MessagePackException e) {
            throw new CommException("Ein 'CommandFailed' Event konnte nicht ins Wire-Format übersetzt werden!", e);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZError.EHOSTUNREACH)
                throw new CommException("Die andere Seite der Verbindung ist bereits geschlossen!", e);
            else throw new CommException(String.format("Serverseitiger Commlibfehler %d! Bitte wenden Sie sich an Ihren Tutor!", e.getErrorCode()), e);
        } finally {
            outputBuffer.reset();
        }
    }
}
