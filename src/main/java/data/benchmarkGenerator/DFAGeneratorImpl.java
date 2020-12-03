package data.benchmarkGenerator;


import data.DFAModelEditor;

import java.util.Random;

class DFAGeneratorAddState extends DFAGenerator {

    DFAGeneratorAddState() {
        super(
                new String[]{"0005", "0010", "0050", "0100", "0250", "0500", "750", "1000"},
                10,
                20,
                10,
                "./benchmarks/DFA_add_state_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {
        modelEditor.addState();
        return modelEditor;
    }

}

class DFAGeneratorRemoveState extends DFAGenerator {

    DFAGeneratorRemoveState() {
        super(
                new String[]{"0005", "0010", "0050", "0100", "0250", "0500", "750", "1000"},
                10,
                20,
                10,
                "./benchmarks/DFA_remove_state_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) throws Exception {
        if (modelEditor.model.getStates().size() >= 2) {
            modelEditor.removeState();
            return modelEditor;
        } else throw new Exception("model cannot remove more state!");
    }

}

class DFAGeneratorAddAlphabet extends DFAGenerator {

    DFAGeneratorAddAlphabet() {
        super(
                new String[]{"0005", "0010", "0050", "0100", "0250", "0500", "750", "1000"},
                10,
                20,
                10,
                "./benchmarks/DFA_add_alphabet_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {
        modelEditor.addAlphabet();
        return modelEditor;
    }

}

class DFAGeneratorRemoveAlphabet extends DFAGenerator {

    DFAGeneratorRemoveAlphabet() {
        super(
                new String[]{"0005", "0010", "0050", "0100", "0250", "0500", "750", "1000"},
                10,
                20,
                10,
                "./benchmarks/DFA_remove_alphabet_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {
        if (modelEditor.model.getInputAlphabet().size() >= 2) {
            modelEditor.removeAlphabet();
        }
        return modelEditor;
    }


}

class DFAGeneratorChangeTail extends DFAGenerator {

    DFAGeneratorChangeTail() {
        super(
                new String[]{"0005", "0010", "0050", "0100", "0250", "0500", "750", "1000"},
                10,
                20,
                10,
                "./benchmarks/DFA_change_tail_learnLib"
        );
    }

    @Override
    protected DFAModelEditor updateModel(DFAModelEditor modelEditor) {
        modelEditor.changeTail();
        return modelEditor;
    }
}


class DFAGeneratorRandom extends DFAGenerator {
    private final static Random rand = new Random(System.currentTimeMillis());

    DFAGeneratorRandom() {
        super(
                new String[]{"0005", "0010", "0050", "0100", "0250", "0500", "750", "1000"},
                10,
                20,
                10,
                "./benchmarks/DFA_random_learnLib"
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
                if (modelEditor.model.getStates().size() < 2) {
                    return updateModel(modelEditor);
                } else {
                    modelEditor.removeState();
                }
                break;
            case 2: // add input symbol
                modelEditor.addAlphabet();
                break;
            case 3: // remove alphabet
                if (modelEditor.model.getInputAlphabet().size() < 2) {
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
        new DFAGeneratorRandom();
//        new DFAGeneratorAddState();
//        new DFAGeneratorAddAlphabet();
//        new DFAGeneratorChangeTail();
//        new DFAGeneratorRemoveAlphabet();
//        new DFAGeneratorRemoveState();
    }
}



