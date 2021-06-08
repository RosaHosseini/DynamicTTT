package moore.data;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Converter<I, O> {

    CompactMoore<I, O> convertMealyToMoore(CompactMealy<I, O> mealy) {
        Collection<Integer> states = mealy.getStates();
        HashMap<Integer, Integer> newStates = new HashMap<>();
        Alphabet<I> alphabet = mealy.getInputAlphabet();
        CompactMoore<I, O> moore = new CompactMoore<>(alphabet);
        for (Integer state : states) {
            ArrayList<CompactMealyTransition<O>> transitionsForState = getTransitionsForState(mealy, state);
            newStates.putAll(createNewStates(mealy, transitionsForState, moore));
        }

        for (Integer s : mealy.getStates()) {
            for (I input : mealy.getInputAlphabet()) {
                CompactMealyTransition<O> transition = mealy.getTransition(s, input);
                List<Integer> sources = findState(newStates, s);
                if (sources.size() == 0)
                    continue;
                for (Integer state : newStates.keySet()) {
                    if (moore.getStateOutput(state).equals(transition.getOutput())
                            && newStates.get(state).equals(transition.getSuccId())) {
                        for (Integer source : sources)
                            moore.addTransition(source, input, state);
                    }
                }
            }
        }

        return moore;
    }

    private HashMap<Integer, Integer> createNewStates(
            CompactMealy<I, O> mealy,
            ArrayList<CompactMealyTransition<O>> transitionsForState,
            CompactMoore<I, O> moore
    ) {
        HashMap<Integer, Integer> statesLabels = new HashMap<>();
        for (CompactMealyTransition<O> transition : transitionsForState) {
            boolean contains = false;
            for (Integer state : moore.getStates()) {
                if (moore.getStateOutput(state).equals(transition.getOutput())) {
                    contains = true;
                }
                if (transition.getSuccId() == mealy.getInitialState()) {
                    contains = true;
                }
            }

            if (!contains) {
                Integer state = moore.addState(transition.getOutput());
                if (transition.getSuccId() == mealy.getInitialState())
                    moore.setInitialState(state);
                statesLabels.put(state, transition.getSuccId());
            }
        }
        return statesLabels;
    }


    private ArrayList<CompactMealyTransition<O>> getTransitionsForState(CompactMealy<I, O> mealy, Integer state) {
        ArrayList<CompactMealyTransition<O>> transitionsForState = new ArrayList<>();
        for (Integer s : mealy.getStates()) {
            for (I input : mealy.getInputAlphabet()) {
                CompactMealyTransition<O> tr = mealy.getTransition(s, input);
                if (tr.getSuccId() == state)
                    transitionsForState.add(tr);
            }
        }
        return transitionsForState;
    }


    private List<CompactMealyTransition<O>> getTransitions(CompactMealy<I, O> mealy) {
        ArrayList<CompactMealyTransition<O>> tr = new ArrayList<>();
        for (Integer s : mealy.getStates()) {
            for (I input : mealy.getInputAlphabet()) {
                tr.add(mealy.getTransition(s, input));
            }
        }
        return tr;
    }

    private List<Integer> findState(HashMap<Integer, Integer> map, Integer oldState) {
        List<Integer> states = new ArrayList<>();
        for (Integer key : map.keySet())
            if (map.get(key).equals(oldState))
                states.add(key);
        return states;
    }

    public static void main(String[] args) throws Exception {
        CompactMealy<String, String> mealy =
                new MealySULReader().parseModelFromDot(new File("Mealy/Nordsec16/client_097.dot"));
        CompactMoore<String, String> moore = new Converter<String,String>().convertMealyToMoore(mealy);
        Visualization.visualize(
                moore,
                moore.getInputAlphabet(),
                new DefaultVisualizationHelper<>()
        );
    }


}
