package moore.modelLearning;

import generic.modelLearning.ModelLearner;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Word;

public abstract class MooreModelLearner<I, O> extends ModelLearner<I, Word<O>, MooreMachine<?, I, ?, O>> {
    public MooreModelLearner(MooreTeacher<I, O> teacher) {
        super(teacher);
    }
}
