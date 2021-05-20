package dfa.dynamicTTT;

import dfa.TTT.DFATTT;
import dfa.dynamicTTT.discriminationTree.DFADynamicDiscriminationTree;
import dfa.modelLearning.DFATeacher;
import generic.TTT.TTT;
import generic.TTT.discriminationTree.DiscriminationTreeInterface;
import generic.TTT.discriminatorTrie.DiscriminatorTrie;
import generic.TTT.spanningTree.SpanningTree;
import generic.dynamicTTT.DynamicTTT;
import generic.dynamicTTT.discriminationTree.DynamicDiscriminationTree;
import generic.modelLearning.Teacher;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class DFADynamicTTT<I> extends DynamicTTT<I, Boolean, MutableDFA<Integer, I>> {

    public DFADynamicTTT(
            Teacher<I, Boolean, MutableDFA<Integer, I>> teacher,
            SpanningTree<I, Boolean> outdatedSpanningTree,
            DiscriminationTreeInterface<I, Boolean> outdatedDiscriminationTree,
            Alphabet<I> updatedAlphabet,
            MutableDFA<Integer, I> hypothesis,
            boolean visulaize
    ) {
        super(teacher, outdatedSpanningTree, outdatedDiscriminationTree, updatedAlphabet, hypothesis, visulaize);
        this.discriminationTree = initialDynamicDiscriminationTree();
    }

    @Override
    protected TTT<I, Boolean, MutableDFA<Integer, I>> initialTTT() {
        return new DFATTT<>(
                (DFATeacher<I>) teacher,
                this.alphabet,
                this.hypothesis,
                this.spanningTree,
                this.discriminationTree,
                new DiscriminatorTrie<>(this.alphabet)
        );
    }

    @Override
    protected DynamicDiscriminationTree<I, Boolean> initialDynamicDiscriminationTree() {
        return new DFADynamicDiscriminationTree<>(this.teacher, alphabet);
    }


    @Override
    public Boolean membershipQuery(Word<I> inputString) {
        return hypothesis.computeOutput(inputString);
    }

}