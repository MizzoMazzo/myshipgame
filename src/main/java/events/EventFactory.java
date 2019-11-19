package events;

import model.Tile.FieldEffect;
import model.Tile.FieldType;

public interface EventFactory<T> {
    T createRegistrationAborted();

    T createGameStarted(int x, int y, int maxCycles, int numTreasures, int numPirates, int numHarbors);

    T createMoved(int x, int y);

    T createMapUpdate(int x, int y, boolean pirate, int treasure, boolean north, boolean east, boolean south, boolean west, FieldType fieldType, FieldEffect fieldEffect);

    T createActNow(int actionsLeft);

    T createGameEnd(int score);

    T createNextCycle(int cyclesLeft);

    T createDamaged(int damage);

    T createRobbed();

    T createRepaired(int cost);

    T createPickedUp(int value);

    T createHit();

    T createSold(int gold);

    T createDropped(int value);

    T createReloaded();

    T createRestocked(int amount);

    T createSwirlEffect(int x, int y);

    T createStormEffect();

    T createCommandFailed(String message);
}
