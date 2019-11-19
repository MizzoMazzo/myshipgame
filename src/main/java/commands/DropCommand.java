package commands;

import events.*;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DropCommand extends Command {
    private final int treasureId;

    public DropCommand(int treasureId) {
        this.treasureId = treasureId;
    }


    @Override
    public int actionsUsed() {
        return 1;
    }

    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {
        Ship player = map.getClientShip();
        TreasureStorage storage = player.getTreasureStorage();
        Tile playerTile = map.getPlayerTile();

        if (storage == null || storage.isEmpty() || !storage.hasTreasureAtIndex(treasureId)) { // Fix von Ole #148
            // Cutter oder TreasureStorage leer oder ID überschreitet gefüllten Bereich bzw. Schatz existiert nicht
            List<Event> eventList = new ArrayList<>();
            eventList.add(new CommandFailedEvent(this));
            return eventList;
        } else {
            // Schatz existiert
            TreasureStorage treasureStorage = player.getTreasureStorage();
            Treasure droppedTreasure = treasureStorage.getTreasure(treasureId);

            List<Event> eventList = new ArrayList<>();
            eventList.add(new DroppedEvent(droppedTreasure.getValue()));

            if (playerTile.getTreasure() == null) {
                // Kein Schatz auf aktuellem Tile, Schatz verschwindet nicht
                playerTile.setTreasure(droppedTreasure);
                eventList.add(new MapUpdateEvent(map.getShipXCoordinate(), map.getShipYCoordinate(), playerTile));
            }
            player.setTreasure(treasureId, 0);
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
        DropCommand that = (DropCommand) o;
        return treasureId == that.treasureId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(treasureId);
    }

    @Override
    public String toString() {
        return "DropCommand";
    }
}
