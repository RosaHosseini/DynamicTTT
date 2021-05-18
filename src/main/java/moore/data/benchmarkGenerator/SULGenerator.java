package moore.data.benchmarkGenerator;

import moore.data.utils.DFAModelEditor;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Symbol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class to initialize our benchmark
 * Here we create the DFA of random SULs and new versions of them
 **/
public abstract class SULGenerator {
    private final String[] ALL_NUM_STATES;
    private final int ALPHABET_RANGE;
    private final int NUM_DFA;
    private final int NUM_VERSION;
    private final String PATH;

    /**
     * @param all_num_states is a list contains the number of states of generated DFAs (SULs)
     * @param alphabet_range shows the number of alphabets in generated DFAs (SULs)s
     * @param num_dfa        shows number of SULs.
     * @param num_version    shows the number of versions for changing each SUL.
     * @param path           is the path of saving result
     **/
    protected SULGenerator(String[] all_num_states, int alphabet_range, int num_dfa, int num_version, String path) {
        NUM_DFA = num_dfa;
        ALL_NUM_STATES = all_num_states;
        ALPHABET_RANGE = alphabet_range;
        NUM_VERSION = num_version;
        PATH = path;
    }

    /**
     * generate benchmark
     **/
    public void generate() {
        try {
            // load mealy machine

            File dir = new File(PATH);
            dir.mkdirs();
            Logger writer = new Logger(dir);

            for (String numStatesStr : ALL_NUM_STATES) {
                int numStates = Integer.parseInt(numStatesStr); //number of states
                //making directory
                File folder = new File(dir, "/s_" + String.format("%04d", numStates) + "/");
                folder.mkdirs();

                for (int i = 0; i < NUM_DFA; i++) {

                    //generate a dfa (old version)
                    DFAModelEditor modelEditor = new DFAModelEditor( numStates, ALPHABET_RANGE);

                    //save generated dfa
                    File childDir = new File(folder, "p_" + String.format("%03d", i) + "/v_000");
                    childDir.getParentFile().mkdirs();
                    saveDFA(childDir, modelEditor.getModel());
                    saveDot(childDir, modelEditor.getModel());


                    //generate updated versions
                    for (int version = 1; version < NUM_VERSION; version++) {
                        try {
                            modelEditor = updateModel(modelEditor);
                        } catch (Exception e) {
                            break;
                        }

                        writer.log(numStatesStr, i, version, modelEditor.getModel().getStates().size(), modelEditor.getLastOperation());

                        childDir = new File(folder, "p_" + String.format("%03d", i) + "/v_" + String.format("%03d", version));
                        saveDFA(childDir, modelEditor.getModel());
                        saveDot(childDir, modelEditor.getModel());
                    }
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * override this function to specify the change of the new version of SUL's DFA
     **/
    protected abstract DFAModelEditor updateModel(DFAModelEditor modelEditor) throws Exception;


    /**
     * save SUL with model of @param dfa in a dot file with path @param dir
     **/
    private void saveDot(File dir, CompactDFA<Symbol> dfa) throws IOException {
        // save sul in a dot file
        File sul_model = new File(dir.getParentFile(), dir.getName() + ".dot");
        FileWriter fw = new FileWriter(sul_model);
        Alphabet<Symbol> abc = ((InputAlphabetHolder<Symbol>) dfa).getInputAlphabet();
        GraphDOT.write(dfa, abc, fw);
    }

    /**
     * save the SUL with model of @param dfa in a fsm file with path @param dir
     **/
    private void saveDFA(File fsm, CompactDFA<Symbol> dfa) throws IOException {
        File sul_model = new File(fsm.getParentFile(), fsm.getName() + ".fsm");
        FileWriter fw = new FileWriter(sul_model);

        List<Integer> states = new ArrayList<>(dfa.getStates());
        states.remove(dfa.getIntInitialState());
        states.add(0, dfa.getIntInitialState());
        for (Integer state : states) {
            for (Symbol in : dfa.getInputAlphabet()) {
                Integer tr = dfa.getTransition(state, in);
                if (tr != null) {
                    fw.append(dfa.getState(state).toString());
                    fw.append(" -- ");
                    fw.append(tr.toString());
                    fw.append(" / ");
                    fw.append(((Boolean) dfa.isAccepting(tr)).toString());
                    fw.append(" -> ");
                    fw.append(dfa.getState(tr).toString());
                    fw.append("\n");
                }
            }
        }
        fw.close();
    }

    static class Logger {
        private final BufferedWriter bw;
        private boolean isClosed = false;


        public Logger(File dir) throws IOException {
            bw = new BufferedWriter(new FileWriter(new File(dir, "/models.tab")));
            bw.write("s_id|p_id|v_id|size|operator");
            bw.write("\n");
        }

        public void log(String numStatesStr, int i, int version, int updatedNumStates, String operation) throws IOException {
            if (isClosed)
                return;
            bw.write(numStatesStr + "|");
            bw.write(i + "|");
            bw.write(version + "|");
            bw.write(updatedNumStates + "|");
            bw.write(operation);
            bw.write("\n");
        }

        public void close() throws IOException {
            bw.close();
            isClosed = true;
        }
    }

}