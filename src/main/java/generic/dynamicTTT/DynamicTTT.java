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
import java.util.stream.Collectors;

public abstract class DynamicTTT<I, O, A extends MutableDeterministic<Integer, I, Integer, O, Void>>
        extends ModelLearner<I, O, A> implements MembershipCounter<I, O> {

    protected final DiscriminationTreeInterface<I, O> outdatedDiscriminationTree;
    protected final TempSpanningTree<I, O> tempSpanningTree = new TempSpanningTree<>();
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
        this.outdatedPrefixesContainer = new OutdatedSpanningTreeContainer<>(outdatedSpanningTree, this.alphabet, tempSpanningTree::contains);
        this.visualize = visulaize;
    }


    @Override
    public A learn() {
        try {
            reconstructHypothesis();
            cleanDiscriminationTree();
            completeHypothesis();
            cleanDiscriminationTree();
            confirmHypothesis();
            if (visualize)
                Visualization.visualize(hypothesis, this.alphabet, new DefaultVisualizationHelper<>());

            TTT<I, O, A> tttLearner = initialTTT();
            tttLearner.stabilizeHypothesis();
            tttLearner.finalizeHypothesis();

            while (true) {
                @Nullable Word<I> ce = teacher.equivalenceQuery(tttLearner.getHypothesis(), alphabet);
                eqCounter++;

                if (ce == null) {
                    return tttLearner.getHypothesis();
                }
                System.out.println("counter example dynamic:" + ce.toString());
                if (visualize)
                    System.out.println(ce);
                tttLearner.refineHypothesis(ce);
                tttLearner.stabilizeHypothesis();
                tttLearner.finalizeHypothesis(); //todo fix this

                if (visualize)
                    Visualization.visualize(hypothesis, this.alphabet, new DefaultVisualizationHelper<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
            discriminationTree.draw();
            spanningTree.draw();
            return null;
        }

    }

    protected void confirmHypothesis() {
        List<Integer> states = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        Word<I> sequence = new WordBuilder<I>().toWord();
        Deque<Word<I>> queue = new ArrayDeque<>();
        queue.addLast(sequence);
        while (queue.size() > 0) {
            states.clear();
            spanningTree.getAllStates().forEach(it -> states.add(it.id));

            sequence = queue.pollFirst();
            @Nullable Integer id = hypothesis.getState(sequence);
            if (sequence.toString().equals("LISTEN SYN(V,V,0) FIN+ACK(V,V,0) ACCEPT CLOSECONNECTION FIN+ACK(V,V,0)")) {
                boolean debug = true;
            }

            if (seen.contains(id))
                continue;
            if (id != null && states.contains(id)) {
                DTLeaf<I, O> discriminationTreePose = discriminationTree.sift(sequence);
                if (discriminationTreePose instanceof EmptyDTLeaf) {
                    TTTNode<I, O> node = spanningTree.getState(id);
                    discriminationTree.discriminate(node, discriminationTreePose);
                } else if (discriminationTreePose.state.id != id) {
                    Word<I> prefix = sequence.prefix(sequence.size() - 1);
                    Integer prefixId = hypothesis.getState(prefix);
                    I transition = sequence.lastSymbol();
                    id = discriminationTreePose.state.id;
                    hypothesis.setTransition(prefixId, transition, id);
                }
            }
            if (id == null || !states.contains(id)) {
                Word<I> prefix = sequence.prefix(sequence.size() - 1);
                Integer prefixId = hypothesis.getState(prefix);
                I transition = sequence.lastSymbol();
                DTLeaf<I, O> pose = discriminationTree.sift(sequence);
                TTTNode<I, O> prefixNode = spanningTree.getState(prefixId);

                Word<I> newSequence = prefixNode.sequenceAccess.append(transition);
                if (pose instanceof EmptyDTLeaf) {
                    TTTNode<I, O> node = findState(newSequence);
                    if (node == null) {
                        node = createState(newSequence, false);
                        boolean result = spanningTree.addState(node);
                    }
                    id = node.id;
                    discriminationTree.discriminate(node, pose);
                    hypothesis.setTransition(prefixId, transition, node.id);
                } else {
                    if (!states.contains(id)) {
                        TTTNode<I, O> node = findState(pose.state.sequenceAccess);
                        if (node == null) {
                            if (hypothesis.getState(pose.state.sequenceAccess) == null)
                                pose.state.id = hypothesis.addState(teacher.membershipQuery(newSequence));
                            pose.state.sequenceAccess = newSequence;
                            boolean result = spanningTree.addState(pose.state);
                        } else {
                            pose.state = node;
                        }
                    }
                    id = pose.state.id;
                    hypothesis.setTransition(prefixId, transition, id);
                }

            }


            if (id == 16 || id == 40) {
                boolean debug = true;
            }
            if (seen.contains(id))
                continue;
            seen.add(id);
            for (I a : alphabet) {
                queue.addLast(sequence.append(a));
            }
        }

    }

    private TTTNode<I, O> findState(Word<I> sequence) {
        for (TTTNode<I, O> node : spanningTree.getAllStates()) {
            if (node.sequenceAccess == sequence)
                return node;
        }
        return null;
    }


    protected abstract TTT<I, O, A> initialTTT();

    abstract protected DynamicDiscriminationTree<I, O> initialDynamicDiscriminationTree();


    public void reconstructHypothesis() throws Exception {
        discriminationTree.initialDiscriminationTree(outdatedDiscriminationTree);

        Iterator<Word<I>> prefixIterator = outdatedPrefixesContainer.getOutdatedPrefixes();
        while (prefixIterator.hasNext()) {

            Word<I> prefix = prefixIterator.next();
            if (tempSpanningTree.contains(prefix))
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
        tempSpanningTree.sort();

        out:
        for (Iterator<TTTNode<I, O>> it = tempSpanningTree.getIterator(); it.hasNext(); ) {
            TTTNode<I, O> node = it.next();
            boolean false_sequence = false;
            Deque<Pair<TTTNode<I, O>, TTTNode<I, O>>> queue = new ArrayDeque<>();
            if (node.sequenceAccess.size() == 0) { //initial node
                spanningTree = new SpanningTree<>(node);
                continue;
            }
            Pair<TTTNode<I, O>, TTTNode<I, O>> pair = Pair.of(node, null);
            while (true) {
                if (pair == null) {
                    // it has other prefix
                    false_sequence = true;
                    break;
                }
                queue.addFirst(pair);
                TTTNode<I, O> currNode = pair.getFirst();
                Word<I> sequence = currNode.sequenceAccess;
                Word<I> prefix = currNode.sequenceAccess.prefix(sequence.size() - 1);
                if (tempSpanningTree.contains(prefix)) {
                    break;
                }
                try {
                    pair = discriminateLongestPrefix(currNode);
                } catch (Exception e) {
                    for (Pair<TTTNode<I, O>, TTTNode<I, O>> p : queue) {
                        tempSpanningTree.remove(p.getFirst());
                        discriminationTree.removeNode(p.getFirst());
                        removeFromEquivalenceStateMap(currNode);
                    }
                    continue out;
                }
            }

            if (false_sequence) {
                for (Pair<TTTNode<I, O>, TTTNode<I, O>> p : queue) {
                    TTTNode<I, O> uaNode = p.getFirst();
                    Word<I> ua = uaNode.sequenceAccess;
                    I a = ua.lastSymbol();
                    Word<I> u = ua.prefix(ua.size() - 1);
                    TTTNode<I, O> vNode = equivalenceStateMap.get(u);
                    Word<I> va = vNode.sequenceAccess.append(a);
                    uaNode.sequenceAccess = va;
                    hypothesis.setTransition(vNode.id, a, uaNode.id);
                    addToEquivalenceStateMap(va, uaNode);
                }
            }

            for (Pair<TTTNode<I, O>, TTTNode<I, O>> p : queue) {
                Word<I> sequence = p.getFirst().sequenceAccess;
                Word<I> prefix = sequence.prefix(sequence.size() - 1);
                int origin_id = hypothesis.getState(prefix);
                hypothesis.setTransition(origin_id, sequence.lastSymbol(), p.getFirst().id);
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
        if (vaNode.id == uaNode.id) {
            return null;
        }
        // find discriminator between u and v
        Word<I> discriminator = discriminationTree.findDiscriminator(va, ua);
        Word<I> newDiscriminator = new WordBuilder<I>().append(a).append(discriminator).toWord();

        // add new node to hypothesis
        TTTNode<I, O> uNode = new TTTNode<>(-1, u, teacher.membershipQuery(u));

        // new node to discrimination tree
        boolean result = discriminationTree.discriminate(newDiscriminator, uNode, vNode);
        if (!result)
            throw new Exception("could not discriminate  u = " + uNode.sequenceAccess +
                    " and v =" + vNode.sequenceAccess + " ua = " + uaNode.sequenceAccess);

        // add new node to hypothesis
        uNode.id = createState(u, false).id;
        tempSpanningTree.add(uNode);
        addToEquivalenceStateMap(u, uNode);
        addToEquivalenceStateMap(ua, uaNode);
        hypothesis.addTransition(uNode.id, a, uaNode.id);
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
                if (hasTransition(hypothesis, origin_id, transition)) //todo why
                    hypothesis.addTransition(origin_id, transition, state_id);
            } else {
                hypothesis.setTransition(origin_id, transition, state_id);
            }
        } else { //if initial state
            state_id = hypothesis.addInitialState(output);
        }
        TTTNode<I, O> node = new TTTNode<>(state_id, sequenceAccess, output);
        addToEquivalenceStateMap(node.sequenceAccess, node);
        return node;
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
                TTTNode<I, O> state = createState(newSequence, false);
                discriminationTree.discriminate(state, newPose);
                tempSpanningTree.add(node);
                addToEquivalenceStateMap(newSequence, state);
                newPose = discriminationTree.findLeaf(newSequence);
            }
            destStateId = newPose.state.id;
            hypothesis.setTransition(node.id, symbol, destStateId);
            addToEquivalenceStateMap(newSequence, newPose.state);

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
                throw new Exception("sequence " + sequence + " is not valid in discrimination Tree!");
            } else {
                if (leaf.state.id == destId) {
                    continue;
                }
                Word<I> prefix = sequence.prefix(sequence.size() - 1);
                I transition = sequence.lastSymbol();
                Integer originState = hypothesis.getState(prefix);
                hypothesis.setTransition(originState, transition, leaf.state.id);
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
                } catch (Exception ignored) {
                }
            }
        }
        return sequences;
    }

    private void addToEquivalenceStateMap(Word<I> sequence, TTTNode<I, O> node) {
        equivalenceStateMap.put(sequence, node);
    }

    private void removeFromEquivalenceStateMap(TTTNode<I, O> node) {
        ArrayList<Word<I>> removeList = new ArrayList<>();
        for (Word<I> key : equivalenceStateMap.keySet()) {
            TTTNode<I, O> value = equivalenceStateMap.get(key);
            if (value.id == node.id) {
                DTLeaf<I, O> newPose = discriminationTree.sift(key);
                if (newPose.state != null) {
                    addToEquivalenceStateMap(key, newPose.state);
                    try {
                        Word<I> sequence = newPose.state.sequenceAccess;
                        I a = sequence.lastSymbol();
                        Word<I> prefix = sequence.prefix(sequence.size() - 1);
                        Integer prefix_id = hypothesis.getState(prefix);
                        Integer destId = hypothesis.getTransition(prefix_id, a);
                        if (destId == null || destId == node.id) {
                            hypothesis.setTransition(prefix_id, a, newPose.state.id);
                        }
                    } catch (Exception ignored) {
                    }
                } else removeList.add(key);
            }
        }
        for (Word<I> key : removeList) {
            equivalenceStateMap.remove(key);
            try {
                I a = key.lastSymbol();
                Word<I> prefix = key.prefix(key.size() - 1);
                Integer prefix_id = hypothesis.getState(prefix);
                Integer destId = hypothesis.getTransition(prefix_id, a);
                if (destId == null || destId == node.id) {
                    hypothesis.removeAllTransitions(prefix_id, a);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public long getEQCounter() {
        return eqCounter;
    }
}