package model;

public class TreasureStorage extends Storage<Treasure> {
    private final Treasure[] content = new Treasure[treasureCapacity];

    @Override
    public void add(Treasure treasure) {
        for (int i = 0; i < treasureCapacity; i++) {
            if (content[i] == null) {
                content[i] = treasure;
                break;
            }
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < treasureCapacity; i++) {
            content[i] = null;
        }
    }

    @Override
    public int getCapacity() {
        return treasureCapacity - getSize();
    }

    @Override
    public int getSize() {
        int counter = 0;
        for (int i = 0; i < treasureCapacity; i++) {
            if (content[i] != null) {
                counter++;
            }
        }
        return counter;
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

    /**
     * Entfernt den Schatz mit entsprechender {@code treasureId}.
     *
     * @param treasureID
     */
    public void remove(int treasureID) {
        content[treasureID] = null;
    }

    public Treasure getTreasure(int treasureID) {
        return content[treasureID];
    }

    public boolean hasTreasureAtIndex(int index) {
        if (index < 0 || index > treasureCapacity - 1) {
            return false;
        }
        return content[index] != null;
    }

    /**
     * @return Kumulierter Wert aller Schätze.
     */
    public int getTotalValue() {
        int treasureValue = 0;
        for (int i = 0; i < treasureCapacity; i++) {
            if (content[i] != null) {
                treasureValue += content[i].getValue();
            }
        }
        return treasureValue;
    }
}
