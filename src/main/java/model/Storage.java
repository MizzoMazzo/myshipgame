package model;

public abstract class Storage<T> {
    protected int treasureCapacity = 3;
    protected int ammunitionCapacity = 5;

    /**
     * Hängt das gegebene {@code object} ans Ende der Liste an.
     *
     * @param object An die Liste anzuhängendes Objekt.
     */
    public abstract void add(T object);

    /**
     * Leert den Storage.
     */
    public abstract void clear();

    /**
     * @return Anzahl der Objekte, die noch in den Storage aufgenommen werden können.
     */
    public abstract int getCapacity();

    /**
     * @return Anzahl der Objekte, die im Storage liegen.
     */
    public abstract int getSize();

    /**
     * @return Ob der Storage leer ist, also keine Objekte beinhaltet.
     */
    public abstract boolean isEmpty();
}
