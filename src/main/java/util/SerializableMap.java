package util;

import model.*;
import model.Map;
import model.Tile.FieldEffect;
import model.Tile.FieldType;
import model.Tile.Direction;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SerializableMap {

    private final int width;
    private final int height;
    private final Coordinate start;
    private final List<String> stringRep;
    private final List<Coordinate> pirates;
    private final List<Coordinate> harbours;
    private final List<Pair<Coordinate, Treasure>> treasures;
    private Coordinate exit;

    /**
     * Erzeuge eine neue Map aus den Parametern.
     *
     * @param width     Breite der Map
     * @param height    Höhe der Mao
     * @param start     Start-Koordinate
     * @param exit      Ziel-Koordinate
     * @param stringRep String Repräsentation (S. Spezifikation S.16, Feld "map")
     * @param pirates   Koordinaten den Piraten
     * @param harbours  Koordinaten der Schätze
     * @param treasures Schätze mit ihren Koordinaten
     */
    public SerializableMap(int width, int height,
                           Coordinate start,
                           Coordinate exit,
                           List<String> stringRep,
                           List<Coordinate> pirates,
                           List<Coordinate> harbours,
                           List<Pair<Coordinate, Treasure>> treasures) {
        this.width = width;
        this.height = height;
        this.start = start;
        this.exit = exit;
        this.stringRep = stringRep;
        this.pirates = pirates;
        this.harbours = harbours;
        this.treasures = treasures;
    }

    /**
     * Erzeuge einen {@link SerializableMap} aus einer {@link Map}.
     *
     * @param notThatKindOfMap die Map
     */
    public SerializableMap(Map notThatKindOfMap) {
        width = notThatKindOfMap.getWidth();
        height = notThatKindOfMap.getHeight();
        start = notThatKindOfMap.getStartPosition();
        pirates = new ArrayList<>(MapUtil.pirateCount(width, height));
        harbours = new ArrayList<>(MapUtil.portCount(width, height));
        treasures = new ArrayList<>(MapUtil.maxTreasureCount(width, height));
        List<String> dummy = new ArrayList<>(height);
        for (int y = 0; y < height; y++) {
            StringBuilder curRow = new StringBuilder(width);
            for (int x = 0; x < width; x++) {
                Tile tile = notThatKindOfMap.getTile(x, y);
                Coordinate coord = new Coordinate(x, y);
                if (tile.isPirate()) {
                    pirates.add(coord);
                }
                if (tile.getTreasure() != null) {
                    treasures.add(new Pair<>(coord, tile.getTreasure()));
                }
                if (tile.getFieldType() == FieldType.HARBOR) {
                    harbours.add(coord);
                }
                if (tile.getFieldType() == FieldType.EXIT) {
                    exit = coord;
                }
                curRow.append(tile);
            }
            dummy.add(curRow.toString());
        }
        stringRep = dummy;
    }

    /**
     * Liest eine Map aus einer Datei.
     *
     * @param path der Pfad zur JSON-Datei
     * @return das Map Objekt
     * @throws JSONException JSON-Parse-Error, oder String-Repräsentation (Key "map") invalide
     * @throws IOException   IO-Error
     * @apiNote fromJSON führt keine Korrrektsheitsüberprüng durch. Es wird auf
     *      - falsche Format (fehlende Keys, Format Koordinaten)
     *      - invalide Koordinaten
     *      - Doppelte Koordinaten bei Treasures, Piraten, Häfen
     *      - Falsches Kartenformat (Höhe / Breite falsch)
     * geachtet
     */

    public static SerializableMap fromJSON(String path) throws IOException {
        var inStream = Files.newBufferedReader(Paths.get(path));
        var parser = new JSONTokener(inStream);
        return Marshall.serializableMapFromJSON(new JSONObject(parser));
    }

    /**
     * Wandelt ein Unicode-Char in eine Liste von Directions um.
     */
    public static List<Direction> directionsFromChar(char c) {
        switch (c) {
            case '\u2500': // ─
                return Arrays.asList(Direction.EAST, Direction.WEST);
            case '\u2502': // │
                return Arrays.asList(Direction.NORTH, Direction.SOUTH);
            case '\u2514': // └
                return Arrays.asList(Direction.NORTH, Direction.EAST);
            case '\u250C': // ┌
                return Arrays.asList(Direction.EAST, Direction.SOUTH);
            case '\u2510': // ┐
                return Arrays.asList(Direction.SOUTH, Direction.WEST);
            case '\u2518': // ┘
                return Arrays.asList(Direction.WEST, Direction.NORTH);
            case '\u252C': // ┬
                return Arrays.asList(Direction.SOUTH, Direction.EAST, Direction.WEST);
            case '\u2524': // ┤
                return Arrays.asList(Direction.WEST, Direction.NORTH, Direction.SOUTH);
            case '\u2534': // ┴
                return Arrays.asList(Direction.EAST, Direction.NORTH, Direction.WEST);
            case '\u251C': // ├
                return Arrays.asList(Direction.NORTH, Direction.EAST, Direction.SOUTH);
            case '\u253C': // ┼
                return Arrays.asList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
            case '\u2574': // ╴
                return Collections.singletonList(Direction.WEST);
            case '\u2575': // ╵
                return Collections.singletonList(Direction.NORTH);
            case '\u2576': // ╶
                return Collections.singletonList(Direction.EAST);
            case '\u2577':
            default:
                return Collections.singletonList(Direction.SOUTH); // ╷
        }
    }

    /**
     * Erzeugt eine Tile anhand der Darstellung als Tile.
     *
     * @param c die Repräsentation
     * @return das Tile, bei dem sonst alle Felder auf false/null gesetzt sind
     */
    protected static Tile tileFomChar(char c) {
        List<Direction> connections = directionsFromChar(c);

        Tile res = new Tile(FieldEffect.NONE, FieldType.NORMAL, false, false, false, false, false, null);
        for (Direction dir : connections) {
            res.addDirection(dir);
        }
        return res;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Coordinate getStart() {
        return start;
    }

    public Coordinate getExit() {
        return exit;
    }

    public List<String> getStringRep() {
        return stringRep;
    }

    public List<Coordinate> getPirates() {
        return pirates;
    }

    public List<Coordinate> getHarbours() {
        return harbours;
    }

    public List<Pair<Coordinate, Treasure>> getTreasures() {
        return treasures;
    }

    /**
     * Konvertiere die Map zu einem JSON-Objekt (S. Spezifikation S. 16)
     *
     * @return das JSON-Objekt
     * @apiNote es wird davon ausgegeangen, dass die Map valide ist (zB alle Tiles gesetzt).
     * In diesem Fall wird ein korrektes Objekt erzeugt.
     */
    public JSONObject toJson() {
        return new JSONObject()
                .put("width", getWidth())
                .put("height", getHeight())
                .put("map", Marshall.toJSON(getStringRep()))
                .put("start", Marshall.toJSON(getStart()))
                .put("exit", Marshall.toJSON(getExit()))
                .put("pirates", Marshall.toJSON(getPirates(), Marshall::toJSON))
                .put("treasures", Marshall.toJSON(getTreasures(), Marshall::toJSON))
                .put("harbors", Marshall.toJSON(getHarbours(), Marshall::toJSON));
    }

    /**
     * Erzeugt ein Feld wie in {@link Map}.
     *
     * @return das Feld
     */
    private Tile[][] tilesFromStringRep() {
        var tiles = new Tile[height][width];
        for (int y = 0; y < height; y++) {
            var curRow = stringRep.get(y);
            for (int x = 0; x < width; x++) {
                tiles[y][x] = tileFomChar(curRow.charAt(x));
            }
        }
        return tiles;
    }

    /**
     * Konvertiert die Map zu einer {@link Map}.
     * @return die Map
     */
    public Map toMap() {
        var tiles = tilesFromStringRep();
        var res = new Map(height, width);
        res.setCycles(0);
        res.setClientShip(null);
        res.setGameStarted(false);
        res.setStartPosition(start);
        res.setTiles(tiles);
        MapUtil.addExit(res, exit);
        MapUtil.addPirates(res, pirates);
        MapUtil.addPorts(res, harbours);
        MapUtil.addTreasures(res, treasures);
        return res;
    }

    /**
     * Konvertiert zu einer {@link Map}, überpüft ob die Map gültig ist.
     * @return die Map
     */
    public Map toMapValid() {
        var map = toMap();
        checkMapLayout(map);
        checkPirates();
        checkStartExit();
        return map;
    }

    private void checkValidConnectionDir(Map map, Coordinate pos, Direction dir) {
        var tile = MapUtil.index(map, pos);
        if (!tile.hasConnection(dir)) {
            return;
        }
        var neighbour = pos.coordinatesOf(dir);
        if (!(MapUtil.isInsideBounds(neighbour, map)
                && MapUtil.index(map, neighbour).hasConnection(MapUtil.invertDir(dir)))) {
            var msg = String.format("Broken connection Tile %s, Conn: %s, adjacent: %s", tile.toString(),
                    dir.toString(), neighbour.toString());
            throw new JSONException(msg);
        }
    }

    /**
     * Überprüft, ob die Verbindungen eines Feldes wohldeiniert sind.
     * @param map die generierte Map
     * @param pos die Koordinaten des Feldes, das überprüft werden soll.
     */
    private void checkValidConnection(Map map, Coordinate pos) {
        checkValidConnectionDir(map, pos, Direction.NORTH);
        checkValidConnectionDir(map, pos, Direction.EAST);
        checkValidConnectionDir(map, pos, Direction.SOUTH);
        checkValidConnectionDir(map, pos, Direction.EAST);
    }

    /**
     * Bestimmt die Blöcke und einer Zeile. Überprüft dabei, ob alle Felder der Reihe wohldefiniert sind.
     * @param map die generierte Map
     * @param y die Zeilen-Nummer
     * @return Blöcke
     */
    public List<Block> getBlocks(Map map, int y) {
        int start = 0;
        int end;
        List<Block> blocks = new ArrayList<>(map.getWidth() / 5);
        while (start < map.getWidth()) {
            end = map.getWidth() - 1;
            for (int x = start; x < map.getWidth(); x++) {
                checkValidConnection(map, new Coordinate(x, y));
                if (!map.getTile(x, y).hasConnection(Direction.EAST)) {
                    end = x;
                    break;
                }
            }
            blocks.add(new Block(start, end, y));
            start = end + 1;
        }
        return blocks;
    }

    /**
     * Bestimmt die vertikalen Verbindung zwischen der y-ten und der (y-1) Reihe
     * @param map die Map
     * @param y die Reihennummer
     * @return die vertikalen Verbindungen
     */
    public Set<Integer> getConnections(Map map, int y) {
        Set<Integer> res = new HashSet<>();
        for (int x = 0; x < map.getWidth(); x++) {
            if (map.getTile(x, y - 1).isHasSouth()) {
                res.add(x);
            }
        }
        return res;
    }

    /**
     * Verbindet alle zusammenhängende Blöcke einer Zeile.
     * @param curRow die aktuelle Zeile
     * @param prevRow die vorherige Zeile
     * @param connections die Verbindungen zwischen curRow und prevRow und
     * @return die (maximal) zummanehängenden Blöcke.
     */
    public List<Block> mergeRow(List<Block> curRow, List<Block> prevRow, Set<Integer> connections) {
        for (var block: curRow) {
            block.addVerticallyConnectedBlocks(prevRow, connections);
        }
        List<Block> remaining = curRow;
        List<Block> merged = new ArrayList<>(curRow.size() / 4);
        Block cur = curRow.get(0);
        while (true) {
            var res = cur.merge(remaining);
            merged.add(res.getFst());
            remaining = res.getSnd();
            if (remaining.isEmpty()) {
                return merged;
            }
            cur = remaining.get(0);
        }
    }

    /**
     * Überprüft, ob ein Block nicht erreichbar ist. Dies ist genau dann der Fall, wenn er keine Verbindung nach unten hat.
     * @param blocks Liste mit den *maximalen = größten zusammenhängenden* Blöcken einer Zeile
     * @param connections Verbindungen
     */
    private void checkPrevBlockReachable(Iterable<Block> blocks, Set<Integer> connections) {
        for (var block: blocks) {
            if (!connections.stream().anyMatch((x) -> block.getStart() <= x && x <= block.getEnd())) {
                throw new JSONException(String.format("Block not reachable: %s", block.toString()));
            }
        }
    }

    /**
     * Überprüft das Layout der Map
     * @param map (de)generiertes Map-Objekt mit den Tiles.
     * Die Methode überprüft die Anzahl der unabhängigen Bereiche, aus denen die Map besteht.
     * Gibt es mehr als einen Bereich, sind nicht alle Felder erreichbar.
     */
    private void checkMapLayout(Map map) {
        var prevRow = getBlocks(map, 0);
        for (int y = 1; y < map.getHeight(); y++) {
            var connections = getConnections(map, y);
            checkPrevBlockReachable(prevRow, connections);
            var curRow = getBlocks(map, y);
            prevRow = mergeRow(curRow, prevRow, connections);
        }
        if (prevRow.size() > 1) {
            throw new JSONException("Map ist not closed: More than one block on last row!");
        }
    }

    /**
     * Überprüft Bedingungen für die Piraten
     */
    private void checkPirates() {
        for (var pirate: pirates) {
            if (harbours.contains(pirate)) {
                var msg = String.format("Pirate cannot spawn on a harbour: Coordinate %s", pirate.toString());
                throw new JSONException(msg);
            }
        }
    }

    /**
     * Überprüft Bedingungen für Start und Exit
     */
    private void checkStartExit() {
        if (start.equals(exit)) {
            throw new JSONException(String.format("Start must not equal exit: %s.", start.toString()));
        }
        if (!harbours.contains(start)) {
            throw new JSONException(String.format("Start %s must be a harbor.", start.toString()));
        }
        if (harbours.contains(exit)) {
            throw new JSONException(String.format("Exit %s must not be a harbor", start.toString()));
        }
    }
}
