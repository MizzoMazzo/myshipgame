package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class SoldEvent extends Event {
    private final int gold;

    public SoldEvent(int gold) {
        this.gold = gold;
    }


    @Override
    public String toString() {
        return "SoldEvent{"
                + "gold=" + gold
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
        SoldEvent soldEvent = (SoldEvent) o;
        return gold == soldEvent.gold;
    }

    public int getGold() {
        return gold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gold);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendSold(this.gold);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
