package generic.TTT.discriminationTree;

import generic.modelLearning.MembershipCounter;
import net.automatalib.words.Word;


public abstract class DiscriminatorNode<I, O> extends DiscriminationNode<I, O> {
    protected Word<I> discriminator;
    protected boolean isFinal = false;


    public boolean isFinal() {
        return isFinal;
    }

    public DiscriminatorNode(DiscriminatorNode<I, O> parent, Word<I> discriminator) {
        super(parent);
        this.discriminator = discriminator;
    }

    public DiscriminatorNode(DiscriminatorNode<I, O> parent, Word<I> discriminator, Boolean isFinal) {
        this(parent, discriminator);
        this.isFinal = isFinal;
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
    public abstract boolean makeFinal(Word<I> finalDiscriminator, MembershipCounter<I, O> hypothesisMQCounter);

    public boolean makeFinal(Word<I> finalDiscriminator) {
        if (isFinal)
            return false;
        this.isFinal = true;
        this.discriminator = finalDiscriminator;
        return true;
    }


}
