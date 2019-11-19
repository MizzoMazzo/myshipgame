package model;

public class Pirate {
    private final int id;
    private Coordinate position;
    private int life = 2;

    public Pirate(Coordinate position, int id) {
        this.position = position;
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public int getLife() {
        return life;
    }

    /**
     * Fügt dem Piraten Schaden zu - reduziert also sein Leben um 1.
     */
    public void damage() {
        if (life > 0) {
            life--;
        }
    }

    /**
     * @return Ob der Pirat kein Leben mehr hat.
     */
    public boolean isDead() {
        return life <= 0;
    }
}
