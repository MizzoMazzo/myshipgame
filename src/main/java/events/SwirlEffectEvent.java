package events;

import commands.Command;
import connection.ServerConnection;

import java.util.Objects;

public class SwirlEffectEvent extends Event {
    private final int x;
    private final int y;

    public SwirlEffectEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SwirlEffectEvent that = (SwirlEffectEvent) o;
        return x == that.x
                && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendSwirlEffect(this.x, this.y);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }

    @Override
    public String toString() {
        return "SwirlEffectEvent{"
                + "x=" + x
                + ", y=" + y
                + '}';
    }
}
