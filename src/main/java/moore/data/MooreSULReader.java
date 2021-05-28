package moore.data;

import generic.data.SULReader;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MooreSULReader implements SULReader<Pair<CompactMoore<String, String>, Alphabet<String>>> {

    public Pair<CompactMoore<String, String>, Alphabet<String>> parseModelFromDot(File file) throws Exception {
        Pattern statePattern = Pattern.compile("\\s*([a-zA-Z0-9,_()+.]+)" +
                "\\s+\\[label=[\"<]\\{\\s([a-zA-Z0-9,_()+.]+)\\s\\|\\s([a-zA-Z0-9,_()+.]+)\\s\\}[\">]" +
                "\\s+shape=[\"<](.+)[\">]" +
                "\\s+style=[\"<](.+)[\">]\\]"
        );
        Pattern relationPattern = Pattern.compile("\\s*([a-zA-Z0-9,_()+.]+)\\s+->\\s+([a-zA-Z0-9,_()+.]+)" +
                "\\s*\\[label=[\"<]([a-zA-Z0-9,_()+.]+)[\">]\\]"
        );

        BufferedReader reader = new BufferedReader(new FileReader(file));

        List<String[]> transitions = new ArrayList<>();
        HashSet<String> inputAlphabetSet = new HashSet<>();
        HashSet<String> outputAlphabetSet = new HashSet<>();
        HashMap<String, String> statesStatus = new HashMap<>();
        HashMap<String, Integer> statesId = new HashMap<>();
        String initialState = null;

        while (reader.ready()) {
            String line = reader.readLine();
            Matcher relationMatcher = relationPattern.matcher(line);
            Matcher stateMatcher = statePattern.matcher(line);
            if (stateMatcher.matches()) {
                String state = stateMatcher.group(2);
                String output = stateMatcher.group(3);

                if (initialState == null)
                    initialState = state;
                statesStatus.put(state, output);
                outputAlphabetSet.add(output);
            } else if (relationMatcher.matches()) {
                String input = relationMatcher.group(3);
                String originState = relationMatcher.group(1);
                String destState = relationMatcher.group(2);

                transitions.add(new String[]{originState, input, destState});
                inputAlphabetSet.add(input);
            } else {
//                System.out.println(line);
            }
        }
        reader.close();

        Alphabet<String> alphabet = Alphabets.fromCollection(inputAlphabetSet);
        Alphabet<String> outputAlphabet = Alphabets.fromCollection(outputAlphabetSet);
        CompactMoore<String, String> moore = new CompactMoore<>(alphabet);
        for (String state : statesStatus.keySet()) {
            String output = statesStatus.get(state);
            int id;
            if (state.equals(initialState))
                id = moore.addInitialState(output);
            else
                id = moore.addState(output);
            statesId.put(state, id);
        }

        for (String[] transition : transitions) {
            int originStateId = statesId.get(transition[0]);
            int destStateId = statesId.get(transition[2]);
            String symbol = transition[1];
            moore.addTransition(originStateId, symbol, destStateId);
        }

        return Pair.of(moore, outputAlphabet);
    }
}


