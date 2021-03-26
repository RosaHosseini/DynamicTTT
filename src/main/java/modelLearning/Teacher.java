package modelLearning;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.DFARandomWordsEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Random;

public class Teacher<I> implements MembershipCounter<I> {
    public final DFACounterOracle<I> mqCounter;
    public final DFARandomWordsEQOracle<I> eqCounter;


    public Teacher(CompactDFA<I> model) {
        // Counters for MQs and EQs
        SimulatorOracle.DFASimulatorOracle<I> simOracle = new SimulatorOracle.DFASimulatorOracle<>(model);
        mqCounter = new DFACounterOracle<>(simOracle, "MQ");
        eqCounter = new DFARandomWordsEQOracle<>(
                new DFACounterOracle<>(simOracle, "EQ"),
                9, 9, 100, new Random(1)
        );
    }

    public boolean membershipQuery(Word<I> inputString) {
        return mqCounter.answerQuery(inputString);
    }

    public @Nullable DefaultQuery<I, Boolean> equivalenceQuery(DFA<?,I> hypothesize, Alphabet<? extends I> alphabet) {
        return eqCounter.findCounterExample(hypothesize, alphabet);
    }
}

