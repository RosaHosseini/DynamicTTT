package generic.TTT.discriminationTree;

import generic.TTT.TTTNode;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public interface DiscriminationTreeInterface<I, O> {

    /**
     * @param sequenceAccess a word that shows the sequence access of a state
     *                       that we want to sift it in the discrimination Tree
     *                       we find the position of sate in the Tree
     * @return a leaf in the discrimination Tree which is the place of the given state.
     **/
    DTLeaf<I, O> sift(Word<I> sequenceAccess);

    /**
     * @param state         a dfa.TTT node that we want to be place in the discrimination Tree
     * @param discriminator a string word shows the discriminator that we want state be a child of it
     *                      discriminate a state by a specific discriminator
     *                      (if the discriminator does not exist in this Discrimination Tree we add it)
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    boolean discriminate(Word<I> discriminator, TTTNode<I, O> state, TTTNode<I, O> from);

    /**
     * @param state a dfa.TTT node that we want to be place in the discrimination Tree
     *              we place the new state in the tree
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    boolean discriminate(TTTNode<I, O> state);

    /**
     * @param state  a dfa.TTT node that we want to place it in the discrimination Tree
     * @param DTLeaf a Leaf in the current discrimination Tree that we want our state place there
     *               we place the new state in the given leaf of our discrimination Tree
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    boolean discriminate(TTTNode<I, O> state, DTLeaf<I, O> DTLeaf);

    /**
     * breadth first search on Discrimination Tree to find all temporary discriminators
     */
    List<DiscriminatorNode<I, O>> findAllTemporaryDiscriminators();

    /**
     * find lowest common ancestor of two inputs in the discrimination Tree
     *
     * @return the discriminator of LCA
     * @throws Exception if word1 or word2 is not available in the discrimination Tree
     */
    Word<I> findDiscriminator(Word<I> word1, Word<I> word2) throws Exception;

    /**
     * find a leaf node in DT
     *
     * @param word the sequence access of  state related to leaf
     * @return the relted leaf
     * @throws Exception if word is not available in any leaves of the discrimination Tree
     */
    DTLeaf<I, O> findLeaf(Word<I> word) throws Exception;

    /**
     * find a discriminator node in DT
     *
     * @param word the word of discriminator
     * @return the discriminatorNode
     * @throws Exception if word is not available in any discriminators of the discrimination Tree
     */
    DiscriminatorNode<I, O> findDiscriminatorNode(Word<I> word) throws Exception;


    /**
     * find lowest common ancestor of two inputs in the sub-Tree of @param node
     *
     * @return the discriminator of LCA
     */
    @Nullable DiscriminationNode<I, O> findLCA(DiscriminationNode<I, O> node, Word<I> word1, Word<I> word2);

    /***
     * Remove any discriminator in sub-tree of @param DTNode which does not discriminate any state!
     */
    void removeRedundantDiscriminators(DiscriminatorNode<I, O> DTNode);

    /***
     Find out a node in the discrimination Tree is connected to its father with which accessor in the output Alphabet
     */
    @Nullable O findAccessorToFather(DiscriminationNode<I, O> node);

    DiscriminatorNode<I, O> getRoot();

    public void draw();

}