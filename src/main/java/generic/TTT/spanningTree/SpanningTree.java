package generic.TTT.spanningTree;

import generic.TTT.TTTNode;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SpanningTree<I, O> {
    SpanningNode<I, O> root;

    public SpanningTree(TTTNode<I, O> initial_node) {
        this.root = new SpanningNode<>(initial_node);
    }

    public boolean addState(TTTNode<I, O> node) {
        return this.root.addState(node);
    }

    public @Nullable TTTNode<I, O> getState(int stateId) {
        return this.root.findState(stateId);
    }

    /**
     * breadth first search on spanning Tree
     */
    public List<TTTNode<I, O>> getAllStates() {
        List<TTTNode<I, O>> listOfNodes = new ArrayList<>();
        Queue<SpanningNode<I, O>> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            SpanningNode<I, O> currentNode = queue.remove();
            listOfNodes.add(currentNode.state);
            for (I trans : currentNode.children.keySet()) {
                SpanningNode<I, O> child = currentNode.children.get(trans);
                queue.add(child);
            }
        }
        return listOfNodes;
    }

    public void draw(){
        StringBuilder builder = new StringBuilder();
        root.print(builder, "", "");
        System.out.println(builder.toString());
    }
}


