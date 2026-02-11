import java.util.*;

class Grammar {

    private final Set<String> VN;
    private final Set<Character> VT;
    private final Map<String, List<String>> productions;
    private final String startSymbol;
    private final Random random;

    public Grammar() {
        VN = new HashSet<>(Arrays.asList("S", "A", "C", "D"));
        VT = new HashSet<>(Arrays.asList('a', 'b', 'd'));
        startSymbol = "S";
        random = new Random();

        productions = new HashMap<>();

        productions.put("S", Arrays.asList("aA"));
        productions.put("A", Arrays.asList("bS", "dD"));
        productions.put("D", Arrays.asList("bC", "aD"));
        productions.put("C", Arrays.asList("a", "bA"));
    }

    public String generateString() {
        StringBuilder result = new StringBuilder();
        String currentSymbol = startSymbol;

        while (VN.contains(currentSymbol)) {
            List<String> rules = productions.get(currentSymbol);
            String chosenRule = rules.get(random.nextInt(rules.size()));

            char terminal = chosenRule.charAt(0);
            result.append(terminal);

            if (chosenRule.length() == 2) {
                currentSymbol = String.valueOf(chosenRule.charAt(1));
            } else {
                break;
            }
        }

        return result.toString();
    }

    public FiniteAutomaton toFiniteAutomaton() {

        Set<String> states = new HashSet<>(VN);
        states.add("F");

        Set<String> finalStates = new HashSet<>();
        finalStates.add("F");

        Map<String, Map<Character, String>> transitionFunction = new HashMap<>();

        for (String nonTerminal : productions.keySet()) {
            for (String production : productions.get(nonTerminal)) {

                char terminal = production.charAt(0);

                transitionFunction
                        .computeIfAbsent(nonTerminal, k -> new HashMap<>());

                if (production.length() == 2) {
                    String nextState = String.valueOf(production.charAt(1));
                    transitionFunction.get(nonTerminal).put(terminal, nextState);
                } else {
                    transitionFunction.get(nonTerminal).put(terminal, "F");
                }
            }
        }

        return new FiniteAutomaton(
                states,
                VT,
                transitionFunction,
                startSymbol,
                finalStates
        );
    }
}


class FiniteAutomaton {

    private final Set<String> Q;
    private final Set<Character> Sigma;
    private final Map<String, Map<Character, String>> delta;
    private final String q0;
    private final Set<String> F;

    public FiniteAutomaton(Set<String> Q,
                           Set<Character> Sigma,
                           Map<String, Map<Character, String>> delta,
                           String q0,
                           Set<String> F) {
        this.Q = Q;
        this.Sigma = Sigma;
        this.delta = delta;
        this.q0 = q0;
        this.F = F;
    }

    public boolean stringBelongToLanguage(String input) {

        String currentState = q0;

        for (char symbol : input.toCharArray()) {

            if (!Sigma.contains(symbol))
                return false;

            if (!delta.containsKey(currentState) ||
                    !delta.get(currentState).containsKey(symbol))
                return false;

            currentState = delta.get(currentState).get(symbol);
        }

        return F.contains(currentState);
    }

    public void printTransitions() {
        System.out.println("\nFinite Automaton Transitions:");
        for (String state : delta.keySet()) {
            for (Character symbol : delta.get(state).keySet()) {
                System.out.println("δ(" + state + ", " + symbol + ") = "
                        + delta.get(state).get(symbol));
            }
        }
    }
}


public class Main {

    public static void main(String[] args) {

        Grammar grammar = new Grammar();

        System.out.println("Generated Strings:");
        for (int i = 0; i < 5; i++) {
            System.out.println((i + 1) + ": " + grammar.generateString());
        }

        FiniteAutomaton fa = grammar.toFiniteAutomaton();
        fa.printTransitions();

        Scanner scanner = new Scanner(System.in);

        System.out.println("\nEnter string to test:");
        String input = scanner.nextLine();

        if (fa.stringBelongToLanguage(input)) {
            System.out.println("String ACCEPTED.");
        } else {
            System.out.println("String REJECTED.");
        }

        scanner.close();
    }
}
