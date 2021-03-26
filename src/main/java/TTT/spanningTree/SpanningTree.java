package TTT.spanningTree;

import TTT.TTTNode;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SpanningTree<I> {
    SpanningNode<I> root;

    public SpanningTree(TTTNode<I> initial_node) {
        this.root = new SpanningNode<>(initial_node);
    }

    public boolean addState(TTTNode<I> node) {
        return this.root.addState(node);
    }

    public @Nullable TTTNode<I> getState(int stateId) {
        return this.root.findState(stateId);
    }

    /**
     * breadth first search on spanning Tree
     */
    public List<TTTNode<I>> getAllStates() {
        List<TTTNode<I>> listOfNodes = new ArrayList<>();
        Queue<SpanningNode<I>> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            SpanningNode<I> currentNode = queue.remove();
            listOfNodes.add(currentNode.state);
            for (I trans : currentNode.children.keySet()) {
                SpanningNode<I> child = currentNode.children.get(trans);
                queue.add(child);
            }
        }
        return listOfNodes;
    }
}


