package dfa.data.utils;

import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.minimizer.hopcroft.HopcroftMinimization;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.*;



/**
 * Here I get a DFA or generate it
 * and you can change that DFA by calling my function
 **/

public class DFAModelEditor {
    private CompactDFA<Symbol> model;
    private String lastOperation = "initial-model";

    private final Random rand = new Random(System.currentTimeMillis());

    public DFAModelEditor(CompactDFA<Symbol> dfa) {
        this.model = dfa;
    }

    /**
     * @param numStates     The number of states of the DFA model
     * @param alphabetSize  The size of alphabet of the DFA model
     * Here the DFAModelEditor class initialize its model with a random DFA
     */
    public DFAModelEditor(int numStates, int alphabetSize){
        model = generateRandomDFA(alphabetSize, numStates);
    }

    public CompactDFA<Symbol> getModel() {
        return model;
    }

    public String getLastOperation() {
        return lastOperation;
    }

    /**
     * add a new random state to the dfa of model
     **/
    public void addState() {
        // alphabets
        Alphabet<Symbol> alphabets = model.getInputAlphabet();
        // copy the model
        FastDFA<Symbol> temp_model = new FastDFA<>(alphabets);
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, model, alphabets, temp_model);

        List<FastDFAState> qSet = new ArrayList<>(temp_model.getStates());

        // select random transition (i.e, origin state + input)
        FastDFAState qi = qSet.get(rand.nextInt(qSet.size()));
        Symbol in = alphabets.getSymbol(rand.nextInt(alphabets.size()));

        // remove 'tr'
        temp_model.removeAllTransitions(qi, in);

        FastDFAState newState = temp_model.addState();
        for (Symbol symbol : alphabets) {
            if (symbol.equals(in)) {
                temp_model.addTransition(qi, symbol, newState);
            }
            FastDFAState new_tr = qSet.get(rand.nextInt(qSet.size()));
            temp_model.addTransition(newState, symbol, new_tr);
        }

        model = HopcroftMinimization.minimizeDFA(temp_model, temp_model.getInputAlphabet(), HopcroftMinimization.PruningMode.PRUNE_AFTER);
        lastOperation = "op_addState";

    }

    /**
     * remove a random state from the dfa of model
     **/
    public void removeState() {
        // get alphabet
        Alphabet<Symbol> alphabets = model.getInputAlphabet();

        //copy model to copy_model
        FastDFA<Symbol> copy_model = new FastDFA<>(alphabets);
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, model, alphabets, copy_model);

        //put states of our model to a set to use it later
        ArrayList<FastDFAState> qSet = new ArrayList<>(copy_model.getStates());

        //removing a random state from our set
        qSet.remove(copy_model.getInitialState());
        Collections.shuffle(qSet);
        FastDFAState removedState = qSet.remove(0);
        qSet.add(copy_model.getInitialState());

        // copy the FSM
        FastDFA<Symbol> temp_dfa = new FastDFA<>(alphabets);

        // map of stateId -> FastDFAState ---- for temp_dfa
        HashMap<Integer, FastDFAState> statesMap = new HashMap<>();
        statesMap.put(Objects.requireNonNull(copy_model.getInitialState()).getId(), temp_dfa.addInitialState());

        for (FastDFAState qi : qSet) {
            // create qi, if missing
            if (!statesMap.containsKey(qi.getId())) {
                statesMap.put(qi.getId(), temp_dfa.addState(qi.isAccepting()));
            }
            // get qi
            FastDFAState state_qi = statesMap.get(qi.getId());

            // iterate for each alphabet symbol
            for (Symbol in : model.getInputAlphabet()) {
                //find the in-accessor of state qi and save it in tr
                @Nullable FastDFAState tr = copy_model.getTransition(qi, in);
                assert tr != null;
                // if tr is the removed state we set qj as  qi[in.in] else tr
                FastDFAState qj = tr;
                if (tr.equals(removedState)) {
                    qj = nextStateToKeep(copy_model, qi, in, removedState);
                }
                assert qj != null;
                // if qj is a new state in temp_dfa we put in the set
                if (!statesMap.containsKey(qj.getId()))
                    statesMap.put(qj.getId(), temp_dfa.addState(qj.isAccepting()));

                // set the qj as the in-accessor of qi in temp_dfa
                FastDFAState state_qj = statesMap.get(qj.getId());
                temp_dfa.addTransition(state_qi, in, state_qj);
            }
        }
        //reset the model with temp_dfa
        model = HopcroftMinimization.minimizeDFA(temp_dfa, temp_dfa.getInputAlphabet(), HopcroftMinimization.PruningMode.PRUNE_AFTER);
        lastOperation = "op_removeState";
    }

    public void addAlphabet() {
        // inputs
        Alphabet<Symbol> alphabets = model.getInputAlphabet();
        int max = 0;
        for (Symbol in : alphabets) {
            assert (in.getUserObject() != null);
            int val = (int) in.getUserObject();
            if (max < val) max = val;
        }
        max++;
        Symbol newSymbol = new Symbol(max);


        // copy the FSM
        ArrayList<Symbol> alphabet_list = new ArrayList<>(alphabets);
        FastDFA<Symbol> temp_model = new FastDFA<>(new FastAlphabet<>(alphabet_list));
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, model, alphabets, temp_model);

        temp_model.addAlphabetSymbol(newSymbol);

        ArrayList<FastDFAState> qSet = new ArrayList<>(temp_model.getStates());
        for (FastDFAState si : qSet) {
            FastDFAState sj = qSet.get(rand.nextInt(qSet.size()));
            temp_model.addTransition(si, newSymbol, sj);
        }

        model = HopcroftMinimization.minimizeDFA(temp_model, temp_model.getInputAlphabet(), HopcroftMinimization.PruningMode.PRUNE_AFTER);
        lastOperation = "op_addAlphabet";
    }

    public void removeAlphabet() {

        // remove a subset of all inputs
        ArrayList<Symbol> abcCol = new ArrayList<>(model.getInputAlphabet().size());
        abcCol.addAll(model.getInputAlphabet());

        // shuffle to remove 'percent2Rm'% inputs
        Collections.shuffle(abcCol);
        abcCol.remove(0);

        // remainder inputs
        Alphabet<Symbol> alphabets = Alphabets.fromCollection(abcCol);

        // copy the FSM by excluding a part of the input set
        model = HopcroftMinimization.minimizeDFA(model, alphabets, HopcroftMinimization.PruningMode.PRUNE_AFTER);
        lastOperation = "op_removeAlphabet";

    }

    public void changeTail() {
        // inputs
        Alphabet<Symbol> alphabets = model.getInputAlphabet();
        // copy the FSM
        FastDFA<Symbol> temp_model = new FastDFA<>(alphabets);
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, model, alphabets, temp_model);

        ArrayList<FastDFAState> qSet = new ArrayList<>(temp_model.getStates());

        // select random transition (i.e, origin state + input)
        FastDFAState qi = qSet.get(rand.nextInt(qSet.size()));
        Symbol in = alphabets.getSymbol(rand.nextInt(alphabets.size()));
        // remove transition
        temp_model.removeAllTransitions(qi, in);

        FastDFAState qj = qSet.get(rand.nextInt(qSet.size()));
        temp_model.addTransition(qi, in, qj);

        model = HopcroftMinimization.minimizeDFA(temp_model, temp_model.getInputAlphabet(), HopcroftMinimization.PruningMode.PRUNE_AFTER);
        lastOperation = "op_changeTail";

    }

    private @Nullable FastDFAState nextStateToKeep(FastDFA<Symbol> model, FastDFAState stateId, Symbol inputIdx, FastDFAState state2Rm) {
        @Nullable FastDFAState tr = model.getTransition(stateId, inputIdx);
        if (tr != null && !tr.equals(state2Rm)) {
            return tr;
        }
        return stateId;
    }


    /**
     * @param size  The size of the alphabet
     * @return      an Alphabet with the given size of type Symbol
     */
    private FastAlphabet<Symbol> generateAlphabet(int size) {
        List<Symbol> outCol = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            outCol.add(new Symbol(i));
        }
        return new FastAlphabet<>(outCol);
    }

    /**
     * @param numStates     The number of states of the DFA
     * @param alphabetSize  The size of alphabet of the DFA
     * @return              A random DFA
     */
    public CompactDFA<Symbol> generateRandomDFA(int alphabetSize, int numStates){
        Alphabet<Symbol> alphabets = generateAlphabet(alphabetSize); //alphabet set
        return RandomAutomata.randomICDFA(rand, numStates, alphabets, true);
    }
}