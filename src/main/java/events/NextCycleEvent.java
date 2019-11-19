package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class NextCycleEvent extends Event {
    private final int score;

    public NextCycleEvent(int score) {
        this.score = score;
    }


    @Override
    public String toString() {
        return "NextCycleEvent{"
                + "score=" + score
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
        NextCycleEvent that = (NextCycleEvent) o;
        return score == that.score;
    }

    @Override
    public int hashCode() {
        return Objects.hash(score);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendNextCycle(this.score);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
