package commands;

import events.*;
import model.*;
import model.Ship.ActorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RegisterCommand extends Command {
    private final String name;
    private final ActorType actorType;

    public RegisterCommand(String name, ActorType actorType) {
        this.name = name;
        this.actorType = actorType;
    }


    @Override
    public int actionsUsed() {
        return 1;
    }

    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {
        if (map.isGameStarted()) {
            List<Event> eventList = new ArrayList<>();
            eventList.add(new CommandFailedEvent(this));
            return eventList;
        } else {
            switch (actorType) {
                case CUTTER:
                    map.setClientShip(new Cutter(map.getStartPosition(), name));
                    break;
                case BARQUE:    // DON'T ADD BREAK AFTER THIS LINE!!!
                default:
                    map.setClientShip(new Barque(map.getStartPosition(), name));
                    break;
            }

            map.setCycles(1);
            map.setGameStarted(true);

            List<Event> eventList = new ArrayList<>();
            eventList.add(new GameStartedEvent(map.getStartPosition().getxCoordinate(), map.getStartPosition().getyCoordinate(), map.getMaxCycles(), map.getNumTreasures(), map.getPirates().size(), map.getNumPorts()));
            eventList.addAll(updatePlayerVision(map));
            return eventList;
        }
    }

    @Override
    public boolean requiresGameStarted() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegisterCommand that = (RegisterCommand) o;
        return Objects.equals(name, that.name) &&
                actorType == that.actorType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, actorType);
    }

    @Override
    public String toString() {
        return "RegisterCommand";
    }
}
