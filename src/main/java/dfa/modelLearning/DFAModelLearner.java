package dfa.modelLearning;

import generic.modelLearning.ModelLearner;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;

public abstract class DFAModelLearner<I> extends ModelLearner<I, Boolean, MutableDFA<Integer,I>>{
    public DFAModelLearner(DFATeacher<I> teacher) {
        super(teacher);
    }

    @Override
    public MutableDFA<Integer, I> learn() {
        return null;
    }
}
