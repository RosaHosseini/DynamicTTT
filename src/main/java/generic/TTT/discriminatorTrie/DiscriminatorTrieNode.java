package generic.TTT.discriminatorTrie;

import net.automatalib.words.Word;

import java.util.HashMap;

class DiscriminatorTrieNode<I, O> {
    HashMap<I, DiscriminatorTrieNode<I, O>> children;
    final Word<I> content;


    DiscriminatorTrieNode(Word<I> content) {
        children = new HashMap<>();
        this.content = content;
    }
}
