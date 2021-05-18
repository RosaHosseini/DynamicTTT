package dfa.dynamicTTT.discriminationTree;

import dfa.TTT.discriminiationTree.*;
import generic.TTT.discriminationTree.DTLeaf;
import generic.TTT.discriminationTree.DiscriminationNode;
import generic.TTT.discriminationTree.DiscriminatorNode;
import generic.TTT.discriminationTree.EmptyDTLeaf;
import generic.modelLearning.MembershipCounter;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.ArrayList;
import java.util.List;

public class DFADynamicDiscriminationTree<I> extends DFADiscriminationTree<I> {
    private final Alphabet<I> alphabet;

    public DFADynamicDiscriminationTree(MembershipCounter<I, Boolean> membershipCounter, Alphabet<I> alphabet) {
        super(membershipCounter);
        this.alphabet = alphabet;
    }

    /***
     * Initialize this discrimination Tree by removing all leaves of @param outdatedDiscriminationTree.
     */
    public void initialDiscriminationTree(DFADiscriminationTree<I> outdatedDiscriminationTree) throws Exception {
        ((DFADiscriminatorNode<I>) root).solidChild = copy(
                (DFADiscriminatorNode<I>) root,
                ((DFADiscriminatorNode<I>) outdatedDiscriminationTree.root).solidChild
        );
        ((DFADiscriminatorNode<I>) root).dashedChild = copy(
                (DFADiscriminatorNode<I>) root,
                ((DFADiscriminatorNode<I>) outdatedDiscriminationTree.root).dashedChild
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
        if ((((DFADiscriminatorNode<I>) root)).solidChild instanceof DFADiscriminatorNode)
            removeRedundantDiscriminators((DiscriminatorNode<I, Boolean>) ((DFADiscriminatorNode<I>) root).solidChild);
        if (((DFADiscriminatorNode<I>) root).dashedChild instanceof DFADiscriminatorNode)
            removeRedundantDiscriminators((DFADiscriminatorNode<I>) ((DFADiscriminatorNode<I>) root).dashedChild);
    }
}