package dfa.modelLearning;

import generic.modelLearning.EQMethod;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.cache.dfa.DFACacheOracle;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.*;
import de.learnlib.oracle.membership.SimulatorOracle;
import generic.modelLearning.Teacher;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

import java.util.Random;


public class DFATeacher<I> extends Teacher<I, Boolean, MutableDFA<Integer, I>> {

    public DFATeacher(CompactDFA<I> model, EQMethod eqOption, boolean withMemory) {
        super(model, withMemory);
        // Counters for MQs and EQs
        SimulatorOracle.DFASimulatorOracle<I> sulSim = new SimulatorOracle.DFASimulatorOracle<>(model);
//        StatisticSUL<I, Boolean> mq_sym = new SymbolCounterSUL<I, Boolean>("MQ", sulSim);
//        StatisticSUL<I, Boolean> mq_rst = new ResetCounterSUL<I, Boolean>("MQ", sulSim);
        DFACacheOracle<I> cacheOracle = DFACacheOracle.createDAGCacheOracle(model.getInputAlphabet(), sulSim);
        mqOracle = new DFACounterOracle<>(cacheOracle, "MQ");
        EquivalenceOracle<DFA<?, I>, I, Boolean> ccTest = cacheOracle.createCacheConsistencyTest();
        eqOracle = buildEqOracle(new Random(System.currentTimeMillis()), eqOption);
    }


    private EquivalenceOracle<MutableDFA<Integer, I>, I, Boolean> buildEqOracle(
            Random rnd_seed,
            EQMethod option
    ) {
        MembershipOracle.DFAMembershipOracle<I> membershipOracle = (MembershipOracle.DFAMembershipOracle<I>) mqOracle.asOracle();
        switch (option) {
            case RAND_WORDS:
                return new RandomWordsEQOracle<>(membershipOracle, 0, 10, 40000);
            case WP:
                return new WpMethodEQOracle<>(membershipOracle, 2);
            case W_RAND:
                return new RandomWMethodEQOracle<>(membershipOracle, 0, 4, 40000, rnd_seed, 1);
            case WP_RAND:
                return new RandomWpMethodEQOracle<>(membershipOracle, 0, 4, 40000, rnd_seed, 1);
            default:
                return new WMethodEQOracle<>(membershipOracle, 2);
        }
    }
}

