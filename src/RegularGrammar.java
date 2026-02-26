import java.util.*;

public class RegularGrammar {

    private Set<String> nonTerminals;
    private Set<String> terminals;
    private Map<String, List<String>> productions;
    private String startSymbol;

    public RegularGrammar() {
        productions = new HashMap<>();
    }

    public void setNonTerminals(Set<String> nonTerminals) {
        this.nonTerminals = nonTerminals;
    }

    public void setTerminals(Set<String> terminals) {
        this.terminals = terminals;
    }

    public void setStartSymbol(String startSymbol) {
        this.startSymbol = startSymbol;
    }

    public void addProduction(String left, String right) {
        productions.putIfAbsent(left, new ArrayList<>());
        productions.get(left).add(right);
    }

    public String classifyGrammar() {

        boolean isRegular = true;

        for (String left : productions.keySet()) {
            for (String right : productions.get(left)) {

                if (right.length() > 2) {
                    isRegular = false;
                }

                if (right.length() == 2) {
                    if (!Character.isLowerCase(right.charAt(0)) ||
                            !Character.isUpperCase(right.charAt(1))) {
                        isRegular = false;
                    }
                }

                if (right.length() == 1) {
                    if (!Character.isLowerCase(right.charAt(0))) {
                        isRegular = false;
                    }
                }
            }
        }

        if (isRegular)
            return "Type 3 — Regular Grammar";

        return "Type 2 — Context-Free Grammar";
    }

    public void printGrammar() {
        System.out.println("Productions:");
        for (String left : productions.keySet()) {
            System.out.print(left + " -> ");
            System.out.println(String.join(" | ", productions.get(left)));
        }
    }
}