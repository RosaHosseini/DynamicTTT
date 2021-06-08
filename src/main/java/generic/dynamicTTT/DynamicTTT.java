package generic.dynamicTTT;

import generic.TTT.TTT;
import generic.TTT.TTTNode;
import generic.TTT.discriminationTree.DTLeaf;
import generic.TTT.discriminationTree.DiscriminationTreeInterface;
import generic.TTT.discriminationTree.EmptyDTLeaf;
import generic.TTT.spanningTree.SpanningTree;
import generic.dynamicTTT.discriminationTree.DynamicDiscriminationTree;
import generic.dynamicTTT.spanningTree.OutdatedSpanningTreeContainer;
import generic.modelLearning.MembershipCounter;
import generic.modelLearning.ModelLearner;
import generic.modelLearning.Teacher;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.commons.util.Pair;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public abstract class DynamicTTT<I, O, A extends MutableDeterministic<Integer, I, Integer, O, Void>>
        extends ModelLearner<I, O, A> implements MembershipCounter<I, O> {

    protected final DiscriminationTreeInterface<I, O> outdatedDiscriminationTree;
    protected final List<TTTNode<I, O>> tempSpanningTree = new ArrayList<>();
    protected final HashMap<Word<I>, TTTNode<I, O>> equivalenceStateMap = new HashMap<>();
    protected final Alphabet<I> alphabet;
    protected final A hypothesis;
    protected DynamicDiscriminationTree<I, O> discriminationTree;
    protected SpanningTree<I, O> spanningTree;
    protected final OutdatedSpanningTreeContainer<I, O> outdatedPrefixesContainer;
    protected long eqCounter = 0L;
    protected final boolean visualize;


    public DynamicTTT(Teacher<I, O, A> teacher,
                      SpanningTree<I, O> outdatedSpanningTree,
                      DiscriminationTreeInterface<I, O> outdatedDiscriminationTree,
                      Alphabet<I> updatedAlphabet,
                      A hypothesis,
                      boolean visulaize) {
        super(teacher);
        this.alphabet = updatedAlphabet;
        this.hypothesis = hypothesis;
        this.outdatedDiscriminationTree = outdatedDiscriminationTree;
//        this.discriminationTree = initialDynamicDiscriminationTree();
        this.outdatedPrefixesContainer = new OutdatedSpanningTreeContainer<>(outdatedSpanningTree, this.alphabet, this::tempSpanningTreeContain);
        this.visualize = visulaize;
    }


    @Override
    public A learn() {
        try {
            reconstructHypothesis();
            completeHypothesis();
            cleanDiscriminationTree();
            if (visualize)
                Visualization.visualize(hypothesis, this.alphabet, new DefaultVisualizationHelper<>());

            TTT<I, O, A> tttLearner = initialTTT();
            tttLearner.finalizeHypothesis();

            while (true) {
                @Nullable Word<I> ce = teacher.equivalenceQuery(tttLearner.getHypothesis(), alphabet);
                eqCounter++;

                if (ce == null) {
//                    tttLearner.finalizeHypothesis();
                    return tttLearner.getHypothesis();
                }
                System.out.println("counter example dynamic:" + ce.toString());
                if (visualize)
                    System.out.println(ce);
                tttLearner.refineHypothesis(ce);
                tttLearner.stabilizeHypothesis();
//                tttLearner.finalizeHypothesis(); //todo fix this

                if (visualize)
                    Visualization.visualize(hypothesis, this.alphabet, new DefaultVisualizationHelper<>());

            }
        } catch (
                Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    protected abstract TTT<I, O, A> initialTTT();

    abstract protected DynamicDiscriminationTree<I, O> initialDynamicDiscriminationTree();


    public void reconstructHypothesis() throws Exception {
        discriminationTree.initialDiscriminationTree(outdatedDiscriminationTree);

        Iterator<Word<I>> prefixIterator = outdatedPrefixesContainer.getOutdatedPrefixes();
        while (prefixIterator.hasNext()) {

            Word<I> prefix = prefixIterator.next();
            if (tempSpanningTreeContain(prefix))
                continue;
            DTLeaf<I, O> pos = this.discriminationTree.sift(prefix);
            if (pos instanceof EmptyDTLeaf) { // add a new state!
                TTTNode<I, O> node = createState(prefix, true);
                addToEquivalenceStateMap(node.sequenceAccess, node);
                tempSpanningTree.add(node);
                this.discriminationTree.discriminate(node, pos);
                outdatedPrefixesContainer.expandPrefix(node.sequenceAccess);
            } else { //update transitions
                addToEquivalenceStateMap(prefix, pos.state);
                int destStateId = pos.state.id;
                Word<I> prefixPrefix = prefix.prefix(prefix.size() - 1);
                int originId = equivalenceStateMap.get(prefixPrefix).id;
                I transition = prefix.lastSymbol();
                if (hasTransition(this.hypothesis, originId, transition))
                    hypothesis.addTransition(originId, transition, destStateId);
            }
        }
    }

    public void completeHypothesis() throws Exception {
        tempSpanningTree.sort(Comparator.comparingInt(o -> o.sequenceAccess.size()));

        for (TTTNode<I, O> node : tempSpanningTree) {
            Deque<Pair<TTTNode<I, O>, TTTNode<I, O>>> queue = new ArrayDeque<>();
            if (node.sequenceAccess.size() == 0) { //initial node
                spanningTree = new SpanningTree<>(node);
                continue;
            }
            Pair<TTTNode<I, O>, TTTNode<I, O>> pair = Pair.of(node, null);
            while (true) {
                queue.addFirst(pair);
                TTTNode<I, O> currNode = pair.getFirst();
                Word<I> sequence = currNode.sequenceAccess;
                Word<I> prefix = currNode.sequenceAccess.prefix(sequence.size() - 1);
                if (tempSpanningTreeContain(prefix))
                    break;
                pair = discriminateLongestPrefix(currNode);
            }

            for (Pair<TTTNode<I, O>, TTTNode<I, O>> p : queue) {
                boolean result = spanningTree.addState(p.getFirst());
                expandStateTransitions(p.getFirst());
                if (p.getSecond() != null)
                    updateAllTransitionsEndTo(p.getSecond().id);
                assert result;
            }
        }
    }


    public void cleanDiscriminationTree() {
        this.discriminationTree.removeRedundantDiscriminators();
    }


    /***
     * Get a node and discriminate it from its longest prefix by finding a new discriminator
     *  and place it in the discrimination Tree
     * @param uaNode is a tttNode which we want to discriminate it form its longest prefix
     * @return the tttNode belong to the longest prefix of ua
     */
    private Pair<TTTNode<I, O>, TTTNode<I, O>> discriminateLongestPrefix(TTTNode<I, O> uaNode) throws Exception {
        Word<I> ua = uaNode.sequenceAccess;
        Word<I> u = ua.prefix(ua.size() - 1);
        I a = ua.lastSymbol();
        TTTNode<I, O> vNode = equivalenceStateMap.get(u);
        Word<I> v = vNode.sequenceAccess;

        TTTNode<I, O> vaNode = equivalenceStateMap.get(v.append(a));
        Word<I> va = vaNode.sequenceAccess;

        // find discriminator between u and v
        Word<I> discriminator = discriminationTree.findDiscriminator(va, ua);
        Word<I> newDiscriminator = new WordBuilder<I>().append(a).append(discriminator).toWord();

        // add new node to hypothesis
        TTTNode<I, O> uNode = createState(u, false);
        addToEquivalenceStateMap(u, uNode);
        hypothesis.addTransition(uNode.id, a, uaNode.id);

        // new node to discrimination tree
        boolean result = discriminationTree.discriminate(newDiscriminator, uNode, vNode);
        if (!result)
            throw new Exception("could not discriminate longest prefix for u = " + uNode.sequenceAccess +
                    "v =" + vNode.sequenceAccess + "ua= " + uaNode.sequenceAccess);
        return Pair.of(uNode, vNode);
    }

    /***
     * Check if a dfa has a specific transition from a origin
     * @param model an `A` model
     * @param originId the id of origin state
     * @param transition the symbol of transition
     * @return true if origin state has transition otherwise false
     */
    private boolean hasTransition(A model, int originId, I transition) {
        return model.getTransition(originId, transition) == null;
    }


    /***
     * define a new state in our hypothesis and create a TTTNode from it
     * @param sequenceAccess the sequence access of a state
     * @return a TTTNode of the given state
     */
    private TTTNode<I, O> createState(Word<I> sequenceAccess, Boolean isTemp) {
        O output = this.teacher.membershipQuery(sequenceAccess);
        int state_id;
        if (sequenceAccess.size() > 0) { //if not initial state (check transition of prefix)
            state_id = hypothesis.addState(output);
            Integer origin_id = hypothesis.getState(sequenceAccess.prefix(sequenceAccess.size() - 1));
            I transition = sequenceAccess.lastSymbol();
            if (isTemp) {
                if (hasTransition(hypothesis, origin_id, transition))
                    hypothesis.addTransition(origin_id, transition, state_id);
            } else {
                hypothesis.removeAllTransitions(origin_id, transition);
                hypothesis.addTransition(origin_id, transition, state_id);
            }
        } else { //if initial state
            state_id = hypothesis.addInitialState(output);
        }
        return new TTTNode<>(state_id, sequenceAccess, output);
    }


    /***
     * for a given state update all transitions
     * @param node the TTTNode of a state
     */
    private void expandStateTransitions(TTTNode<I, O> node) throws Exception {
        for (I symbol : alphabet) {

            Word<I> newSequence = node.sequenceAccess.append(symbol);
            DTLeaf<I, O> newPose = discriminationTree.sift(newSequence);

            int destStateId;
            if (newPose instanceof EmptyDTLeaf) {
                throw new Exception("");
            } else {
                destStateId = newPose.state.id;
                hypothesis.removeAllTransitions(node.id, symbol);
                hypothesis.addTransition(node.id, symbol, destStateId);
                addToEquivalenceStateMap(newSequence, newPose.state);
            }
        }
    }

    /**
     * Update all transitions in hypothesis which end to a specific state
     *
     * @param destId the state id of destination of transitions
     * @throws Exception if the transition ends to a state which is not available in hypothesis
     */
    private void updateAllTransitionsEndTo(int destId) throws Exception {
        Collection<Word<I>> sequences = getAllSequenceAccesses(destId);

        for (Word<I> sequence : sequences) {
            DTLeaf<I, O> leaf = discriminationTree.sift(sequence);
            if (leaf instanceof EmptyDTLeaf) {
                throw new Exception("sequence" + sequence + "is not valid in discrimination Tree!");
            } else {
                if (leaf.state.id == destId) {
                    continue;
                }
                Word<I> prefix = sequence.prefix(sequence.size() - 1);
                I transition = sequence.lastSymbol();
                Integer originState = hypothesis.getState(prefix);
                hypothesis.removeAllTransitions(originState, transition);
                hypothesis.addTransition(originState, transition, leaf.state.id);
            }
        }
    }


    /***
     * @param stateId the id of a state
     * @return all sequence accesses in hypothesis that end to state with id of 'stateId'
     */
    private Collection<Word<I>> getAllSequenceAccesses(int stateId) {
        Set<Word<I>> sequences = new HashSet<>();
        Collection<Integer> states = hypothesis.getStates();
        for (int state : states) {
            for (I symbol : alphabet) {
                try { //todo remove this try!
                    if (hypothesis.getSuccessor(state, symbol) == stateId) {
                        @Nullable TTTNode<I, O> spanningNode = spanningTree.getState(state);
                        if (spanningNode != null)
                            sequences.add(spanningNode.sequenceAccess.append(symbol));
                    }
                }catch (Exception ignored){
                }
            }
        }
        return sequences;
    }


    private void addToEquivalenceStateMap(Word<I> sequence, TTTNode<I, O> node) {
        equivalenceStateMap.put(sequence, node);
    }


    public boolean tempSpanningTreeContain(Word<I> prefix) {
        for (TTTNode<I, O> node : tempSpanningTree) {
            if (node.sequenceAccess.equals(prefix))
                return true;
        }
        return false;
    }


    public long getEQCounter() {
        return eqCounter;
    }

}