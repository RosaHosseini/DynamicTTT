package moore.dynamicTTT.discriminationTree;

import generic.TTT.discriminationTree.*;
import generic.dynamicTTT.discriminationTree.DynamicDiscriminationTree;
import generic.modelLearning.MembershipCounter;
import moore.TTT.discriminiationTree.MooreDiscriminationTree;
import moore.TTT.discriminiationTree.MooreDiscriminatorNode;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.ArrayList;
import java.util.List;

public class MooreDynamicDiscriminationTree<I, O> extends DynamicDiscriminationTree<I, O> {
    private final Alphabet<O> outputAlphabet;
    private final Alphabet<I> alphabet;

    public MooreDynamicDiscriminationTree(
            MembershipCounter<I, O> membershipCounter,
            Alphabet<O> outputAlphabet,
            Alphabet<I> alphabet
    ) {
        super();
        this.alphabet = alphabet;
        this.outputAlphabet = outputAlphabet;
        DT = createBaseDiscriminationTree(membershipCounter);
    }

    protected DiscriminationTree<I, O> createBaseDiscriminationTree(MembershipCounter<I, O> membershipCounter) {
        return new MooreDiscriminationTree<>(membershipCounter, outputAlphabet);
    }

    @Override
    public void initialDiscriminationTree(DiscriminationTreeInterface<I, O> outdatedDiscriminationTree) throws Exception {

        for (O key : outputAlphabet) {
            ((MooreDiscriminatorNode<I, O>) DT.root).children.put(
                    key,
                    copy(
                            (MooreDiscriminatorNode<I, O>) DT.root,
                            ((MooreDiscriminatorNode<I, O>) outdatedDiscriminationTree.getRoot()).children.get(key)
                    )
            );
        }
    }

    @Override
    public void removeRedundantDiscriminators() {
        for (O key: outputAlphabet){
            DiscriminationNode<I, O> child  =(((MooreDiscriminatorNode<I, O>) DT.root)).children.get(key);
            if (child instanceof  MooreDiscriminatorNode)
                removeRedundantDiscriminators((MooreDiscriminatorNode<I, O>)child);
        }
    }

    /***
     * Copy all sub tree of a node if they are discriminator node!
     * and if a discriminator have a symbol out of alphabet we remove that symbol!
     */
    private DiscriminationNode<I, O> copy(MooreDiscriminatorNode<I, O> parent, DiscriminationNode<I, O> DTNode) throws Exception {
        if (DTNode instanceof DTLeaf) {
            return new EmptyDTLeaf<>(parent);
        } else if (DTNode instanceof MooreDiscriminatorNode) {
            Word<I> discriminator = ((MooreDiscriminatorNode<I, O>) DTNode).getDiscriminator();
            Word<I> newDiscriminator = removeOutAlphabetSymbols(discriminator);
            MooreDiscriminatorNode<I, O> copiedDTNode = new MooreDiscriminatorNode<>(
                    parent, newDiscriminator, outputAlphabet, false
            );
            for (O key : outputAlphabet) {
                copiedDTNode.children.put(
                        key, copy(copiedDTNode, ((MooreDiscriminatorNode<I, O>) DTNode).children.get(key))
                );
            }
            return copiedDTNode;
        } else{
            return new EmptyDTLeaf<>(parent);
        }
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

}