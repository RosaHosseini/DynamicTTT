package TTT.discriminiationTree;

import TTT.TTTNode;
import modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DTLeaf<I> extends DiscriminationNode<I> {
    public TTTNode<I> state;

    public DTLeaf(DiscriminatorNode<I> parent, TTTNode<I> node) {
        super(parent);
        this.state = node;
    }

    @Override
    public DTLeaf<I> sift(Word<I> sequenceAccess, MembershipCounter<I> membershipCounter) {
        return this;
    }

    @Override
    protected boolean assertDiscriminator(
            Word<I> finalDiscriminator,
            MembershipCounter<I> membershipCounter,
            boolean expectedResult
    ) {
        return membershipCounter.membershipQuery(state.sequenceAccess.concat(finalDiscriminator)) == expectedResult;
    }

    @Override
    public @Nullable DTLeaf<I> find(Word<I> word) {
        if (state.sequenceAccess.equals(word)){
            return this;
        }return null;
    }
}
