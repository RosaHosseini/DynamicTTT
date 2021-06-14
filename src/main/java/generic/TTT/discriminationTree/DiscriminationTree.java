package generic.TTT.discriminationTree;

import generic.TTT.TTTNode;
import generic.modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public abstract class DiscriminationTree<I, O> implements DiscriminationTreeInterface<I, O> {
    public final DiscriminatorNode<I, O> root;
    protected MembershipCounter<I, O> membershipCounter;

    public DiscriminationTree(MembershipCounter<I, O> membershipCounter, DiscriminatorNode<I, O> root) {
        this.membershipCounter = membershipCounter;
        this.root = root;
    }


    /**
     * @param sequenceAccess a word that shows the sequence access of a state
     *                       that we want to sift it in the discrimination Tree
     *                       we find the position of sate in the Tree
     * @return a leaf in the discrimination Tree which is the place of the given state.
     **/
    public DTLeaf<I, O> sift(Word<I> sequenceAccess) {
        return root.sift(sequenceAccess, membershipCounter);
    }

    /**
     * @param state         a dfa.TTT node that we want to be place in the discrimination Tree
     * @param discriminator a string word shows the discriminator that we want state be a child of it
     *                      discriminate a state by a specific discriminator
     *                      (if the discriminator does not exist in this Discrimination Tree we add it)
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    public abstract boolean discriminate(Word<I> discriminator, TTTNode<I, O> state, TTTNode<I, O> from);

    /**
     * @param state a dfa.TTT node that we want to be place in the discrimination Tree
     *              we place the new state in the tree
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    public boolean discriminate(TTTNode<I, O> state) {
        DTLeaf<I, O> DTLeaf = sift(state.sequenceAccess);
        return discriminate(state, DTLeaf);
    }

    /**
     * @param state  a dfa.TTT node that we want to place it in the discrimination Tree
     * @param DTLeaf a Leaf in the current discrimination Tree that we want our state place there
     *               we place the new state in the given leaf of our discrimination Tree
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    public abstract boolean discriminate(TTTNode<I, O> state, DTLeaf<I, O> DTLeaf);

    /**
     * breadth first search on Discrimination Tree to find all temporary discriminators
     */
    public abstract List<DiscriminatorNode<I, O>> findAllTemporaryDiscriminators();

    /**
     * find lowest common ancestor of two inputs in the discrimination Tree
     *
     * @return the discriminator of LCA
     * @throws Exception if word1 or word2 is not available in the discrimination Tree
     */
    public Word<I> findDiscriminator(Word<I> word1, Word<I> word2) throws Exception {

        DiscriminationNode<I, O> LCA = findLCA(root, word1, word2);
        if (LCA instanceof DiscriminatorNode)
            return (((DiscriminatorNode<I, O>) LCA).discriminator);

        throw new Exception("words are not available!");
    }

    /**
     * find a leaf node in DT
     *
     * @param word the sequence access of  state related to leaf
     * @return the relted leaf
     * @throws Exception if word is not available in any leaves of the discrimination Tree
     */
    public DTLeaf<I, O> findLeaf(Word<I> word) throws Exception {
        @Nullable DTLeaf<I, O> result = root.find(word);
        return result;
    }

    /**
     * find a discriminator node in DT
     *
     * @param word the word of discriminator
     * @return the discriminatorNode
     * @throws Exception if word is not available in any discriminators of the discrimination Tree
     */
    public abstract DiscriminatorNode<I, O> findDiscriminatorNode(Word<I> word) throws Exception;


    /**
     * find lowest common ancestor of two inputs in the sub-Tree of @param node
     *
     * @return the discriminator of LCA
     */
    public abstract @Nullable DiscriminationNode<I, O> findLCA(DiscriminationNode<I, O> node, Word<I> word1, Word<I> word2);

    /***
     * Remove any discriminator in sub-tree of @param DTNode which does not discriminate any state!
     */
    public abstract void removeRedundantDiscriminators(DiscriminatorNode<I, O> DTNode);

    /***
     Find out a node in the discrimination Tree is connected to its father with which accessor in the output Alphabet
     */
    public abstract @Nullable O findAccessorToFather(DiscriminationNode<I, O> node);

    public DiscriminatorNode<I, O> getRoot() {
        return this.root;
    }

    public void draw() {
        StringBuilder buffer = new StringBuilder();
        root.print(buffer, "", "");
        System.out.println(buffer.toString());
    }
}