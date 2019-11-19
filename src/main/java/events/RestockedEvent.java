package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class RestockedEvent extends Event {
    private final int amount;

    public RestockedEvent(int amount) {
        this.amount = amount;
    }


    @Override
    public String toString() {
        return "RestockedEvent{"
                + "amount=" + amount
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
        RestockedEvent that = (RestockedEvent) o;
        return amount == that.amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendRestocked(amount);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}
