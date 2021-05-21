package moore.TTT.discriminiationTree;

import generic.TTT.TTTNode;
import generic.TTT.discriminationTree.*;
import generic.modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class MooreDiscriminationTree<I, O> extends DiscriminationTree<I, O> {
    private final Collection<O> outputAlphabet;

    public MooreDiscriminationTree(MembershipCounter<I, O> membershipCounter, Collection<O> outputAlphabet) {
        super(membershipCounter, new MooreDiscriminatorNode<>(
                null,
                new WordBuilder<I>().toWord(),
                outputAlphabet,
                true
        ));
        this.outputAlphabet = outputAlphabet;
    }

    @Override
    public boolean discriminate(Word<I> discriminator, TTTNode<I, O> state, TTTNode<I, O> from) {
        DTLeaf<I, O> DTLeaf = sift(state.sequenceAccess);

        if (DTLeaf.parent.getDiscriminator().equals(discriminator)) {
            return discriminate(state, DTLeaf);
        }
        if (!(DTLeaf instanceof EmptyDTLeaf)) {
            if (DTLeaf.state.sequenceAccess.equals(from.sequenceAccess)) {
                return placeNewLeafInFilledLeaf(discriminator, DTLeaf, state);
            }

            DiscriminatorNode<I, O> LCA = (DiscriminatorNode<I, O>) findLCA(root, DTLeaf.state.sequenceAccess, from.sequenceAccess);
            if (LCA == null)
                return false;
            DiscriminatorNode<I, O> newDiscriminatorNode = reDiscriminate(discriminator, LCA);

            DTLeaf<I, O> newPose = sift(state.sequenceAccess);
            boolean result = discriminate(state, newPose);

            removeRedundantDiscriminators(newDiscriminatorNode);
            return result;
        } else {
            placeNewLeafInEmptyLeaf(discriminator, (EmptyDTLeaf<I, O>) DTLeaf, state);
        }
        return true;
    }

    @Override
    public boolean discriminate(TTTNode<I, O> state, DTLeaf<I, O> DTLeaf) {
        if (DTLeaf instanceof EmptyDTLeaf) {
            DTLeaf<I, O> newDTLeaf = new DTLeaf<>(DTLeaf.parent, state);
            MooreDiscriminatorNode<I, O> parent = (MooreDiscriminatorNode<I, O>) DTLeaf.parent;
            O key = findAccessorToFather(DTLeaf);
            if (key != null) {
                parent.children.put(key, newDTLeaf);
                return true;
            }
        }
        return false;
    }


    @Override
    public List<DiscriminatorNode<I, O>> findAllTemporaryDiscriminators() {
        List<DiscriminatorNode<I, O>> listOfNodes = new ArrayList<>();
        Queue<MooreDiscriminatorNode<I, O>> queue = new ArrayDeque<>();
        queue.add((MooreDiscriminatorNode<I, O>) root);
        while (!queue.isEmpty()) {
            MooreDiscriminatorNode<I, O> currentNode = queue.remove();
            if (!currentNode.isFinal())
                listOfNodes.add(currentNode);
            for (O key : currentNode.children.keySet()) {
                DiscriminationNode<I, O> child = currentNode.children.get(key);
                if (child instanceof MooreDiscriminatorNode) {
                    queue.add((MooreDiscriminatorNode<I, O>) child);
                }
            }
        }
        return listOfNodes;
    }

    @Override
    public DiscriminatorNode<I, O> findDiscriminatorNode(Word<I> word) throws Exception {
        Queue<MooreDiscriminatorNode<I, O>> queue = new ArrayDeque<>();
        queue.add((MooreDiscriminatorNode<I, O>) root);
        while (!queue.isEmpty()) {
            MooreDiscriminatorNode<I, O> currentNode = queue.remove();
            if (currentNode.getDiscriminator().equals(word))
                return currentNode;
            for (O key : currentNode.children.keySet()) {
                DiscriminationNode<I, O> child = currentNode.children.get(key);
                if (child instanceof MooreDiscriminatorNode) {
                    queue.add((MooreDiscriminatorNode<I, O>) child);
                }
            }
        }
        throw new Exception("the given word is not valid in any discriminators of discrimination Tree");
    }

    @Override
    public  @Nullable DiscriminationNode<I, O> findLCA
            (DiscriminationNode<I, O> node, Word<I> word1, Word<I> word2) {
        if (node instanceof EmptyDTLeaf)
            return null;
        if (node instanceof DTLeaf) {
            if (
                    ((DTLeaf<I, O>) node).state.sequenceAccess.equals(word1) ||
                            ((DTLeaf<I, O>) node).state.sequenceAccess.equals(word2)
            )
                return node;
            else return null;
        }
        MooreDiscriminatorNode<I, O> currentNode = (MooreDiscriminatorNode<I, O>) node;
        ArrayList<DiscriminationNode<I, O>> lcaList = new ArrayList<>();
        for (O key : currentNode.children.keySet()) {
            DiscriminationNode<I, O> child = currentNode.children.get(key);
            DiscriminationNode<I, O> lca = findLCA(child, word1, word2);
            if (lca != null) {
                lcaList.add(lca);
            }
        }

        if (lcaList.size() > 1)
            return node;
        if (lcaList.size() == 1)
            return lcaList.get(0);
        return null;
    }

    @Override
    public void removeRedundantDiscriminators(DiscriminatorNode<I, O> DTNode) {
        MooreDiscriminatorNode<I, O> node = (MooreDiscriminatorNode<I, O>) DTNode;
        MooreDiscriminatorNode<I, O> parent = (MooreDiscriminatorNode<I, O>) DTNode.parent;

        ArrayList<DiscriminationNode<I, O>> fillChildren = new ArrayList<>();
        for (O key : node.children.keySet()) {
            DiscriminationNode<I, O> child = node.children.get(key);
            if (child instanceof DiscriminatorNode)
                removeRedundantDiscriminators((DiscriminatorNode<I, O>) child);

            child = node.children.get(key);
            if (!(child instanceof EmptyDTLeaf))
                fillChildren.add(child);
        }
        if (fillChildren.size() == 1) {
            DiscriminationNode<I, O> fillNode = fillChildren.get(0);
            fillNode.parent = parent;
            O key = findAccessorToFather(DTNode);
            if (key != null)
                parent.children.put(key, fillNode);
        }
    }

    /***
     Find out a node in the discrimination Tree is connected to its father with which accessor in the output Alphabet
     */
    public  @Nullable O findAccessorToFather(DiscriminationNode<I, O> node) {
        MooreDiscriminatorNode<I, O> parent = (MooreDiscriminatorNode<I, O>) node.parent;
        for (O key : parent.children.keySet()) {
            DiscriminationNode<I, O> child = parent.children.get(key);
            if (child.equals(node)) {
                return key;
            }
        }
        return null;
    }


    private boolean placeNewLeafInFilledLeaf(Word<I> discriminator, DTLeaf<I, O> DTLeaf, TTTNode<I, O> state) {
        O oldDTLeafOutput = membershipCounter.membershipQuery(DTLeaf.state.sequenceAccess.concat(discriminator));
        O newDTLeafOutput = membershipCounter.membershipQuery(state.sequenceAccess.concat(discriminator));
        if (newDTLeafOutput.equals(oldDTLeafOutput))
            return false;

        MooreDiscriminatorNode<I, O> discriminatorNode = new MooreDiscriminatorNode<>(
                DTLeaf.parent, discriminator, this.outputAlphabet);
        DTLeaf<I, O> newDTLeaf = new DTLeaf<>(discriminatorNode, state);

        discriminatorNode.children.put(oldDTLeafOutput, DTLeaf);
        discriminatorNode.children.put(newDTLeafOutput, newDTLeaf);

        O accessor = findAccessorToFather(DTLeaf);
        if (accessor != null) {
            MooreDiscriminatorNode<I, O> parent = (MooreDiscriminatorNode<I, O>) DTLeaf.parent;
            parent.children.put(accessor, discriminatorNode);
            DTLeaf.parent = discriminatorNode;
            return true;
        }
        return false;
    }

    private DiscriminatorNode<I, O> reDiscriminate(Word<I> discriminator, DiscriminatorNode<I, O> root) {
        MooreDiscriminatorNode<I, O> newDiscriminatorNode = new MooreDiscriminatorNode<>(
                root.parent, discriminator, this.outputAlphabet);

        O accessor = findAccessorToFather(root);
        if (accessor != null) {
            MooreDiscriminatorNode<I, O> parent = (MooreDiscriminatorNode<I, O>) root.parent;
            parent.children.put(accessor, root);
        }
        Map<O, DiscriminationNode<I, O>> children = split(discriminator, root);
        newDiscriminatorNode.children = children;
        for (O key : children.keySet()) {
            children.get(key).parent = newDiscriminatorNode;
        }
        return newDiscriminatorNode;
    }

    private void placeNewLeafInEmptyLeaf(Word<I> discriminator, EmptyDTLeaf<I, O> DTLeaf, TTTNode<I, O> state) {
        MooreDiscriminatorNode<I, O> discriminatorNode = new MooreDiscriminatorNode<>(
                DTLeaf.parent, discriminator, this.outputAlphabet);
        O newOutput = membershipCounter.membershipQuery(state.sequenceAccess.concat(discriminatorNode.getDiscriminator()));
        DTLeaf<I, O> newDTLeaf = new DTLeaf<>(discriminatorNode, state);
        discriminatorNode.children.put(newOutput, newDTLeaf);

        O accessor = findAccessorToFather(DTLeaf);
        ((MooreDiscriminatorNode<I, O>) DTLeaf.parent).children.put(accessor, discriminatorNode);
    }


    private Map<O, DiscriminationNode<I, O>> split(Word<I> discriminator, DiscriminationNode<I, O> rootNode) {
        if (rootNode instanceof MooreDiscriminatorNode)
            return split(discriminator, (MooreDiscriminatorNode<I, O>) rootNode);
        return split(discriminator, (DTLeaf<I, O>) rootNode);
    }

    private Map<O, DiscriminationNode<I, O>> split(Word<I> discriminator, MooreDiscriminatorNode<I, O> rootNode) {

        Map<O, Map<O, DiscriminationNode<I, O>>> childrenSplits = new HashMap<>();
        for (O key : rootNode.children.keySet())
            childrenSplits.put(key, split(discriminator, rootNode.children.get(key)));

        Map<O, DiscriminationNode<I, O>> splits = new HashMap<>();
        for (O key : rootNode.children.keySet()) {
            Map<O, DiscriminationNode<I, O>> keyChildren = new HashMap<>();
            for (O key2 : rootNode.children.keySet())
                keyChildren.put(key2, childrenSplits.get(key2).get(key));

            MooreDiscriminatorNode<I, O> keyNode = new MooreDiscriminatorNode<>(
                    rootNode.parent,
                    rootNode.getDiscriminator(),
                    keyChildren,
                    rootNode.isFinal()
            );

            for (O o : keyChildren.keySet())
                keyChildren.get(o).parent = keyNode;

            splits.put(key, keyNode);
        }
        return splits;
    }

    private Map<O, DiscriminationNode<I, O>> split(Word<I> discriminator, DTLeaf<I, O> rootNode) {
        //initialize children
        Map<O, DiscriminationNode<I, O>> children = new HashMap<>();
        for (O output : outputAlphabet)
            children.put(output, new EmptyDTLeaf<>(rootNode.parent));

        if (rootNode instanceof EmptyDTLeaf)
            return children;

        O output = membershipCounter.membershipQuery(rootNode.state.sequenceAccess.concat(discriminator));
        children.put(output, rootNode);
        return children;
    }
}