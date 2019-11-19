package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class GameEndEvent extends Event {
    public final static int LOST_SCORE = -1;
    private final int score;

    public GameEndEvent(int score) {
        this.score = score;
    }


    @Override
    public String toString() {
        return "GameEndEvent{"
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
        GameEndEvent that = (GameEndEvent) o;
        return score == that.score;
    }

    @Override
    public int hashCode() {
        return Objects.hash(score);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendGameEnd(this.score);
    }

    @Override
    public boolean shouldExit() {
        return true;
    }
}
