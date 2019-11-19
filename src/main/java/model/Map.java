package model;

import model.Ship.ActorType;
import model.Tile.Direction;
import model.Tile.FieldEffect;
import model.Tile.FieldType;
import util.SerializableMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Map {
    public static final int SCORE_GOLD_MULT = 9;
    public static final double SCORE_CYCLE_MULT = 0.1;
    private final int height;
    private final int width;
    private int numPorts;
    private int cycles;
    private int maxCycles;
    private int numTreasures;
    private boolean gameStarted;
    private Coordinate startPosition;
    private Ship clientShip;
    private List<Pirate> pirates;
    private Tile[][] tiles;

    public Map(int height, int width) {
        this.height = height;
        this.width = width;
        maxCycles = height * width * 2;
        cycles = 0;
        pirates = new ArrayList<>();
    }

    public static Map fromJson(String path) throws IOException {
        return SerializableMap.fromJSON(path).toMapValid();
    }

    /**
     * @return List mit allen vom Spieler aus sichbaren Koordinaten in Leserichtung.
     * @see #getVisiblePlayerTiles()
     */
    public static List<Coordinate> getVisiblePlayerCoordinatesAbsolute(int x, int y, int width, int height) {
        List<Coordinate> visibleCoordinates = new ArrayList<>();
        int dumpX = x;
        int dumpY = y - 2;
        if (validPosition(dumpX, dumpY, width, height)) {
            visibleCoordinates.add(new Coordinate(dumpX, dumpY));
        }
        dumpY++;
        dumpX--;
        for (int i = 0; i < 3; i++) {
            if (validPosition(dumpX, dumpY, width, height)) {
                visibleCoordinates.add(new Coordinate(dumpX, dumpY));
            }
            dumpX++;
        }
        dumpY++;
        dumpX -= 4;
        for (int i = 0; i < 5; i++) {
            if (validPosition(dumpX, dumpY, width, height)) {
                visibleCoordinates.add(new Coordinate(dumpX, dumpY));
            }
            dumpX++;
        }
        dumpY++;
        dumpX -= 4;
        for (int i = 0; i < 3; i++) {
            if (validPosition(dumpX, dumpY, width, height)) {
                visibleCoordinates.add(new Coordinate(dumpX, dumpY));
            }
            dumpX++;
        }
        dumpY++;
        dumpX -= 2;
        if (validPosition(dumpX, dumpY, width, height)) {
            visibleCoordinates.add(new Coordinate(dumpX, dumpY));
        }
        return visibleCoordinates;
    }

    public static boolean validPosition(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getNumTreasures() {
        return numTreasures;
    }

    public void setNumTreasures(int numTreasures) {
        this.numTreasures = numTreasures;
    }

    public Ship getClientShip() {
        return clientShip;
    }

    public void setClientShip(Ship clientShip) {
        this.clientShip = clientShip;
    }

    public int getCycles() {
        return cycles;
    }

    public void setCycles(int cycles) {
        this.cycles = cycles;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public List<Pirate> getPirates() {
        return pirates;
    }

    public void setPirates(List<Pirate> pirates) {
        this.pirates = pirates;
    }

    /**
     * Gibt den Piraten mit den gegebenen Koordinaten zurück.
     */
    public Pirate getPirateAtCoordinate(int x, int y) {
        return pirates.stream()
                .filter(pirate -> pirate.getPosition().equals(new Coordinate(x, y)))
                .findAny()
                .orElse(null);
    }

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles.clone();
    }

    public Tile getTile(int x, int y) {
        return tiles[y][x];
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getNumPorts() {
        return numPorts;
    }

    public void setNumPorts(int numPorts) {
        this.numPorts = numPorts;
    }

    public Coordinate getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Coordinate startPosition) {
        this.startPosition = startPosition;
    }

    public int getCyclesLeft() {
        return maxCycles - cycles;
    }

    public int getMaxCycles() {
        return maxCycles;
    }

    public void setMaxCycles(int maxCycles) {
        this.maxCycles = maxCycles;
    }

    public int getShipXCoordinate() {
        return clientShip.getPosition().getxCoordinate();
    }

    public int getShipYCoordinate() {
        return clientShip.getPosition().getyCoordinate();
    }

    public int computeScore() {
        int cycleScore = (int) Math.floor(getCyclesLeft() * SCORE_CYCLE_MULT);
        if (clientShip.getActorType() == ActorType.BARQUE) {
            return clientShip.getGold() + cycleScore + (clientShip.getTreasureStorage().getSize() * SCORE_GOLD_MULT);
        }
        return cycleScore;
    }

    public void updateTile(int x, int y, Treasure treasure) {
        tiles[y][x].setTreasure(treasure);
    }

    public void updateTile(int x, int y, FieldEffect effect) {
        tiles[y][x].setEffect(effect);
    }

    public Tile getPlayerTile() {
        return tiles[clientShip.getPosition().getyCoordinate()][clientShip.getPosition().getxCoordinate()];
    }

    public Tile getRelativeTile(Direction direction) {
        Coordinate currentPosition = clientShip.getPosition();
        Coordinate desiredPosition = currentPosition.coordinatesOf(direction);
        return tiles[desiredPosition.getyCoordinate()][desiredPosition.getxCoordinate()];
    }

    /**
     * @return List mit allen vom Spieler aus sichbaren Tiles in Leserichtung.
     * @see #getVisiblePlayerCoordinates()
     */
    public List<Tile> getVisiblePlayerTiles() {
        List<Coordinate> visibleCoordinates = getVisiblePlayerCoordinates();
        List<Tile> visibleTiles = new ArrayList<>(visibleCoordinates.size());
        for (Coordinate coordinate : visibleCoordinates) {
            visibleTiles.add(getTile(coordinate.getxCoordinate(), coordinate.getyCoordinate()));
        }
        return visibleTiles;
    }

    public List<Coordinate> getVisiblePlayerCoordinates() {
        return getVisiblePlayerCoordinatesAbsolute(getShipXCoordinate(), getShipYCoordinate(), width, height);
    }

    public boolean hasGameEnded() {
        return maxCycles < cycles || clientShip.isDead();
    }

    public void spawnEffects(Random random) {
        int ran = random.nextInt(height * width);
        int x = ran % width;
        int y = (int) Math.floor(ran / (double) width);
        Tile target = getTile(x, y);
        Coordinate targetCoordinate = new Coordinate(x, y);
        if (target.getFieldType() != FieldType.HARBOR && !target.isPirate() && target.getEffect() == FieldEffect.NONE && !getVisiblePlayerCoordinates().contains(targetCoordinate)) {
            target.setEffect(FieldEffect.STORM);
        }
        ran = random.nextInt(height * width);
        x = ran % width;
        y = (int) Math.floor(ran / (double) width);
        target = getTile(x, y);
        targetCoordinate = new Coordinate(x, y);
        if (target.getFieldType() != FieldType.HARBOR && !target.isPirate() && target.getEffect() == FieldEffect.NONE && !getVisiblePlayerCoordinates().contains(targetCoordinate)) {
            target.setEffect(FieldEffect.SWIRL);
        }
    }

    public boolean tryShoot(Direction direction) {
        if (!getPlayerTile().hasConnection(direction)) {
            return false;
        }
        Tile shootField = getRelativeTile(direction);
        if (shootField.isPirate()) {
            Coordinate shootFieldPosition = new Coordinate(clientShip.getPosition().getxCoordinate(), clientShip.getPosition().getyCoordinate()).coordinatesOf(direction);
            for (Pirate pirate : pirates) {
                if (pirate.getPosition().getxCoordinate() == shootFieldPosition.getxCoordinate() && pirate.getPosition().getyCoordinate() == shootFieldPosition.getyCoordinate()) {
                    pirate.damage();
                    if (pirate.getLife() == 0) {
                        shootField.setPirate(false);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public Optional<Integer> tryPickUp() {
        if (getPlayerTile().getTreasure() == null) {
            return Optional.empty();
        } else {
            return Optional.of(getPlayerTile().getTreasure().getValue());
        }
    }

    public Tile getPirateTile(int id) {
        for (Pirate pirate : pirates) {
            if (pirate.getID() == id) {
                return tiles[pirate.getPosition().getyCoordinate()][pirate.getPosition().getxCoordinate()];
            }
        }
        return null;
    }
}
