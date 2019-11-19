package commands;

import events.*;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class EndTurnCommand extends Command {
    private int actionsLeft;

    @Override
    public int actionsUsed() {
        return actionsLeft;
    }

    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {
        this.actionsLeft = actionsLeft;
        return new ArrayList<>();
    }

    @Override
    public boolean requiresGameStarted() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public String toString() {
        return "EndTurnCommand";
    }
}
