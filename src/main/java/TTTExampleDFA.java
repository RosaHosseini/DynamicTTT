import TTT.TTT;
import data.ResultWriter;
import data.benchmarkReader.SULReader;
import data.utils.DFAConstants;
import dynamicTTT.DynamicTTT;
import modelLearning.EQMethod;
import modelLearning.ModelLearningInfo;
import modelLearning.Teacher;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static data.utils.DFAConstants.BASE_BENCHMARK_PATH;


public class TTTExampleDFA {
    public TTTExampleDFA() {
    }

    public static void main(String[] args) throws Exception {
        //TTT
        ResultWriter writer = new ResultWriter();
        List<ModelLearningInfo> results;

        String basePath = "results/data";
        String[] methods = {
                "/DFA_random_learnLib",
                "/DFA_change_tail_learnLib",
                "/DFA_remove_alphabet_learnLib",
                "/DFA_add_alphabet_learnLib",
                "/DFA_remove_state_learnLib",
                "/DFA_add_state_learnLib"
        };
        EQMethod eqMethod = EQMethod.WP_RAND;
        System.out.println(eqMethod);
//
//        for (String method : methods) {
//
//            results = test2(method, 5, eqMethod);
//            writer.toCSV(results, basePath + "/" + eqMethod + method + "/0005s_20a.csv");
//
//            results = test2(method, 10, eqMethod);
//            writer.toCSV(results, basePath + "/" + eqMethod + method + "/0010s_20a.csv");
////
//            results = test2(method, 50, eqMethod);
//            writer.toCSV(results, basePath + "/" + eqMethod + method + "/0050s_20a.csv");
//        }
    }


    private static CompactDFA<String> generateOutdatedModel() {
        String[] symbols = new String[]{"a", "b"};
        Alphabet<String> alphabet = Alphabets.fromArray(symbols);
        CompactDFA<String> dfa = new CompactDFA<>(alphabet);
        Integer q0 = dfa.addInitialState();
        Integer q1 = dfa.addState();
        Integer q2 = dfa.addState();
        dfa.addTransition(q0, "b", q0);
        dfa.addTransition(q1, "b", q1);
        dfa.addTransition(q2, "b", q2);
        dfa.addTransition(q0, "a", q1);
        dfa.addTransition(q1, "a", q2);
        dfa.addTransition(q2, "a", q0);
        dfa.setAccepting(q2, true);
        return dfa;

    }

    private static CompactDFA<String> generateUpdatedModel() {
        String[] symbols = new String[]{"a", "b"};
        Alphabet<String> alphabet = Alphabets.fromArray(symbols);
        CompactDFA<String> dfa = new CompactDFA<>(alphabet);
        Integer q0 = dfa.addInitialState();
        Integer q1 = dfa.addState();
        Integer q2 = dfa.addState();
        Integer q3 = dfa.addState();
        dfa.addTransition(q0, "b", q0);
        dfa.addTransition(q1, "b", q1);
        dfa.addTransition(q2, "b", q3);
        dfa.addTransition(q3, "b", q3);
        dfa.addTransition(q0, "a", q1);
        dfa.addTransition(q1, "a", q2);
        dfa.addTransition(q2, "a", q0);
        dfa.addTransition(q3, "a", q0);
        dfa.setAccepting(q3, true);
        return dfa;
    }

    public static void test() throws Exception {
        CompactDFA<String> dfa = generateOutdatedModel();
        Teacher<String> teacher = new Teacher<>(dfa, EQMethod.W);
        TTT<String> tttLearner = new TTT<>(teacher, dfa.getInputAlphabet());
        DFA<?, String> hypothesis = tttLearner.learn();
        if (hypothesis == null)
            throw new Exception("error in outdated ttt");
        System.out.println(teacher.getMQCount() + ", " + tttLearner.getEQCounter());
        System.out.println("-------------------------------------------------");

        CompactDFA<String> dfa2 = generateUpdatedModel();
        Teacher<String> teacher2 = new Teacher<>(dfa2, EQMethod.W);
        DynamicTTT<String> dynamicLearner = new DynamicTTT<>(teacher2, tttLearner.getSpanningTree(), tttLearner.getDiscriminationTree(), dfa2.getInputAlphabet());
        DFA<?, String> hypothesis2 = dynamicLearner.learn();
        if (hypothesis2 == null)
            throw new Exception("error in dynamic TTT");
        System.out.println(teacher2.getMQCount() + ", " + dynamicLearner.getEQCounter());
        System.out.println("-------------------------------------------------");


        Teacher<String> teacher3 = new Teacher<>(dfa2, EQMethod.W);
        TTT<String> ttt2 = new TTT<>(teacher3, dfa2.getInputAlphabet());
        DFA<?, String> hypothesis3 = ttt2.learn();
        if (hypothesis3 == null)
            throw new Exception("error in updated TTT");
        System.out.println(teacher3.getMQCount() + ", " + ttt2.getEQCounter());
        System.out.println("-------------------------------------------------");
    }

    public static List<ModelLearningInfo> test2(String method, int stateNum, EQMethod eqOption) {
        List<ModelLearningInfo> results = new ArrayList<>();

        int id = 0;

        out:
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                try {
                    id++;
                    String state = "/s_" + String.format("%04d", stateNum);
                    String p = "/p_" + String.format("%03d", i);
                    //TTT
                    String path = BASE_BENCHMARK_PATH + method + state + p + "/v_000.dot";
                    File f = new File(path);
                    System.out.println("TTT" + path);
                    CompactDFA<String> dfa = new SULReader().parseDFAFromDot(f);

                    TTT<String> tttLearner = new TTT<>(new Teacher<>(dfa, eqOption), dfa.getInputAlphabet());
                    DFA<?, String> hypothesis = tttLearner.learn();
                    if (hypothesis == null)
                        throw new Exception("what");
//                    Visualization.visualize(dfa, dfa.getInputAlphabet(), new DefaultVisualizationHelper<>());


                    //Dynamic TTT
                    path = BASE_BENCHMARK_PATH + method + state + p + "/v_" + String.format("%03d", j) + ".dot";
                    f = new File(path);
                    System.out.println("Dynamic TTT" + path);
                    dfa = new SULReader().parseDFAFromDot(f);
                    Teacher<String> teacher = new Teacher<>(dfa, eqOption);
                    teacher.mqCounter.getCount();
//                    Visualization.visualize(dfa, dfa.getInputAlphabet(), new DefaultVisualizationHelper<>());
//
                    DynamicTTT<String> dynamicTTTLearner = new DynamicTTT<>(teacher,
                            tttLearner.getSpanningTree(),
                            tttLearner.getDiscriminationTree(),
                            dfa.getInputAlphabet());
                    DFA<?, String> updatedHypothesis = dynamicTTTLearner.learn();
                    if (updatedHypothesis == null)
                        throw new Exception("what");
                    long dMQ = teacher.getMQCount();
                    long dEQ = dynamicTTTLearner.getEQCounter();
                    System.out.println(dEQ + ", " + dMQ);
                    results.add(new ModelLearningInfo(
                            dMQ, dEQ, stateNum, DFAConstants.ALPHABET_SIZE, j, "dynamicTTT", id)
                    );


                    //TTT
                    System.out.println("TTT2       " + path);
                    dfa = new SULReader().parseDFAFromDot(f);
                    teacher = new Teacher<>(dfa, eqOption);
                    teacher.mqCounter.getCount();
                    TTT<String> tttLearner2 = new TTT<>(teacher, dfa.getInputAlphabet());
                    DFA<?, String> hyp = tttLearner2.learn();
                    if (hyp == null)
                        throw new Exception("what");
                    long MQ = teacher.getMQCount();
                    long EQ = tttLearner2.getEQCounter();
                    System.out.println(EQ + ", " + MQ);
                    results.add(new ModelLearningInfo(
                            MQ, EQ, stateNum, DFAConstants.ALPHABET_SIZE, j, "TTT", id)
                    );
//                    //TTT learnlib
//                    path = BASE_BENCHMARK_PATH + method + state + p + "/v_00" + j + ".dot";
//                    f = new File(path);
//                    System.out.println("learnlib   " + path);
//                    dfa = new SULReader().parseDFAFromDot(f);
//                    teacher = new Teacher<>(dfa, eqOption);
//                    teacher.mqCounter.getCount();
//                    LearnLibTTT<String> tttLearner3 = new LearnLibTTT<>(teacher, dfa.getInputAlphabet());
//                    DFA<?, String> hyp2 = tttLearner3.learn();
//                    if (hyp2 == null)
//                        throw new Exception("what");
//                    System.out.println(teacher.getMQCount());
//                    System.out.println("-------------------------------------------------");
                } catch (FileNotFoundException e) {
                    continue out;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(BASE_BENCHMARK_PATH + method + "/s" + stateNum + "/p" + j + "/v_" + String.format("%03d", j) + ".dot");
                    continue out;
                }
            }
        }
        return results;
    }

}

