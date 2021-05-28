package moore.dynamicTTT;

import generic.TTT.TTT;
import generic.TTT.discriminationTree.DiscriminationTreeInterface;
import generic.TTT.discriminatorTrie.DiscriminatorTrie;
import generic.TTT.spanningTree.SpanningTree;
import generic.dynamicTTT.DynamicTTT;
import generic.dynamicTTT.discriminationTree.DynamicDiscriminationTree;
import generic.modelLearning.Teacher;
import moore.TTT.MooreTTT;
import moore.dynamicTTT.discriminationTree.MooreDynamicDiscriminationTree;
import moore.modelLearning.MooreTeacher;
import net.automatalib.automata.transducers.MutableMooreMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class MooreDynamicTTT<I, O> extends DynamicTTT<I, O, MutableMooreMachine<Integer, I, Integer, O>> {

    private final Alphabet<O> outputAlphabet;

    public MooreDynamicTTT(
            Teacher<I, O, MutableMooreMachine<Integer,
                    I, Integer, O>> teacher,
            SpanningTree<I, O> outdatedSpanningTree,
            DiscriminationTreeInterface<I, O> outdatedDiscriminationTree,
            Alphabet<I> updatedAlphabet,
            MutableMooreMachine<Integer, I, Integer, O> hypothesis,
            Alphabet<O> outputAlphabet,
            boolean visulaize) {
        super(teacher, outdatedSpanningTree, outdatedDiscriminationTree, updatedAlphabet, hypothesis, visulaize);
        this.outputAlphabet = outputAlphabet;
        this.discriminationTree = initialDynamicDiscriminationTree();
    }

    @Override
    protected TTT<I, O, MutableMooreMachine<Integer, I, Integer, O>> initialTTT() {
        return new MooreTTT<>(
                (MooreTeacher<I, O>) teacher,
                this.alphabet,
                this.outputAlphabet,
                this.hypothesis,
                this.spanningTree,
                this.discriminationTree,
                new DiscriminatorTrie<>(this.alphabet)
        );
    }

    @Override
    protected DynamicDiscriminationTree<I, O> initialDynamicDiscriminationTree() {
        return new MooreDynamicDiscriminationTree<>(teacher, outputAlphabet, alphabet);
    }

    @Override
    public O membershipQuery(Word<I> inputString) {
        return hypothesis.computeOutput(inputString).lastSymbol();
    }
}