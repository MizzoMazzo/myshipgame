package model;

public class Cutter extends Ship{
    private static final int MAXHEALTH = 1;
    private int health = 1;

    public Cutter(Coordinate coordinate, String name) {
        super(coordinate, name, new AmmunitionStorage(false), false);
        setTreasureStorage(null);
    }

    @Override
    public boolean isLoaded() {
        return false;
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

    @Override
    public int getGold() {
        return -1;
    }

    @Override
    public void setGold(int gold) {
        //DoNothing
    }

    @Override
    public ActorType getActorType() {
        return ActorType.CUTTER;
    }

    @Override
    public boolean hasPenaltyWhenShooting() {
        return false;
    }
}
