package TTT;

import net.automatalib.words.Word;

public class TTTNode<I> {

    public final int id;
    public final Word<I> sequenceAccess;
    public final boolean isAccepting;


    public TTTNode(int id, Word<I> sequenceAccess, boolean isAccepting) {
        this.id = id;
        this.sequenceAccess = sequenceAccess;
        this.isAccepting = isAccepting;
    }
}
