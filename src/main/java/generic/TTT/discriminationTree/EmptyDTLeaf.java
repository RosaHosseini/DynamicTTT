package generic.TTT.discriminationTree;

import generic.modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EmptyDTLeaf<I, O> extends DTLeaf<I, O> {

    public EmptyDTLeaf(DiscriminatorNode<I, O> parent) {
        super(parent, null);
    }

    @Override
    public boolean assertDiscriminator(Word<I> finalDiscriminator, MembershipCounter<I, O> membershipCounter, O expectedResult) {
        return true;
    }

    @Override
    public @Nullable DTLeaf<I, O> find(Word<I> word) {
        return null;
    }

}