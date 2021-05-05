package data.benchmarkReader;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.DefaultDOTVisualizationHelper;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static data.utils.DFAConstants.BASE_BENCHMARK_PATH;

public class SULReader {

    public  void read() throws Exception {
        String path = BASE_BENCHMARK_PATH + "/DFA_random_learnLib" + "/s_0005/p_000/v_000.dot";
//        for (int i = 0; i < 10; i++) {
//            String file_dir = path + "v_00" + i + ".dot";
//
//            new DOTMutableAutomatonParser<>()
//        }    }
        File f = new File(path);
        CompactDFA<String> dfa = parseDFAFromDot(f);
        Visualization.visualize(dfa, dfa.getInputAlphabet(), new DefaultDOTVisualizationHelper<>());

    }

    public CompactDFA<String> parseDFAFromDot(File file) throws Exception {
        Pattern statePattern = Pattern.compile("\\s*([a-zA-Z0-9]+)\\s+\\[shape=[\"<](.+)[\">]\\s+label=[\"<](.+)[\">]\\];?");
        Pattern relationPattern = Pattern.compile("\\s*([a-zA-Z0-9]+)\\s+->\\s+([a-zA-Z0-9]+)\\s*\\[label=[\"<](.+)[\">]\\];?");

        BufferedReader reader = new BufferedReader(new FileReader(file));

        List<String[]> transitions = new ArrayList<>();
        HashSet<String> alphabetSet = new HashSet<>();
        HashMap<String, Boolean> statesStatus = new HashMap<>();
        HashMap<String, Integer> statesId = new HashMap<>();
        String initialState = null;

        while (reader.ready()) {
            String line = reader.readLine();
            Matcher relationMatcher = relationPattern.matcher(line);
            Matcher stateMatcher = statePattern.matcher(line);
            if (stateMatcher.matches()) {
                String state = stateMatcher.group(1);

                if (initialState == null)
                    initialState = state;

                boolean accepting = false;
                if (stateMatcher.group((2)).equals("doublecircle"))
                    accepting = true;
                statesStatus.put(state, accepting);
            }
            if (relationMatcher.matches()) {
                String input = relationMatcher.group(3);
                String originState = relationMatcher.group(1);
                String destState = relationMatcher.group(2);

                transitions.add(new String[]{originState, input, destState});
                alphabetSet.add(input);
            }
        }
        reader.close();

        Alphabet<String> alphabet = Alphabets.fromCollection(alphabetSet);
        CompactDFA<String> dfa = new CompactDFA<>(alphabet);
        for (String state : statesStatus.keySet()) {
            boolean accepting = statesStatus.get(state);
            int id;
            if (state.equals(initialState))
                id= dfa.addInitialState(accepting);
            else
                id = dfa.addState(accepting);
            statesId.put(state, id);
        }

        for (String[] transition : transitions) {
            int originStateId = statesId.get(transition[0]);
            int destStateId = statesId.get(transition[2]);
            String symbol = transition[1];
            dfa.addTransition(originStateId, symbol, destStateId);
        }

        return dfa;
    }


    public void saveDFAAsKiss(CompactDFA<String> dfa, File f) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        List<Integer> states = new ArrayList<>(dfa.getStates());
        states.removeAll(dfa.getInitialStates());
        states.addAll(0, dfa.getInitialStates());
        for (Integer si : states) {
            for (String in : dfa.getInputAlphabet()) {
                int sj = dfa.getTransition(si, in);
                boolean out = dfa.isAccepting(sj);
                bw.append(String.format("%d -- %s / %s -> %d\n", si, in, out, sj));
            }
        }
        bw.close();
    }
}
