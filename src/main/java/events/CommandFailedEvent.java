package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class CommandFailedEvent extends Event {
    private final String message;

    public CommandFailedEvent(Command command) {
        // Danke an Pascal für den Tipp!
        this(command.toString());
    }

    public CommandFailedEvent(String message) {
        this.message = message;
    }


    @Override
    public String toString() {
        return "CommandFailedEvent{"
                + "message=" + message
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendCommandFailed(message);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
