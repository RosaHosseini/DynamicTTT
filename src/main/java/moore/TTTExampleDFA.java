package moore;

import moore.TTT.MooreTTT;
import moore.data.benchmarkReader.SULReader;
import moore.data.utils.DFAConstants;
import moore.dynamicTTT.DynamicTTT;
import moore.modelLearning.MooreTeacher;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static dfa.data.utils.DFAConstants.BASE_BENCHMARK_PATH;


public class TTTExampleDFA {
    public TTTExampleDFA() {
    }

    public static void main(String[] args) throws Exception {
        //dfa.TTT
        ResultWriter writer = new ResultWriter();
        List<MooreModelLearningInfo> results;

        String basePath = "results/dfa.data";
        String[] methods = {
                "/DFA_random_learnLib",
                "/DFA_change_tail_learnLib",
                "/DFA_remove_alphabet_learnLib",
                "/DFA_add_alphabet_learnLib",
                "/DFA_remove_state_learnLib",
                "/DFA_add_state_learnLib",
//                "/test"
        };
        EQMethod eqMethod = EQMethod.WP;
        for (String method : methods) {

            results = test2(method, 5, eqMethod, false);
            writer.toCSV(results, basePath + "/" + eqMethod + method + "/0005s_5a.csv");

            results = test2(method, 10, eqMethod, false);
            writer.toCSV(results, basePath + "/" + eqMethod + method + "/0010s_20a.csv");
//
            results = test2(method, 50, eqMethod, false);
            writer.toCSV(results, basePath + "/" + eqMethod + method + "/0050s_20a.csv");
        }
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
        MooreTeacher<String> teacher = new MooreTeacher<>(dfa, EQMethod.W);
        MooreTTT<String> tttLearner = new MooreTTT<>(teacher, dfa.getInputAlphabet());
        DFA<?, String> hypothesis = tttLearner.learn();
        if (hypothesis == null)
            throw new Exception("error in outdated ttt");
        System.out.println(teacher.getMQCount() + ", " + tttLearner.getEQCounter());
        System.out.println("-------------------------------------------------");

        CompactDFA<String> dfa2 = generateUpdatedModel();
        MooreTeacher<String> teacher2 = new MooreTeacher<>(dfa2, EQMethod.W);
        DynamicTTT<String> dynamicLearner = new DynamicTTT<>(teacher2, tttLearner.getSpanningTree(), tttLearner.getDiscriminationTree(), dfa2.getInputAlphabet(), false);
        DFA<?, String> hypothesis2 = dynamicLearner.learn();
        if (hypothesis2 == null)
            throw new Exception("error in dynamic dfa.TTT");
        System.out.println(teacher2.getMQCount() + ", " + dynamicLearner.getEQCounter());
        System.out.println("-------------------------------------------------");


        MooreTeacher<String> teacher3 = new MooreTeacher<>(dfa2, EQMethod.W);
        MooreTTT<String> ttt2 = new MooreTTT<>(teacher3, dfa2.getInputAlphabet());
        DFA<?, String> hypothesis3 = ttt2.learn();
        if (hypothesis3 == null)
            throw new Exception("error in updated dfa.TTT");
        System.out.println(teacher3.getMQCount() + ", " + ttt2.getEQCounter());
        System.out.println("-------------------------------------------------");
    }

    public static List<MooreModelLearningInfo> test2(String method, int stateNum, EQMethod eqOption, Boolean visualize) {
        List<MooreModelLearningInfo> results = new ArrayList<>();

        int id = 0;

        out:
        for (int i = 0; i < 20; i++) {
            for (int j = 1; j < 10; j++) {
                try {
                    id++;
                    String state = "/s_" + String.format("%04d", stateNum);
                    String p = "/p_" + String.format("%03d", i);
                    //dfa.TTT
                    String path = BASE_BENCHMARK_PATH + method + state + p + "/v_000.dot";
                    File f = new File(path);
                    System.out.println("dfa/TTT" + path);
                    CompactDFA<String> dfa = new SULReader().parseDFAFromDot(f);

                    MooreTTT<String> tttLearner = new MooreTTT<>(new MooreTeacher<>(dfa, eqOption), dfa.getInputAlphabet());
                    DFA<?, String> hypothesis = tttLearner.learn();
                    if (hypothesis == null)
                        throw new Exception("what");
                    if (visualize)
                        Visualization.visualize(dfa, dfa.getInputAlphabet(), new DefaultVisualizationHelper<>());


                    //Dynamic dfa.TTT
                    path = BASE_BENCHMARK_PATH + method + state + p + "/v_" + String.format("%03d", j) + ".dot";
                    f = new File(path);
                    System.out.println("Dynamic dfa.TTT" + path);
                    dfa = new SULReader().parseDFAFromDot(f);
                    MooreTeacher<String> teacher = new MooreTeacher<>(dfa, eqOption);
                    teacher.mqOracle.getCount();
                    if (visualize)
                        Visualization.visualize(dfa, dfa.getInputAlphabet(), new DefaultVisualizationHelper<>());
                    DynamicTTT<String> dynamicTTTLearner = new DynamicTTT<>(teacher,
                            tttLearner.getSpanningTree(),
                            tttLearner.getDiscriminationTree(),
                            dfa.getInputAlphabet(),
                            visualize
                    );
                    DFA<?, String> updatedHypothesis = dynamicTTTLearner.learn();
                    if (updatedHypothesis == null)
                        throw new Exception("what");
                    long dMQ = teacher.getMQCount();
                    long dEQ = dynamicTTTLearner.getEQCounter();
                    System.out.println(dEQ + ", " + dMQ);
                    results.add(new MooreModelLearningInfo(
                            dMQ, dEQ, stateNum, DFAConstants.ALPHABET_SIZE, j, "dfa/dynamicTTT", id)
                    );


                    //dfa.TTT
                    System.out.println("TTT2       " + path);
                    dfa = new SULReader().parseDFAFromDot(f);
                    teacher = new MooreTeacher<>(dfa, eqOption);
                    teacher.mqOracle.getCount();
                    MooreTTT<String> tttLearner2 = new MooreTTT<>(teacher, dfa.getInputAlphabet());
                    DFA<?, String> hyp = tttLearner2.learn();
                    if (hyp == null)
                        throw new Exception("what");
                    long MQ = teacher.getMQCount();
                    long EQ = tttLearner2.getEQCounter();
                    System.out.println(EQ + ", " + MQ);
                    results.add(new MooreModelLearningInfo(
                            MQ, EQ, stateNum, DFAConstants.ALPHABET_SIZE, j, "dfa/TTT", id)
                    );
//                    //dfa.TTT learnlib
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
                    System.out.println(BASE_BENCHMARK_PATH + method + "/s" + stateNum + "/p" + i + "/v_" + String.format("%03d", j) + ".dot");
                    continue out;
                }
            }
        }
        return results;
    }

}

