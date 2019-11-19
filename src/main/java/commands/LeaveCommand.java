package commands;

import events.*;
import model.*;
import model.Tile.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LeaveCommand extends Command {
    private Tile currentTile;

    @Override
    public int actionsUsed() {
        return 1;
    }

    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {
        currentTile = map.getPlayerTile();

        if (currentTile.getFieldType() == FieldType.EXIT) {
            List<Event> eventList = new ArrayList<>();
            eventList.add(new GameEndEvent(map.computeScore()));
            return eventList;
        } else {
            int penalty = Math.min(map.getHeight(), map.getWidth());

            map.setMaxCycles(map.getMaxCycles() - penalty);
            List<Event> eventList = new ArrayList<>();
            eventList.add(new CommandFailedEvent(this));
            if (map.getCyclesLeft() < 0) {
                eventList.add(new GameEndEvent(GameEndEvent.LOST_SCORE));
            }
            return eventList;
        }
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LeaveCommand that = (LeaveCommand) o;
        return Objects.equals(currentTile, that.currentTile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTile);
    }

    @Override
    public String toString() {
        return "LeaveCommand";
    }
}
