package generic.TTT;

import net.automatalib.words.Word;

public class TTTNode<I, O> {

    public int id;
    public Word<I> sequenceAccess;
    public O output;


    public TTTNode(int id, Word<I> sequenceAccess, O output) {
        this.id = id;
        this.sequenceAccess = sequenceAccess;
        this.output = output;
    }
}
