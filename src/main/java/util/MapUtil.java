package util;

import model.*;
import model.Tile.Direction;
import model.Tile.FieldType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Verschieden Hilfsfuktonen, die bei der Generierung / Serialization der Map
 * verwendet werden.
 */
public final class MapUtil {

    // Konstanten / Funktionen ... aus der Spezifikatoin
    // Multiplikator zu Berechnung der Anzahl Piraten
    public static final double PIRATE_COUNNT_MULT = 0.05;
    public static final double PORT_COUNTM_MULT = 0.01;
    public static final double TREASURE_COUNT_MIN_MULT = 0.05;
    public static final double TREASURE_COUNT_MAX_MULT = 0.2;

    private MapUtil() {
    }

    /**
     * Gibt die Anzahl der Häfen, die die Map haben sollte, zurück.
     *
     * @param width  die Bereited der Map
     * @param height die Höhe der Map
     * @return Anzahl Häfen
     */
    public static int portCount(int width, int height) {
        return (int) Math.ceil(width * height * PORT_COUNTM_MULT);
    }

    /**
     * @see #portCount(int, int)
     */
    public static int portCount(Map map) {
        return portCount(map.getWidth(), map.getHeight());
    }

    /**
     * Gibt die Anzahl der Piraten, die die Map haben sollte, zurück.
     *
     * @param width  Breite der Map
     * @param height Höhe der Map
     * @return Anzahl der Piratem
     */
    public static int pirateCount(int width, int height) {
        return (int) Math.ceil(width * height * PIRATE_COUNNT_MULT);
    }

    /**
     * @see #pirateCount(int, int)
     */
    public static int pirateCount(Map map) {
        return pirateCount(map.getWidth(), map.getHeight());
    }

    /**
     * Minimaler Abstand der Piraten zum Spieler beim Spawn.
     *
     * @param width  Breite des Feldes
     * @param height Höhe des Felde
     * @return minmaler Abstand
     */
    public static int minPlayerDistance(int width, int height) {
        return Math.floorDiv(Math.min(width, height), 2);
    }

    /**
     * @see #minPlayerDistance(int, int)
     */
    public static int minPlayerDistance(Map map) {
        return minPlayerDistance(map.getWidth(), map.getHeight());
    }

    /**
     * Gibt die minimale Anzahl der Schätze, die die Map haben sollte, zurück.
     *
     * @param width  Breite der Map
     * @param height Höhe der Map
     * @return minimale Anzahl der Schätze
     */
    public static int minTreasureCount(int width, int height) {
        return (int) Math.ceil(width * height * TREASURE_COUNT_MIN_MULT);
    }

    /**
     * @see #minTreasureCount(int, int)
     */
    public static int minTreasureCount(Map map) {
        return minTreasureCount(map.getWidth(), map.getHeight());
    }

    /**
     * Gibt die minimale Anzahl der Schätze, die die Map haben sollte, zurück.
     *
     * @param width  Breite der Map
     * @param height Höhe der Map
     * @return maximale Anzahl der Schätze
     */
    public static int maxTreasureCount(int width, int height) {
        return (int) Math.ceil(width * height * TREASURE_COUNT_MAX_MULT);
    }

    /**
     * @see #maxTreasureCount(int, int)
     */
    public static int maxTreasureCount(Map map) {
        return maxTreasureCount(map.getWidth(), map.getHeight());
    }

    /**
     * Überprüft, ob Höhe / Breite der Map der Spezifikation entspricht.
     *
     * @param sz Höhe / Breite der Map
     * @return
     */
    public static boolean mapSizeValid(int sz) {
        return 3 <= sz && sz <= 50;
    }

    /**
     * Überprüft, ob eine Koordinate für die Höhe/Breite zulässig ist
     *
     * @param coord  Koordinaten
     * @param width  Höhe der Map
     * @param height Beriet der Map
     */
    public static boolean isInsideBounds(Coordinate coord, int width, int height) {
        var x = coord.getxCoordinate();
        var y = coord.getyCoordinate();
        return 0 <= x && x < width
            && 0 <= y && y < height;
    }

    public static boolean isInsideBounds(Coordinate coord, Map map) {
        return isInsideBounds(coord, map.getWidth(), map.getHeight());
    }

    /**
     * Indixert eine Map mit einer Koordinate.
     *
     * @param tiles das Feld (Vergl. {@link Map}
     * @param c     die Koordinate des Feldes
     * @return das Feld an der Stelle
     */
    public static Tile index(Tile[][] tiles, Coordinate c) {
        return tiles[c.getyCoordinate()][c.getxCoordinate()];
    }

    /**
     * Indixert eine Map mit einer Koordinate.
     *
     * @param map die Map
     * @param c   die Koordinate des Feldes
     * @return das Feld an der Stelle
     */
    public static Tile index(Map map, Coordinate c) {
        return map.getTile(c.getxCoordinate(), c.getyCoordinate());
    }

    public static Direction invertDir(Direction dir) {
        switch (dir) {
            case NORTH:
                return Direction.SOUTH;
            case EAST:
                return Direction.WEST;
            case SOUTH:
                return Direction.NORTH;
            case WEST:
                return Direction.EAST;
            default:
                return Direction.HERE;
        }
    }

    /**
     * Erzeugt einen neuen Piraten der aktuellen Piraten-Klasse.
     *
     * @param coord Positon des Piraten
     * @param id    ID
     * @return ein neuer Pirat
     */
    static public Pirate spwanPirate(Coordinate coord, int id) {
        return new Pirate(coord, id);
    }

    /**
     * Fügt eine Liste von Koordinaten als Piraten der Map hinzu.
     *
     * @param map     die Map
     * @param pirates Koordinaten der Piraten
     */
    static public void addPirates(Map map, Collection<Coordinate> pirates) {
        List<Pirate> pirateShips = new ArrayList<>(pirates.size());
        int pirateId = 0;
        for (Coordinate pos : pirates) {
            pirateShips.add(MapUtil.spwanPirate(pos, pirateId));
            index(map, pos).setPirate(true);
            pirateId++;
        }
        map.setPirates(pirateShips);
    }

    /**
     * Fügt eine Liste von Koordinaten als Häfen der Map hinzu.
     *
     * @param map   die Map
     * @param ports Koordinaten der Häfen
     */
    static public void addPorts(Map map, Collection<Coordinate> ports) {
        for (Coordinate coord : ports) {
            index(map, coord).setFieldType(FieldType.HARBOR);
        }
        map.setNumPorts(ports.size());
    }

    /**
     * Fügt Schätze der Map hinzu.
     *
     * @param map       die Map
     * @param treasures Schätze + Koordinaten
     */
    static public void addTreasures(Map map, Collection<Pair<Coordinate, Treasure>> treasures) {
        for (var treasure : treasures) {
            index(map, treasure.getFst()).setTreasure(treasure.getSnd());
        }
        map.setNumTreasures(treasures.size());
    }

    /**
     * Fügt Ausgang zur Map hnizu
     *
     * @param map  die Map
     * @param exit Koordinaten des Ausgangs
     */
    static public void addExit(Map map, Coordinate exit) {
        index(map, exit).setFieldType(FieldType.EXIT);
    }

    /**
     * Random-Generator Mock, das zum Debuging des Mapgenerators verwendet werden könnte.
     *
     * @param <T> Die Debug-Information, die der MapGenerator zu Verfügung stellt.
     */
    public interface RandGenMock<T> {
        int nextInt(T debugInfo, int lowerBound, int upperBound);

        double nextDouble(T debugInfo);
    }

    /**
     * Wrapped einen gewöhnlichen Random-Generator.
     *
     * @param <T> Die Debug-Information, die vom mapGenerator verwendet wird.
     */
    public static class RandGenAdapter<T> implements RandGenMock<T> {
        private final Random randGen;

        public RandGenAdapter(long seed) {
            this.randGen = new Random(seed);
        }

        public RandGenAdapter() {
            this.randGen = new Random();
        }

        @Override
        public int nextInt(T debugInfo, int lowerBound, int upperBound) {
            assert lowerBound <= upperBound;
            if (lowerBound == upperBound) {
                return lowerBound;
            } else {
                return randGen.nextInt(upperBound - lowerBound) + lowerBound;
            }
        }

        @Override
        public double nextDouble(T debugInfo) {
            return randGen.nextDouble();
        }
    }

    /**
     * Zufallsgenerator, der Schatz-Werte mit der Pareto-Verteilung generiert.
     */
    public static class TreasureValueGenerator {
        private static final double PAR_SCALE = 0.1;        //  Alpha aus Spezi
        private static final int MIN_TREASURE_VAL = 1;      //  Kleinster Schatzwert
        private static final int MAX_TREASURE_VAL = 9;      //  größter Schatzwert
        private static final double TOTAL;                  //  Summe pareto-Werte

        static {
            double sum = 0;
            for (int x = MIN_TREASURE_VAL; x <= MAX_TREASURE_VAL; x++) {
                sum += pareto(x);
            }
            TOTAL = sum;
        }

        private final List<Pair<Double, Integer>> propTable;

        public TreasureValueGenerator() {
            propTable = new ArrayList<>(MAX_TREASURE_VAL - MIN_TREASURE_VAL);
            double prev = 0;
            for (int x = MIN_TREASURE_VAL; x <= MAX_TREASURE_VAL; x++) {
                double bound = probabiliy(x) + prev;
                propTable.add(new Pair<>(bound, x));
                prev = bound;
            }
        }

        public static double pareto(double x) {
            return PAR_SCALE * Math.pow(MIN_TREASURE_VAL, PAR_SCALE) / Math.pow(x, PAR_SCALE + 1);
        }

        public static double probabiliy(double x) {
            return pareto(x) / TOTAL;
        }

        public static boolean validValue(int val) {
            return MIN_TREASURE_VAL <= val && val <= MAX_TREASURE_VAL;
        }

        public <T> int nextValue(T debugInfo, RandGenMock<T> randGen) {
            double dice = randGen.nextDouble(debugInfo);
            double lowerBound = 0;
            for (var entry : propTable) {
                var bound = entry.getFst();
                var value = entry.getSnd();
                if (lowerBound <= dice && dice < bound) {
                    return value;
                }
                lowerBound = bound;
            }
            return MAX_TREASURE_VAL;
        }
    }

}
