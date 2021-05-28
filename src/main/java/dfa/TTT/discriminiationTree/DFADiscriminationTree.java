package dfa.TTT.discriminiationTree;

import generic.TTT.TTTNode;
import generic.TTT.discriminationTree.*;
import generic.modelLearning.MembershipCounter;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DFADiscriminationTree<I> extends DiscriminationTree<I, Boolean> {


    public DFADiscriminationTree(MembershipCounter<I, Boolean> membershipCounter) {
        super(
                membershipCounter,
                new DFADiscriminatorNode<>(null, new WordBuilder<I>().toWord(), true)
        );
    }

    public boolean discriminate(Word<I> discriminator, TTTNode<I, Boolean> state, TTTNode<I, Boolean> from) {
        DTLeaf<I, Boolean> DTLeaf = sift(state.sequenceAccess);


        if (DTLeaf.parent.getDiscriminator().equals(discriminator)) {
            return discriminate(state, DTLeaf);
        }
        if (!(DTLeaf instanceof EmptyDTLeaf)) {
            if (DTLeaf.state.sequenceAccess.equals(from.sequenceAccess)) {
                return placeNewLeafInFilledLeaf(discriminator, DTLeaf, state);
            }

            DFADiscriminatorNode<I> LCA = (DFADiscriminatorNode<I>) findLCA(root, DTLeaf.state.sequenceAccess, from.sequenceAccess);
            if (LCA == null)
                return false;
            DFADiscriminatorNode<I> newDiscriminatorNode = reDiscriminate(discriminator, LCA);

            DTLeaf<I, Boolean> newPose = sift(state.sequenceAccess);
            boolean result = discriminate(state, newPose);

            removeRedundantDiscriminators(newDiscriminatorNode);
            return result;
        } else {
            placeNewLeafInEmptyLeaf(discriminator, (EmptyDTLeaf<I, Boolean>) DTLeaf, state);
        }
        return true;
    }


    public boolean discriminate(TTTNode<I, Boolean> state, DTLeaf<I, Boolean> DTLeaf) {
        if (DTLeaf instanceof EmptyDTLeaf) {
            DTLeaf<I, Boolean> newDTLeaf = new DTLeaf<>(DTLeaf.parent, state);
            boolean isDashedChild = findAccessorToFather(DTLeaf);
            if (isDashedChild)
                ((DFADiscriminatorNode<I>) DTLeaf.parent).dashedChild = newDTLeaf;
            else
                ((DFADiscriminatorNode<I>) DTLeaf.parent).solidChild = newDTLeaf;
            return true;
        } else
            return false;
    }

    public List<DiscriminatorNode<I, Boolean>> findAllTemporaryDiscriminators() {
        List<DiscriminatorNode<I, Boolean>> listOfNodes = new ArrayList<>();
        Queue<DFADiscriminatorNode<I>> queue = new ArrayDeque<>();
        queue.add((DFADiscriminatorNode<I>) root);
        while (!queue.isEmpty()) {
            DFADiscriminatorNode<I> currentNode = queue.remove();
            if (!currentNode.isFinal())
                listOfNodes.add(currentNode);
            if (currentNode.dashedChild instanceof DFADiscriminatorNode)
                queue.add((DFADiscriminatorNode<I>) currentNode.dashedChild);
            if (currentNode.solidChild instanceof DFADiscriminatorNode)
                queue.add((DFADiscriminatorNode<I>) currentNode.solidChild);
        }
        return listOfNodes;
    }


    public DFADiscriminatorNode<I> findDiscriminatorNode(Word<I> word) throws Exception {
        Queue<DFADiscriminatorNode<I>> queue = new ArrayDeque<>();
        queue.add((DFADiscriminatorNode<I>) root);
        while (!queue.isEmpty()) {
            DFADiscriminatorNode<I> currentNode = queue.remove();
            if (currentNode.getDiscriminator().equals(word))
                return currentNode;
            if (currentNode.dashedChild instanceof DFADiscriminatorNode)
                queue.add((DFADiscriminatorNode<I>) currentNode.dashedChild);
            if (currentNode.solidChild instanceof DFADiscriminatorNode)
                queue.add((DFADiscriminatorNode<I>) currentNode.solidChild);
        }
        throw new Exception("the given word is not valid in any discriminators of discrimination Tree");
    }


    private boolean placeNewLeafInFilledLeaf(Word<I> discriminator, DTLeaf<I, Boolean> DTLeaf, TTTNode<I, Boolean> state) {
        boolean oldDTLeafAccepting = membershipCounter.membershipQuery(DTLeaf.state.sequenceAccess.concat(discriminator));
        boolean newDTLeafAccepting = membershipCounter.membershipQuery(state.sequenceAccess.concat(discriminator));
        if (newDTLeafAccepting == oldDTLeafAccepting)
            return false;

        DFADiscriminatorNode<I> discriminatorNode = new DFADiscriminatorNode<>((DFADiscriminatorNode<I>) DTLeaf.parent, discriminator);
        DTLeaf<I, Boolean> newDTLeaf = new DTLeaf<>(discriminatorNode, state);

        if (newDTLeafAccepting) {
            discriminatorNode.dashedChild = newDTLeaf;
            discriminatorNode.solidChild = DTLeaf;
        } else {
            discriminatorNode.solidChild = newDTLeaf;
            discriminatorNode.dashedChild = DTLeaf;
        }

        boolean accepting = findAccessorToFather((DTLeaf));

        if (accepting)
            ((DFADiscriminatorNode<I>) DTLeaf.parent).dashedChild = discriminatorNode;
        else
            ((DFADiscriminatorNode<I>) DTLeaf.parent).solidChild = discriminatorNode;
        DTLeaf.parent = discriminatorNode;
        return true;
    }


    private void placeNewLeafInEmptyLeaf(Word<I> discriminator, EmptyDTLeaf<I, Boolean> DTLeaf, TTTNode<I, Boolean> state) {

        DFADiscriminatorNode<I> discriminatorNode = new DFADiscriminatorNode<>((DFADiscriminatorNode<I>) DTLeaf.parent, discriminator);
        boolean newAccepting = membershipCounter
                .membershipQuery(state.sequenceAccess.concat(discriminatorNode.getDiscriminator()));
        DTLeaf<I, Boolean> newDTLeaf = new DTLeaf<>(discriminatorNode, state);

        if (newAccepting) {
            discriminatorNode.dashedChild = newDTLeaf;
        } else {
            discriminatorNode.solidChild = newDTLeaf;
        }

        boolean accepting = findAccessorToFather(DTLeaf);

        if (accepting)
            ((DFADiscriminatorNode<I>) DTLeaf.parent).dashedChild = discriminatorNode;
        else
            ((DFADiscriminatorNode<I>) DTLeaf.parent).solidChild = discriminatorNode;
    }

    private DFADiscriminatorNode<I> reDiscriminate(Word<I> discriminator, DFADiscriminatorNode<I> root) {
        DFADiscriminatorNode<I> newDiscriminatorNode = new DFADiscriminatorNode<>(
                (DFADiscriminatorNode<I>) root.parent,
                discriminator
        );
        if (findAccessorToFather(root))
            ((DFADiscriminatorNode<I>) root.parent).dashedChild = newDiscriminatorNode;
        else ((DFADiscriminatorNode<I>) root.parent).solidChild = newDiscriminatorNode;

        Pair<DiscriminationNode<I, Boolean>, DiscriminationNode<I, Boolean>> pair = split(discriminator, root);
        newDiscriminatorNode.solidChild = pair.getFirst();
        newDiscriminatorNode.dashedChild = pair.getSecond();
        newDiscriminatorNode.solidChild.parent = newDiscriminatorNode;
        newDiscriminatorNode.dashedChild.parent = newDiscriminatorNode;

        return newDiscriminatorNode;
    }

    private Pair<DiscriminationNode<I, Boolean>, DiscriminationNode<I, Boolean>> split(Word<I> discriminator, DiscriminationNode<I, Boolean> rootNode) {
        if (rootNode instanceof DFADiscriminatorNode)
            return split(discriminator, (DFADiscriminatorNode<I>) rootNode);
        return split(discriminator, (DTLeaf<I, Boolean>) rootNode);
    }

    private Pair<DiscriminationNode<I, Boolean>, DiscriminationNode<I, Boolean>> split(Word<I> discriminator, DFADiscriminatorNode<I> rootNode) {

        Pair<DiscriminationNode<I, Boolean>, DiscriminationNode<I, Boolean>> solidPair =
                split(discriminator, rootNode.solidChild);
        Pair<DiscriminationNode<I, Boolean>, DiscriminationNode<I, Boolean>> dashedPair =
                split(discriminator, rootNode.dashedChild);

        DFADiscriminatorNode<I> solid = new DFADiscriminatorNode<>(
                (DFADiscriminatorNode<I>) rootNode.parent,
                rootNode.getDiscriminator(),
                dashedPair.getFirst(),
                solidPair.getFirst(),
                rootNode.isFinal()
        );

        DFADiscriminatorNode<I> dashed = new DFADiscriminatorNode<>(
                (DFADiscriminatorNode<I>) rootNode.parent,
                rootNode.getDiscriminator(),
                dashedPair.getSecond(),
                solidPair.getSecond(),
                rootNode.isFinal()
        );
        solid.dashedChild.parent = solid;
        solid.solidChild.parent = solid;

        dashed.dashedChild.parent = dashed;
        dashed.solidChild.parent = dashed;

        return Pair.of(solid, dashed);
    }

    private Pair<DiscriminationNode<I, Boolean>, DiscriminationNode<I, Boolean>> split(Word<I> discriminator, DTLeaf<I, Boolean> rootNode) {
        if (rootNode instanceof EmptyDTLeaf)
            return Pair.of(rootNode, rootNode);
        boolean accepting = membershipCounter.membershipQuery(rootNode.state.sequenceAccess.concat(discriminator));
        if (accepting)
            return Pair.of(new EmptyDTLeaf<>(root.parent), rootNode);
        return Pair.of(rootNode, new EmptyDTLeaf<>(rootNode.parent));
    }


    public  @Nullable DiscriminationNode<I, Boolean> findLCA(DiscriminationNode<I, Boolean> node, Word<I> word1, Word<I> word2) {
        if (node instanceof EmptyDTLeaf)
            return null;
        if (node instanceof DTLeaf) {
            if (
                    ((DTLeaf<I, Boolean>) node).state.sequenceAccess.equals(word1) ||
                            ((DTLeaf<I, Boolean>) node).state.sequenceAccess.equals(word2)
            )
                return node;
            else return null;
        }

        DiscriminationNode<I, Boolean> leftLCA = findLCA(((DFADiscriminatorNode<I>) node).solidChild, word1, word2);
        DiscriminationNode<I, Boolean> rightLCA = findLCA(((DFADiscriminatorNode<I>) node).dashedChild, word1, word2);

        if (rightLCA != null && leftLCA != null)
            return node;

        return (leftLCA != null) ? leftLCA : rightLCA;
    }


    @Override
    public void removeRedundantDiscriminators(DiscriminatorNode<I, Boolean> DTNode) {
        if (((DFADiscriminatorNode<I>) DTNode).solidChild instanceof DFADiscriminatorNode)
            removeRedundantDiscriminators((DFADiscriminatorNode<I>) ((DFADiscriminatorNode<I>) DTNode).solidChild);
        if (((DFADiscriminatorNode<I>) DTNode).dashedChild instanceof DFADiscriminatorNode)
            removeRedundantDiscriminators((DFADiscriminatorNode<I>) ((DFADiscriminatorNode<I>) DTNode).dashedChild);

        DFADiscriminatorNode<I> parent = (DFADiscriminatorNode<I>) DTNode.parent;
        if (((DFADiscriminatorNode<I>) DTNode).dashedChild instanceof EmptyDTLeaf) {
            ((DFADiscriminatorNode<I>) DTNode).solidChild.parent = parent;
            if (findAccessorToFather(DTNode))
                parent.dashedChild = ((DFADiscriminatorNode<I>) DTNode).solidChild;
            else parent.solidChild = ((DFADiscriminatorNode<I>) DTNode).solidChild;
        } else if (((DFADiscriminatorNode<I>) DTNode).solidChild instanceof EmptyDTLeaf) {
            ((DFADiscriminatorNode<I>) DTNode).dashedChild.parent = parent;
            if (findAccessorToFather(DTNode))
                parent.dashedChild = ((DFADiscriminatorNode<I>) DTNode).dashedChild;
            else parent.solidChild = ((DFADiscriminatorNode<I>) DTNode).dashedChild;
        }
    }

    @Override
    public Boolean findAccessorToFather(DiscriminationNode<I, Boolean> node) {
        DFADiscriminatorNode<I> parent = (DFADiscriminatorNode<I>) node.parent;
        return parent.dashedChild == node;
    }

}