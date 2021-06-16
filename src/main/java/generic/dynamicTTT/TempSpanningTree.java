package generic.dynamicTTT;

import generic.TTT.TTTNode;
import net.automatalib.words.Word;

import java.util.*;

public class TempSpanningTree<I, O> {

    protected final List<TTTNode<I, O>> nodesList = new ArrayList<>();
    protected final List<TTTNode<I, O>> removeList = new ArrayList<>();

    public void add(TTTNode<I, O> node) {
        nodesList.add(node);
    }

    public void remove(TTTNode<I, O> node) {
        removeList.add(node);
    }

    public TTTNode<I, O> getState(Integer id) {
        for (TTTNode<I, O> node : nodesList) {
            if (node.id == id) {
                if (!removeListContain(node.sequenceAccess))
                    return node;
                else
                    return null;
            }
        }
        return null;
    }


    public TTTNode<I, O> getState(Word<I> accessSequence) {
        for (TTTNode<I, O> node : nodesList) {
            if (node.sequenceAccess.equals(accessSequence)) {
                if (!removeListContain(node.sequenceAccess))
                    return node;
                else
                    return null;
            }
        }
        return null;
    }

    public boolean contains(Word<I> prefix) {
        return getState(prefix) != null;
    }

    private boolean removeListContain(Word<I> prefix) {
        for (TTTNode<I, O> node : removeList) {
            if (node.sequenceAccess.equals(prefix))
                return true;
        }
        return false;
    }

    public Iterator<TTTNode<I, O>> getIterator() {
        return new Iterator<TTTNode<I, O>>() {
            int pointer = -1;

            @Override
            public boolean hasNext() {
                int newPointer = pointer + 1;
                if (newPointer >= nodesList.size())
                    return false;
                for (int i = 0; i < nodesList.size() - newPointer; i++) {
                    if (!removeList.contains(nodesList.get(newPointer + i)))
                        return true;
                }
                return false;
            }

            @Override
            public TTTNode<I, O> next() {
                while (pointer < nodesList.size() - 1) {
                    pointer += 1;
                    TTTNode<I, O> currNode = nodesList.get(pointer);
                    if (!removeList.contains(currNode)) {
                        return currNode;
                    }
                }
                return null;
            }
        };
    }

    public void sort() {
        nodesList.sort(Comparator.comparingInt(o -> o.sequenceAccess.size()));
    }

}
