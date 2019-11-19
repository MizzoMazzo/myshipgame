package util;

import model.Tile.Direction;
import model.Tile;
import org.msgpack.core.annotations.VisibleForTesting;

import java.util.*;

/**
 * Eine Block ist ein horizontaler zusammenhängender Abschnitt der Map:
 * Beispiel:
 * ┌───╴ ╶┬┬┬┬──┬┬┴
 * ^   ^ ^        ^
 * Blöcke werden im Mapgenerator und bei der Validierung der Karte dazu verwendet,
 * zusammenhängende Teile der Map zu erkennen. Dadurch kann bestimmt werden, in welchem
 * Bereich vertikale Verbindungen existieren müssen, damit alle Teile ereichbar sind.
 *
 * Beispiel:
 * ╶─┬┬┬─┬┬─╴┌╴┌╴╷
 * ╶─┘└┴─┴┴─┬┴─┘╷│
 * ╶┬──┬┬─╴╶┼┬┬─┘│
 * ╶┴──┴┴──╴└┼┴┬┬┘
 * ^       ^
 * Dieser Block ist nicht erreichbar, da er nach oben getrennt ist, und auch keine Verbindung
 * nach unten hat.
 */

/**
 * Zum Finden verbundener Blöcke dient die {@link Block#merge(List)}-Funktion. Blöcke, die über gemeinsame Nachbarn
 * oberhalb bzw. daneben verbunden sind werden zu einem neunen Block zusammengefügt.
 *
 * Beispiel:
 * ┌─┬───┬─┬───┬──┬┬─┬─┬─┬──┬┬─┬┬─────┬─┬───╴╶─┐┌─┬╴╷
 * └─┴───┼─┴┬──┴┬─┴┴─┘╶┴┬┴──┴┴─┴┴─────┴─┴─┬────┘└─┴┬┘
 * ^ (1)             ^^    (2)                 ^^(3)^
 * Blöcke (1), (2) sind über den oberen Block verbunden. Block (3) ist isoliert
 *
 * Nach Merge:
 * ┌─┬───┬─┬───┬──┬┬─┬─┬─┬──┬┬─┬┬─────┬─┬───╴╶─┐┌─┬╴╷
 * └─┴───┼─┴┬──┴┬─┴┴─┘╶┴┬┴──┴┴─┴┴─────┴─┴─┬────┘└─┴┬┘
 * ^ (1)                                       ^^(2)^
 * (1), (2) sind nun die maixmal zusammenhängenden Blöcke.
 *
 * Srry für die schlechte Benennung :/
 * */
public class Block {

    private final int start;
    private final int end;
    private final int row;
    private Set<Block> connectedBlocks;

    /**
     * Erzeugt einen neuen Block
     * @param start X-Koordinate des Startes
     * @param end X-Koordinate des Endes
     * @param row Y-Koordinate des Blocks
     */
    public Block(int start, int end, int row) {
        assert start <= end;
        this.start = start;
        this.end = end;
        this.row = row;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getRow() {
        return row;
    }

    public Set<Block> getConnectedBlocks() {
        return connectedBlocks;
    }

    public int length() {
        return end - start + 1;
    }

    /**
     * Überprüft, ob der Block mit einem Block darüber Verbunden hat
     * @param block der andere Block
     * @param connections die X-Koordinaten der vertikalen Verbindungen
     */
    @VisibleForTesting
    public boolean hasVerticalConnection(Block block, Set<Integer> connections) {
        return block.row + 1 == row
            && end >= block.start
            && start <= block.end
            && connections.stream().anyMatch(conn -> Math.max(start, block.start) <= conn && conn <= Math.min(end, block.end));
    }

    /**
     * Bestimmt alle Blöcke der letzten Reihe, zu denen der Block eine Verbindung hat
     * @param prevRow die letzte Generation / obere Reihe
     * @param connections die Verbindungen zu letzten Generation, S. {@link Block#hasVerticalConnection(Block, Set)}
     */
    public void addVerticallyConnectedBlocks(List<Block> prevRow, Set<Integer> connections) {
        connectedBlocks = new HashSet<>();
        for (Block block : prevRow) {
            if (hasVerticalConnection(block, connections)) {
                connectedBlocks.add(block);
            }
        }
    }

    /**
     * Verschmiltzt alle Blöcke, die zusammenhängend sind (dh. die mit mindesten
     * einem gemeinsamen Block in der Zeile obendrüber verbunden ist.
     * @apiNote die Connections müssen schon mi {@link Block#hasVerticalConnection(Block, Set)} hinzugefügt worden sein!
     * @param blocks die Blöcke der Zeile
     * @return ein 2-Tupel mit dem größten zusammenhängenden Block und den übrigen Blöcken der Zeile.
     */
    public Pair<Block, List<Block>> merge(List<Block> blocks) {
        List<Block> remaing = new ArrayList<>(blocks);
        Set<Block> connectedBlocks = getConnectedBlocks();
        int newEnd = getEnd();
        for (Block block : blocks) {
            if (equals(block) || nonemptySection(connectedBlocks, block.getConnectedBlocks())) {
                newEnd = block.end;
                connectedBlocks.addAll(block.getConnectedBlocks());
                remaing.remove(block);
            } else {
                break;
            }
        }
        Block mergedBlock = new Block(getStart(), newEnd, getRow());
        return new Pair<>(mergedBlock, remaing);
    }

    /**
     * Fügt die horizontalen Verbindungen des Blocks zu eimem Tile-Array hinzu.
     * @param map das Tile-Array
     */
    public void drawInMap(Tile[][] map) {
        if (start == end) {
            return;
        }
        map[row][start].addDirection(Direction.EAST);
        for (int i = start + 1; i <= end - 1; i++) {
            map[row][i].addDirection(Direction.EAST);
            map[row][i].addDirection(Direction.WEST);
        }
        map[row][end].addDirection(Direction.WEST);
    }

    /**
     * Prüft ob zwei Elemente gemeinsame Elemente haben
     *
     * @param fst Collection 1
     * @param snd Collection 2
     * @param <T> Element-Typ
     * @return ob die Collections gemeinsame Elemente haben
     */
    private static <T> boolean nonemptySection(Collection<T> fst, Collection<T> snd) {
        return fst.stream().anyMatch(snd::contains);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Block block = (Block) o;
        return getStart() == block.getStart()
            && getEnd() == block.getEnd()
            && getRow() == block.getRow();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), getRow());
    }

    @Override
    public String toString() {
        return String.format("Block[x: %d - %d, y: %d]", start, end, row);
    }
}
