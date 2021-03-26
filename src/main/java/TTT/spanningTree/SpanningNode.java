package TTT.spanningTree;

import TTT.TTTNode;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;

class SpanningNode<I> {
    HashMap<I, SpanningNode<I>> children;
    final TTTNode<I> state;

    SpanningNode(TTTNode<I> state, HashMap<I, SpanningNode<I>> children) {
        this.children = children;
        this.state = state;
    }

    SpanningNode(TTTNode<I> state) {
        this.children = new HashMap<>();
        this.state = state;
    }

    boolean addState(TTTNode<I> node) {
        if (node.sequenceAccess == this.state.sequenceAccess)
            return false;
        Word<I> prefix = node.sequenceAccess.prefix(this.state.sequenceAccess.size() + 1);
        I transitionSymbol = prefix.lastSymbol();
        if (!children.containsKey(transitionSymbol)) {
            if (prefix.size() != node.sequenceAccess.size())
                return false;
            children.put(transitionSymbol, new SpanningNode<>(node));
            return true;
        } else
            return children.get(transitionSymbol).addState(node);
    }

    @Nullable TTTNode<I> findState(int stateId) {
        if (state.id == stateId)
            return state;
        else {
            for (I transition : children.keySet()) {
                SpanningNode<I> child = children.get(transition);
                @Nullable TTTNode<I> result = child.findState(stateId);
                if (result != null)
                    return result;
            }
        }
        return null;
    }
}
