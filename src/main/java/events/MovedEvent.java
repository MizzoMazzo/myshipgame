package events;

import commands.Command;
import connection.ServerConnection;
import model.Coordinate;

import java.util.Objects;

public class MovedEvent extends Event {
    private final int x;
    private final int y;

    public MovedEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }


    @Override
    public String toString() {
        return "MovedEvent{"
                + "x=" + x
                + ", y=" + y
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
        MovedEvent that = (MovedEvent) o;
        return x == that.x
                && y == that.y;
    }

    public Coordinate getNewPosition() {
        return new Coordinate(x, y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendMoved(this.x, this.y);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
