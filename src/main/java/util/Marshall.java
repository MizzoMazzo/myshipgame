package util;

import model.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;

public final class Marshall<T> {

    private Marshall() {
    }

    public static Object toJSON(Object obj) {
        return obj;
    }

    /**
     * Konvertiert eine {@link Coordinate} zu JSON.
     *
     * @param coordinate die Koordinate
     * @return JSON-Array
     */
    public static JSONArray toJSON(Coordinate coordinate) {
        return new JSONArray().put(coordinate.getxCoordinate()).put(coordinate.getyCoordinate());
    }

    /**
     * Konvertiert einen {@link Treasure} zu JSON.
     *
     * @param treasure Schatz + Koordinate
     * @return JSON-Array
     */
    public static JSONArray toJSON(Pair<Coordinate, Treasure> treasure) {
        return toJSON(treasure.getFst()).put(treasure.getSnd().getValue());
    }

    /**
     * Marshalled ein Collection zu einem JSON-Array.
     *
     * @param collection die Collecton
     * @param op         Funktion, die Elemente zu JSON konvertiert
     * @param <T>        der Element-Typ
     * @return ein JSON-Array
     */
    public static <T> JSONArray toJSON(Collection<T> collection, Function<T, Object> op) {
        JSONArray res = new JSONArray();
        for (T item : collection) {
            res.put(op.apply(item));
        }
        return res;
    }


    /**
     * Liest {@link Coordinate} aus einem JSON-Array aus.
     *
     * @param array JSON-Array
     * @return die Koordinaten
     * @throws JSONException wenn Parse-Error auftreten oder das Array >= 2 Elemente hat
     */
    public static Coordinate coordFromJSON(JSONArray array, int width, int height) {
        try {
            if (array.length() != 2) {
                throw new JSONException(String.format("Expected format: [x, y], got %s", array.toString()));
            }
            var coord = new Coordinate(array.getInt(0), array.getInt(1));
            if (!MapUtil.isInsideBounds(coord, width, height)) {
                throw new JSONException(String.format("Coordinate out of range: %s", coord.toString()));
            }
            return coord;
        } catch (JSONException excp) {
            throw new JSONException(String.format("Failed to parse coordinate: %s", excp.getMessage()), excp);
        }
    }

    /**
     * Liest Treasure aus einem JSON-Array aus.
     *
     * @param array das JSOn-Array
     * @return den Schatz
     * @throws JSONException wenn Parse-Error auftreten oder das Array >= 3 Elemente hat
     */
    public static Pair<Coordinate, Treasure> treasurefromJSON(JSONArray array, int width, int height) {
        try {
            if (array.length() != 3) {
                throw new JSONException(String.format("Expected format: [x, y, value], got %s", array.toString()));
            }
            var pos = new Coordinate(array.getInt(0), array.getInt(1));
            if (!MapUtil.isInsideBounds(pos, width, height)) {
                throw new JSONException(String.format("Treasure pos %s out of range", pos.toString()));
            }
            var value = array.getInt(2);
            if (!MapUtil.TreasureValueGenerator.validValue(value)) {
                throw new JSONException(String.format("Treasure value out of range: %d", value));
            }
            return new Pair<>(pos, new Treasure(value));
        } catch (JSONException excp) {
            throw new JSONException(String.format("Failed to parse treasure: %s", excp.getMessage()), excp);
        }
    }

    /**
     * Liest Liste aus Koordinaten ein.
     *
     * @param array JSON-Array
     * @param desc  Beschreibung, was die Koordinaten Bescreiben
     * @return Koordinaten-Liste
     * @throws JSONException
     */
    public static List<Coordinate> coordListFromJSON(JSONArray array, String desc, int width, int height) {
        try {
            Set<Coordinate> res = new LinkedHashSet<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                Object item = array.get(i);
                if (!(item instanceof JSONArray)) {
                    throw new JSONException("Non coordinate in coordinate list!");
                }
                var cord = coordFromJSON((JSONArray) item, width, height);
                if (res.contains(cord)) {
                    throw new JSONException(String.format("List constains %s %s twice", desc, item.toString()));
                }
                res.add(cord);
            }
            return new ArrayList<>(res);
        } catch (JSONException excp) {
            throw new JSONException(String.format("Failed to parse %s list: %s", desc, excp.getMessage()), excp);
        }
    }

        private static boolean isValidStringRepChar(int c) {
        return c == '\u2500'
            || c == '\u2502'
            || c == '\u2514'
            || c == '\u250C'
            || c == '\u2510'
            || c == '\u2518'
            || c == '\u252C'
            || c == '\u2524'
            || c == '\u2534'
            || c == '\u251C'
            || c == '\u253C'
            || c == '\u2574'
            || c == '\u2575'
            || c == '\u2576'
            || c == '\u2577';
    }

    public static boolean isInvalidStringRep(int c) {
        return !isValidStringRepChar(c);
    }

    /**
     * Liest Liste aus Schätzen ein.
     *
     * @param array JSON-Array
     * @return Koordinaten-Liste
     * @throws JSONException
     */

    public static List<Pair<Coordinate, Treasure>> treasureListFromJSON(JSONArray array, int width, int height) {
        try {
            List<Pair<Coordinate, Treasure>> res = new ArrayList<>(array.length());
            Set<Coordinate> occupied = new HashSet<>(MapUtil.maxTreasureCount(width, height));
            for (int i = 0; i < array.length(); i++) {
                Object item = array.get(i);
                if (!(item instanceof JSONArray)) {
                    throw new JSONException("Non treasure in treasure list");
                }
                var treasure = treasurefromJSON((JSONArray) item, width, height);
                if (occupied.contains(treasure.getFst())) {
                    throw new JSONException(String.format("Treasure @ %s appears twice", treasure.getFst()));
                }
                occupied.add(treasure.getFst());
                res.add(treasure);
            }
            return res;
        } catch (JSONException excp) {
            throw new JSONException(String.format("Failed to parse treasure list: %s", excp.getMessage()), excp);
        }
    }

    public static <T> List<T> fromOptionalArray(JSONObject obj, Function<JSONArray, List<T>> f, String key, String desc) {
        if (!obj.has(key)) {
            return new ArrayList<>(1);
        } else if (obj.get(key) instanceof JSONArray) {
            return f.apply((JSONArray) obj.get(key));
        } else {
            throw new JSONException(String.format("Failed to parse %s list: Item at key %s is not a JSONArray", desc, key));
        }
    }

    /**
     * Liest String-Repräsentation der Map ein.
     *
     * @param array  JSON-Array
     * @param width  Breite der Map
     * @param height Höhe der Map
     * @return String Repräsentation der Map
     * @throws JSONException ParseError, Invalide Größe oder invalides Zeichen
     */
    public static List<String> stringRepFromJSON(JSONArray array, int width, int height) {
        try {
            if (array.length() > height) {
                throw new JSONException(String.format("Too much rows, expected: %d, got %d", height, array.length()));
            }
            List<String> res = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                String string = array.getString(i);
                if (string.chars().anyMatch(Marshall::isInvalidStringRep)) {
                    throw new JSONException("Invalid character in string representation");
                }
                if (string.length() != width) {
                    throw new JSONException(String.format("Line is too long: Expected %d, got %d", width, string.length()));
                }
                res.add(string);
            }
            return res;
        } catch (JSONException excp) {
            throw new JSONException("Failed to parse string representation", excp);
        }
    }

    /**
     * Erzeut eine eine Map aus einer JSON-Datei.
     *
     * @param object das JSON-Objekt
     * @return die Map
     */
    public static SerializableMap serializableMapFromJSON(JSONObject object) {
        var width = object.getInt("width");
        if (!MapUtil.mapSizeValid(width)) {
            throw new JSONException(String.format("Invalid map size: %d", width));
        }
        var height = object.getInt("height");
        if (!MapUtil.mapSizeValid(height)) {
            throw new JSONException(String.format("Invalid map size: %d", height));
        }
        var start = coordFromJSON(object.getJSONArray("start"), width, height);
        var exit = coordFromJSON(object.getJSONArray("exit"), width, height);
        var pirates = fromOptionalArray(object, (a) -> coordListFromJSON(a, "pirate", width, height), "pirates", "pirate");
        var ports = coordListFromJSON(object.getJSONArray("harbors"), "harbor", width, height);
        var stringRep = stringRepFromJSON(object.getJSONArray("map"), width, height);
        var treasures = fromOptionalArray(object, (a) -> treasureListFromJSON(a, width, height), "treasures", "treasure");
        return new SerializableMap(width, height, start, exit, stringRep, pirates, ports, treasures);
    }

    static public String prettyPrintJSON(JSONObject obj) {
        return obj.write(new StringWriter(), 2, 2).toString();
    }
}
