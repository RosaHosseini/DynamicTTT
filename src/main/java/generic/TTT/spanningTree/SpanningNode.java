package generic.TTT.spanningTree;

import generic.TTT.TTTNode;
import generic.TTT.discriminationTree.DiscriminationNode;
import generic.TTT.discriminationTree.EmptyDTLeaf;
import moore.TTT.discriminiationTree.MooreDiscriminatorNode;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class SpanningNode<I, O> {
    HashMap<I, SpanningNode<I, O>> children;
    final TTTNode<I, O> state;

    SpanningNode(TTTNode<I, O> state, HashMap<I, SpanningNode<I, O>> children) {
        this.children = children;
        this.state = state;
    }

    SpanningNode(TTTNode<I, O> state) {
        this.children = new HashMap<>();
        this.state = state;
    }

    boolean addState(TTTNode<I, O> node) {
        if (node.sequenceAccess == this.state.sequenceAccess)
            return false;
        try {
            Word<I> prefix = node.sequenceAccess.prefix(this.state.sequenceAccess.size() + 1);
            I transitionSymbol = prefix.lastSymbol();
            if (!children.containsKey(transitionSymbol)) {
                if (prefix.size() != node.sequenceAccess.size())
                    return false;
                children.put(transitionSymbol, new SpanningNode<>(node));
                return true;
            } else
                return children.get(transitionSymbol).addState(node);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    @Nullable TTTNode<I, O> findState(int stateId) {
        if (state.id == stateId)
            return state;
        else {
            for (I transition : children.keySet()) {
                SpanningNode<I, O> child = children.get(transition);
                @Nullable TTTNode<I, O> result = child.findState(stateId);
                if (result != null)
                    return result;
            }
        }
        return null;
    }

    public void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(state.id);
        buffer.append('\n');
        Iterator<Map.Entry<I, SpanningNode<I, O>>> it = children.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<I, SpanningNode<I, O>> next = it.next();
            SpanningNode<I, O> node = next.getValue();
            if (it.hasNext()) {
                node.print(buffer, childrenPrefix + "├──── " + next.getKey() + " ──── ", childrenPrefix + "│   ");
            } else {
                node.print(buffer, childrenPrefix + "└──── " + next.getKey() + " ──── ", childrenPrefix + "    ");
            }
        }
    }
}
