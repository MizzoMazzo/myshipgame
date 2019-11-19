package model;

import java.util.Objects;

public class Coordinate {
    private final int xCoordinate;
    private final int yCoordinate;

    public Coordinate(int xCoordinate, int yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Coordinate that = (Coordinate) o;
        return xCoordinate == that.xCoordinate &&
                yCoordinate == that.yCoordinate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xCoordinate, yCoordinate);
    }

    public int compareTo(Coordinate coordinate) {
        int cmpFirst = Integer.compare(getxCoordinate(), coordinate.getxCoordinate());
        if (cmpFirst == 0) {
            return Integer.compare(getyCoordinate(), coordinate.getyCoordinate());
        } else {
            return cmpFirst;
        }
    }

    @Override
    public String toString() {
        return getxCoordinate() + ", " + getyCoordinate();
    }

    public int getxCoordinate() {
        return xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public Coordinate coordinatesOf(Tile.Direction direction) {
        switch (direction) {
            case SOUTH:
                return new Coordinate(xCoordinate, yCoordinate + 1);

            case EAST:
                return new Coordinate(xCoordinate + 1, yCoordinate);

            case WEST:
                return new Coordinate(xCoordinate - 1, yCoordinate);

            case NORTH:
                return new Coordinate(xCoordinate, yCoordinate - 1);

            case HERE:

            default:
                return new Coordinate(xCoordinate, yCoordinate);

        }
    }

    public int distance(Coordinate coordinate) {
        return Math.abs(getxCoordinate() - coordinate.getxCoordinate()) + Math.abs(getyCoordinate() - coordinate.getyCoordinate());
    }
}
