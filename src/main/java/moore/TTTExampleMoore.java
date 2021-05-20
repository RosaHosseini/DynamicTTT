package moore;

import generic.data.ResultWriter;
import generic.modelLearning.EQMethod;
import generic.modelLearning.ModelLearningInfo;
import moore.TTT.MooreTTT;
import moore.dynamicTTT.MooreDynamicTTT;
import moore.modelLearning.MooreTeacher;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.MutableMooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import java.util.List;


public class TTTExampleMoore {

    public TTTExampleMoore() {
    }

    public static void main(String[] args) throws Exception {
        //dfa.TTT
        test();
//        ResultWriter writer = new ResultWriter();
//        List<ModelLearningInfo> results;
//
//        String basePath = "results/dfa.data";
//        String[] methods = {
//                "/DFA_random_learnLib",
//                "/DFA_change_tail_learnLib",
//                "/DFA_remove_alphabet_learnLib",
//                "/DFA_add_alphabet_learnLib",
//                "/DFA_remove_state_learnLib",
//                "/DFA_add_state_learnLib",
////                "/test"
//        };
//        EQMethod eqMethod = EQMethod.WP;
//        for (String method : methods) {
//
//            results = test2(method, 5, eqMethod, false);
//            writer.toCSV(results, basePath + "/" + eqMethod + method + "/0005s_5a.csv");
//
//            results = test2(method, 10, eqMethod, false);
//            writer.toCSV(results, basePath + "/" + eqMethod + method + "/0010s_20a.csv");
////
//            results = test2(method, 50, eqMethod, false);
//            writer.toCSV(results, basePath + "/" + eqMethod + method + "/0050s_20a.csv");
//        }
    }


    private static Pair<CompactMoore<String, String>, Alphabet<String>> generateOutdatedModel() {
        String[] symbols = new String[]{"a", "b"};
        Alphabet<String> alphabet = Alphabets.fromArray(symbols);
        String[] outputSymbols = new String[]{"true", "false"};
        Alphabet<String> outputAlphabet = Alphabets.fromArray(outputSymbols);
        CompactMoore<String, String> moore = new CompactMoore<>(alphabet);
        Integer q0 = moore.addInitialState();
        Integer q1 = moore.addState();
        Integer q2 = moore.addState();
        moore.addTransition(q0, "b", q0);
        moore.addTransition(q1, "b", q1);
        moore.addTransition(q2, "b", q2);
        moore.addTransition(q0, "a", q1);
        moore.addTransition(q1, "a", q2);
        moore.addTransition(q2, "a", q0);
        moore.setStateOutput(q0, "false");
        moore.setStateOutput(q1, "false");
        moore.setStateOutput(q2, "true");
        return Pair.of(moore, outputAlphabet);

    }

    private static Pair<CompactMoore<String, String>, Alphabet<String>> generateUpdatedModel() {
        String[] symbols = new String[]{"a", "b"};
        Alphabet<String> alphabet = Alphabets.fromArray(symbols);
        String[] outputSymbols = new String[]{"true", "false"};
        Alphabet<String> outputAlphabet = Alphabets.fromArray(outputSymbols);
        CompactMoore<String, String> moore = new CompactMoore<>(alphabet);
        Integer q0 = moore.addInitialState();
        Integer q1 = moore.addState();
        Integer q2 = moore.addState();
        Integer q3 = moore.addState();
        moore.addTransition(q0, "b", q0);
        moore.addTransition(q1, "b", q1);
        moore.addTransition(q2, "b", q3);
        moore.addTransition(q3, "b", q3);
        moore.addTransition(q0, "a", q1);
        moore.addTransition(q1, "a", q2);
        moore.addTransition(q2, "a", q0);
        moore.addTransition(q3, "a", q0);
        moore.setStateOutput(q0,"false");
        moore.setStateOutput(q1,"false");
        moore.setStateOutput(q2, "false");
        moore.setStateOutput(q3, "true");
        return Pair.of(moore, outputAlphabet);
    }

    public static void test() throws Exception {
        boolean withMemory = true;

        Pair<CompactMoore<String, String>, Alphabet<String>> moore = generateOutdatedModel();
        MooreTeacher<String, String> teacher = new MooreTeacher<>(moore.getFirst(), EQMethod.W, withMemory);
        MooreTTT<String, String> tttLearner = new MooreTTT<>(teacher, moore.getFirst().getInputAlphabet(), moore.getSecond());
        MutableMooreMachine<Integer, String, Integer, String> hypothesis = tttLearner.learn();
        if (hypothesis == null)
            throw new Exception("error in outdated ttt");
        System.out.println(teacher.getMQCount() + ", " + tttLearner.getEQCounter());
        System.out.println("-------------------------------------------------");

        Pair<CompactMoore<String, String>, Alphabet<String>> moore2 = generateUpdatedModel();
        MooreTeacher<String, String> teacher2 = new MooreTeacher<>(moore2.getFirst(), EQMethod.W, withMemory);
        MooreDynamicTTT<String, String> dynamicLearner = new MooreDynamicTTT<>(
                teacher2,
                tttLearner.getSpanningTree(),
                tttLearner.getDiscriminationTree(),
                moore2.getFirst().getInputAlphabet(),
                new CompactMoore<>(moore2.getFirst().getInputAlphabet()),
                moore2.getSecond(),
                false
        );
        MutableMooreMachine<Integer, String, Integer, String> hypothesis2 = dynamicLearner.learn();
        if (hypothesis2 == null)
            throw new Exception("error in dynamic dfa.TTT");
        System.out.println(teacher2.getMQCount() + ", " + dynamicLearner.getEQCounter());
        System.out.println("-------------------------------------------------");


        MooreTeacher<String,String> teacher3 = new MooreTeacher<>(moore2.getFirst(), EQMethod.W, withMemory);
        MooreTTT<String,String> ttt2 = new MooreTTT<>(teacher3, moore2.getFirst().getInputAlphabet(), moore2.getSecond());
        MutableMooreMachine<Integer, String, Integer, String> hypothesis3 = ttt2.learn();
        if (hypothesis3 == null)
            throw new Exception("error in updated dfa.TTT");
        System.out.println(teacher3.getMQCount() + ", " + ttt2.getEQCounter());
        System.out.println("-------------------------------------------------");
    }


}

