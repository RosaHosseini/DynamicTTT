package dfa.dynamicTTT.spanningTree;

import net.automatalib.words.Word;

public interface SpanningTreeContainer<I> {
    boolean contain(Word<I> prefix);
}
