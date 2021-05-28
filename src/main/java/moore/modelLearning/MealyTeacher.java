package moore.modelLearning;

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.filter.cache.mealy.MealyCacheOracle;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterSUL;
import de.learnlib.oracle.equivalence.*;
import de.learnlib.oracle.membership.SULOracle;
import generic.modelLearning.EQMethod;
import generic.modelLearning.Teacher;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Word;

import java.util.Random;


public class MealyTeacher<I, O> extends Teacher<I, Word<O>, MealyMachine<?, I, ?, O>> {

    private final SymbolCounterSUL<I, O> mqSymbolCounter;
    private final SymbolCounterSUL<I, O> eqSymbolCounter;

    private final ResetCounterSUL<I, O> mqResetCounter;
    private final ResetCounterSUL<I, O> eqRestCounter;


    public MealyTeacher(CompactMealy<I, O> model, EQMethod eqOption, boolean withMemory) {
        super(model, withMemory);
        //////////////////////////////////
        // Setup objects related to MQs	//
        //////////////////////////////////

        SUL<I, O> sulSim = new MealySimulatorSUL<>(model);
        // Counters for MQs
        mqSymbolCounter = new SymbolCounterSUL<>("MQ", sulSim);
        mqResetCounter = new ResetCounterSUL<>("MQ", mqSymbolCounter);
        SULOracle<I, O> sulOracle = new SULOracle<>(mqResetCounter);
        // use caching to avoid duplicate queries
        MealyCacheOracle<I, O> cacheOracle = MealyCacheOracle.createDAGCacheOracle(model.getInputAlphabet(), sulOracle);
        mqOracle = new MealyCounterOracle<>(cacheOracle, "MQ");

        //////////////////////////////////
        // Setup objects related to EQs	//
        //////////////////////////////////

        SUL<I, O> eqSim = new MealySimulatorSUL<>(model);
        // Counters for EQs
        eqSymbolCounter = new SymbolCounterSUL<>("EQ", eqSim);
        eqRestCounter = new ResetCounterSUL<>("EQ", eqSymbolCounter);
        SULOracle<I, O> eqSulOracle = new SULOracle<>(eqRestCounter);
        // use caching to avoid duplicate queries
        MealyCacheOracle<I, O> eqCacheOracle = MealyCacheOracle.createDAGCacheOracle(model.getInputAlphabet(), eqSulOracle);
        MembershipOracle.MealyMembershipOracle<I, O> eqCounter = new MealyCounterOracle<>(eqCacheOracle, "EQ");
        eqOracle = buildEqOracle(new Random(System.currentTimeMillis()), eqOption, eqCounter);
    }


    public long getMQRestCount() {
        return mqResetCounter.getStatisticalData().getCount();
    }

    public long getMQSymbols() {
        return mqSymbolCounter.getStatisticalData().getCount();
    }


    public long getEQRestCount() {
        return eqRestCounter.getStatisticalData().getCount();
    }

    public long getEQSymbols() {
        return eqSymbolCounter.getStatisticalData().getCount();
    }


    private EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>> buildEqOracle(
            Random rnd_seed,
            EQMethod option,
            MembershipOracle.MealyMembershipOracle<I, O> eqSUL
    ) {
        switch (option) {
            case RAND_WORDS:
                return new MealyRandomWordsEQOracle<>(eqSUL, 0, 10, 40000);
            case WP:
                return new MealyWpMethodEQOracle<>(eqSUL, 2);
            case W_RAND:
                return new MealyRandomWMethodEQOracle<>(eqSUL, 0, 4, 40000, rnd_seed, 1);
            case WP_RAND:
                return new MealyRandomWpMethodEQOracle<>(eqSUL, 0, 4, 40000, rnd_seed, 1);
            default:
                return new MealyWMethodEQOracle<>(eqSUL, 2);
        }
    }


}

