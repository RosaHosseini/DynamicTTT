package generic.modelLearning;

import net.automatalib.words.Word;

public interface MembershipCounter<I, O>{
    O membershipQuery(Word<I> inputString);
}
