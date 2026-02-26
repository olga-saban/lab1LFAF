import java.util.*;

public class Main {
    public static void main(String[] args) {
        Set<String> states = Set.of("Q0", "Q1", "Q2");
        Set<String> alphabet = Set.of("a", "b");
        Set<String> finalStates = Set.of("Q2");

        FiniteAutomaton fa =
                new FiniteAutomaton(states, alphabet, "Q0", finalStates);

        fa.addTransition("Q0", "b", "Q0");
        fa.addTransition("Q0", "b", "Q1");
        fa.addTransition("Q1", "b", "Q2");
        fa.addTransition("Q0", "a", "Q0");
        fa.addTransition("Q1", "a", "Q1");
        fa.addTransition("Q2", "a", "Q2");

        System.out.println("Original FA:");
        fa.printTransitions();

        System.out.println("\nIs deterministic? " + fa.isDeterministic());

        System.out.println("\nRegular Grammar:");
        RegularGrammar grammar = fa.toRegularGrammar();
        grammar.printGrammar();

        System.out.println("\nGrammar Type:");
        System.out.println(grammar.classifyGrammar());

        System.out.println("\nConverted DFA:");
        FiniteAutomaton dfa = fa.convertToDFA();
        dfa.printTransitions();
    }
}