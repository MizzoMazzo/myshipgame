package model;

public abstract class Ship {
    private final String name;
    private final AmmunitionStorage ammunitionStorage;
    private int stormPenalty;
    protected boolean loaded;
    private Coordinate position;
    private TreasureStorage treasureStorage;

    public Ship(Coordinate coordinate, String name, AmmunitionStorage ammunitionStorage, boolean loaded) {
        this.name = name;
        this.position = coordinate;
        this.ammunitionStorage = ammunitionStorage;
        this.loaded = loaded;
    }

    public enum ActorType {
        BARQUE,
        CUTTER;
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public TreasureStorage getTreasureStorage() {
        return treasureStorage;
    }

    public void setTreasureStorage(TreasureStorage treasureStorage) {
        this.treasureStorage = treasureStorage;
    }

    public void setTreasure(int treasureID, int treasureValue) {
        if (treasureValue == 0) {
            treasureStorage.remove(treasureID);
        } else {
            treasureStorage.add(new Treasure(treasureValue));
        }
    }

    public AmmunitionStorage getAmmunitionStorage() {
        return ammunitionStorage;
    }

    public abstract boolean isLoaded();

    public void setLoaded(boolean status) {
        loaded = status;
    }

    public int getStormPenalty() {
        return stormPenalty;
    }

    public void setStormPenalty(int penalty) {
        stormPenalty = penalty;
    }

    public abstract boolean isDead();

    public abstract int getHealth();

    public abstract void setHealth(int health);

    public abstract int getMaxHealth();

    public abstract int getGold();

    public abstract void setGold(int gold);

    public abstract ActorType getActorType();

    /**
     * @return Ob der Spieler eine Strafe bei Schuss ohne Kugel bekommt.
     */
    public abstract boolean hasPenaltyWhenShooting();
}
