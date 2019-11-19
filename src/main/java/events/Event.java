package events;

import commands.Command;
import connection.ServerConnection;

public abstract class Event {

    /**
     * Führt den entsprechenden Befehl samt Parametern auf der {@link ServerConnection} aus.
     */
    public abstract void sendEvent(ServerConnection<Command> serverConnection);

    /**
     * Ob nach Senden dieses Events der Server beendet werden soll. Ist insbesondere bei {@link GameEndEvent} der Fall.
     */
    public abstract boolean shouldExit();
}
