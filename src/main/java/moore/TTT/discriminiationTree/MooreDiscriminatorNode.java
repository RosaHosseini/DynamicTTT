package moore.TTT.discriminiationTree;

import generic.TTT.discriminationTree.DTLeaf;
import generic.TTT.discriminationTree.DiscriminationNode;
import generic.TTT.discriminationTree.DiscriminatorNode;
import generic.TTT.discriminationTree.EmptyDTLeaf;
import generic.modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class MooreDiscriminatorNode<I, O> extends DiscriminatorNode<I, O> {
    public Map<O, DiscriminationNode<I, O>> children;

    public MooreDiscriminatorNode(DiscriminatorNode<I, O> parent,
                                  Word<I> discriminator,
                                  Map<O, DiscriminationNode<I, O>> children) {
        super(parent, discriminator);
        this.children = children;
    }

    public MooreDiscriminatorNode(DiscriminatorNode<I, O> parent,
                                  Word<I> discriminator,
                                  Map<O, DiscriminationNode<I, O>> children,
                                  boolean isFinal) {
        this(parent, discriminator, children);
        this.isFinal = isFinal;
    }

    public MooreDiscriminatorNode(DiscriminatorNode<I, O> parent, Word<I> discriminator, Collection<O> outputAlphabet) {
        super(parent, discriminator);
        this.children = new HashMap<>();
        for (O o : outputAlphabet)
            children.put(o, new EmptyDTLeaf<>(this));
    }

    public MooreDiscriminatorNode(DiscriminatorNode<I, O> parent,
                                  Word<I> discriminator,
                                  Collection<O> outputAlphabet,
                                  Boolean isFinal) {
        this(parent, discriminator, outputAlphabet);
        this.isFinal = isFinal;
    }


    /***
     * renew the discriminator and change this node from temporary to a final node.
     * @param finalDiscriminator a word as a new discriminator
     * @param hypothesisMQCounter a hypothesis that we can ask membership queries
     * @return True if given finalDiscriminator can be an alternative discriminator
     */
    public boolean makeFinal(Word<I> finalDiscriminator, MembershipCounter<I, O> hypothesisMQCounter) {
        if (this.isFinal)
            return false;

        for (O ouput : children.keySet()) {
            DiscriminationNode<I, O> child = children.get(ouput);
            boolean assertion = child.assertDiscriminator(finalDiscriminator, hypothesisMQCounter, ouput);
            if (!assertion)
                return false;
        }
        this.isFinal = true;
        this.discriminator = finalDiscriminator;
        return true;
    }


    @Override
    public DTLeaf<I, O> sift(Word<I> sequenceAccess, MembershipCounter<I, O> membershipCounter) {
        O output = membershipCounter.membershipQuery(sequenceAccess.concat(discriminator));
        return children.get(output).sift(sequenceAccess, membershipCounter);
    }


    @Override
    public boolean assertDiscriminator(
            Word<I> finalDiscriminator,
            MembershipCounter<I, O> membershipCounter,
            O expectedResult
    ) {
        for (O o : children.keySet()) {
            DiscriminationNode<I, O> child = children.get(o);
            boolean assertion = child.assertDiscriminator(finalDiscriminator, membershipCounter, expectedResult);
            if (!assertion)
                return false;
        }
        return true;
    }

    @Override
    public @Nullable DTLeaf<I, O> find(Word<I> word) {
        for (O ouput : children.keySet()) {
            DiscriminationNode<I, O> child = children.get(ouput);
            @Nullable DTLeaf<I, O> result = child.find(word);
            if (result != null)
                return result;
        }
        return null;
    }

}
