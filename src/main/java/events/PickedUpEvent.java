package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class PickedUpEvent extends Event {
    private final int value;

    public PickedUpEvent(int value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return "PickedUpEvent{"
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
        PickedUpEvent that = (PickedUpEvent) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendPickedUp(this.value);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }

}
