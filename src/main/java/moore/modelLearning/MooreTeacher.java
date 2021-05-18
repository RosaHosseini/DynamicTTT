package moore.modelLearning;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.cache.mealy.MealyCacheOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.equivalence.*;
import de.learnlib.oracle.membership.SimulatorOracle;
import generic.modelLearning.EQMethod;
import generic.modelLearning.Teacher;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.automata.transducers.MutableMooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Random;


public class MooreTeacher<I, O> extends Teacher<I, O, MutableMooreMachine<Integer, I, Integer, O>> {

    public CounterOracle<I, Word<O>> mooreMqOracle;
    public EquivalenceOracle<MooreMachine<?, I, ?, O>, I, Word<O>> mooreEqOracle;

    public MooreTeacher(CompactMoore<I, O> model, EQMethod eqOption, boolean withMemory) {
        super(model, withMemory);
        //////////////////////////////////
        // Setup objects related to MQs	//
        //////////////////////////////////

        MembershipOracle<I, Word<O>> membershipOracle = new SimulatorOracle<>(model);
        MealyCacheOracle<I, O> cacheOracle = MealyCacheOracle.createDAGCacheOracle(model.getInputAlphabet(), membershipOracle);
        mooreMqOracle = new CounterOracle<>(cacheOracle, "MQ");

        //////////////////////////////////
        // Setup objects related to MQs	//
        //////////////////////////////////
        MembershipOracle<I, Word<O>> eOracle = new SimulatorOracle<>(model);
        MealyCacheOracle<I, O> eqCacheOracle = MealyCacheOracle.createDAGCacheOracle(model.getInputAlphabet(), eOracle);
        CounterOracle<I, Word<O>> eqCounter = new CounterOracle<>(eqCacheOracle, "EQ");
        mooreEqOracle = buildEqOracle(new Random(System.currentTimeMillis()), eqOption, eqCounter);
    }


    private EquivalenceOracle<MooreMachine<?, I, ?, O>, I, Word<O>> buildEqOracle(
            Random rnd_seed,
            EQMethod option,
            MembershipOracle<I, Word<O>> eqSUL
    ) {
        switch (option) {
            case RAND_WORDS:
                return new RandomWordsEQOracle<>(eqSUL, 0, 10, 40000);
            case WP:
                return new WpMethodEQOracle<>(eqSUL, 2);
            case W_RAND:
                return new RandomWMethodEQOracle<>(eqSUL, 0, 4, 40000, rnd_seed, 1);
            case WP_RAND:
                return new RandomWpMethodEQOracle<>(eqSUL, 0, 4, 40000, rnd_seed, 1);
            default:
                return new WMethodEQOracle<>(eqSUL, 2);
        }
    }


    public O membershipQuery(Word<I> inputString) {
        if (withMemory)
            if (history.containsKey(inputString))
                return history.get(inputString);

        O response = mooreMqOracle.answerQuery(inputString).lastSymbol();
        if (withMemory)
            history.put(inputString, response);
        return response;
    }

    public @Nullable Word<I> equivalenceQuery(
            MooreMachine<?, I, ?, O> hypothesis,
            Alphabet<? extends I> alphabet
    ) {
        DefaultQuery<I, Word<O>> eq = mooreEqOracle.findCounterExample(hypothesis, alphabet);
        if (eq == null) {
            return null;
        }
        return eq.getInput();
    }

    public long getMQCount() {
        return mooreMqOracle.getCount();
    }


    public long getEQCount() {
        return eqCount;
    }

}

