package util;

import java.util.Objects;

/**
 * Straight-forward Implementierung eines 2-Tuple
 *
 * @param <S> Typ erste Komponente
 * @param <T> Typ zweite Komponente
 */
public class Pair<S, T> {

    private final S fst;
    private final T snd;

    public Pair(S fst, T snd) {
        this.fst = fst;
        this.snd = snd;
    }

    public S getFst() {
        return fst;
    }

    public T getSnd() {
        return snd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return getFst().equals(pair.getFst()) &&
            getSnd().equals(pair.getSnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFst(), getSnd());
    }

    @Override
    public String toString() {
        return "Pair{" +
            "1=" + fst +
            ", 2=" + snd +
            '}';
    }
}
