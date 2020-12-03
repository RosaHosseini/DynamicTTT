package data.benchmarkGenerator;

import java.io.*;
import java.util.*;

import data.DFAModelEditor;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.*;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

public abstract class DFAGenerator {
    private final String[] ALL_NUM_STATES;
    private final int ALPHABET_RANGE;
    private final int NUM_DFA;
    private final int NUM_VERSION;
    private final String PATH;

    protected DFAGenerator(String[] all_num_states, int alphabet_range, int num_dfa, int num_version, String path) {
        NUM_DFA = num_dfa;
        ALL_NUM_STATES = all_num_states;
        ALPHABET_RANGE = alphabet_range;
        NUM_VERSION = num_version;
        PATH = path;
        execute();
    }

    protected void execute() {
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

                    Alphabet<Symbol> alphabets = generateAlphabet(); //alphabet set

                    Random rand = new Random(1234);
                    //generate a dfa (old version)
                    CompactDFA<Symbol> modelDFA = RandomAutomata.randomICDFA(rand, numStates, alphabets, true);
                    DFAModelEditor modelEditor = new DFAModelEditor(modelDFA);

                    //save generated dfa
                    File childDir = new File(folder, "p_" + String.format("%03d", i) + "/v_000");
                    childDir.getParentFile().mkdirs();
                    saveDFA(childDir, modelEditor.model);
                    saveDot(childDir, modelEditor.model);


                    //generate updated versions
                    for (int version = 0; version < NUM_VERSION; version++) {
                        modelEditor = updateModel(modelEditor);

                        writer.log(numStatesStr, i, version, modelEditor.model.getStates().size(), modelEditor.lastOperation);

                        childDir = new File(folder, "p_" + String.format("%03d", i) + "/v_" + String.format("%03d", version));
                        saveDFA(childDir, modelEditor.model);
                        saveDot(childDir, modelEditor.model);
                    }
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract DFAModelEditor updateModel(DFAModelEditor modelEditor) throws Exception;

    private FastAlphabet<Symbol> generateAlphabet() {
        List<Symbol> outCol = new ArrayList<>();
        for (int i = 0; i < ALPHABET_RANGE; i++) {
            outCol.add(new Symbol(i));
        }
        return new FastAlphabet<>(outCol);
    }

    private void saveDot(File dir, CompactDFA<Symbol> dfa) throws IOException {
        // save sul in a dot file
        File sul_model = new File(dir.getParentFile(), dir.getName() + ".dot");
        FileWriter fw = new FileWriter(sul_model);
        Alphabet<Symbol> abc = ((InputAlphabetHolder<Symbol>) dfa).getInputAlphabet();
        GraphDOT.write(dfa, abc, fw);
    }


    private void saveDFA(File fsm, CompactDFA<Symbol> dfa) throws IOException {
        // save sul as in a fsm file (i.e., mealyss)
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


}

class Logger {
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




