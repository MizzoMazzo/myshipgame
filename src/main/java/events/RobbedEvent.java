package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class RobbedEvent extends Event {
    public static final int PIRATE_DAMAGE = 1;

    @Override
    public String toString() {
        return "RobbedEvent{}";
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
        serverConnection.sendRobbed();
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
