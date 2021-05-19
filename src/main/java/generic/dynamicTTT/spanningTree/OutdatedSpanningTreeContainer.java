package generic.dynamicTTT.spanningTree;

import generic.TTT.TTTNode;
import generic.TTT.spanningTree.SpanningTree;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.*;

public class OutdatedSpanningTreeContainer<I, O> {
    private final Map<Integer, List<Word<I>>> outdatedPrefixes;
    private final Alphabet<I> alphabet;
    private final SpanningTreeContainer<I> tempSpanningTreeContainer;

    public OutdatedSpanningTreeContainer(SpanningTree<I, O> spanningTree,
                                         Alphabet<I> alphabet,
                                         SpanningTreeContainer<I> tempSpanningTreeContainer) {
        this.outdatedPrefixes = new HashMap<>();
        this.alphabet = alphabet;
        this.tempSpanningTreeContainer = tempSpanningTreeContainer;
        List<TTTNode<I, O>> spanningNodes = spanningTree.getAllStates();
        for (TTTNode<I, O> tttNode : spanningNodes) {
            Word<I> prefix = tttNode.sequenceAccess;
            int size = prefix.size();

            if (size == 0) { //epsilon
                List<Word<I>> prefix0 = new ArrayList<>();
                prefix0.add(prefix);
                outdatedPrefixes.put(size, prefix0);
            }
            if (this.alphabet.containsAll(prefix.asList())) {
                if (!outdatedPrefixes.containsKey(size + 1))
                    outdatedPrefixes.put(size + 1, new ArrayList<>());
                List<Word<I>> nextPrefixList = outdatedPrefixes.get(size + 1);
                for (I symbol : this.alphabet)
                    nextPrefixList.add(prefix.append(symbol));
                outdatedPrefixes.put(size + 1, nextPrefixList);
            }
        }
    }

    public Iterator<Word<I>> getOutdatedPrefixes() {
        return new Iterator<Word<I>>() {

            @Override
            public boolean hasNext() {
                return outdatedPrefixes.keySet().size() > 0;
            }

            @Override
            public Word<I> next() {
                Set<Integer> sizes = outdatedPrefixes.keySet();
                if (sizes.size() == 0)
                    return null;

                int currentSize = Collections.min(sizes);

                Word<I> nextPrefix = null;

                List<Word<I>> currentSizePrefixes = outdatedPrefixes.get(currentSize);
                for (Word<I> prefix : currentSizePrefixes) {
                    nextPrefix = prefix;
                    if (currentSize == 0)
                        break;

                    Word<I> father = prefix.prefix(prefix.size() - 1);
                    if (tempSpanningTreeContainer.contain(father))
                        break;
                }

                currentSizePrefixes.remove(nextPrefix);
                if (currentSizePrefixes.size() == 0) {
                    outdatedPrefixes.remove(currentSize);
                } else {
                    outdatedPrefixes.put(currentSize, currentSizePrefixes);
                }
                return nextPrefix;
            }
        };
    }

    private boolean containPrefix(Word<I> prefix) {
        int size = prefix.size();
        if (outdatedPrefixes.containsKey(size)) {
            List<Word<I>> sizePrefixes = outdatedPrefixes.get(size);
            for (Word<I> currPrefix : sizePrefixes) {
                if (currPrefix.equals(prefix))
                    return true;
            }
        }
        return false;
    }

    public void expandPrefix(Word<I> prefix) {
        int size = prefix.size();
        if (!outdatedPrefixes.containsKey(size + 1))
            outdatedPrefixes.put(size + 1, new ArrayList<>());
        List<Word<I>> nextPrefixList = outdatedPrefixes.get(size + 1);
        for (I symbol : this.alphabet) {
            Word<I> child = prefix.append(symbol);
            if (!containPrefix(child))
                nextPrefixList.add(child);
        }
        outdatedPrefixes.put(size + 1, nextPrefixList);
    }
}
