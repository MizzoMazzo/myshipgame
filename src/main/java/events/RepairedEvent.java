package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class RepairedEvent extends Event {
    private final int cost;

    public RepairedEvent(int cost) {
        this.cost = cost;
    }


    @Override
    public String toString() {
        return "RepairedEvent{"
                + "cost=" + cost
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
        RepairedEvent that = (RepairedEvent) o;
        return cost == that.cost;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendRepaired(this.cost);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
