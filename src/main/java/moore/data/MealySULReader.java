package moore.data;

import generic.data.SULReader;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MealySULReader implements SULReader<CompactMealy<String, String>> {

    public CompactMealy<String, String> parseModelFromDot(File file) throws Exception {
        final String OMEGA_SYMBOL = "Ω";

        Pattern pattern = Pattern.compile("\\s*([a-zA-Z0-9]+)\\s+->\\s+([a-zA-Z0-9]+)\\s*\\[label=[\"<](.+)[\">]\\];?");

        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String[]> transitions = new ArrayList<>();
        HashSet<String> abcSet = new HashSet<>();

        while (reader.ready()) {
            String line = reader.readLine();
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                String[] tr = new String[4];
                tr[0] = m.group(1);
                tr[1] = m.group(3);
                tr[3] = m.group(2);
                if (tr[1].contains("<br />")) {
                    String[] trr = tr[1].split("<br />");
                    tr[1] = trr[0];
                    tr[2] = trr[1];
                    trr = tr[1].split(" \\| ");
                    for (String string : trr) {
                        String[] trrr = new String[4];
                        trrr[0] = tr[0];
                        trrr[1] = string;
                        trrr[2] = tr[2];
                        trrr[3] = tr[3];
                        transitions.add(trrr);
                        abcSet.add(trrr[1]);
                    }
                } else {
                    String[] trr = tr[1].split("\\s*/\\s*");
                    tr[1] = trr[0];
                    tr[2] = trr[1];
                    transitions.add(tr);
                    abcSet.add(tr[1]);
                }
            }
        }

        reader.close();

        List<String> abc = new ArrayList<>(abcSet);
        Collections.sort(abc);
        Alphabet<String> alphabet = Alphabets.fromCollection(abc);
        CompactMealy<String, String> mealym = new CompactMealy<>(alphabet);

        Map<String, Integer> states = new HashMap<>();
        Integer si, sf;

        Map<String, Word<String>> words = new HashMap<>();


        WordBuilder<String> aux = new WordBuilder<>();

        aux.append(OMEGA_SYMBOL);
        words.put(OMEGA_SYMBOL, aux.toWord());


        for (String[] tr : transitions) {
            if (!states.containsKey(tr[0])) states.put(tr[0], mealym.addState());
            if (!states.containsKey(tr[3])) states.put(tr[3], mealym.addState());

            si = states.get(tr[0]);
            sf = states.get(tr[3]);

            if (!words.containsKey(tr[1])) {
                aux.clear();
                aux.add(tr[1]);
                words.put(tr[1], aux.toWord());
            }
            if (!words.containsKey(tr[2])) {
                aux.clear();
                aux.add(tr[2]);
                words.put(tr[2], aux.toWord());
            }
            mealym.addTransition(si, words.get(tr[1]).toString(), sf, words.get(tr[2]).toString());
        }

        for (Integer st : mealym.getStates()) {
            for (String in : alphabet) {
                if (mealym.getTransition(st, in) == null) {
                    mealym.addTransition(st, in, st, OMEGA_SYMBOL);
                }
            }
        }


        mealym.setInitialState(states.get("s0"));

        return mealym;
    }

    public static void main(String[] args) throws Exception {
        new MealySULReader().parseModelFromDot(new File("Mealy/Nordsec16/client_097.dot"));
    }
}
