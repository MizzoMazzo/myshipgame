package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class ReloadedEvent extends Event {
    @Override
    public String toString() {
        return "ReloadedEvent{}";
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
        serverConnection.sendReloaded();
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
