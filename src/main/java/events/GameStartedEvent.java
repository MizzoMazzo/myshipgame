package events;

import commands.Command;
import connection.ServerConnection;
import model.*;

import java.util.Objects;

public class GameStartedEvent extends Event {
    private final int x;
    private final int y;
    private final int maxCycles;
    private final int numTreasures;
    private final int numPirates;
    private final int numHarbors;

    public GameStartedEvent(int x, int y, int maxCycles, int numTreasures, int numPirates, int numHarbors) {
        this.x = x;
        this.y = y;
        this.maxCycles = maxCycles;
        this.numTreasures = numTreasures;
        this.numPirates = numPirates;
        this.numHarbors = numHarbors;
    }


    @Override
    public String toString() {
        return "GameStartedEvent{"
                + "x=" + x
                + ", y=" + y
                + ", maxCycles=" + maxCycles
                + ", numTreasures=" + numTreasures
                + ", numPirates=" + numPirates
                + ", numHarbors=" + numHarbors
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
        GameStartedEvent that = (GameStartedEvent) o;
        return x == that.x
                && y == that.y
                && maxCycles == that.maxCycles
                && numTreasures == that.numTreasures
                && numPirates == that.numPirates
                && numHarbors == that.numHarbors;
    }

    public Coordinate startPos() {
        return new Coordinate(x, y);
    }

    public int getMaxCycles() {
        return maxCycles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, maxCycles, numTreasures, numPirates, numHarbors);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendGameStarted(this.x, this.y, this.maxCycles, this.numTreasures, this.numPirates, this.numHarbors);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }

}
