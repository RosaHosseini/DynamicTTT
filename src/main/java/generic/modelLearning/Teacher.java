package generic.modelLearning;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;


public abstract class Teacher<I, O, A> implements MembershipCounter<I, O> {

    public CounterOracle<I, O> mqOracle;
    public EquivalenceOracle<A, I, O> eqOracle;

    protected long eqCount = 0;

    protected final Map<Word<I>, O> history = new HashMap<>();
    protected boolean withMemory;

    public Teacher(A model, boolean withMemory) {
        this.withMemory = withMemory;
    }

    public O membershipQuery(Word<I> inputString) {
        if (withMemory)
            if (history.containsKey(inputString))
                return history.get(inputString);

        O response = mqOracle.answerQuery(inputString);
        if (withMemory)
            history.put(inputString, response);
        return response;
    }

    public @Nullable Word<I> equivalenceQuery(
            A hypothesis,
            Alphabet<? extends I> alphabet
    ) {
        eqCount += 1;
        DefaultQuery<I, O> eq = eqOracle.findCounterExample(hypothesis, alphabet);
        if (eq == null) {
            return null;
        }
        return eq.getInput();
    }

    public long getMQCount() {
        return mqOracle.getCount();
    }


    public long getEQCount() {
        return eqCount;
    }


}

