package commands;

import events.*;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PickupCommand extends Command {

    @Override
    public int actionsUsed() {
        return 1;
    }

    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {
        Ship player = map.getClientShip();

        //Null-Check für den Cutter - Ole
        if (player.getTreasureStorage() == null) {
            List<Event> eventList = new ArrayList<>();
            eventList.add(new CommandFailedEvent(this));
            return eventList;
        }
        Tile playerTile = map.getPlayerTile();
        if (playerTile.getTreasure() != null && player.getTreasureStorage().getCapacity() > 0) {
            // Schiff hat Storage, Schatz auf Tile und Spieler hat Platz => Füge Schatz zu Storage hinzu und entferne von Karte
            Treasure treasure = playerTile.getTreasure();
            player.getTreasureStorage().add(treasure);
            playerTile.setTreasure(null);

            // Komische Reihenfolge gem. https://forum.se.cs.uni-saarland.de/t/488/2
            List<Event> eventList = new ArrayList<>();
            eventList.add(new PickedUpEvent(treasure.getValue()));
            eventList.add(new MapUpdateEvent(map.getShipXCoordinate(), map.getShipYCoordinate(), playerTile));
            return eventList;
        } else {
            List<Event> eventList = new ArrayList<>();
            eventList.add(new CommandFailedEvent(this));
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
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public String toString() {
        return "PickUpCommand";
    }
}
