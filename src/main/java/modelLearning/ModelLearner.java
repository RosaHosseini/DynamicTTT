package modelLearning;

import net.automatalib.automata.fsa.DFA;

public abstract class ModelLearner<I> {
    protected final Teacher<I> teacher;

    public ModelLearner(Teacher<I> teacher) {
        this.teacher = teacher;
    }

    abstract public DFA<?, I> learn();
}
