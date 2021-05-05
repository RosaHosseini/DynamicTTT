package modelLearning;

import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.serialization.dot.DefaultDOTVisualizationHelper;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;
import static de.learnlib.acex.analyzers.AcexAnalyzers.LINEAR_FWD;


public class LearnLibTTT<I> extends ModelLearner<I> {
    private final Alphabet<I> alphabet;

    public LearnLibTTT(Teacher<I> teacher, Alphabet<I> initialAlphabet){
        super(teacher);
        this.alphabet = initialAlphabet;
    }

    @Override
    public DFA<?, I> learn() {
        // construct TTT instance
        TTTLearnerDFA<I> learner = new TTTLearnerDFA<>(alphabet, teacher.mqCounter, LINEAR_FWD);
        learner.startLearning();

        while (true) {
            DFA<?, I> model = learner.getHypothesisModel();
            @Nullable DefaultQuery<I, Boolean> eq = this.teacher.equivalenceQuery(model, this.alphabet);
            if (eq == null)
                return model;
//            System.out.println(eq.getInput());
            learner.refineHypothesis(eq);
        }
    }


}




