package generic.dynamicTTT.discriminationTree;

import generic.TTT.discriminationTree.DiscriminationTree;
import generic.TTT.discriminationTree.DiscriminatorNode;
import generic.modelLearning.MembershipCounter;

abstract public class DynamicDiscriminationTree<I, O> extends DiscriminationTree<I, O> {

    public DynamicDiscriminationTree(MembershipCounter<I, O> membershipCounter, DiscriminatorNode<I, O> root) {
        super(membershipCounter, root);
    }

    /***
     * Initialize this discrimination Tree by removing all leaves of @param outdatedDiscriminationTree.
     */
    public abstract void initialDiscriminationTree(DiscriminationTree<I, O> outdatedDiscriminationTree) throws Exception;


    /***
     * Remove any discriminator in this DT which does not discriminate any state!
     */
    public abstract void removeRedundantDiscriminators();
}