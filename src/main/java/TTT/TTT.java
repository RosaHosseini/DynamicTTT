package TTT;

import TTT.discriminiationTree.*;
import de.learnlib.api.query.DefaultQuery;
import modelLearning.MembershipCounter;
import modelLearning.ModelLearner;
import modelLearning.Teacher;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.Triple;
import net.automatalib.words.Alphabet;
import TTT.spanningTree.SpanningTree;
import TTT.discriminatorTrie.DiscriminatorTrie;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class TTT<I> extends ModelLearner<I> implements MembershipCounter<I> {
    private final Alphabet<I> alphabet;
    private final CompactDFA<I> hypothesis;
    private SpanningTree<I> spanningTree;
    private DiscriminationTree<I> discriminationTree;
    private DiscriminatorTrie<I> discriminatorTrie;

    public TTT(Teacher<I> teacher, Alphabet<I> initialAlphabet) {
        super(teacher);
        this.alphabet = initialAlphabet;
        this.hypothesis = new CompactDFA<>(alphabet);
    }

    public TTT(Teacher<I> teacher,
               Alphabet<I> initialAlphabet,
               CompactDFA<I> hypothesis,
               SpanningTree<I> spanningTree,
               DiscriminationTree<I> discriminationTree,
               DiscriminatorTrie<I> discriminatorTrie) {
        super(teacher);
        this.alphabet = initialAlphabet;
        this.hypothesis = hypothesis;
        this.spanningTree = spanningTree;
        this.discriminationTree = discriminationTree;
        this.discriminatorTrie = discriminatorTrie;
    }

    @Override
    public DFA<?, I> learn() {
        initializeHypothesis();
        while (true) {
            try {
                @Nullable DefaultQuery<I, Boolean> eq = teacher.equivalenceQuery(hypothesis, alphabet);
                if (eq == null)
                    return this.hypothesis;
                refineHypothesis(eq);
                stabilizeHypothesis();
                finalizeHypothesis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initializeHypothesis() {
        Word<I> initial_sequence = new WordBuilder<I>().toWord();
        TTTNode<I> initial_node = createState(initial_sequence);

        this.spanningTree = new SpanningTree<>(initial_node);
        this.discriminationTree = new DiscriminationTree<>(this.teacher);
        this.discriminationTree.discriminate(new WordBuilder<I>().toWord(), initial_node);
        this.discriminatorTrie = new DiscriminatorTrie<>(this.alphabet);

        expandStateTransitions(initial_node);
    }

    public void refineHypothesis(DefaultQuery<I, Boolean> eq) throws Exception {

        // decompose counter example -------------------------
        // Triple<Word<I>, I, Word<I>> decomposition = decomposeCounterExample(eq);
        Triple<Word<I>, I, Word<I>> decomposition = null;
        Word<I> u = null;
        I a = null;
        Word<I> v = null;
        Word<I> access_u = null;

        Word<I> ce = eq.getInput();
        for (int i = 0; i < ce.size(); i++) {
            u = ce.prefix(i);
            a = ce.getSymbol(i);
            v = ce.suffix(ce.size() - i - 1);

            TTTNode<I> access_u_node = spanningTree.getState(hypothesis.getState(u));
            assert access_u_node != null;
            access_u = access_u_node.sequenceAccess;

            TTTNode<I> access_ua_node = spanningTree.getState(hypothesis.getState(u.append(a)));
            assert access_ua_node != null;
            Word<I> access_ua = access_ua_node.sequenceAccess;

            boolean output_ua_v = teacher.membershipQuery(access_ua.concat(v));
            boolean output_u_a_v = teacher.membershipQuery(access_u.append(a).concat(v));

            if (output_u_a_v != output_ua_v) {
                decomposition = Triple.of(u, a, v);
                break;
            }
        }

        if (decomposition == null)
            throw new Exception("can not find appropriate decomposition in counter example of hypothesis refinement");
        //---------------------------------------------------

        Word<I> newStateSequence = access_u.append(a);
        TTTNode<I> newNode = createState(newStateSequence);
        spanningTree.addState(newNode);
        discriminationTree.discriminate(v, newNode);
        expandStateTransitions(newNode);
    }

    public void stabilizeHypothesis() throws Exception {
        while (true) {
            List<TTTNode<I>> tttNodes = spanningTree.getAllStates();

            boolean isStable = true;
            for (TTTNode<I> tttNode : tttNodes) {
                @Nullable DefaultQuery<I, Boolean> ce = checkStabilization(tttNode);
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
        List<DiscriminatorNode<I>> tempDiscriminationNodes = discriminationTree.findAllTemporaryDiscriminators();

        outer:
        while (true) {
            if (tempDiscriminationNodes.size() == 0) // all discriminators are final
                return;
            List<Word<I>> reservedSuffixes = discriminatorTrie.findAllCandidateDiscriminators();
            for (Word<I> suffix : reservedSuffixes) {
                for (DiscriminatorNode<I> tempDiscriminator : tempDiscriminationNodes) {
                    //todo i'm not sure about MQ counter maybe you should change it to teacher
                    boolean isFinalDiscriminator = tempDiscriminator.makeFinal(suffix, this);
                    if (isFinalDiscriminator) {
                        discriminatorTrie.insert(suffix);
                        tempDiscriminationNodes.remove(tempDiscriminator);
                        continue outer;
                    }
                }
            }
        }
    }

    /***
     * define a new state in our hypothesis and add it  and spanning Tree and discrimination Tree
     * @param sequenceAccess the sequence access of a state
     */
    private void addState(Word<I> sequenceAccess, @Nullable EmptyDTLeaf<I> pos) {
        TTTNode<I> node = createState(sequenceAccess);
        addNodeToTTT(node, pos);
        expandStateTransitions(node);
    }

    /***
     * define a new state in our hypothesis and create a TTTNode from it
     * @param sequenceAccess the sequence access of a state
     * @return a TTTNode of the given state
     */
    private TTTNode<I> createState(Word<I> sequenceAccess) {
        boolean accepting = this.teacher.membershipQuery(sequenceAccess);
        int state_id;
        if (sequenceAccess.size() > 0) {
            state_id = hypothesis.addState(accepting);
            int origin_id = hypothesis.getState(sequenceAccess.prefix(sequenceAccess.size() - 1));
            I transition = sequenceAccess.lastSymbol();
            hypothesis.removeAllTransitions(origin_id, transition);
            hypothesis.addTransition(origin_id, transition, state_id);
        } else {
            state_id = hypothesis.addInitialState(accepting);
        }
        return new TTTNode<>(state_id, sequenceAccess, accepting);
    }


    /***
     * add a given state in our spanning Tree and discrimination Tree
     * @param node the TTTNode of a state
     */
    private void addNodeToTTT(TTTNode<I> node, @Nullable EmptyDTLeaf<I> pos) {
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
    private void expandStateTransitions(TTTNode<I> node) {
        for (I symbol : alphabet) {
            Word<I> newSequence = node.sequenceAccess.append(symbol);
            DTLeaf<I> newPose = discriminationTree.sift(newSequence);

            int destStateId;
            if (newPose instanceof EmptyDTLeaf) {
                addState(newSequence, (EmptyDTLeaf<I>) newPose);
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
     * @return counter example if TTT is not consistent otherwise null
     */
    private @Nullable DefaultQuery<I, Boolean> checkStabilization(TTTNode<I> tttNode) throws Exception {
        DTLeaf<I> DTNode = discriminationTree.find(tttNode.sequenceAccess);
        assert !(DTNode instanceof EmptyDTLeaf);

        DiscriminatorNode<I> parent = DTNode.parent;
        DiscriminationNode<I> child = DTNode;

        while (true) {
            if (parent == null) //root node
                return null;
            Word<I> query = tttNode.sequenceAccess.concat(parent.getDiscriminator());
            boolean hypothesisOutput = hypothesis.computeOutput(query);

            boolean TTTOutput = false;
            if (parent.isDashChild(child))
                TTTOutput = true;

            if (hypothesisOutput != TTTOutput)
                return new DefaultQuery<>(query);

            child = parent;
            parent = parent.parent;
        }
    }

    /***
     * @param inputString the input word of query
     * @return the output of hypothesis by given input
     */
    @Override
    public boolean membershipQuery(Word<I> inputString) {
        return hypothesis.computeOutput(inputString);
    }

    public CompactDFA<I> getHypothesis(){
        return hypothesis;
    }

    public SpanningTree<I> getSpanningTree() {
        return spanningTree;
    }

    public DiscriminationTree<I> getDiscriminationTree() {
        return discriminationTree;
    }
}


