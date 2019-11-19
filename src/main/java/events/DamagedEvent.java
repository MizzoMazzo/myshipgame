package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class DamagedEvent extends Event{
    private final int damage;

    public DamagedEvent(int damage) {
        this.damage = damage;
    }


    @Override
    public String toString() {
        return "DamagedEvent{"
                + "damage=" + damage
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
        DamagedEvent that = (DamagedEvent) o;
        return damage == that.damage;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(damage);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendDamaged(damage);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
