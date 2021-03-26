package TTT.discriminiationTree;

import modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EmptyDTLeaf<I> extends DTLeaf<I> {

    public EmptyDTLeaf(DiscriminatorNode<I> parent) {
        super(parent, null);
    }

    @Override
    protected boolean assertDiscriminator(Word<I> finalDiscriminator, MembershipCounter<I> membershipCounter, boolean expectedResult) {
        return true;
    }

    @Override
    public @Nullable DTLeaf<I> find(Word<I> word) {
        return null;
    }

}
