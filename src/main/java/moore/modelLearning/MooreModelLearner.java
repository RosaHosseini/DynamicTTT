package moore.modelLearning;

import generic.modelLearning.ModelLearner;
import net.automatalib.automata.transducers.MooreMachine;

public abstract class MooreModelLearner<I, O> extends ModelLearner<I, O, MooreMachine<?, I, ?, O>> {
    public MooreModelLearner(MooreTeacher<I, O> teacher) {
        super(teacher);
    }
}
