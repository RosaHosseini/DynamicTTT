package data.benchmarkGenerator;

import data.utils.DFAModelEditor;
import data.utils.DFAConstants;

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
                DFAConstants.BASE_BENCHMARK_PATH + "/DFA_add_state_learnLib");
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
                DFAConstants.BASE_BENCHMARK_PATH + "/DFA_remove_state_learnLib"
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
                DFAConstants.BASE_BENCHMARK_PATH + "/DFA_add_alphabet_learnLib"
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
                DFAConstants.BASE_BENCHMARK_PATH + "/DFA_remove_alphabet_learnLib"
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
                DFAConstants.BASE_BENCHMARK_PATH + "/DFA_change_tail_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {
        modelEditor.changeTail();
        return modelEditor;
    }
}


class SULGeneratorRandom extends SULGenerator {
    private final static Random rand = new Random(System.currentTimeMillis());

    SULGeneratorRandom() {
        super(DFAConstants.STATES_NUMS,
                DFAConstants.ALPHABET_SIZE,
                DFAConstants.NUM_DFA,
                DFAConstants.NUM_VERSION,
                DFAConstants.BASE_BENCHMARK_PATH + "/DFA_random_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {

        //generate a new random dfa
        int opt = rand.nextInt(4);
        switch (opt) {
            case 0: // add new state
                modelEditor.addState();
                break;
            case 4: // remove existing state
                if (modelEditor.getModel().getStates().size() < 2) {
                    return updateModel(modelEditor);
                } else {
                    modelEditor.removeState();
                }
                break;
            case 2: // add input symbol
                modelEditor.addAlphabet();
                break;
            case 3: // remove alphabet
                if (modelEditor.getModel().getInputAlphabet().size() < 2) {
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
        new SULGeneratorRandom().generate();
        new SULGeneratorAddState().generate();
        new SULGeneratorAddAlphabet().generate();
        new SULGeneratorChangeTail().generate();
        new SULGeneratorRemoveAlphabet().generate();
//        new DFAGeneratorRemoveState().generate();
    }
}