package dfa.TTT;

import dfa.TTT.discriminiationTree.DFADiscriminationTree;
import dfa.modelLearning.DFATeacher;
import generic.TTT.TTT;
import generic.TTT.discriminationTree.DiscriminationTreeInterface;
import generic.TTT.discriminatorTrie.DiscriminatorTrie;
import generic.TTT.spanningTree.SpanningTree;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class DFATTT<I> extends TTT<I, Boolean, MutableDFA<Integer, I>> {
    @Override
    protected DiscriminationTreeInterface<I, Boolean> initializeDiscriminationTree() {
        return new DFADiscriminationTree<>(this.teacher);
    }

    @Override
    public Boolean membershipQuery(Word<I> inputString) {
        return this.getHypothesis().computeOutput(inputString);
    }


    public DFATTT(DFATeacher<I> teacher, Alphabet<I> initialAlphabet) {
        super(teacher, initialAlphabet, new CompactDFA<>(initialAlphabet));
    }

    public DFATTT(DFATeacher<I> teacher,
                  Alphabet<I> initialAlphabet,
                  MutableDFA<Integer, I> hypothesis,
                  SpanningTree<I, Boolean> spanningTree,
                  DiscriminationTreeInterface<I, Boolean> discriminationTree,
                  DiscriminatorTrie<I, Boolean> discriminatorTrie
    ) {
        super(teacher, initialAlphabet, hypothesis, spanningTree, discriminationTree, discriminatorTrie);
    }

}