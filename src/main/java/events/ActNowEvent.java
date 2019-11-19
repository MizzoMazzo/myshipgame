package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class ActNowEvent extends Event {
    private final int movesLeft;

    public ActNowEvent(int movesLeft) {
        this.movesLeft = movesLeft;
    }


    @Override
    public String toString() {
        return "ActNowEvent{"
                + "movesLeft=" + movesLeft
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
        ActNowEvent that = (ActNowEvent) o;
        return movesLeft == that.movesLeft;
    }

    @Override
    public int hashCode() {
        return Objects.hash(movesLeft);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendActNow(this.movesLeft);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
