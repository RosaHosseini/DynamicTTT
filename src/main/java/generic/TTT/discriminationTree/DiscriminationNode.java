package generic.TTT.discriminationTree;

import generic.modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class DiscriminationNode<I, O> {
    public DiscriminatorNode<I, O> parent;

    public DiscriminationNode(DiscriminatorNode<I, O> parent) {
        this.parent = parent;
    }

    /**
     * @param sequenceAccess    a word which is the sequence access of the state that we want to sift it in the discrimination Tree
     * @param membershipCounter we can ask MQs with it.
     *                          we find the position of sate in the Tree
     * @return a leaf in the discrimination Tree which is the place of the given state.
     **/
    public abstract DTLeaf<I, O> sift(Word<I> sequenceAccess, MembershipCounter<I, O> membershipCounter);


    /**
     * check all subTree has the expected output with given suffix
     *
     * @param finalDiscriminator a word as a suffix
     * @param membershipCounter  a membershipCounter which we can ask membership queries s
     * @param expectedOutput     expected output in based on the given suffix
     * @return True if all leaves in subTree has the expected output with given suffix Otherwise False
     */
    abstract public boolean assertDiscriminator(
            Word<I> finalDiscriminator,
            MembershipCounter<I, O> membershipCounter,
            O expectedOutput
    );

    /**
     * Search the leaf related to an state in the subTree
     * @param word shows the sequence access of state we want search
     * @return  null if there is not valid any leaf with given sequence access otherwise the leaf
     */
    public abstract @Nullable DTLeaf<I, O> find(Word<I> word);

    public abstract void print(StringBuilder buffer, String prefix, String childrenPrefix);

}
