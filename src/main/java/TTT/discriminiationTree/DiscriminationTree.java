package TTT.discriminiationTree;

import TTT.TTTNode;
import modelLearning.MembershipCounter;
import net.automatalib.commons.util.Pair;
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
     * @param sequenceAccess a word that shows the sequence access of a state
     *                       that we want to sift it in the discrimination Tree
     *                       we find the position of sate in the Tree
     * @return a leaf in the discrimination Tree which is the place of the given state.
     **/
    public DTLeaf<I> sift(Word<I> sequenceAccess) {
        return root.sift(sequenceAccess, membershipCounter);
    }

    /**
     * @param state         a TTT node that we want to be place in the discrimination Tree
     * @param discriminator a string word shows the discriminator that we want state be a child of it
     *                      discriminate a state by a specific discriminator
     *                      (if the discriminator does not exist in this Discrimination Tree we add it)
     * @return false if we can't discriminate the state in the current discrimination Tree other wise true
     **/
    public boolean discriminate(Word<I> discriminator, TTTNode<I> state, TTTNode<I> from) {
        DTLeaf<I> DTLeaf = sift(state.sequenceAccess);


        if (DTLeaf.parent.getDiscriminator().equals(discriminator)) {
            return discriminate(state, DTLeaf);
        }
        if (!(DTLeaf instanceof EmptyDTLeaf)) {
            if (DTLeaf.state.sequenceAccess.equals(from.sequenceAccess)) {
                return placeNewLeafInFilledLeaf(discriminator, DTLeaf, state);
            }

            DiscriminatorNode<I> LCA = (DiscriminatorNode<I>) findLCA(root, DTLeaf.state.sequenceAccess, from.sequenceAccess);
            if (LCA == null)
                return false;
            DiscriminatorNode<I> newDiscriminatorNode = reDiscriminate(discriminator, LCA);

            DTLeaf<I> newPose = sift(state.sequenceAccess);
            boolean result = discriminate(state, newPose);

            removeRedundantDiscriminators(newDiscriminatorNode);
            return result;
        } else {
            placeNewLeafInEmptyLeaf(discriminator, (EmptyDTLeaf<I>) DTLeaf, state);
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
     * @param state  a TTT node that we want to place it in the discrimination Tree
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

    /**
     * find a leaf node in DT
     *
     * @param word the sequence access of  state related to leaf
     * @return the relted leaf
     * @throws Exception if word is not available in any leaves of the discrimination Tree
     */
    public DTLeaf<I> findLeaf(Word<I> word) throws Exception {
        @Nullable DTLeaf<I> result = root.find(word);
        if (result == null)
            throw new Exception("the given word is not valid in the discrimination Tree");
        return result;
    }

    /**
     * find a discriminator node in DT
     *
     * @param word the word of discriminator
     * @return the discriminatorNode
     * @throws Exception if word is not available in any discriminators of the discrimination Tree
     */
    public DiscriminatorNode<I> findDiscriminatorNode(Word<I> word) throws Exception {
        Queue<DiscriminatorNode<I>> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            DiscriminatorNode<I> currentNode = queue.remove();
            if (currentNode.discriminator.equals(word))
                return currentNode;
            if (currentNode.dashedChild instanceof DiscriminatorNode)
                queue.add((DiscriminatorNode<I>) currentNode.dashedChild);
            if (currentNode.solidChild instanceof DiscriminatorNode)
                queue.add((DiscriminatorNode<I>) currentNode.solidChild);
        }
        throw new Exception("the given word is not valid in any discriminators of discrimination Tree");
    }


    private boolean placeNewLeafInFilledLeaf(Word<I> discriminator, DTLeaf<I> DTLeaf, TTTNode<I> state) {
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
        return true;
    }


    private void placeNewLeafInEmptyLeaf(Word<I> discriminator, EmptyDTLeaf<I> DTLeaf, TTTNode<I> state) {
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

    private DiscriminatorNode<I> reDiscriminate(Word<I> discriminator, DiscriminatorNode<I> root) {
        DiscriminatorNode<I> newDiscriminatorNode = new DiscriminatorNode<>(root.parent, discriminator);
        if (root.parent.isDashChild(root))
            root.parent.dashedChild = newDiscriminatorNode;
        else root.parent.solidChild = newDiscriminatorNode;

        Pair<DiscriminationNode<I>, DiscriminationNode<I>> pair = split(discriminator, root);
        newDiscriminatorNode.solidChild = pair.getFirst();
        newDiscriminatorNode.dashedChild = pair.getSecond();
        newDiscriminatorNode.solidChild.parent = newDiscriminatorNode;
        newDiscriminatorNode.dashedChild.parent = newDiscriminatorNode;

        return newDiscriminatorNode;
    }

    private Pair<DiscriminationNode<I>, DiscriminationNode<I>> split(Word<I> discriminator, DiscriminationNode<I> rootNode) {
        if (rootNode instanceof DiscriminatorNode)
            return split(discriminator, (DiscriminatorNode<I>) rootNode);
        return split(discriminator, (DTLeaf<I>) rootNode);
    }

    private Pair<DiscriminationNode<I>, DiscriminationNode<I>> split(Word<I> discriminator, DiscriminatorNode<I> rootNode) {

        Pair<DiscriminationNode<I>, DiscriminationNode<I>> solidPair =
                split(discriminator, rootNode.solidChild);
        Pair<DiscriminationNode<I>, DiscriminationNode<I>> dashedPair =
                split(discriminator, rootNode.dashedChild);

        DiscriminatorNode<I> solid = new DiscriminatorNode<>(
                rootNode.parent,
                rootNode.discriminator,
                dashedPair.getFirst(),
                solidPair.getFirst(),
                rootNode.isFinal
        );

        DiscriminatorNode<I> dashed = new DiscriminatorNode<>(
                rootNode.parent,
                rootNode.discriminator,
                dashedPair.getSecond(),
                solidPair.getSecond(),
                rootNode.isFinal
        );
        solid.dashedChild.parent = solid;
        solid.solidChild.parent = solid;

        dashed.dashedChild.parent = dashed;
        dashed.solidChild.parent = dashed;

        return Pair.of(solid, dashed);
    }

    private Pair<DiscriminationNode<I>, DiscriminationNode<I>> split(Word<I> discriminator, DTLeaf<I> rootNode) {
        if (rootNode instanceof EmptyDTLeaf)
            return Pair.of(rootNode, rootNode);
        boolean accepting = membershipCounter.membershipQuery(rootNode.state.sequenceAccess.concat(discriminator));
        if (accepting)
            return Pair.of(new EmptyDTLeaf<>(root.parent), rootNode);
        return Pair.of(rootNode, new EmptyDTLeaf<>(rootNode.parent));
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

    /***
     * Remove any discriminator in sub-tree of @param DTNode which does not discriminate any state!
     */
    protected void removeRedundantDiscriminators(DiscriminatorNode<I> DTNode) {
        if (DTNode.solidChild instanceof DiscriminatorNode)
            removeRedundantDiscriminators((DiscriminatorNode<I>) DTNode.solidChild);
        if (DTNode.dashedChild instanceof DiscriminatorNode)
            removeRedundantDiscriminators((DiscriminatorNode<I>) DTNode.dashedChild);

        DiscriminatorNode<I> parent = DTNode.parent;
        if (DTNode.dashedChild instanceof EmptyDTLeaf) {
            DTNode.solidChild.parent = parent;
            if (parent.isDashChild(DTNode))
                parent.dashedChild = DTNode.solidChild;
            else parent.solidChild = DTNode.solidChild;
        } else if (DTNode.solidChild instanceof EmptyDTLeaf) {
            DTNode.dashedChild.parent = parent;
            if (parent.isDashChild(DTNode))
                parent.dashedChild = DTNode.dashedChild;
            else parent.solidChild = DTNode.dashedChild;
        }
    }
}