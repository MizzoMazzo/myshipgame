package controller;

import commands.*;
import connection.ServerConnection;
import events.*;
import model.*;
import model.Tile.Direction;
import model.Tile.FieldType;
import model.Tile.FieldEffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Server {
    private final Map map;
    private final ServerConnection<Command> serverConnection;
    private final Random random;

        /**
         * Server-Konstruktor Nr. 1 - ohne Debug-Mode.
         *
         * @param map              Karte zum Initialisieren
         * @param seed             Seed für das Random
         * @param serverConnection ServerConnection
         */
        public Server(Map map, long seed, ServerConnection<Command> serverConnection) {
            this.map = map;
            this.serverConnection = serverConnection;
            this.random = new Random(seed);
        }

        /**
         * Starte das Spiel mit dieser Methode
         * 1. Blockiere die ServerConnection mit NextCommand()
         * 2. Führe den Empfangenen Command aus
         * 3. Führe sendEvent auf allen Events aus
         * 4. Prüfe ob Spiel vorbei oder nicht
         * 5. Sende NextCycle an die ServerConnection
         * 6. Rufe runGame auf
         */
        public void startGame() {
            try {
                Command registerCommand = serverConnection.nextCommand();
                List<Event> events = registerCommand.execute(map, random, 3);

                //Iteriere über Event-Liste
                for (Event e : events) {
                    e.sendEvent(serverConnection);
                    if (e.shouldExit()) {
                        exitGame();
                        return;
                    }
                }

                //Registrierung erfolgreich abgeschlossen, Spiel fängt an
                runGame();
            } catch (TimeoutException e) {
                exitGame();
            }
        }

        public void exitGame() {
            serverConnection.close();
        }

        /**
         * Hauptlogik des Spielablaufs
         * 1. Sind noch Zyklen übrig?
         * 2. NextCycle an ServerConnection senden (mit übrigen Zyklen als Parameter)
         * 3. ActionsLeft auf die Health des Spielers setzen
         * 4. Sind noch Aktionen übrig?
         * 5. NextMove an ServerConnection senden (mit übrigen Aktionen als Parameter)
         * 6. NextCommand an Serverconnection senden um diese zu blockieren
         * 7. Empfangenen Command ausführen
         * 8. Events versenden und auf shouldExit() prüfen
         * 9. ActionsLeft dekrementieren
         * 10. Falls keine Aktionen mehr übrig, Piraten bewegen und Events versenden
         * 11. alle 21 Runden Effekte spawnen
         * 12. map.cycles inkrementieren
         * 12. Zurück zu Punkt Nr. 1
         * 13. Falls keine Zyklen mehr vorhanden, exitGame
         */
        public void runGame() {
            while (map.getCycles() <= map.getMaxCycles()) {
                serverConnection.sendNextCycle(map.getMaxCycles() - map.getCycles() + 1);
                //ActionsLeft initialisieren
                int actionsLeft = map.getClientShip().getHealth() - map.getClientShip().getStormPenalty();
                map.getClientShip().setStormPenalty(Math.abs(Math.min(0, actionsLeft)));
                //Solange noch Aktionen übrig sind, ausführen
                while (actionsLeft > 0) {
                    serverConnection.sendActNow(actionsLeft);
                    try {
                        Command nextCommand = serverConnection.nextCommand();
                        List<Event> events = nextCommand.execute(map, random, actionsLeft);

                        for (Event e : events) {
                            e.sendEvent(serverConnection);
                            if (e.shouldExit()) {
                                exitGame();
                                return;
                            }
                        }
                        actionsLeft = actionsLeft - nextCommand.actionsUsed() - map.getClientShip().getStormPenalty();
                        map.getClientShip().setStormPenalty(Math.abs(Math.min(0, actionsLeft)));
                    } catch (TimeoutException e) {
                        serverConnection.sendGameEnd(-1);
                        exitGame();
                        return;
                    }
                }

                //Piraten bewegen
                List<Event> pirateEvents = movePirates();

                for (Event e : pirateEvents) {
                    e.sendEvent(serverConnection);
                    if (e.shouldExit()) {
                        exitGame();
                        return;
                    }
                }

                //Effekte spawnen
                if (map.getCycles() % 21 == 0) {
                    map.spawnEffects(random);
                }

                //Cycle muss jede Runde um 1 erhöht werden
                map.setCycles(map.getCycles() + 1);

            }

            serverConnection.sendGameEnd(-1);
            //Keine Zyklen mehr übrig: Spiel beenden
            exitGame();
        }

        /**
         * Muwt alle Piraten. Falls Pirat nicht tot, nicht auf Player, zieht priorisiert auf Player ansonsten auf
         * random adjacent Tile.
         *
         * @return Die List an Events die ALLE Piraten verursachen
         */
        public List<Event> movePirates() {
            List<Direction> possibleMove;
            List<Event> res = new ArrayList<>();
            for (Pirate k : map.getPirates()) {
                possibleMove = new ArrayList<>();
                //falls pirat tot || pirate steht auf player
                if (k.isDead() || map.getClientShip().getPosition().equals(k.getPosition())) {
                    continue;
                }
                this.adjacentTiles(possibleMove, k);
                this.calculatePirateMove(res, possibleMove, k);
            }
            return res;
        }


        /**
         * Helferfunktion
         * Name ist Programm
         *
         * @param coord Das gewollte Tile
         * @return Das gewollte Tile
         */
        public Tile tileFromCoordinate(Coordinate coord) {
            return map.getTile(coord.getxCoordinate(), coord.getyCoordinate());
        }

        /**
         * Bewegt den {@code pirate} in eine Richtung enhalten in {@code dir}
         * zuerst wird gestestet ob PLAYER in sichtweite ist und bewegt die entsprechende Richtung
         * ansonsten wird in eine zufällige richting enthalten in {@code dir} bewegt.
         *
         * @param events Resultierende Events nach move und ggf. rob/dmg event
         * @param dir    Liste ALLER moeglichen zuege
         * @param pirate Der zu bewegende Pirat
         */
        public void calculatePirateMove(List<Event> events, List<Direction> dir, Pirate pirate) {
            boolean movedFlag = false;
            List<Direction> direcitonWithoutHarborAndPirate;
            for (Direction d : dir) {
                //player sichtbar
                if (map.getClientShip().getPosition().equals(pirate.getPosition().coordinatesOf(d))) {
                    //pirat||hafen mit spieler
                    if (this.tileFromCoordinate(map.getClientShip().getPosition()).isPirate()
                            || this.tileFromCoordinate(pirate.getPosition().coordinatesOf(d)).getFieldType() == FieldType.HARBOR) {
                        movedFlag = true;
                        continue;
                    }
                    //falls das startfeld des Piraten im Sichtfeld ist wird es als MapUpdate geadded
                    if (map.getVisiblePlayerCoordinates().contains(pirate.getPosition())) {
                        events.add(new MapUpdateEvent(pirate.getPosition().getxCoordinate(), pirate.getPosition().getyCoordinate(), this.addMapUpdateTileWithoutPirate(pirate)));
                    }
                    //Pirate zieht auf spieler
                    this.movePiratesInGivenDirection(d, pirate);
                    //falls das zielfeld des Pirate im Sichtfeld ist wird es als MapUpdate geadded
                    if (map.getVisiblePlayerCoordinates().contains(pirate.getPosition())) {
                        events.add(new MapUpdateEvent(pirate.getPosition().getxCoordinate(), pirate.getPosition().getyCoordinate(), this.tileFromCoordinate(pirate.getPosition())));
                    }
                    this.addEvents(events);
                    movedFlag = true;
                }
            }
            //pirate macht random move auf felder Ohne Harbor
            if (!movedFlag && !dir.isEmpty()) {
                direcitonWithoutHarborAndPirate = this.tilesWithoutHarborAndPirate(dir.iterator(), pirate);
                if (!direcitonWithoutHarborAndPirate.isEmpty()) {
                    //falls das startfeld des Piraten im Sichtfeld ist wird es als MapUpdate geadded
                    if (map.getVisiblePlayerCoordinates().contains(pirate.getPosition())) {
                        events.add(new MapUpdateEvent(pirate.getPosition().getxCoordinate(), pirate.getPosition().getyCoordinate(), this.addMapUpdateTileWithoutPirate(pirate)));
                    }
                    movePiratesInGivenDirection(direcitonWithoutHarborAndPirate.get(random.nextInt(direcitonWithoutHarborAndPirate.size())), pirate);
                    //falls das zielfeld des Pirate im Sichtfeld ist wird es als MapUpdate geadded
                    if (map.getVisiblePlayerCoordinates().contains(pirate.getPosition())) {
                        events.add(new MapUpdateEvent(pirate.getPosition().getxCoordinate(), pirate.getPosition().getyCoordinate(), this.tileFromCoordinate(pirate.getPosition())));
                    }
                }
            }

        }

        /**
         * Ändert die Position des {@code Pirate} auf der Map
         *
         * @param dir    die richtung in die Bewegt wird
         * @param pirate der yu bewegende Pirat
         */
        public void movePiratesInGivenDirection(Direction dir, Pirate pirate) {
            this.tileFromCoordinate(pirate.getPosition()).setPirate(false);
            pirate.setPosition(pirate.getPosition().coordinatesOf(dir));
            this.tileFromCoordinate(pirate.getPosition()).setPirate(true);
        }

        /**
         * Prüft ob Pirat {@code k} in Richtung {@code dir} muwen kann,
         * falls tile verbunden und KEIN effekt.
         *
         * @param dir in die zu bewegende Richtung
         * @param k   der zu bewegende Pirat
         * @return true, falls dir valid, sonst false
         */
        public boolean pirateCanMoveInDirection(Direction dir, Pirate k) {
            return this.fromDirectionToConnection(dir, k)
                    && this.tileFromCoordinate(k.getPosition().coordinatesOf(dir)).getEffect() == FieldEffect.NONE;
        }

        /**
         * Hilfsfunktion, Name ist Programm
         *
         * @param dir    die zu pruefende Verbindung
         * @param pirate der betrachtete Pirat
         * @return true, falls verbindung in dir besteht, sonst false
         */
        public boolean fromDirectionToConnection(Direction dir, Pirate pirate) {
            switch (dir) {
                case NORTH:
                    return map.getPirateTile(pirate.getID()).isHasNorth();
                case EAST:
                    return map.getPirateTile(pirate.getID()).isHasEast();
                case SOUTH:
                    return map.getPirateTile(pirate.getID()).isHasSouth();
                case WEST:
                    return map.getPirateTile(pirate.getID()).isHasWest();
                default:
                    return false;
            }
        }

        /**
         * Nachdem alle benachbarten tiles gecheckt wurden muessen nun jene mit Harborn auf ihnen geloescht werden
         *
         * @param withHarbor liste aller benachbarten felder
         * @param k          der betrachtete Pirat
         * @return liste alle benachbarten felder ohne harbor
         */
        public List<Direction> tilesWithoutHarborAndPirate(Iterator<Direction> withHarbor, Pirate k) {
            List<Direction> res = new ArrayList<>();
            Direction current;
            while (withHarbor.hasNext()) {
                current = withHarbor.next();
                if (this.tileFromCoordinate(k.getPosition().coordinatesOf(current)).getFieldType() != FieldType.HARBOR
                        && !this.tileFromCoordinate(k.getPosition().coordinatesOf(current)).isPirate()) {
                    res.add(current);
                }
            }
            return res;
        }

        /**
         * Brechnet alle tiles mit verbindungen zum eigenen Tile
         * AUCH MIT HARBOR
         *
         * @param res    der Rueckgabewert in dem alle verbundenen Tiles enthalten sind
         * @param pirate der betrachtete Pirat
         */
        public void adjacentTiles(List<Direction> res, Pirate pirate) {
            //tile Has North Conn && not Port && not FieldEffect && not Pirate
            if (this.pirateCanMoveInDirection(Direction.NORTH, pirate)) {
                res.add(Direction.NORTH);
            }
            //tile Has East Conn && not Port && not FieldEffect && not Pirate
            if (this.pirateCanMoveInDirection(Direction.EAST, pirate)) {
                res.add(Direction.EAST);
            }
            //tile Has SOUTH Conn && not Port && not FieldEffect && not Pirate
            if (this.pirateCanMoveInDirection(Direction.SOUTH, pirate)) {
                res.add(Direction.SOUTH);
            }
            //tile Has west Conn && not Port && not FieldEffect && not Pirate
            if (this.pirateCanMoveInDirection(Direction.WEST, pirate)) {
                res.add(Direction.WEST);
            }
        }


        public Tile addMapUpdateTileWithoutPirate(Pirate k) {
            Tile pirateTile = this.tileFromCoordinate(k.getPosition());
            return new Tile(pirateTile.getEffect(), pirateTile.getFieldType(), pirateTile.isHasNorth(), pirateTile.isHasEast(), pirateTile.isHasSouth(), pirateTile.isHasWest(), false, pirateTile.getTreasure());
        }

        /**
         * events werden geadded falls pirat auf player tile zieht
         *
         * @param events die resultierende EventList
         */
        public void addEvents(Collection<Event> events) {
            //Events werden geadded
            events.add(new RobbedEvent());
            events.add(new DamagedEvent(1));
            //Events werden 'physikalisch' am schiff ausgelassen
            if (map.getClientShip().getTreasureStorage() != null) {
                map.getClientShip().getTreasureStorage().clear();
            }
            map.getClientShip().setHealth(map.getClientShip().getHealth() - 1);
            if (map.hasGameEnded()) {
                events.add(new GameEndEvent(GameEndEvent.LOST_SCORE));
            }
        }


        public Random getRandom() {
            return random;
        }

    }
