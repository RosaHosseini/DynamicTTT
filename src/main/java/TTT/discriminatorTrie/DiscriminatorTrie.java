package TTT.discriminatorTrie;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DiscriminatorTrie<I> {
    private final DiscriminatorTrieNode<I> root;
    private final Alphabet<I> alphabet;

    public DiscriminatorTrie(Alphabet<I> alphabet) {
        root = new DiscriminatorTrieNode<>(new WordBuilder<I>().toWord());
        this.alphabet = alphabet;
    }


    public void insert(Word<I> word) {
        DiscriminatorTrieNode<I> current = root;

        for (I l : word) {
            Word<I> currentContent = current.content;
            current = current.children.computeIfAbsent(l, c -> new DiscriminatorTrieNode<>(currentContent.append(l)));
        }
    }


    public List<Word<I>> findAllCandidateDiscriminators() {
        List<Word<I>> discriminators = findAllCandidateDiscriminators(root);
        discriminators.sort(Comparator.comparingInt(Word::length));
        return discriminators;
    }

    private List<Word<I>> findAllCandidateDiscriminators(DiscriminatorTrieNode<I> node) {
        List<Word<I>> result = new ArrayList<>();
        if (node.children.size() < alphabet.size())
            for (I symbol : alphabet) {
                if (node.children.containsKey(symbol)) {
                    DiscriminatorTrieNode<I> child = node.children.get(symbol);
                    result.addAll(findAllCandidateDiscriminators(child));
                }
                result.add(new WordBuilder<I>().append(symbol).append(node.content).toWord());
            }
        return result;
    }

}


