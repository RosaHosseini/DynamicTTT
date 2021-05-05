package modelLearning;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.cache.dfa.DFACacheOracle;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.*;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Teacher<I> implements MembershipCounter<I> {
    public final DFACounterOracle<I> mqCounter;
    public final EquivalenceOracle<DFA<?, I>, I, Boolean> eqCounter;
    private final Map<Word<I>, Boolean> history = new HashMap<>();


    public Teacher(CompactDFA<I> model, EQMethod eqOption) {
        // Counters for MQs and EQs
        SimulatorOracle.DFASimulatorOracle<I> sulSim = new SimulatorOracle.DFASimulatorOracle<>(model);
//        StatisticSUL<I, Boolean>  mq_sym = new SymbolCounterSUL<I, Boolean>("MQ", sulSim);
//        StatisticSUL<I, Boolean>  mq_rst = new ResetCounterSUL <I, Boolean>("MQ", sulSim);
        DFACacheOracle<I> cacheOracle = DFACacheOracle.createDAGCacheOracle(model.getInputAlphabet(), sulSim);
        mqCounter = new DFACounterOracle<>(cacheOracle, "MQ");
        EquivalenceOracle<DFA<?, I>, I, Boolean> ccTest = cacheOracle.createCacheConsistencyTest();
        eqCounter = buildEqOracle(new Random(System.currentTimeMillis()), eqOption);
    }

    public boolean membershipQuery(Word<I> inputString) {
        if (history.containsKey(inputString))
            return history.get(inputString);
        boolean output = mqCounter.answerQuery(inputString);
        history.put(inputString, output);
        return output;
    }

    public @Nullable DefaultQuery<I, Boolean> equivalenceQuery(DFA<?, I> hypothesis, Alphabet<? extends I> alphabet) {
        return eqCounter.findCounterExample(hypothesis, alphabet);
    }

    public int getMQCount() {
        return (int) mqCounter.getCount();
    }

    private EquivalenceOracle<DFA<?, I>, I, Boolean> buildEqOracle(
            Random rnd_seed,
            EQMethod option
    ) {
        switch (option) {
            case RAND_WORDS:
                return new DFARandomWordsEQOracle<>(mqCounter, 0, 10, 40000);
            case WP:
                return new DFAWpMethodEQOracle<>(mqCounter, 2);
            case W_RAND:
                return new DFARandomWMethodEQOracle<>(mqCounter, 0, 4, 40000, rnd_seed, 1);
            case WP_RAND:
                return new DFARandomWpMethodEQOracle<>(mqCounter, 0, 4, 40000, rnd_seed, 1);
            default:
                return new DFAWMethodEQOracle<>(mqCounter, 2);
        }
    }


}

