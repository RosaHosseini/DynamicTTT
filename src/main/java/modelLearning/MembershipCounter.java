package modelLearning;

import net.automatalib.words.Word;

public interface MembershipCounter<I>{
    boolean membershipQuery(Word<I> inputString);
}
