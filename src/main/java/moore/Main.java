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

//        results = run(OPEN_SSL_CLIENT_MAP, eqMethod, false,
//                "./benchmarks/moore/Nordsec16/client_version_info.csv"
//        );
//        writer.toCSV(results, basePath + "/" + eqMethod + "/OPEN_SSL_CLIENT.csv");
//
//        results = run( OPEN_SSL_SERVER_MAP, eqMethod, false,
//                "./benchmarks/moore/Nordsec16/server_version_info.csv");
//        writer.toCSV(results, basePath + "/" + eqMethod + "/OPEN_SSL_SERVER.csv");

        results = run(TCP_MAP, eqMethod, false, null);
        writer.toCSV(results, basePath + "/" + eqMethod + "/TCP.csv");

    }

    public static List<ModelLearningInfo> run(Map<String, String> versionMapper, EQMethod eqOption, Boolean visualize, String versionFile) {
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

        for (String updatedPath : versionMapper.keySet()) {
            try {
                String outdatedPath = versionMapper.get(updatedPath);
                int distance = 1;
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
                System.out.println("error in: " + BASE_BENCHMARK_PATH + updatedPath);
            }
        }
        return results;
    }

    private static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}

