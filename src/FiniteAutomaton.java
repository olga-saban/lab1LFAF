import java.util.*;

public class FiniteAutomaton {

    private Set<String> states;
    private Set<String> alphabet;
    private Map<String, Map<String, Set<String>>> transitions;
    private String startState;
    private Set<String> finalStates;

    public FiniteAutomaton(Set<String> states,
                           Set<String> alphabet,
                           String startState,
                           Set<String> finalStates) {

        this.states = states;
        this.alphabet = alphabet;
        this.startState = startState;
        this.finalStates = finalStates;
        this.transitions = new HashMap<>();

        for (String state : states) {
            transitions.put(state, new HashMap<>());
        }
    }

    public void addTransition(String from, String symbol, String to) {
        transitions.putIfAbsent(from, new HashMap<>());
        transitions.get(from).putIfAbsent(symbol, new HashSet<>());
        transitions.get(from).get(symbol).add(to);
    }

    public boolean isDeterministic() {
        for (String state : transitions.keySet()) {
            for (String symbol : transitions.get(state).keySet()) {
                if (transitions.get(state).get(symbol).size() > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public RegularGrammar toRegularGrammar() {
        RegularGrammar grammar = new RegularGrammar();

        grammar.setNonTerminals(states);
        grammar.setTerminals(alphabet);
        grammar.setStartSymbol(startState);

        for (String state : transitions.keySet()) {
            for (String symbol : transitions.get(state).keySet()) {
                for (String nextState : transitions.get(state).get(symbol)) {

                    grammar.addProduction(state, symbol + nextState);

                    if (finalStates.contains(nextState)) {
                        grammar.addProduction(state, symbol);
                    }
                }
            }
        }

        return grammar;
    }

    public FiniteAutomaton convertToDFA() {

        Set<String> newStates = new HashSet<>();
        Map<String, Map<String, Set<String>>> newTransitions = new HashMap<>();
        Set<String> newFinalStates = new HashSet<>();

        Queue<Set<String>> queue = new LinkedList<>();
        Set<String> startSet = new HashSet<>();
        startSet.add(startState);

        queue.add(startSet);

        while (!queue.isEmpty()) {

            Set<String> currentSet = queue.poll();
            String currentName = currentSet.toString();

            newStates.add(currentName);
            newTransitions.putIfAbsent(currentName, new HashMap<>());

            for (String symbol : alphabet) {

                Set<String> newSet = new HashSet<>();

                for (String state : currentSet) {
                    if (transitions.containsKey(state)
                            && transitions.get(state).containsKey(symbol)) {
                        newSet.addAll(transitions.get(state).get(symbol));
                    }
                }

                if (!newSet.isEmpty()) {
                    String newName = newSet.toString();

                    newTransitions.get(currentName)
                            .put(symbol, Set.of(newName));

                    if (!newStates.contains(newName)) {
                        queue.add(newSet);
                    }
                }
            }

            for (String state : currentSet) {
                if (finalStates.contains(state)) {
                    newFinalStates.add(currentName);
                }
            }
        }

        FiniteAutomaton dfa =
                new FiniteAutomaton(newStates, alphabet,
                        startSet.toString(), newFinalStates);

        for (String from : newTransitions.keySet()) {
            for (String symbol : newTransitions.get(from).keySet()) {
                for (String to : newTransitions.get(from).get(symbol)) {
                    dfa.addTransition(from, symbol, to);
                }
            }
        }

        return dfa;
    }

    public void printTransitions() {
        for (String state : transitions.keySet()) {
            for (String symbol : transitions.get(state).keySet()) {
                for (String next : transitions.get(state).get(symbol)) {
                    System.out.println("δ(" + state + ", " + symbol + ") = " + next);
                }
            }
        }
    }
}