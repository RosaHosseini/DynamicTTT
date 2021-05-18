package moore.TTT;

import generic.TTT.TTT;
import generic.TTT.discriminationTree.DiscriminationTree;
import generic.TTT.discriminatorTrie.DiscriminatorTrie;
import generic.TTT.spanningTree.SpanningTree;
import generic.modelLearning.MembershipCounter;
import moore.TTT.discriminiationTree.MooreDiscriminationTree;
import moore.modelLearning.MooreTeacher;
import net.automatalib.automata.transducers.MutableMooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class MooreTTT<I, O> extends TTT<I, O, MutableMooreMachine<Integer, I, Integer, O>> implements MembershipCounter<I, O> {
    private final Alphabet<O> outputAlphabet;

    public MooreTTT(MooreTeacher<I, O> teacher, Alphabet<I> initialAlphabet, Alphabet<O> outputAlphabet) {
        super(teacher, initialAlphabet, new CompactMoore<>(initialAlphabet));
        this.outputAlphabet = outputAlphabet;
    }

    public MooreTTT(MooreTeacher<I, O> teacher,
                    Alphabet<I> initialAlphabet,
                    Alphabet<O> outputAlphabet,
                    CompactMoore<I, O> hypothesis,
                    SpanningTree<I, O> spanningTree,
                    MooreDiscriminationTree<I, O> discriminationTree,
                    DiscriminatorTrie<I, O> discriminatorTrie) {
        super(teacher, initialAlphabet, hypothesis, spanningTree, discriminationTree, discriminatorTrie);
        this.outputAlphabet = outputAlphabet;

    }


    @Override
    protected DiscriminationTree<I, O> initializeDiscriminationTree() {
        return new MooreDiscriminationTree<>(this.teacher, outputAlphabet);
    }


    @Override
    public O membershipQuery(Word<I> inputString) {
        return getHypothesis().computeOutput(inputString).lastSymbol();
    }


}