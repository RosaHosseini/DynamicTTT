package generic.dynamicTTT.discriminationTree;

import generic.TTT.TTTNode;
import generic.TTT.discriminationTree.*;
import generic.modelLearning.MembershipCounter;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public abstract class DynamicDiscriminationTree<I, O> implements DiscriminationTreeInterface<I,O> {


    protected DiscriminationTree<I, O> DT;


//    public DynamicDiscriminationTree(MembershipCounter<I, O> membershipCounter) {
//        DT = createBaseDiscriminationTree(membershipCounter);
//    }

    protected abstract DiscriminationTree<I, O> createBaseDiscriminationTree(
            MembershipCounter<I, O> membershipCounter
    );

    /***
     * Initialize this discrimination Tree by removing all leaves of @param outdatedDiscriminationTree.
     */
    public abstract void initialDiscriminationTree(DiscriminationTreeInterface<I, O> outdatedDiscriminationTree) throws Exception;


    /***
     * Remove any discriminator in this DT which does not discriminate any state!
     */
    public abstract void removeRedundantDiscriminators();


    @Override
    public DTLeaf<I, O> sift(Word<I> sequenceAccess) {
        return DT.sift(sequenceAccess);
    }

    @Override
    public boolean discriminate(Word<I> discriminator, TTTNode<I, O> state, TTTNode<I, O> from) {
        return DT.discriminate(discriminator, state, from);
    }

    @Override
    public boolean discriminate(TTTNode<I, O> state) {
        return false;
    }

    public boolean discriminate(TTTNode<I, O> state, DTLeaf<I, O> DTLeaf) {
        return DT.discriminate(state, DTLeaf);
    }

    public List<DiscriminatorNode<I, O>> findAllTemporaryDiscriminators() {
        return DT.findAllTemporaryDiscriminators();
    }

    @Override
    public Word<I> findDiscriminator(Word<I> word1, Word<I> word2) throws Exception {
        return DT.findDiscriminator(word1, word2);
    }

    @Override
    public DTLeaf<I, O> findLeaf(Word<I> word) throws Exception {
        return DT.findLeaf(word);
    }

    public DiscriminatorNode<I, O> findDiscriminatorNode(Word<I> word) throws Exception {
        return DT.findDiscriminatorNode(word);
    }

    public @Nullable DiscriminationNode<I, O> findLCA(DiscriminationNode<I, O> node, Word<I> word1, Word<I> word2) {
        return DT.findLCA(node, word1, word2);
    }

    public void removeRedundantDiscriminators(DiscriminatorNode<I, O> DTNode) {
        DT.removeRedundantDiscriminators(DTNode);
    }

    public O findAccessorToFather(DiscriminationNode<I, O> node) {
        return DT.findAccessorToFather(node);
    }

    public DiscriminatorNode<I, O> getRoot(){return DT.getRoot();}

    public abstract void removeNode(TTTNode<I,O> node) ;

    public void draw(){
        StringBuilder buffer = new StringBuilder();
        getRoot().print(buffer, "", "");
        System.out.println(buffer.toString());
    }

}