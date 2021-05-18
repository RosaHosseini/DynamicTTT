package moore.dynamicTTT;

import de.learnlib.api.query.DefaultQuery;
import moore.TTT.MooreTTT;
import generic.TTT.TTTNode;
import moore.TTT.discriminatorTrie.DiscriminatorTrie;
import moore.TTT.discriminiationTree.MooreDiscriminationTree;
import moore.TTT.discriminiationTree.EmptyDTLeaf;
import generic.TTT.spanningTree.SpanningTree;
import moore.dynamicTTT.discriminationTree.DynamicMealyDiscriminationTree;
import moore.dynamicTTT.spanningTree.OutdatedSpanningTreeContainer;
import moore.modelLearning.MooreModelLearner;
import moore.modelLearning.MooreTeacher;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class DynamicTTT<I> extends MooreModelLearner<I> implements MealyMembershipCounter<I> {

    private final MooreDiscriminationTree<I> outdatedDiscriminationTree;
    private final List<TTTNode<I>> tempSpanningTree = new ArrayList<>();
    private final HashMap<Word<I>, TTTNode<I>> equivalenceStateMap = new HashMap<>();
    private final Alphabet<I> alphabet;
    private final CompactDFA<I> hypothesis;
    private final DynamicMealyDiscriminationTree<I> discriminationTree;
    private SpanningTree<I> spanningTree;
    private final OutdatedSpanningTreeContainer<I> outdatedPrefixesContainer;
    private long eqCounter = 0L;
    private boolean visualize = false;


    public DynamicTTT(MooreTeacher<I> teacher,
                      SpanningTree<I> outdatedSpanningTree,
                      MooreDiscriminationTree<I> outdatedDiscriminationTree,
                      Alphabet<I> updatedAlphabet,
                      boolean visulaize) {
        super(teacher);
        this.alphabet = updatedAlphabet;
        this.hypothesis = new CompactDFA<>(this.alphabet);
        this.outdatedDiscriminationTree = outdatedDiscriminationTree;
        this.discriminationTree = new DynamicMealyDiscriminationTree<>(this.teacher, this.alphabet);
        this.outdatedPrefixesContainer = new OutdatedSpanningTreeContainer<>(outdatedSpanningTree, this.alphabet, this::tempSpanningTreeContain);
        this.visualize = visulaize;
    }

    @Override
    public DFA<?, I> learn() {
        try {
            reconstructHypothesis();
            completeHypothesis();
            cleanDiscriminationTree();
            if (visualize)
                Visualization.visualize(hypothesis, hypothesis.getInputAlphabet(), new DefaultVisualizationHelper<>());

            MooreTTT<I> tttLearner = new MooreTTT<>(
                    teacher,
                    this.alphabet,
                    this.hypothesis,
                    this.spanningTree,
                    this.discriminationTree,
                    new DiscriminatorTrie<>(this.alphabet)
            );
            tttLearner.finalizeHypothesis();
            while (true) {
                @Nullable DefaultQuery<I, Boolean> eq = teacher.equivalenceQuery(tttLearner.getHypothesis(), alphabet);
                eqCounter++;

                if (eq == null) {
                    tttLearner.finalizeHypothesis();
                    return tttLearner.getHypothesis();
                }
                if (visualize)
                    System.out.println(eq.toString());
                tttLearner.refineHypothesis(eq);

                tttLearner.stabilizeHypothesis();
                if (visualize)
                    Visualization.visualize(hypothesis, hypothesis.getInputAlphabet(), new DefaultVisualizationHelper<>());

            }
        } catch (
                Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void reconstructHypothesis() throws Exception {
        discriminationTree.initialDiscriminationTree(outdatedDiscriminationTree);

        Iterator<Word<I>> prefixIterator = outdatedPrefixesContainer.getOutdatedPrefixes();
        while (prefixIterator.hasNext()) {

            Word<I> prefix = prefixIterator.next();
            if (tempSpanningTreeContain(prefix))
                continue;
            DTLeaf<I> pos = this.discriminationTree.sift(prefix);
            if (pos instanceof EmptyDTLeaf) { // add a new state!
                TTTNode<I> node = createState(prefix, true);
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

        for (TTTNode<I> node : tempSpanningTree) {
            Deque<Pair<TTTNode<I>, TTTNode<I>>> queue = new ArrayDeque<>();
            if (node.sequenceAccess.size() == 0) { //initial node
                spanningTree = new SpanningTree<>(node);
                continue;
            }
            Pair<TTTNode<I>, TTTNode<I>> pair = Pair.of(node, null);
            while (true) {
                queue.addFirst(pair);
                TTTNode<I> currNode = pair.getFirst();
                Word<I> sequence = currNode.sequenceAccess;
                Word<I> prefix = currNode.sequenceAccess.prefix(sequence.size() - 1);
                if (tempSpanningTreeContain(prefix))
                    break;
                pair = discriminateLongestPrefix(currNode);
            }

            for (Pair<TTTNode<I>, TTTNode<I>> p : queue) {
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
    private Pair<TTTNode<I>, TTTNode<I>> discriminateLongestPrefix(TTTNode<I> uaNode) throws Exception {
        Word<I> ua = uaNode.sequenceAccess;
        Word<I> u = ua.prefix(ua.size() - 1);
        I a = ua.lastSymbol();
        TTTNode<I> vNode = equivalenceStateMap.get(u);
        Word<I> v = vNode.sequenceAccess;

        TTTNode<I> vaNode = equivalenceStateMap.get(v.append(a));
        Word<I> va = vaNode.sequenceAccess;

        // find discriminator between u and v
        Word<I> discriminator = discriminationTree.findDiscriminator(va, ua);
        Word<I> newDiscriminator = new WordBuilder<I>().append(a).append(discriminator).toWord();

        // add new node to hypothesis
        TTTNode<I> uNode = createState(u, false);
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
     * @param dfa the DFA
     * @param originId the id of origin state
     * @param transition the symbol of transition
     * @return true if origin state has transition otherwise false
     */
    private boolean hasTransition(CompactDFA<I> dfa, int originId, I transition) {
        return dfa.getTransition(originId, transition) == null;
    }


    /***
     * define a new state in our hypothesis and create a TTTNode from it
     * @param sequenceAccess the sequence access of a state
     * @return a TTTNode of the given state
     */
    private TTTNode<I> createState(Word<I> sequenceAccess, Boolean isTemp) {
        boolean accepting = this.teacher.membershipQuery(sequenceAccess);
        int state_id;
        if (sequenceAccess.size() > 0) { //if not initial state (check transition of prefix)
            state_id = hypothesis.addState(accepting);
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
            state_id = hypothesis.addInitialState(accepting);
        }
        return new TTTNode<>(state_id, sequenceAccess, accepting);
    }


    /***
     * for a given state update all transitions
     * @param node the TTTNode of a state
     */
    private void expandStateTransitions(TTTNode<I> node) throws Exception {
        for (I symbol : alphabet) {

            Word<I> newSequence = node.sequenceAccess.append(symbol);
            DTLeaf<I> newPose = discriminationTree.sift(newSequence);

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
            DTLeaf<I> leaf = discriminationTree.sift(sequence);
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
                if (hypothesis.getSuccessor(state, symbol) == stateId) {
                    TTTNode<I> spanningNode = spanningTree.getState(state);
                    if (spanningNode != null)
                        sequences.add(spanningNode.sequenceAccess.append(symbol));
                }
            }
        }
        return sequences;
    }


    private void addToEquivalenceStateMap(Word<I> sequence, TTTNode<I> node) {
        equivalenceStateMap.put(sequence, node);
    }


    public boolean tempSpanningTreeContain(Word<I> prefix) {
        for (TTTNode<I> node : tempSpanningTree) {
            if (node.sequenceAccess.equals(prefix))
                return true;
        }
        return false;
    }

    @Override
    public Word<O> membershipQuery(Word<I> inputString) {
        return hypothesis.computeOutput(inputString);
    }


    public long getEQCounter() {
        return eqCounter;
    }
}