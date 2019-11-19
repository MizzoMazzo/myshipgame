package model;

import java.util.ArrayList;
import java.util.List;

public class AmmunitionStorage extends Storage<Ammunition> {
    private final List<Ammunition> content = new ArrayList<>(ammunitionCapacity);

    public AmmunitionStorage(boolean fill) {
        if (fill) {
            for (int i = 0; i < 5; i++) {
                content.add(new Ammunition());
            }
        }
    }

    @Override
    public void add(Ammunition ammunition) {
        if (getCapacity() > 0) {
            content.add(ammunition);
        }
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public int getCapacity() {
        return ammunitionCapacity - content.size();
    }

    @Override
    public int getSize() {
        return content.size();
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    /**
     * Entfernt an der ersten Stelle eine Munition.
     */
    public void remove() {
        if (!content.isEmpty()) {
            content.remove(0);
        }
    }
}
