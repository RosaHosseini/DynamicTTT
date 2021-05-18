package dfa.TTT.discriminiationTree;

import generic.TTT.discriminationTree.DTLeaf;
import generic.TTT.discriminationTree.DiscriminationNode;
import generic.TTT.discriminationTree.DiscriminatorNode;
import generic.TTT.discriminationTree.EmptyDTLeaf;
import generic.modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;


public class DFADiscriminatorNode<I> extends DiscriminatorNode<I, Boolean> {
    public DiscriminationNode<I, Boolean> dashedChild;
    public DiscriminationNode<I, Boolean> solidChild;


    public DFADiscriminatorNode(DFADiscriminatorNode<I> parent,
                                Word<I> discriminator,
                                DiscriminationNode<I, Boolean> dashedChild,
                                DiscriminationNode<I, Boolean> solidChild) {
        super(parent, discriminator);
        this.dashedChild = dashedChild;
        this.solidChild = solidChild;
    }


    public DFADiscriminatorNode(DFADiscriminatorNode<I> parent, Word<I> discriminator) {
        super(parent, discriminator);
        this.dashedChild = new EmptyDTLeaf<>(this);
        this.solidChild = new EmptyDTLeaf<>(this);
    }

    public DFADiscriminatorNode(
            DFADiscriminatorNode<I> parent,
            Word<I> discriminator,
            DiscriminationNode<I, Boolean> dashedChild,
            DiscriminationNode<I, Boolean> solidChild,
            boolean isFinal) {
        super(parent, discriminator, isFinal);
        this.dashedChild = dashedChild;
        this.solidChild = solidChild;
    }

    public DFADiscriminatorNode(DFADiscriminatorNode<I> parent, Word<I> discriminator, boolean isFinal) {
        super(parent, discriminator, isFinal);
    }


    @Override
    public DTLeaf<I, Boolean> sift(Word<I> sequenceAccess, MembershipCounter<I, Boolean> membershipCounter) {
        boolean accepting = membershipCounter.membershipQuery(sequenceAccess.concat(discriminator));
        if (accepting)
            return dashedChild.sift(sequenceAccess, membershipCounter);
        else
            return solidChild.sift(sequenceAccess, membershipCounter);
    }


    /***
     * renew the discriminator and change this node from temporary to a final node.
     * @param finalDiscriminator a word as a new discriminator
     * @param hypothesisMQCounter a hypothesis that we can ask membership queries
     * @return True if given finalDiscriminator can be an alternative discriminator
     */
    public boolean makeFinal(Word<I> finalDiscriminator, MembershipCounter<I, Boolean> hypothesisMQCounter) {
        if (this.isFinal)
            return false;
        boolean solidChildAssertion = solidChild.assertDiscriminator(finalDiscriminator, hypothesisMQCounter, false);
        boolean dashedChildAssertion = dashedChild.assertDiscriminator(finalDiscriminator, hypothesisMQCounter, true);
        if (solidChildAssertion && dashedChildAssertion) {
            this.isFinal = true;
            this.discriminator = finalDiscriminator;
            return true;
        } else if ((!solidChildAssertion) && (!dashedChildAssertion)) {
            this.isFinal = true;
            this.discriminator = finalDiscriminator;
            DiscriminationNode<I, Boolean> tempSolidChild = solidChild;
            this.solidChild = dashedChild;
            this.dashedChild = tempSolidChild;
            return true;
        }
        return false;
    }

    @Override
    public boolean assertDiscriminator(
            Word<I> finalDiscriminator,
            MembershipCounter<I, Boolean> membershipCounter,
            Boolean expectedResult
    ) {
        return
                solidChild.assertDiscriminator(finalDiscriminator, membershipCounter, expectedResult) &&
                        dashedChild.assertDiscriminator(finalDiscriminator, membershipCounter, expectedResult);
    }

    @Override
    public @Nullable DTLeaf<I, Boolean> find(Word<I> word) {
        @Nullable DTLeaf<I, Boolean> dashedSearch = dashedChild.find(word);
        if (dashedSearch != null)
            return dashedSearch;
        @Nullable DTLeaf<I, Boolean> solidSearch = solidChild.find(word);
        return solidSearch;
    }

}
