package moore;

import dfa.data.utils.DFAConstants;
import generic.data.ResultWriter;
import generic.modelLearning.EQMethod;
import generic.modelLearning.ModelLearningInfo;
import moore.TTT.MooreTTT;
import moore.data.MooreSULReader;
import moore.dynamicTTT.MooreDynamicTTT;
import moore.modelLearning.MooreTeacher;
import net.automatalib.automata.transducers.MutableMooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.commons.util.Pair;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static moore.data.MooreConstants.*;


public class Main {

    public static void main(String[] args) throws Exception {

        ResultWriter writer = new ResultWriter();
        List<ModelLearningInfo> results;

        String basePath = "./results/moore/data";
        EQMethod eqMethod = EQMethod.WP;

        results = test2(OPEN_SSL_CLIENT, PREV_OPEN_SSL_CLIENT, eqMethod, false);
        writer.toCSV(results, basePath + "/" + eqMethod + "/OPEN_SSL_CLIENT.csv");

        results = test2(OPEN_SSL_SERVER, PREV_OPEN_SSL_SERVER, eqMethod, false);
        writer.toCSV(results, basePath + "/" + eqMethod + "/OPEN_SSL_SERVER.csv");

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
        moore.setStateOutput(q0, "false");
        moore.setStateOutput(q1, "false");
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


        MooreTeacher<String, String> teacher3 = new MooreTeacher<>(moore2.getFirst(), EQMethod.W, withMemory);
        MooreTTT<String, String> ttt2 = new MooreTTT<>(teacher3, moore2.getFirst().getInputAlphabet(), moore2.getSecond());
        MutableMooreMachine<Integer, String, Integer, String> hypothesis3 = ttt2.learn();
        if (hypothesis3 == null)
            throw new Exception("error in updated dfa.TTT");
        System.out.println(teacher3.getMQCount() + ", " + ttt2.getEQCounter());
        System.out.println("-------------------------------------------------");
    }


    public static List<ModelLearningInfo> test2(String[] benchmarks, String[] outdated_benchmarks, EQMethod eqOption, Boolean visualize) {
        List<ModelLearningInfo> results = new ArrayList<>();
        long start, end;
        String path;
        File f;
        Pair<CompactMoore<String, String>, Alphabet<String>> updateMoorePair, outdatedMoorePair;
        long MQ, EQ;
        MooreTeacher<String, String> teacher, outdatedTeacher;
        for (int i = 0; i < benchmarks.length; i++) {
            try {
//                outdated mealy TTT
                path = BASE_BENCHMARK_PATH + outdated_benchmarks[i];
                f = new File(path);
                System.out.println("Base TTT mealy for " + path);
                outdatedMoorePair = new MooreSULReader().parseModelFromDot(f);
//                Visualization.visualize(outdatedMoorePair.getFirst(), outdatedMoorePair.getFirst().getInputAlphabet(), new DefaultVisualizationHelper<>());
                outdatedTeacher = new MooreTeacher<>(outdatedMoorePair.getFirst(), eqOption, true);
                MooreTTT<String, String> outdatedTTTLearner = new MooreTTT<>(
                        outdatedTeacher,
                        outdatedMoorePair.getFirst().getInputAlphabet(),
                        outdatedMoorePair.getSecond()
                );
                MutableMooreMachine<Integer, String, Integer, String> hypothesis = outdatedTTTLearner.learn();
                if (hypothesis == null)
                    throw new Exception("what");
                if (visualize)
                    Visualization.visualize(
                            outdatedMoorePair.getFirst(),
                            outdatedMoorePair.getFirst().getInputAlphabet(),
                            new DefaultVisualizationHelper<>()
                    );
                MQ = outdatedTeacher.getMQCount();
                EQ = outdatedTTTLearner.getEQCounter();
                System.out.println(EQ + ", " + MQ);

                //Dynamic mealy TTT
                path = BASE_BENCHMARK_PATH + benchmarks[i];
                f = new File(path);
                System.out.println("Dynamic TTT mealy for " + path);
                updateMoorePair = new MooreSULReader().parseModelFromDot(f);
//                Visualization.visualize(updateMoorePair.getFirst(), updateMoorePair.getFirst().getInputAlphabet(), new DefaultVisualizationHelper<>());

                teacher = new MooreTeacher<>(updateMoorePair.getFirst(), eqOption, true);
                if (visualize)
                    Visualization.visualize(updateMoorePair.getFirst(), updateMoorePair.getFirst().getInputAlphabet(), new DefaultVisualizationHelper<>());
                MooreDynamicTTT<String, String> dynamicTTTLearner = new MooreDynamicTTT<>(
                        teacher,
                        outdatedTTTLearner.getSpanningTree(),
                        outdatedTTTLearner.getDiscriminationTree(),
                        updateMoorePair.getFirst().getInputAlphabet(),
                        new CompactMoore<>(updateMoorePair.getFirst().getInputAlphabet()),
                        updateMoorePair.getSecond(),
                        visualize
                );
                start = getCurrentTimestamp();
                MutableMooreMachine<Integer, String, Integer, String> updatedHypothesis = dynamicTTTLearner.learn();
                end = getCurrentTimestamp();
                if (updatedHypothesis == null)
                    throw new Exception("what");
                long dMQ = teacher.getMQCount();
                long dEQ = dynamicTTTLearner.getEQCounter();
                System.out.println(dEQ + ", " + dMQ);
                results.add(new ModelLearningInfo(
                        dMQ, dEQ, updateMoorePair.getFirst().getStates().size(), DFAConstants.ALPHABET_SIZE,
                        1, "mealy/dynamicTTT", benchmarks[i], end - start)
                );


                //mealy TTT
                System.out.println("TTT mealy       " + path);
                updateMoorePair = new MooreSULReader().parseModelFromDot(f);
                teacher = new MooreTeacher<>(updateMoorePair.getFirst(), eqOption, true);
                start = getCurrentTimestamp();
                MooreTTT<String, String> tttLearner = new MooreTTT<>(
                        teacher,
                        updateMoorePair.getFirst().getInputAlphabet(),
                        updateMoorePair.getSecond()
                );
                MutableMooreMachine<Integer, String, Integer, String> hyp = tttLearner.learn();
                end = getCurrentTimestamp();
                if (hyp == null)
                    throw new Exception("what");
                MQ = teacher.getMQCount();
                EQ = tttLearner.getEQCounter();
                System.out.println(EQ + ", " + MQ);
                results.add(new ModelLearningInfo(
                        MQ, EQ, updateMoorePair.getFirst().getStates().size(), DFAConstants.ALPHABET_SIZE,
                        1, "mealy/TTT", benchmarks[i], end - start)
                );
            } catch (FileNotFoundException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(BASE_BENCHMARK_PATH + benchmarks[i]);
            }
        }
        return results;
    }

    public static void testOpenSSLClient() throws Exception {
        boolean withMemory = true;

        Pair<CompactMoore<String, String>, Alphabet<String>> moore = new MooreSULReader().
                parseModelFromDot(new File("./benchmarks/moore/Nordsec16/client_098f.dot"));
        Visualization.visualize(
                moore.getFirst(),
                moore.getFirst().getInputAlphabet(),
                new DefaultVisualizationHelper<>()
        );
        MooreTeacher<String, String> teacher = new MooreTeacher<>(moore.getFirst(), EQMethod.WP, withMemory);
        MooreTTT<String, String> ttt = new MooreTTT<>(teacher, moore.getFirst().getInputAlphabet(), moore.getSecond());
        MutableMooreMachine<Integer, String, Integer, String> hypothesis = ttt.learn();
        if (hypothesis == null)
            throw new Exception("error in open ssl client 0.9.7 mealy TTT");
        Visualization.visualize(hypothesis, moore.getFirst().getInputAlphabet(), new DefaultVisualizationHelper<>());

        System.out.println("MQ and EQ:");
        System.out.println(teacher.getMQCount() + ", " + ttt.getEQCounter());
    }


    public static void testToyModel() throws Exception {
        boolean withMemory = true;

        Pair<CompactMoore<String, String>, Alphabet<String>> moore = new MooreSULReader().
                parseModelFromDot(new File(BASE_BENCHMARK_PATH + "/Toy.dot"));

        MooreTeacher<String, String> teacher = new MooreTeacher<>(moore.getFirst(), EQMethod.WP, withMemory);
        MooreTTT<String, String> ttt = new MooreTTT<>(teacher, moore.getFirst().getInputAlphabet(), moore.getSecond());
        MutableMooreMachine<Integer, String, Integer, String> hypothesis = ttt.learn();
        if (hypothesis == null)
            throw new Exception("error in toy mealy TTT");
        Visualization.visualize(hypothesis, moore.getFirst().getInputAlphabet(), new DefaultVisualizationHelper<>());

        System.out.println("MQ and EQ:");
        System.out.println(teacher.getMQCount() + ", " + ttt.getEQCounter());
    }


    private static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}

