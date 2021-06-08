package generic.TTT;

import generic.TTT.discriminationTree.*;
import generic.TTT.discriminatorTrie.DiscriminatorTrie;
import generic.TTT.spanningTree.SpanningTree;
import generic.modelLearning.MembershipCounter;
import generic.modelLearning.ModelLearner;
import generic.modelLearning.Teacher;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.commons.util.Triple;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;


public abstract class TTT<I, O, A extends MutableDeterministic<Integer, I, Integer, O, Void>>
        extends ModelLearner<I, O, A> implements MembershipCounter<I, O> {
    private final Alphabet<I> alphabet;
    private final A hypothesis;
    private SpanningTree<I, O> spanningTree;
    private DiscriminationTreeInterface<I, O> discriminationTree;
    private DiscriminatorTrie<I, O> discriminatorTrie;
    private Deque<Word<I>> tempDiscriminators = new ArrayDeque<>();
    private long eqCounter = 0L;


    public TTT(Teacher<I, O, A> teacher, Alphabet<I> initialAlphabet, A hypothesis) {
        super(teacher);
        this.alphabet = initialAlphabet;
        this.hypothesis = hypothesis;
    }

    public TTT(Teacher<I, O, A> teacher,
               Alphabet<I> initialAlphabet,
               A hypothesis,
               SpanningTree<I, O> spanningTree,
               DiscriminationTreeInterface<I, O> discriminationTree,
               DiscriminatorTrie<I, O> discriminatorTrie) {
        super(teacher);
        this.alphabet = initialAlphabet;
        this.hypothesis = hypothesis;
        this.spanningTree = spanningTree;
        this.discriminationTree = discriminationTree;
        this.discriminatorTrie = discriminatorTrie;
    }

    @Override
    public A learn() throws Exception {
        initializeHypothesis();
        while (true) {

            @Nullable Word<I> ce = teacher.equivalenceQuery(hypothesis, alphabet);
            eqCounter++;
            if (ce == null) {
//                    finalizeHypothesis();
                return this.hypothesis;
            }
            System.out.println("counter example " + ce);
            refineHypothesis(ce);
            stabilizeHypothesis();
//            finalizeHypothesis(); //todo fix this

        }
    }

    public void initializeHypothesis() {
        Word<I> initial_sequence = new WordBuilder<I>().toWord();
        TTTNode<I, O> initial_node = createState(initial_sequence);

        this.spanningTree = new SpanningTree<>(initial_node);
        this.discriminationTree = initializeDiscriminationTree();
        this.discriminationTree.discriminate(initial_node);
        this.discriminatorTrie = new DiscriminatorTrie<>(this.alphabet);

        expandStateTransitions(initial_node);
    }

    public void refineHypothesis(Word<I> ce) throws Exception {

        // decompose counter example -------------------------
        // Triple<Word<I>, I, Word<I>> decomposition = decomposeCounterExample(eq);
        Triple<Word<I>, I, Word<I>> decomposition = null;
        Word<I> u;
        I a = null;
        Word<I> v = null;
        Word<I> access_u = null;
        Word<I> access_ua;
        TTTNode<I, O> access_u_node;
        TTTNode<I, O> access_ua_node = null;

        for (int i = 0; i < ce.size(); i++) {
            u = ce.prefix(i);
            a = ce.getSymbol(i);
            v = ce.suffix(ce.size() - i - 1);

            access_u_node = spanningTree.getState(hypothesis.getState(u));
            assert access_u_node != null;
            access_u = access_u_node.sequenceAccess;

            access_ua_node = spanningTree.getState(hypothesis.getState(u.append(a)));
            assert access_ua_node != null;
            access_ua = access_ua_node.sequenceAccess;

            O output_ua_v = teacher.membershipQuery(access_ua.concat(v));
            O output_u_a_v = teacher.membershipQuery(access_u.append(a).concat(v));

            if (!output_u_a_v.equals(output_ua_v)) {
                decomposition = Triple.of(u, a, v);
                break;
            }
        }

        if (decomposition == null) {
            throw new Exception("can not find appropriate decomposition in counter example of hypothesis refinement");
        }
        //---------------------------------------------------

        TTTNode<I, O> oldNode = access_ua_node;
        Word<I> newStateSequence = access_u.append(a);
        TTTNode<I, O> newNode = createState(newStateSequence);
        boolean result = spanningTree.addState(newNode);
        if (!result)
            return;

        result = discriminationTree.discriminate(v, newNode, oldNode);
        if (result)
            tempDiscriminators.addFirst(v);
        else
            throw new Exception("can not place new discriminator " + v.toString() + ", in  the discrimination Tree");
        expandStateTransitions(newNode);
        updateAllTransitionsEndTo(oldNode.id);
    }

    public void stabilizeHypothesis() throws Exception {
        while (true) {
            List<TTTNode<I, O>> tttNodes = spanningTree.getAllStates();

            boolean isStable = true;
            for (TTTNode<I, O> tttNode : tttNodes) {
                @Nullable Word<I> ce = checkStabilization(tttNode);
                if (ce != null) {
                    refineHypothesis(ce);
                    isStable = false;
                    break;
                }
            }
            if (isStable)
                return;
        }
    }

    public void finalizeHypothesis() {
//        Deque<Word<I>> tempList = new ArrayDeque<>();
//
//        outer:
//        while (!tempDiscriminators.isEmpty()) {
//            Word<I> discriminator = tempDiscriminators.removeFirst();
//            DiscriminatorNode<I> tempDiscriminatorNode = discriminationTree.findDiscriminatorNode(discriminator);
//            if (tempDiscriminatorNode.isFinal()) {
//                continue;
//            }
//
//            List<Word<I>> reservedSuffixes = discriminatorTrie.findAllCandidateDiscriminators();
//            for (Word<I> suffix : reservedSuffixes) {
//                //todo i'm not sure about MQ counter maybe you should change it to teacher
//                boolean isFinalDiscriminator = tempDiscriminatorNode.makeFinal(suffix, this);
//                if (isFinalDiscriminator) {
//                    discriminatorTrie.insert(suffix);
//                    continue outer;
//                }
//            }
//            tempList.addFirst(discriminator);
//        }
//        while (!tempList.isEmpty()) {
//            Word<I> tempDiscriminator = tempList.removeFirst();
//            tempDiscriminators.addFirst(tempDiscriminator);
//        }
//        assert tempDiscriminators.size() == 0;
        List<DiscriminatorNode<I, O>> tempDiscriminationNodes = discriminationTree.findAllTemporaryDiscriminators();
        int count = 0;

        outer:
        while (true) {
            if (count > tempDiscriminationNodes.size())
                return;
            if (tempDiscriminationNodes.size() == 0) // all discriminators are final
                return;
            List<Word<I>> reservedSuffixes = discriminatorTrie.findAllCandidateDiscriminators();

            for (DiscriminatorNode<I, O> tempDiscriminator : tempDiscriminationNodes) {
                Word<I> suffix = tempDiscriminator.getDiscriminator();
                if (reservedSuffixes.contains(suffix)) {
                    tempDiscriminator.makeFinal(suffix);
                    discriminatorTrie.insert(suffix);
                    tempDiscriminationNodes.remove(tempDiscriminator);
                    continue outer;
                }
            }

            for (Word<I> suffix : reservedSuffixes) {
                for (DiscriminatorNode<I, O> tempDiscriminator : tempDiscriminationNodes) {
                    //todo i'm not sure about MQ counter maybe you should change it to teacher
                    boolean isFinalDiscriminator = tempDiscriminator.makeFinal(suffix, this);
                    if (isFinalDiscriminator) {
                        discriminatorTrie.insert(suffix);
                        tempDiscriminationNodes.remove(tempDiscriminator);
                        continue outer;
                    }
                }
            }
            count += 1;
        }
    }

    protected abstract DiscriminationTreeInterface<I, O> initializeDiscriminationTree();


    /***
     * define a new state in our hypothesis and add it  and spanning Tree and discrimination Tree
     * @param sequenceAccess the sequence access of a state
     */
    private void addState(Word<I> sequenceAccess, @Nullable EmptyDTLeaf<I, O> pos) {
        TTTNode<I, O> node = createState(sequenceAccess);
        addNodeToTTT(node, pos);
        expandStateTransitions(node);
    }

    /***
     * define a new state in our hypothesis and create a TTTNode from it
     * @param sequenceAccess the sequence access of a state
     * @return a TTTNode of the given state
     */
    private TTTNode<I, O> createState(Word<I> sequenceAccess) {
        O output = this.teacher.membershipQuery(sequenceAccess);
        int state_id;
        if (sequenceAccess.size() > 0) {
            state_id = hypothesis.addState(output);
            int origin_id = hypothesis.getState(sequenceAccess.prefix(sequenceAccess.size() - 1));
            I transition = sequenceAccess.lastSymbol();
            hypothesis.removeAllTransitions(origin_id, transition);
            hypothesis.addTransition(origin_id, transition, state_id);
        } else {
            state_id = hypothesis.addInitialState(output);
        }
        return new TTTNode<>(state_id, sequenceAccess, output);
    }


    /***
     * add a given state in our spanning Tree and discrimination Tree
     * @param node the TTTNode of a state
     */
    private void addNodeToTTT(TTTNode<I, O> node, @Nullable EmptyDTLeaf<I, O> pos) {
        this.spanningTree.addState(node);

        if (pos == null) {
            this.discriminationTree.discriminate(node);
        } else
            this.discriminationTree.discriminate(node, pos);
    }

    /***
     * for a given state update all transitions
     * @param node the TTTNode of a state
     */
    private void expandStateTransitions(TTTNode<I, O> node) {
        for (I symbol : alphabet) {
            Word<I> newSequence = node.sequenceAccess.append(symbol);
            DTLeaf<I, O> newPose = discriminationTree.sift(newSequence);

            int destStateId;
            if (newPose instanceof EmptyDTLeaf) {
                addState(newSequence, (EmptyDTLeaf<I, O>) newPose);
            } else {
                destStateId = newPose.state.id;
                hypothesis.removeAllTransitions(node.id, symbol);
                hypothesis.addTransition(node.id, symbol, destStateId);
            }
        }
    }

    /***
     * for a given state checks that the discriminationTree is consistent with the hypothesis or not
     * @param tttNode the TTTNode of a state
     * @return counter example if dfa.TTT is not consistent otherwise null
     */
    @Nullable
    public Word<I> checkStabilization(TTTNode<I, O> tttNode) throws Exception {
        DTLeaf<I, O> DTNode = getDiscriminationTree().findLeaf(tttNode.sequenceAccess);
        assert !(DTNode instanceof EmptyDTLeaf);

        DiscriminatorNode<I, O> parent = DTNode.parent;
        DiscriminationNode<I, O> child = DTNode;

        while (true) {
            if (parent == null) //root node
                return null;
            Word<I> query = tttNode.sequenceAccess.concat(parent.getDiscriminator());
            O hypothesisOutput = membershipQuery(query);

            O TTTOutput = this.getDiscriminationTree().findAccessorToFather(child);

            if (!hypothesisOutput.equals(TTTOutput))
                return query;

            child = parent;
            parent = parent.parent;
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
                int originState = hypothesis.getState(prefix);
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
                    sequences.add(spanningTree.getState(state).sequenceAccess.append(symbol));
                }
            }
        }
        return sequences;
    }


    /***
     * @param inputString the input word of query
     * @return the output of hypothesis by given input
     */
    @Override
    public abstract O membershipQuery(Word<I> inputString);

    public A getHypothesis() {
        return hypothesis;
    }

    public SpanningTree<I, O> getSpanningTree() {
        return spanningTree;
    }

    public DiscriminationTreeInterface<I, O> getDiscriminationTree() {
        return discriminationTree;
    }

    public long getEQCounter() {
        return eqCounter;
    }

    private void visualize() {
        Visualization.visualize(hypothesis, alphabet, new DefaultVisualizationHelper<>());
    }
}