package model;

public class Barque extends Ship {
    private static final int MAXHEALTH = 3;
    private int health = 3;
    private int gold;

    public Barque(Coordinate coordinate, String name) {
        super(coordinate, name, new AmmunitionStorage(true), true);
        setTreasureStorage(new TreasureStorage());
    }

    @Override
    public int getGold() {
        return gold;
    }

    @Override
    public void setGold(int gold) {
        this.gold = gold;
    }

    @Override
    public ActorType getActorType() {
        return ActorType.BARQUE;
    }

    @Override
    public boolean hasPenaltyWhenShooting() {
        return true;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public boolean isDead() {
        return health == 0;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public int getMaxHealth() {
        return MAXHEALTH;
    }
}
