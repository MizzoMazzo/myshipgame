package events;

import commands.Command;
import connection.ServerConnection;
import model.Tile;
import model.Tile.FieldEffect;
import model.Tile.FieldType;

import java.util.Objects;

public class MapUpdateEvent extends Event {
    private int x;
    private int y;
    private boolean pirate;
    private int treasure;
    private boolean north;
    private boolean east;
    private boolean south;
    private boolean west;
    private FieldType fieldType;
    private FieldEffect fieldEffect;

    /**
     * Kurzform von {@link MapUpdateEvent#MapUpdateEvent(int, int, boolean, int, boolean, boolean, boolean, boolean, FieldType, FieldEffect)},
     * die ausgelassenen Parameter werden aus {@code tile} entnommen.
     *
     * @param tile Tile, aus welchem Attribute wie {@code pirate}, {@code treasure}, {@code fieldType}, {@code fieldEffect} und Richtungen entnommen werden.
     */
    public MapUpdateEvent(int x, int y, Tile tile) {
        this(x, y, tile.isPirate(), tile.getTreasure() == null ? 0 : tile.getTreasure().getValue(), tile.isHasNorth(),
                tile.isHasEast(), tile.isHasSouth(), tile.isHasWest(), tile.getFieldType(), tile.getEffect());
    }

    /**
     * @param pirate   Ob ein Pirat auf dem Tile ist.
     * @param treasure Wert des Schatzes auf dem Tile, falls vorhanden, sonst 0.
     */
    public MapUpdateEvent(int x, int y, boolean pirate, int treasure, boolean north, boolean east, boolean south, boolean west, FieldType fieldType, FieldEffect fieldEffect) {
        this.x = x;
        this.y = y;
        this.pirate = pirate;
        this.treasure = treasure;
        this.north = north;
        this.east = east;
        this.south = south;
        this.west = west;
        this.fieldType = fieldType;
        this.fieldEffect = fieldEffect;
    }


    @Override
    public String toString() {
        return "MapUpdateEvent{"
                + "x=" + x
                + ", y=" + y
                + ", pirate=" + pirate
                + ", treasure=" + treasure
                + ", north=" + north
                + ", east=" + east
                + ", south=" + south
                + ", west=" + west
                + ", fieldType=" + fieldType
                + ", fieldEffect=" + fieldEffect
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
        MapUpdateEvent that = (MapUpdateEvent) o;
        return x == that.x
                && y == that.y
                && pirate == that.pirate
                && treasure == that.treasure
                && north == that.north
                && east == that.east
                && south == that.south
                && west == that.west
                && fieldType == that.fieldType
                && fieldEffect == that.fieldEffect;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, pirate, treasure, north, east, south, west, fieldType, fieldEffect);
    }

    @Override
    public void sendEvent(ServerConnection<Command> serverConnection) {
        serverConnection.sendMapUpdate(this.x, this.y, this.pirate, this.treasure, this.north, this.east, this.south, this.west, this.fieldType, this.fieldEffect);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPirate() {
        return pirate;
    }

    public void setPirate(boolean pirate) {
        this.pirate = pirate;
    }

    public int getTreasure() {
        return treasure;
    }

    public void setTreasure(int treasure) {
        this.treasure = treasure;
    }

    public boolean isNorth() {
        return north;
    }

    public boolean isEast() {
        return east;
    }

    public boolean isSouth() {
        return south;
    }

    public boolean isWest() {
        return west;
    }

    public Tile.FieldType getFieldType() {
        return fieldType;
    }

    public Tile.FieldEffect getFieldEffect() {
        return fieldEffect;
    }

    public void setFieldEffect(FieldEffect fieldEffect) {
        this.fieldEffect = fieldEffect;
    }
}
