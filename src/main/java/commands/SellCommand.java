package commands;

import events.*;
import model.*;
import model.Tile.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SellCommand extends Command {
    private static final int TREASUREVALUE_TO_GOLD_MULTIPLICATOR = 3;

    @Override
    public int actionsUsed() {
        return 1;
    }

    @Override
    public List<Event> exec(Map map, Random random, int usedActions) {
        Ship player = map.getClientShip();
        TreasureStorage treasureStorage = player.getTreasureStorage();

        if (map.getPlayerTile().getFieldType() == FieldType.HARBOR) {
            // Spieler auf Hafen
            if (treasureStorage == null || treasureStorage.isEmpty()) {
                // Spieler hat keine Schätze, oder keinen Storage
                List<Event> eventList = new ArrayList<>();
                eventList.add(new CommandFailedEvent(this));
                return eventList;
            } else {
                // Spieler hat Schätze => berechne Wert, entferne Schätze und überweise Spieler seine Moneten
                int totalValue = treasureStorage.getTotalValue() * TREASUREVALUE_TO_GOLD_MULTIPLICATOR;

                treasureStorage.clear();
                player.setGold(player.getGold() + totalValue);

                List<Event> eventList = new ArrayList<>();
                eventList.add(new SoldEvent(player.getGold()));
                return eventList;
            }
        } else {
            // Nicht auf Hafen => Alle Schätze fujitsu (https://forum.se.cs.uni-saarland.de/t/519/)
            if (treasureStorage != null) {
                treasureStorage.clear();
            }

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
    public String toString() {
        return "SellCommand";
    }
}
