package commands;

import model.Ship.ActorType;
import model.Tile.Direction;

public interface CommandFactory<T> {
    T createRegister(String name, ActorType actorType);

    T createMove(Direction direction);

    T createEndTurn();

    T createRepair();

    T createFire(Direction direction);

    T createPickup();

    T createSell();

    T createDrop(int index);

    T createReload();

    T createRestock(int amount);

    T createLeave();
}
