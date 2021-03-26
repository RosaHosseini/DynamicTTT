package dynamicTTT.discriminationTree;

import TTT.discriminiationTree.*;
import modelLearning.MembershipCounter;

public class DynamicDiscriminationTree<I> extends DiscriminationTree<I> {
    public DynamicDiscriminationTree(MembershipCounter<I> membershipCounter) {
        super(membershipCounter);
    }

    /***
     * Initialize this discrimination Tree by removing all leaves of @param outdatedDiscriminationTree.
     */
    public void initialDiscriminationTree(DiscriminationTree<I> outdatedDiscriminationTree) throws Exception {
        root.solidChild = copy(root, outdatedDiscriminationTree.root.solidChild);
        root.dashedChild = copy(root, outdatedDiscriminationTree.root.dashedChild);
    }

    /***
     * Copy all sub tree of a node if they are discriminator node!
     */
    private DiscriminationNode<I> copy(DiscriminatorNode<I> parent, DiscriminationNode<I> DTNode) throws Exception {
        if (DTNode instanceof DTLeaf) {
            return new EmptyDTLeaf<>(parent);
        } else if (DTNode instanceof DiscriminatorNode) {
            DiscriminatorNode<I> copiedDTNode = new DiscriminatorNode<>(
                    parent,
                    ((DiscriminatorNode<I>) DTNode).getDiscriminator(),
                    false
            );
            copiedDTNode.dashedChild = copy(copiedDTNode, ((DiscriminatorNode<I>) DTNode).dashedChild);
            copiedDTNode.solidChild = copy(copiedDTNode, ((DiscriminatorNode<I>) DTNode).solidChild);
            return copiedDTNode;
        }
        throw new Exception("your discrimination node is not discriminator nor leaf!");
    }

    /***
     * Remove any discriminator in this DT which does not discriminate any state!
     */
    public void removeRedundantDiscriminators() {
        if (root.solidChild instanceof DiscriminatorNode)
            removeRedundantDiscriminators((DiscriminatorNode<I>) root.solidChild);
        if (root.dashedChild instanceof DiscriminatorNode)
            removeRedundantDiscriminators((DiscriminatorNode<I>) root.dashedChild);
    }

    /***
     * Remove any discriminator in sub-tree of @param DTNode which does not discriminate any state!
     */
    private void removeRedundantDiscriminators(DiscriminatorNode<I> DTNode) {
        DiscriminatorNode<I> parent = DTNode.parent;
        if (DTNode.dashedChild instanceof EmptyDTLeaf) {
            if (DTNode.solidChild instanceof EmptyDTLeaf) {
                if (parent.isDashChild(DTNode))
                    parent.dashedChild = new EmptyDTLeaf<>(parent);
                else parent.solidChild = new EmptyDTLeaf<>(parent);
            } else if (DTNode.solidChild instanceof DiscriminatorNode) {
                DTNode.solidChild.parent = parent;
                if (parent.isDashChild(DTNode))
                    parent.dashedChild = DTNode.solidChild;
                else parent.solidChild = DTNode.solidChild;
                removeRedundantDiscriminators((DiscriminatorNode<I>) DTNode.solidChild);
            }
        } else if (DTNode.dashedChild instanceof DiscriminatorNode) {
            if (DTNode.solidChild instanceof EmptyDTLeaf) {
                DTNode.dashedChild.parent = parent;
                if (parent.isDashChild(DTNode))
                    parent.dashedChild = DTNode.dashedChild;
                else parent.solidChild = DTNode.dashedChild;
            } else if (DTNode.solidChild instanceof DiscriminatorNode) {
                removeRedundantDiscriminators((DiscriminatorNode<I>) DTNode.solidChild);
            }
            removeRedundantDiscriminators((DiscriminatorNode<I>) DTNode.dashedChild);

        } else if (DTNode.solidChild instanceof DiscriminatorNode) {
            removeRedundantDiscriminators((DiscriminatorNode<I>) DTNode.solidChild);
        }
    }

}

