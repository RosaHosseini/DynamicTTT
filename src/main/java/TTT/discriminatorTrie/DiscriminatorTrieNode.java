package TTT.discriminatorTrie;

import net.automatalib.words.Word;

import java.util.HashMap;

class DiscriminatorTrieNode<I> {
    HashMap<I, DiscriminatorTrieNode<I>> children;
    final Word<I> content;


    DiscriminatorTrieNode(Word<I> content) {
        children = new HashMap<>();
        this.content = content;
    }
}
