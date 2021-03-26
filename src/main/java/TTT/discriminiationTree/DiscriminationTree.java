package TTT.discriminiationTree;

import TTT.TTTNode;
import modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DiscriminationTree<I> {
    protected List<Word<I>> discriminators = new ArrayList<>();
    public final DiscriminatorNode<I> root = new DiscriminatorNode<>(null, new WordBuilder<I>().toWord(), true);
    protected MembershipCounter<I> membershipCounter;

    public DiscriminationTree(MembershipCounter<I> membershipCounter) {
        this.membershipCounter = membershipCounter;
    }


    /**
     * @param state         a TTT node that we want to be place in the discrimination Tree
     * @param discriminator a string word shows the discriminator that we want state be a child of it
     *                      discriminate a state by a specific discriminator
     *                      (if the discriminator does not exist in this Discrimination Tree we add it)
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    public boolean discriminate(Word<I> discriminator, TTTNode<I> state) {
        DTLeaf<I> DTLeaf = sift(state.sequenceAccess);
        if (DTLeaf.parent.getDiscriminator().equals(discriminator))
            return discriminate(state, DTLeaf);

        if (!(DTLeaf instanceof EmptyDTLeaf)) {
            boolean oldDTLeafAccepting = membershipCounter.membershipQuery(DTLeaf.state.sequenceAccess.concat(discriminator));
            boolean newDTLeafAccepting = membershipCounter.membershipQuery(state.sequenceAccess.concat(discriminator));
            if (newDTLeafAccepting == oldDTLeafAccepting)
                return false;

            if (!discriminators.contains(discriminator))
                discriminators.add(discriminator);

            DiscriminatorNode<I> discriminatorNode = new DiscriminatorNode<>(DTLeaf.parent, discriminator);
            DTLeaf<I> newDTLeaf = new DTLeaf<>(discriminatorNode, state);

            if (newDTLeafAccepting) {
                discriminatorNode.dashedChild = newDTLeaf;
                discriminatorNode.solidChild = DTLeaf;
            } else {
                discriminatorNode.solidChild = newDTLeaf;
                discriminatorNode.dashedChild = DTLeaf;
            }

            boolean accepting = false;
            if (DTLeaf.parent.dashedChild == DTLeaf)
                accepting = true;

            if (accepting)
                DTLeaf.parent.dashedChild = discriminatorNode;
            else
                DTLeaf.parent.solidChild = discriminatorNode;
            DTLeaf.parent = discriminatorNode;

        } else {
            if (!discriminators.contains(discriminator))
                discriminators.add(discriminator);

            DiscriminatorNode<I> discriminatorNode = new DiscriminatorNode<>(DTLeaf.parent, discriminator);
            boolean newAccepting = membershipCounter
                    .membershipQuery(state.sequenceAccess.concat(discriminatorNode.getDiscriminator()));
            DTLeaf<I> newDTLeaf = new DTLeaf<>(discriminatorNode, state);

            if (newAccepting) {
                discriminatorNode.dashedChild = newDTLeaf;
            } else {
                discriminatorNode.solidChild = newDTLeaf;
            }

            boolean accepting = false;
            if (DTLeaf.parent.dashedChild == DTLeaf)
                accepting = true;

            if (accepting)
                DTLeaf.parent.dashedChild = discriminatorNode;
            else
                DTLeaf.parent.solidChild = discriminatorNode;
        }
        return true;
    }

    /**
     * @param state a TTT node that we want to be place in the discrimination Tree
     *              we place the new state in the tree
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    public boolean discriminate(TTTNode<I> state) {
        DTLeaf<I> DTLeaf = sift(state.sequenceAccess);
        return discriminate(state, DTLeaf);
    }


    /**
     * @param state  a TTT node that we want to be place in the discrimination Tree
     * @param DTLeaf a Leaf in the current discrimination Tree that we want our state place there
     *               we place the new state in the given leaf of our discrimination Tree
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    public boolean discriminate(TTTNode<I> state, DTLeaf<I> DTLeaf) {
        if (DTLeaf instanceof EmptyDTLeaf) {
            DTLeaf<I> newDTLeaf = new DTLeaf<>(DTLeaf.parent, state);
            if (DTLeaf.parent.isSolidChild(DTLeaf))
                DTLeaf.parent.solidChild = newDTLeaf;
            else
                DTLeaf.parent.dashedChild = newDTLeaf;
            return true;
        } else
            return false;
    }

    /**
     * @param sequenceAccess a word that shows the sequence access of a state
     *                       that we want to sift it in the discrimination Tree
     *                       we find the position of sate in the Tree
     * @return a leaf in the discrimination Tree which is the place of the given state.
     **/
    public DTLeaf<I> sift(Word<I> sequenceAccess) {
        return root.sift(sequenceAccess, membershipCounter);
    }


    /**
     * breadth first search on Discrimination Tree to find all temporary discriminators
     */
    public List<DiscriminatorNode<I>> findAllTemporaryDiscriminators() {
        List<DiscriminatorNode<I>> listOfNodes = new ArrayList<>();
        Queue<DiscriminatorNode<I>> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            DiscriminatorNode<I> currentNode = queue.remove();
            if (!currentNode.isFinal())
                listOfNodes.add(currentNode);
            if (currentNode.dashedChild instanceof DiscriminatorNode)
                queue.add((DiscriminatorNode<I>) currentNode.dashedChild);
            if (currentNode.solidChild instanceof DiscriminatorNode)
                queue.add((DiscriminatorNode<I>) currentNode.solidChild);
        }
        return listOfNodes;
    }

    /**
     * find lowest common ancestor of two inputs in the discrimination Tree
     *
     * @return the discriminator of LCA
     * @throws Exception if word1 or word2 is not available in the discrimination Tree
     */
    public Word<I> findDiscriminator(Word<I> word1, Word<I> word2) throws Exception {

        DiscriminationNode<I> LCA = findLCA(root, word1, word2);
        if (LCA instanceof DiscriminatorNode)
            return (((DiscriminatorNode<I>) LCA).discriminator);

        throw new Exception("words are not available!");
    }

    public DTLeaf<I> find(Word<I> word) throws Exception {
        @Nullable DTLeaf<I> result  = root.find(word);
        if (result == null)
            throw new Exception("the given word is not valid in the discrimination Tree");
        return result;
    }

    /**
     * find lowest common ancestor of two inputs in the sub-Tree of @param node
     *
     * @return the discriminator of LCA
     */
    private @Nullable DiscriminationNode<I> findLCA(DiscriminationNode<I> node, Word<I> word1, Word<I> word2) {
        if (node instanceof EmptyDTLeaf)
            return null;
        if (node instanceof DTLeaf) {
            if (
                    ((DTLeaf<I>) node).state.sequenceAccess.equals(word1) ||
                            ((DTLeaf<I>) node).state.sequenceAccess.equals(word2)
            )
                return node;
            else return null;
        }

        DiscriminationNode<I> leftLCA = findLCA(((DiscriminatorNode<I>) node).solidChild, word1, word2);
        DiscriminationNode<I> rightLCA = findLCA(((DiscriminatorNode<I>) node).dashedChild, word1, word2);

        if (rightLCA != null && leftLCA != null)
            return node;

        return (leftLCA != null) ? leftLCA : rightLCA;
    }


}