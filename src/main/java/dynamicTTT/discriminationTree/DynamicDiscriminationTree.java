package dynamicTTT.discriminationTree;

import TTT.discriminiationTree.*;
import modelLearning.MembershipCounter;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.ArrayList;
import java.util.List;

public class DynamicDiscriminationTree<I> extends DiscriminationTree<I> {
    private final Alphabet<I> alphabet;

    public DynamicDiscriminationTree(MembershipCounter<I> membershipCounter, Alphabet<I> alphabet) {
        super(membershipCounter);
        this.alphabet = alphabet;
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
     * and if a discriminator have a symbol out of alphabet we remove that symbol!
     */
    private DiscriminationNode<I> copy(DiscriminatorNode<I> parent, DiscriminationNode<I> DTNode) throws Exception {
        if (DTNode instanceof DTLeaf) {
            return new EmptyDTLeaf<>(parent);
        } else if (DTNode instanceof DiscriminatorNode) {
            Word<I> discriminator = ((DiscriminatorNode<I>) DTNode).getDiscriminator();
            Word<I> newDiscriminator = removeOutAlphabetSymbols(discriminator);
            DiscriminatorNode<I> copiedDTNode = new DiscriminatorNode<>(parent, newDiscriminator, false);
            copiedDTNode.dashedChild = copy(copiedDTNode, ((DiscriminatorNode<I>) DTNode).dashedChild);
            copiedDTNode.solidChild = copy(copiedDTNode, ((DiscriminatorNode<I>) DTNode).solidChild);
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
        if (root.solidChild instanceof DiscriminatorNode)
            removeRedundantDiscriminators((DiscriminatorNode<I>) root.solidChild);
        if (root.dashedChild instanceof DiscriminatorNode)
            removeRedundantDiscriminators((DiscriminatorNode<I>) root.dashedChild);
    }
}