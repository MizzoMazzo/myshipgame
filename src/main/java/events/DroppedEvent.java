package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class DroppedEvent extends Event {
    private final int value;

    public DroppedEvent(int value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return "DroppedEvent{"
                + "value=" + value
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroppedEvent that = (DroppedEvent) o;
        return value == that.value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendDropped(this.value);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
