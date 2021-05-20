package moore.modelLearning;

import generic.modelLearning.ModelLearner;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.automata.transducers.MutableMooreMachine;

public abstract class MooreModelLearner<I, O> extends ModelLearner<I, O, MutableMooreMachine<Integer, I, Integer, O>> {
    public MooreModelLearner(MooreTeacher<I, O> teacher) {
        super(teacher);
    }
}
