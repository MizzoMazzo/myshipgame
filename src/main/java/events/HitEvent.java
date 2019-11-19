package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class HitEvent extends Event {
    @Override
    public String toString() {
        return "HitEvent{}";
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
        serverConnection.sendHit();
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
