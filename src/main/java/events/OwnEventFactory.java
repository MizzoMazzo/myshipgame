package events;

import model.Tile.FieldEffect;
import model.Tile.FieldType;

public class OwnEventFactory<T> implements EventFactory<Event> {
    @Override
    public Event createRegistrationAborted() {
        return new RegistrationAbortedEvent();
    }

    @Override
    public Event createGameStarted(int x, int y, int maxCycles, int numTreasures, int numPirates, int numHarbors) {
        return new GameStartedEvent(x, y, maxCycles, numTreasures, numPirates, numHarbors);
    }

    @Override
    public Event createMoved(int x, int y) {
        return new MovedEvent(x, y);
    }

    @Override
    public Event createMapUpdate(int x, int y, boolean pirate, int treasure, boolean north, boolean east, boolean south, boolean west, FieldType fieldType, FieldEffect fieldEffect) {
        return new MapUpdateEvent(x, y, pirate, treasure, north, east, south, west, fieldType, fieldEffect);
    }

    @Override
    public Event createActNow(int actionsLeft) {
        return new ActNowEvent(actionsLeft);
    }

    @Override
    public Event createGameEnd(int score) {
        return new GameEndEvent(score);
    }

    @Override
    public Event createNextCycle(int cyclesLeft) {
        return new NextCycleEvent(cyclesLeft);
    }

    @Override
    public Event createDamaged(int damage) {
        return new DamagedEvent(damage);
    }

    @Override
    public Event createRobbed() {
        return new RobbedEvent();
    }

    @Override
    public Event createRepaired(int cost) {
        return new RepairedEvent(cost);
    }

    @Override
    public Event createPickedUp(int value) {
        return new PickedUpEvent(value);
    }

    @Override
    public Event createHit() {
        return new HitEvent();
    }

    @Override
    public Event createSold(int gold) {
        return new SoldEvent(gold);
    }

    @Override
    public Event createDropped(int value) {
        return new DroppedEvent(value);
    }

    @Override
    public Event createReloaded() {
        return new ReloadedEvent();
    }

    @Override
    public Event createRestocked(int amount) {
        return new RestockedEvent(amount);
    }

    @Override
    public Event createSwirlEffect(int x, int y) {
        return new SwirlEffectEvent(x, y);
    }

    @Override
    public Event createStormEffect() {
        return new StormEffectEvent();
    }

    @Override
    public Event createCommandFailed(String message) {
        return new CommandFailedEvent(message);
    }
}
