package dfa.dynamicTTT.discriminationTree;

import dfa.TTT.discriminiationTree.DFADiscriminationTree;
import dfa.TTT.discriminiationTree.DFADiscriminatorNode;
import generic.TTT.TTTNode;
import generic.TTT.discriminationTree.*;
import generic.dynamicTTT.discriminationTree.DynamicDiscriminationTree;
import generic.modelLearning.MembershipCounter;
import moore.TTT.discriminiationTree.MooreDiscriminatorNode;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.ArrayList;
import java.util.List;

public class DFADynamicDiscriminationTree<I> extends DynamicDiscriminationTree<I, Boolean> {

    private final Alphabet<I> alphabet;

    public DFADynamicDiscriminationTree(MembershipCounter<I, Boolean> membershipCounter, Alphabet<I> alphabet
    ) {
        this.alphabet = alphabet;
        DT = createBaseDiscriminationTree(membershipCounter);
    }

    protected DFADiscriminationTree<I> createBaseDiscriminationTree(
            MembershipCounter<I, Boolean> membershipCounter
    ) {
        return new DFADiscriminationTree<>(membershipCounter);
    }

    @Override
    public void initialDiscriminationTree(DiscriminationTreeInterface<I, Boolean> outdatedDiscriminationTree) throws Exception {
        ((DFADiscriminatorNode<I>) DT.root).solidChild = copy(
                (DFADiscriminatorNode<I>) DT.root,
                ((DFADiscriminatorNode<I>) outdatedDiscriminationTree.getRoot()).solidChild
        );
        ((DFADiscriminatorNode<I>) DT.root).dashedChild = copy(
                (DFADiscriminatorNode<I>) DT.root,
                ((DFADiscriminatorNode<I>) outdatedDiscriminationTree.getRoot()).dashedChild
        );
    }

    /***
     * Copy all sub tree of a node if they are discriminator node!
     * and if a discriminator have a symbol out of alphabet we remove that symbol!
     */
    private DiscriminationNode<I, Boolean> copy(DFADiscriminatorNode<I> parent, DiscriminationNode<I, Boolean> DTNode) throws Exception {
        if (DTNode instanceof DTLeaf) {
            return new EmptyDTLeaf<>(parent);
        } else if (DTNode instanceof DFADiscriminatorNode) {
            Word<I> discriminator = ((DFADiscriminatorNode<I>) DTNode).getDiscriminator();
            Word<I> newDiscriminator = removeOutAlphabetSymbols(discriminator);
            DFADiscriminatorNode<I> copiedDTNode = new DFADiscriminatorNode<>(parent, newDiscriminator, false);
            copiedDTNode.dashedChild = copy(copiedDTNode, ((DFADiscriminatorNode<I>) DTNode).dashedChild);
            copiedDTNode.solidChild = copy(copiedDTNode, ((DFADiscriminatorNode<I>) DTNode).solidChild);
            return copiedDTNode;
        }
        throw new Exception("Your discrimination node is not discriminator nor leaf!");
    }

    /***
     * Remove all symbols of a word which are not in the alphabet
     */
    private Word<I> removeOutAlphabetSymbols(Word<I> word) {
        List<I> wordAsList = new ArrayList<>(word.asList());
        wordAsList.removeIf(symbol -> !alphabet.contains(symbol));
        WordBuilder<I> builder = new WordBuilder<>();
        for (I symbol : wordAsList)
            builder.append(symbol);
        return builder.toWord();
    }

    /***
     * Remove any discriminator in this DT which does not discriminate any state!
     */
    public void removeRedundantDiscriminators() {
        if ((((DFADiscriminatorNode<I>) DT.root)).solidChild instanceof DFADiscriminatorNode)
            removeRedundantDiscriminators((DiscriminatorNode<I, Boolean>) ((DFADiscriminatorNode<I>) DT.root).solidChild);
        if (((DFADiscriminatorNode<I>) DT.root).dashedChild instanceof DFADiscriminatorNode)
            removeRedundantDiscriminators((DFADiscriminatorNode<I>) ((DFADiscriminatorNode<I>) DT.root).dashedChild);
    }

    @Override
    public void removeNode(TTTNode<I, Boolean> node) {
        try {
            DTLeaf<I, Boolean> leaf = findLeaf(node.sequenceAccess);
            if(leaf == null)
                throw new Exception("the given word (" + node.sequenceAccess + ") is not valid in the discrimination Tree");
            Boolean isDashed = findAccessorToFather(leaf);
            if (isDashed)
                ((DFADiscriminatorNode<I>) (leaf.parent)).dashedChild = new EmptyDTLeaf<>(leaf.parent);
            else
                ((DFADiscriminatorNode<I>) (leaf.parent)).solidChild = new EmptyDTLeaf<>(leaf.parent);
        } catch (Exception ignored) {
        }
    }
}