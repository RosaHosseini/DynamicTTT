package generic.TTT;

import net.automatalib.words.Word;

public class TTTNode<I, O> {

    public final int id;
    public final Word<I> sequenceAccess;
    public final O output;


    public TTTNode(int id, Word<I> sequenceAccess, O output) {
        this.id = id;
        this.sequenceAccess = sequenceAccess;
        this.output = output;
    }
}
