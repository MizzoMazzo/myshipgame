package commands;

import events.*;
import model.*;
import model.Tile.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class FireCommand extends Command {
    private static final int UNLOADED_SHOOT_DAMAGE = 2;
    private final Direction direction;

    public FireCommand(Direction direction) {
        this.direction = direction;
    }


    @Override
    public int actionsUsed() {
        return 1;
    }

    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {
        Ship player = map.getClientShip();

        if (player.isLoaded()) {
            // Spieler hat geladen => Kugel ist nun weg
            player.setLoaded(false);

            if (map.tryShoot(this.direction)) {
                Tile targetTile = map.getRelativeTile(this.direction);
                List<Event> eventList = new ArrayList<>();
                eventList.add(new HitEvent());

                if (!targetTile.isPirate()) {
                    // Pirat starb. Nice! => MapUpdateEvent
                    Coordinate hitCoordinate = new Coordinate(map.getShipXCoordinate(), map.getShipYCoordinate()).coordinatesOf(this.direction);
                    eventList.add(new MapUpdateEvent(hitCoordinate.getxCoordinate(), hitCoordinate.getyCoordinate(), targetTile));
                }
                return eventList;
            } else {
                List<Event> eventList = new ArrayList<>();
                eventList.add(new CommandFailedEvent(this));
                return eventList;
            }
        } else if (player.hasPenaltyWhenShooting()) {
            // Spieler hat nicht geladen => Penalty
            player.setHealth(player.getHealth() - UNLOADED_SHOOT_DAMAGE);

            List<Event> eventList = new ArrayList<>();
            eventList.add(new CommandFailedEvent(this));
            eventList.add(new DamagedEvent(UNLOADED_SHOOT_DAMAGE));
            // Falls Spieler stirbt => GameEndEvent
            if (isGameOver(map)) {
                eventList.add(new GameEndEvent(GameEndEvent.LOST_SCORE));
            }
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FireCommand that = (FireCommand) o;
        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction);
    }

    @Override
    public String toString() {
        return "FireCommand";
    }
}
