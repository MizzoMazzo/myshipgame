package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tile {
    private FieldEffect effect;
    private FieldType fieldType;
    private boolean hasNorth;
    private boolean hasEast;
    private boolean hasSouth;
    private boolean hasWest;
    private boolean pirate;
    private Treasure treasure;

    public Tile(FieldEffect effect, FieldType fieldType, boolean hasNorth, boolean hasEast, boolean hasSouth, boolean hasWest, boolean pirate, Treasure treasure) {
        this.effect = effect;
        this.fieldType = fieldType;
        this.hasNorth = hasNorth;
        this.hasEast = hasEast;
        this.hasSouth = hasSouth;
        this.hasWest = hasWest;
        this.pirate = pirate;
        this.treasure = treasure;
    }

    public enum FieldEffect {
        STORM,
        SWIRL,
        NONE;
    }

    public enum FieldType {
        EXIT,
        HARBOR,
        NORMAL;
    }

    public enum Direction {
        NORTH,
        WEST,
        EAST,
        SOUTH,
        HERE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tile tile = (Tile) o;
        return hasNorth == tile.hasNorth &&
                hasEast == tile.hasEast &&
                hasSouth == tile.hasSouth &&
                hasWest == tile.hasWest &&
                pirate == tile.pirate &&
                effect == tile.effect &&
                fieldType == tile.fieldType &&
                Objects.equals(treasure, tile.treasure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effect, fieldType, hasNorth, hasEast, hasSouth, hasWest, pirate, treasure);
    }

    public FieldEffect getEffect() {
        return effect;
    }

    public void setEffect(FieldEffect effect) {
        this.effect = effect;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isHasNorth() {
        return hasNorth;
    }

    public boolean isHasEast() {
        return hasEast;
    }

    public boolean isHasSouth() {
        return hasSouth;
    }

    public boolean isHasWest() {
        return hasWest;
    }

    public boolean hasConnection(Direction direction) {
        switch (direction) {
            case NORTH:
                return hasNorth;

            case EAST:
                return hasEast;

            case SOUTH:
                return hasSouth;

            case WEST:
                return hasWest;

            default:
                return true;
        }
    }

    public List<Direction> getDirections() {
        var res = new ArrayList<Direction>(4);
        if (isHasNorth()) {
            res.add(Direction.NORTH);
        }
        if (isHasEast()) {
            res.add(Direction.EAST);
        }
        if (isHasSouth()) {
            res.add(Direction.SOUTH);
        }
        if (isHasWest()) {
            res.add(Direction.WEST);
        }
        return res;
    }

    public enum Shape {
        VERTICE,
        DEADEND,
        PATH,
        UNKNOWN;
    }

    public Shape getShape() {
        List<Direction> dirs = getDirections();
        switch (dirs.size()) {
            case 1:
                return Shape.DEADEND;
            case 2:
                return Shape.PATH;
            case 3:
            default:
                return Shape.VERTICE;
        }
    }

    public boolean isPirate() {
        return pirate;
    }

    public void setPirate(boolean pirate) {
        this.pirate = pirate;
    }

    public Treasure getTreasure() {
        return treasure;
    }

    public void setTreasure(Treasure treasure) {
        this.treasure = treasure;
    }

    /**
     * Setzt das Attribut zur jeweiligen Richtung auf {@code true}.
     *
     * @param direction Gewünschte Richtung
     */
    public void addDirection(Direction direction) {
        switch (direction) {
            case NORTH:
                hasNorth = true;
                break;

            case EAST:
                hasEast = true;
                break;

            case SOUTH:
                hasSouth = true;
                break;

            case WEST:
                hasWest = true;
                break;

            default:
        }
    }

    @Override
    public String toString() {
        List<Direction> dirs = new ArrayList<>();
        if (isHasNorth()) {
            dirs.add(Direction.NORTH);
        }
        if (isHasEast()) {
            dirs.add(Direction.EAST);
        }
        if (isHasSouth()) {
            dirs.add(Direction.SOUTH);
        }
        if (isHasWest()) {
            dirs.add(Direction.WEST);
        }
        switch (dirs.size()) {
            case 4:
                return "\u253C";

            case 3:
                return toString2(dirs);

            case 2:
                return toString3(dirs);

            case 1:
                return toString4(dirs);

            case 0:
                return " ";

            default:
                return "?";
        }
    }

    public String toString2(List<Direction> dirs) {
        if (!dirs.contains(Direction.NORTH)) {
            return "\u252C";
        }
        if (!dirs.contains(Direction.EAST)) {
            return "\u2524";
        }
        if (!dirs.contains(Direction.SOUTH)) {
            return "\u2534";
        }
        return "\u251C";
    }

    public String toString3(List<Direction> dirs) {
        if (dirs.contains(Direction.NORTH) && dirs.contains(Direction.SOUTH)) {
            return "\u2502";
        } else if (dirs.contains(Direction.NORTH) && dirs.contains(Direction.WEST)) {
            return "\u2518";
        } else if (dirs.contains(Direction.NORTH) && dirs.contains(Direction.EAST)) {
            return "\u2514";
        } else if (dirs.contains(Direction.SOUTH) && dirs.contains(Direction.WEST)) {
            return "\u2510";
        } else if (dirs.contains(Direction.SOUTH) && dirs.contains(Direction.EAST)) {
            return "\u250C";
        } else {
            return "\u2500";
        }
    }

    public String toString4(List<Direction> dirs) {
        if (dirs.contains(Direction.NORTH)) {
            return "\u2575";
        } else if (dirs.contains(Direction.SOUTH)) {
            return "\u2577";
        } else if (dirs.contains(Direction.EAST)) {
            return "\u2576";
        } else {
            return "\u2574";
        }
    }
}
