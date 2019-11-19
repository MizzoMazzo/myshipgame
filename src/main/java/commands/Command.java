package commands;

import events.*;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Command {
    /**
     * Generiert für jedes für den Spieler sichtbare Tile ein MapUpdate-Event.
     */
    protected static List<Event> updatePlayerVision(Map map) {
        List<Coordinate> view = map.getVisiblePlayerCoordinates();
        List<Event> eventList = new ArrayList<>(view.size());
        for (Coordinate c : view) {
            eventList.add(new MapUpdateEvent(c.getxCoordinate(), c.getyCoordinate(), map.getTile(c.getxCoordinate(), c.getyCoordinate())));
        }
        return eventList;
    }

    /**
     * Prüft, ob ob das Spiel aufgrund der Gesundheit verloren wurde.
     */
    protected static boolean isGameOver(Map map) {
        return map.getClientShip().getHealth() <= 0;
    }

    /**
     * Anzahl der durch diesen Command verbrauchten Züge.
     */
    public abstract int actionsUsed();

    /**
     * Implementiert das Verhalten des jeweiligen Commands.
     * Der Aufruf darf ausschließlich über {@link Command#execute(Map, Random, int)} erfolgen.
     * Für Testzwecke ist bei statisch bekanntem Typ Zugriff auf exec zulässig (in Klassen public).
     */
    protected abstract List<Event> exec(Map map, Random random, int actionsLeft);

    public final List<Event> execute(Map map, Random random, int actionsLeft) {
        if (requiresGameStarted() && !map.isGameStarted()) {
            List<Event> eventList = new ArrayList<>();
            eventList.add(new RegistrationAbortedEvent());
            return eventList;
        } else {
            return handlePirates(map, exec(map, random, actionsLeft));
        }
    }

    /**
     * Ob der Command nur während einem laufenden Spiel ausgeführt werden darf.
     */
    public abstract boolean requiresGameStarted();


    /**
     *Handled Piraten am Ende einer Spieleraktion.
     * @param map - die Map.
     * @param retlist - die Eventlist der Aktion.
     * @return geupdatete und finale Eventlist.
     */
    protected List<Event> handlePirates(Map map, List<Event> retlist) {
        if (retlist.stream().anyMatch(event -> event.getClass().equals(GameEndEvent.class))) {
            return retlist;
        } else if (map.getClientShip() != null && map.getPlayerTile().isPirate()) {
            retlist.add(new RobbedEvent());
            TreasureStorage arrrgh = map.getClientShip().getTreasureStorage();
            if (arrrgh != null) {
                arrrgh.clear();
            }
            retlist.add(new DamagedEvent(RobbedEvent.PIRATE_DAMAGE));
            map.getClientShip().setHealth(map.getClientShip().getHealth() - RobbedEvent.PIRATE_DAMAGE);
            if (isGameOver(map)) {
                retlist.add(new GameEndEvent(GameEndEvent.LOST_SCORE));
            }
        }
        return retlist;
    }
}
