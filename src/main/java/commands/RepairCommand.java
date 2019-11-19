package commands;

import events.*;
import model.*;
import model.Tile.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RepairCommand extends Command {
    private static final int COST_ON_SEA = 27;
    private static final int COST_HARBOR = 5;
    private static final int NOMONEY_DAMAGE = 1;

    @Override
    public int actionsUsed() {
        return 1;
    }

    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {
        Ship player = map.getClientShip();
        int cost = map.getPlayerTile().getFieldType() == FieldType.HARBOR ? COST_HARBOR : COST_ON_SEA;

        if (player.getGold() < cost) {
            // Spieler hat nicht genug Gold => Penalty
            player.setHealth(player.getHealth() - NOMONEY_DAMAGE);

            List<Event> eventList = new ArrayList<>();
            eventList.add(new CommandFailedEvent(this));
            eventList.add(new DamagedEvent(NOMONEY_DAMAGE));
            // Falls Spieler stirbt => GameEndEvent
            if (isGameOver(map)) {
                eventList.add(new GameEndEvent(GameEndEvent.LOST_SCORE));
            }
            return eventList;
        } else {
            // Spieler hat genug Gold => Heilen und Kosten abziehen
            player.setHealth(player.getMaxHealth());
            player.setGold(player.getGold() - cost);

            List<Event> eventList = new ArrayList<>();
            eventList.add(new RepairedEvent(cost));
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
        return "RepairCommand";
    }
}
