package TTT.discriminiationTree;

import modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;


public class DiscriminatorNode<I> extends DiscriminationNode<I> {
    protected Word<I> discriminator;
    public DiscriminationNode<I> dashedChild;
    public DiscriminationNode<I> solidChild;
    protected boolean isFinal = false;


    public boolean isFinal() {
        return isFinal;
    }


    public DiscriminatorNode(DiscriminatorNode<I> parent,
                             Word<I> discriminator,
                             DiscriminationNode<I> dashedChild,
                             DiscriminationNode<I> solidChild) {
        super(parent);
        this.discriminator = discriminator;
        this.dashedChild = dashedChild;
        this.solidChild = solidChild;
    }

    public DiscriminatorNode(DiscriminatorNode<I> parent, Word<I> discriminator) {
        super(parent);
        this.discriminator = discriminator;
        this.dashedChild = new EmptyDTLeaf<>(this);
        this.solidChild = new EmptyDTLeaf<>(this);
    }

    public DiscriminatorNode(DiscriminatorNode<I> parent, Word<I> discriminator, Boolean isFinal) {
        super(parent);
        this.discriminator = discriminator;
        this.dashedChild = new EmptyDTLeaf<>(this);
        this.solidChild = new EmptyDTLeaf<>(this);
        this.isFinal = isFinal;
    }


    @Override
    public DTLeaf<I> sift(Word<I> sequenceAccess, MembershipCounter<I> membershipCounter) {
        boolean accepting = membershipCounter.membershipQuery(sequenceAccess.concat(discriminator));
        if (accepting)
            return dashedChild.sift(sequenceAccess, membershipCounter);
        else
            return solidChild.sift(sequenceAccess, membershipCounter);
    }


    public boolean isDashChild(DiscriminationNode<I> node) {
        return dashedChild.equals(node);
    }

    public boolean isSolidChild(DiscriminationNode<I> node) {
        return solidChild.equals(node);
    }

    public Word<I> getDiscriminator() {
        return discriminator;
    }

    /***
     * renew the discriminator and change this node from temporary to a final node.
     * @param finalDiscriminator a word as a new discriminator
     * @param hypothesisMQCounter a hypothesis that we can ask membership queries
     * @return True if given finalDiscriminator can be an alternative discriminator
     */
    public boolean makeFinal(Word<I> finalDiscriminator, MembershipCounter<I> hypothesisMQCounter) {
        if (this.isFinal)
            return false;
        boolean solidChildAssertion = solidChild.assertDiscriminator(finalDiscriminator, hypothesisMQCounter, false);
        boolean dashedChildAssertion = dashedChild.assertDiscriminator(finalDiscriminator, hypothesisMQCounter, true);
        if (solidChildAssertion && dashedChildAssertion) {
            this.isFinal = true;
            this.discriminator = finalDiscriminator;
            return true;
        }
        return false;
    }

    public boolean makeFinal(Word<I> finalDiscriminator) {
        if (isFinal)
            return false;
        this.isFinal = true;
        this.discriminator = finalDiscriminator;
        return true;
    }

    @Override
    protected boolean assertDiscriminator(
            Word<I> finalDiscriminator,
            MembershipCounter<I> membershipCounter,
            boolean expectedResult
    ) {
        return
                solidChild.assertDiscriminator(finalDiscriminator, membershipCounter, expectedResult) &&
                        dashedChild.assertDiscriminator(finalDiscriminator, membershipCounter, expectedResult);
    }

    @Override
    public @Nullable DTLeaf<I> find(Word<I> word) {
        @Nullable DTLeaf<I> dashedSearch = dashedChild.find(word);
        if (dashedSearch != null)
            return dashedSearch;
        @Nullable DTLeaf<I> solidSearch = solidChild.find(word);
        return solidSearch;
    }

}
