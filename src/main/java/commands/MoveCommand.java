package commands;

import events.*;
import model.*;
import model.Tile.Direction;
import model.Tile.FieldEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MoveCommand extends Command {
    private static final int INVALIDMOVE_DAMAGE = 2;
    private static final int STORM_ABANDONMENT_PENALTY = 2;
    private static final int SWIRL_DAMAGE = 1;

    private final Direction direction;
    private int actionsUsedInt;

    public MoveCommand(Direction direction) {
        this.direction = direction;
    }


    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {
        this.actionsUsedInt = 1;
        Ship player = map.getClientShip();
        List<Event> eventList = new ArrayList<>();

        // is valid Move?
        if (!isMoveValid(map, this.direction)) {
            player.setHealth(player.getHealth() - INVALIDMOVE_DAMAGE);
            eventList.add(new CommandFailedEvent(this));
            eventList.add(new DamagedEvent(INVALIDMOVE_DAMAGE));
            if (isGameOver(map)) {
                eventList.add(new GameEndEvent(GameEndEvent.LOST_SCORE));
            }
            return eventList;
        }

        // Events vor dem FeldCheck
        player.setPosition(player.getPosition().coordinatesOf(direction));
        eventList.add(new MovedEvent(map.getShipXCoordinate(), map.getShipYCoordinate()));
        eventList.addAll(updatePlayerVision(map));

        // Moved on SwirlEffect
        // loop mit SwirlEffect, Damaged, MapUpdate solange bis Spieler nicht mehr auf einem Swirl-Feld steht.
        if (map.getPlayerTile().getEffect() == FieldEffect.SWIRL) {

            while (map.getPlayerTile().getEffect() == FieldEffect.SWIRL) {
                map.getPlayerTile().setEffect(FieldEffect.NONE);
                Coordinate posAfterSwirl = this.execSwirl(map, random);
                player.setPosition(posAfterSwirl);
                eventList.add(new SwirlEffectEvent(posAfterSwirl.getxCoordinate(), posAfterSwirl.getyCoordinate()));
                eventList.add(new DamagedEvent(1));
                eventList.addAll(updatePlayerVision(map));
                // Game Over nach dem Swirl?
                if (isGameOver(map)){
                    eventList.add(new GameEndEvent(GameEndEvent.LOST_SCORE));
                    return eventList;
                }
                //Fix für Aktionen nach Swirl - Ole
                if (map.getPlayerTile().getEffect() == FieldEffect.STORM) {
                    eventList.addAll(this.execStorm(map));
                    map.getPlayerTile().setEffect(FieldEffect.NONE);
                    eventList.add(new MapUpdateEvent(map.getShipXCoordinate(), map.getShipYCoordinate(), map.getPlayerTile()));
                }
            }

            // Moved on StormEffect
        } else if (map.getPlayerTile().getEffect() == FieldEffect.STORM) {
            eventList.addAll(this.execStorm(map));
            map.getPlayerTile().setEffect(FieldEffect.NONE);
            eventList.add(new MapUpdateEvent(map.getShipXCoordinate(), map.getShipYCoordinate(), map.getPlayerTile()));
        }

        return eventList;
    }

    @Override
    public boolean requiresGameStarted() {
        return true;
    }

    /**
     * Führt den Storm-FieldEffect auf die Map an.
     */
    private List<Event> execStorm(Map map) {
        map.getPlayerTile().setEffect(FieldEffect.NONE);
        map.getClientShip().setStormPenalty(STORM_ABANDONMENT_PENALTY);
        List<Event> eventList = new ArrayList<>();
        eventList.add(new StormEffectEvent());
        return eventList;
    }

    /**
     * Führt den Swirl-FieldEffect auf die Map an.
     */
    private Coordinate execSwirl(Map map, Random random) {
        List<Coordinate> possibleCoordinates = getPossibleShipCoordinates(map);
        Ship player = map.getClientShip();
        player.setHealth(player.getHealth() - SWIRL_DAMAGE);
        //int sample = random.nextInt(Integer.MAX_VALUE) % possibleCoordinates.size();
        return possibleCoordinates.get(random.nextInt(possibleCoordinates.size()));
    }

    /**
     * @return Ob die Bewegung des Spielers in die gegebene {@code direction} möglich ist.
     */
    private boolean isMoveValid(Map map, Direction direction) {
        Tile playerTile = map.getPlayerTile();
        switch (direction) {
            case NORTH:
                return playerTile.isHasNorth();
            case HERE:
                return true;
            case WEST:
                return playerTile.isHasWest();
            case SOUTH:
                return playerTile.isHasSouth();
            case EAST:
                return playerTile.isHasEast();
            default:
                return false;
        }
    }

    public List<Coordinate> getPossibleShipCoordinates(Map map) {
        Ship player = map.getClientShip();

        List<Coordinate> possibleDirections = new ArrayList<>();
        if (map.getPlayerTile().isHasNorth()) {
            possibleDirections.add(player.getPosition().coordinatesOf(Direction.NORTH));
        }
        if (map.getPlayerTile().isHasEast()) {
            possibleDirections.add(player.getPosition().coordinatesOf(Direction.EAST));
        }
        if (map.getPlayerTile().isHasSouth()) {
            possibleDirections.add(player.getPosition().coordinatesOf(Direction.SOUTH));
        }
        if (map.getPlayerTile().isHasWest()) {
            possibleDirections.add(player.getPosition().coordinatesOf(Direction.WEST));
        }
        return possibleDirections;
    }

    @Override
    public int actionsUsed() {
        return this.actionsUsedInt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MoveCommand that = (MoveCommand) o;
        return direction == that.direction;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction, actionsUsedInt);
    }

    @Override
    public String toString() {
        return "MoveCommand";
    }
}
