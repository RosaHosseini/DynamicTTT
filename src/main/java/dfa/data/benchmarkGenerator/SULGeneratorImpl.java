package dfa.data.benchmarkGenerator;

import dfa.data.utils.DFAModelEditor;
import dfa.data.utils.DFAConstants;

import java.util.Random;


/**
 * Implementation of SUL Generator
 **/

class SULGeneratorAddState extends SULGenerator {

    SULGeneratorAddState() {
        super(DFAConstants.STATES_NUMS,
                DFAConstants.ALPHABET_SIZE,
                DFAConstants.NUM_DFA,
                DFAConstants.NUM_VERSION,
                DFAConstants.BASE_BENCHMARK_PATH + "/add_state_learnLib");
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {
        modelEditor.addState();
        return modelEditor;
    }

}

class SULGeneratorRemoveState extends SULGenerator {

    SULGeneratorRemoveState() {
        super(DFAConstants.STATES_NUMS,
                DFAConstants.ALPHABET_SIZE,
                DFAConstants.NUM_DFA,
                DFAConstants.NUM_VERSION,
                DFAConstants.BASE_BENCHMARK_PATH + "/remove_state_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) throws Exception {
        if (modelEditor.getModel().getStates().size() >= 2) {
            modelEditor.removeState();
            return modelEditor;
        } else throw new Exception("model cannot remove more state!");
    }

}

class SULGeneratorAddAlphabet extends SULGenerator {

    SULGeneratorAddAlphabet() {
        super(DFAConstants.STATES_NUMS,
                DFAConstants.ALPHABET_SIZE,
                DFAConstants.NUM_DFA,
                DFAConstants.NUM_VERSION,
                DFAConstants.BASE_BENCHMARK_PATH + "/add_alphabet_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {
        modelEditor.addAlphabet();
        return modelEditor;
    }

}

class SULGeneratorRemoveAlphabet extends SULGenerator {

    SULGeneratorRemoveAlphabet() {
        super(DFAConstants.STATES_NUMS,
                DFAConstants.ALPHABET_SIZE,
                DFAConstants.NUM_DFA,
                DFAConstants.NUM_VERSION,
                DFAConstants.BASE_BENCHMARK_PATH + "/remove_alphabet_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) throws Exception {
        if (modelEditor.getModel().getInputAlphabet().size() >= 2) {
            modelEditor.removeAlphabet();
            return modelEditor;
        } else throw new Exception("The alphabet size is too low");
    }


}

class SULGeneratorChangeTail extends SULGenerator {

    SULGeneratorChangeTail() {
        super(DFAConstants.STATES_NUMS,
                DFAConstants.ALPHABET_SIZE,
                DFAConstants.NUM_DFA,
                DFAConstants.NUM_VERSION,
                DFAConstants.BASE_BENCHMARK_PATH + "/change_tail_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {
        modelEditor.changeTail();
        return modelEditor;
    }
}


class TestGenerator extends SULGenerator {
    private final static Random rand = new Random(System.currentTimeMillis());

    TestGenerator() {
        super(DFAConstants.STATES_NUMS2,
                DFAConstants.ALPHABET_SIZE2,
                DFAConstants.NUM_DFA2,
                DFAConstants.NUM_VERSION2,
                DFAConstants.BASE_BENCHMARK_PATH2 + "/test"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) throws Exception {
        //generate a new random dfa
        if (modelEditor.getModel().getStates().size() >= 2) {
            modelEditor.removeState();
            return modelEditor;
        } else throw new Exception("model cannot remove more state!");
    }

}

class SULGeneratorRandom extends SULGenerator {
    private final static Random rand = new Random(System.currentTimeMillis());

    SULGeneratorRandom() {
        super(DFAConstants.STATES_NUMS,
                DFAConstants.ALPHABET_SIZE,
                DFAConstants.NUM_DFA,
                DFAConstants.NUM_VERSION,
                DFAConstants.BASE_BENCHMARK_PATH + "/random_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {

        //generate a new random dfa
        int opt = rand.nextInt(3);
        switch (opt) {
            case 0: // add new state
                modelEditor.addState();
                break;
            case 3: // remove existing state
                if (modelEditor.getModel().getStates().size() < 2) {
                    return updateModel(modelEditor);
                } else {
                    modelEditor.removeState();
                }
                break;
            case 2: // add input symbol
                modelEditor.addAlphabet();
                break;
            case 4: // remove alphabet
                if (modelEditor.getModel().getInputAlphabet().size() < 2 || modelEditor.getModel().getStates().size() < 2) {
                    return updateModel(modelEditor);
                } else {
                    modelEditor.removeAlphabet();
                }
                break;
            case 1: // change tail state
                modelEditor.changeTail();
                break;
        }
        return modelEditor;
    }

}


class Main {
    public static void main(String[] args) {
//        new SULGeneratorRandom().generate();
//        new SULGeneratorAddState().generate();
//        new SULGeneratorAddAlphabet().generate();
//        new SULGeneratorChangeTail().generate();
//        new SULGeneratorRemoveAlphabet().generate();
//        new SULGeneratorRemoveState().generate();
        new TestGenerator().generate();
    }
}