package moore;

import generic.data.ResultWriter;
import generic.modelLearning.EQMethod;
import generic.modelLearning.ModelLearningInfo;
import moore.TTT.MooreTTT;
import moore.data.MooreSULReader;
import moore.data.VersionReader;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static moore.data.MooreConstants.*;


public class Main {

    public static void main(String[] args) throws Exception {

        ResultWriter writer = new ResultWriter();
        List<ModelLearningInfo> results;

        String basePath = "./results/moore/data";
        EQMethod eqMethod = EQMethod.WP;

//        results = test2(OPEN_SSL_CLIENT, OPEN_SSL_CLIENT_MAP, eqMethod, false,
//                "./benchmarks/moore/Nordsec16/client_version_info.csv"
//        );
//        writer.toCSV(results, basePath + "/" + eqMethod + "/OPEN_SSL_CLIENT.csv");
//
//        results = test2(OPEN_SSL_SERVER, OPEN_SSL_SERVER_MAP, eqMethod, false,
//                "./benchmarks/moore/Nordsec16/server_version_info.csv");
//        writer.toCSV(results, basePath + "/" + eqMethod + "/OPEN_SSL_SERVER.csv");

        results = test2(OPEN_SSL_SERVER, OPEN_SSL_SERVER_MAP, eqMethod, false, null);
        writer.toCSV(results, basePath + "/" + eqMethod + "/MQTT.csv");


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


    public static List<ModelLearningInfo> test2(String[] benchmarks, Map<String, String> versionMapper, EQMethod eqOption, Boolean visualize, String versionFile) {
        HashMap<String, String> versions = null;
        if (versionFile != null) {
            versions = new VersionReader().readVersions(versionFile);
        }
        List<ModelLearningInfo> results = new ArrayList<>();
        long start, end;
        File f;
        Pair<CompactMoore<String, String>, Alphabet<String>> updateMoorePair, outdatedMoorePair;
        long MQ, EQ;
        MooreTeacher<String, String> teacher, outdatedTeacher;

        for (String updatedPath : benchmarks) {
            try {
                String outdatedPath = versionMapper.get(updatedPath);
                int distance=1;
                if (versionFile != null) {
                    String outdatedVersionDate = versions.get(outdatedPath);
                    String updatedVersionDate = versions.get(updatedPath);

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date outdatedDate = dateFormatter.parse(outdatedVersionDate);
                    Date updatedDate = dateFormatter.parse(updatedVersionDate);


                    distance = (int) (((updatedDate.getTime() - outdatedDate.getTime()) / 1000) / (60.0 * 60 * 24));
                } 

//                outdated mealy TTT
                f = new File(BASE_BENCHMARK_PATH + outdatedPath);
                System.out.println("Base TTT mealy for " + outdatedPath);
                outdatedMoorePair = new MooreSULReader().parseModelFromDot(f);
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
                f = new File(BASE_BENCHMARK_PATH + updatedPath);
                System.out.println("Dynamic TTT mealy for " + updatedPath);
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
                        dMQ, dEQ, updateMoorePair.getFirst().getStates().size(),
                        updateMoorePair.getFirst().getInputAlphabet().size(),
                        distance, "mealy/dynamicTTT", updatedPath,
                        end - start));


                //mealy TTT
                System.out.println("TTT mealy       " + updatedPath);
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
                        MQ, EQ, updateMoorePair.getFirst().getStates().size(),
                        updateMoorePair.getFirst().getInputAlphabet().size(),
                        distance, "mealy/TTT", updatedPath,
                        end - start
                ));

            } catch (FileNotFoundException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(BASE_BENCHMARK_PATH + updatedPath);
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

