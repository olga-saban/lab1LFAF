import java.util.*;

public class CNFNormalizer {

    private Set<String> nonTerminals;
    private Set<String> terminals;
    private Map<String, List<List<String>>> productions;
    private String startSymbol;
    private int newSymbolCounter = 0;

    public CNFNormalizer(Set<String> nonTerminals, Set<String> terminals,
                         Map<String, List<List<String>>> productions, String startSymbol) {
        this.nonTerminals = new LinkedHashSet<>(nonTerminals);
        this.terminals = new LinkedHashSet<>(terminals);
        this.productions = deepCopy(productions);
        this.startSymbol = startSymbol;
    }

    private Map<String, List<List<String>>> deepCopy(Map<String, List<List<String>>> original) {
        Map<String, List<List<String>>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<List<String>>> e : original.entrySet()) {
            List<List<String>> rhsCopy = new ArrayList<>();
            for (List<String> rhs : e.getValue()) {
                rhsCopy.add(new ArrayList<>(rhs));
            }
            copy.put(e.getKey(), rhsCopy);
        }
        return copy;
    }

    private String newSymbol() {
        String name = "X" + newSymbolCounter++;
        nonTerminals.add(name);
        return name;
    }

    public void eliminateEpsilonProductions() {
        System.out.println("\n1. Eliminate ε-productions");

        Set<String> nullable = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, List<List<String>>> e : productions.entrySet()) {
                String lhs = e.getKey();
                if (!nullable.contains(lhs)) {
                    for (List<String> rhs : e.getValue()) {
                        if (rhs.isEmpty() || (rhs.size() == 1 && rhs.get(0).equals("ε"))) {
                            nullable.add(lhs);
                            changed = true;
                        } else if (nullable.containsAll(rhs)) {
                            nullable.add(lhs);
                            changed = true;
                        }
                    }
                }
            }
        }
        System.out.println("Nullable symbols: " + nullable);

        Map<String, List<List<String>>> newProductions = deepCopy(productions);
        for (String lhs : productions.keySet()) {
            List<List<String>> toAdd = new ArrayList<>();
            for (List<String> rhs : productions.get(lhs)) {
                List<List<String>> combinations = generateCombinations(rhs, nullable);
                for (List<String> combo : combinations) {
                    if (!combo.isEmpty() && !combo.equals(rhs) && !newProductions.get(lhs).contains(combo)) {
                        toAdd.add(combo);
                    }
                }
            }
            newProductions.get(lhs).addAll(toAdd);
        }

        for (String lhs : newProductions.keySet()) {
            newProductions.get(lhs).removeIf(rhs -> rhs.isEmpty() ||
                    (rhs.size() == 1 && rhs.get(0).equals("ε")));
        }

        productions = newProductions;
        System.out.println("After elimination:");
        printProductions();
    }

    private List<List<String>> generateCombinations(List<String> rhs, Set<String> nullable) {
        List<List<String>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (String symbol : rhs) {
            List<List<String>> newResult = new ArrayList<>();
            for (List<String> existing : result) {
                List<String> withSymbol = new ArrayList<>(existing);
                withSymbol.add(symbol);
                newResult.add(withSymbol);
                if (nullable.contains(symbol)) {
                    newResult.add(new ArrayList<>(existing));
                }
            }
            result = newResult;
        }
        return result;
    }

    public void eliminateRenamings() {
        System.out.println("\n2. Eliminate renamings (unit productions)");

        boolean changed = true;
        while (changed) {
            changed = false;
            Map<String, List<List<String>>> newProductions = deepCopy(productions);
            for (String lhs : productions.keySet()) {
                List<List<String>> toAdd = new ArrayList<>();
                List<List<String>> toRemove = new ArrayList<>();
                for (List<String> rhs : productions.get(lhs)) {
                    if (rhs.size() == 1 && nonTerminals.contains(rhs.get(0))) {
                        String target = rhs.get(0);
                        toRemove.add(rhs);
                        if (productions.containsKey(target)) {
                            for (List<String> targetRhs : productions.get(target)) {
                                if (!newProductions.get(lhs).contains(targetRhs) && !toAdd.contains(targetRhs)) {
                                    toAdd.add(new ArrayList<>(targetRhs));
                                }
                            }
                        }
                        changed = true;
                    }
                }
                newProductions.get(lhs).removeAll(toRemove);
                newProductions.get(lhs).addAll(toAdd);
            }
            productions = newProductions;
        }

        System.out.println("After elimination:");
        printProductions();
    }

    public void eliminateInaccessibleSymbols() {
        System.out.println("\n3. Eliminate inaccessible symbols");

        Set<String> accessible = new HashSet<>();
        accessible.add(startSymbol);
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<String> toAdd = new HashSet<>();
            for (String sym : accessible) {
                if (productions.containsKey(sym)) {
                    for (List<String> rhs : productions.get(sym)) {
                        for (String s : rhs) {
                            if (!accessible.contains(s)) {
                                toAdd.add(s);
                                changed = true;
                            }
                        }
                    }
                }
            }
            accessible.addAll(toAdd);
        }

        System.out.println("Accessible symbols: " + accessible);
        Set<String> inaccessible = new HashSet<>(nonTerminals);
        inaccessible.removeAll(accessible);
        System.out.println("Inaccessible (removed): " + inaccessible);

        nonTerminals.retainAll(accessible);
        productions.keySet().retainAll(accessible);

        System.out.println("After elimination:");
        printProductions();
    }

    public void eliminateNonProductiveSymbols() {
        System.out.println("\n4. Eliminate non-productive symbols");

        Set<String> productive = new HashSet<>(terminals);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, List<List<String>>> e : productions.entrySet()) {
                if (!productive.contains(e.getKey())) {
                    for (List<String> rhs : e.getValue()) {
                        if (productive.containsAll(rhs)) {
                            productive.add(e.getKey());
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }

        Set<String> nonProductive = new HashSet<>(nonTerminals);
        nonProductive.removeAll(productive);
        System.out.println("Productive symbols: " + productive);
        System.out.println("Non-productive (removed): " + nonProductive);

        nonTerminals.retainAll(productive);
        productions.keySet().retainAll(productive);
        for (String lhs : new ArrayList<>(productions.keySet())) {
            productions.get(lhs).removeIf(rhs -> {
                for (String sym : rhs) {
                    if (!productive.contains(sym)) return true;
                }
                return false;
            });
        }

        System.out.println("After elimination:");
        printProductions();
    }

    public void convertToCNF() {
        System.out.println("\n5. Convert to Chomsky Normal Form");

        Map<String, String> terminalMap = new HashMap<>();
        Map<String, List<List<String>>> newProductions = deepCopy(productions);

        for (String lhs : productions.keySet()) {
            List<List<String>> updatedRules = new ArrayList<>();
            for (List<String> rhs : productions.get(lhs)) {
                if (rhs.size() >= 2) {
                    List<String> newRhs = new ArrayList<>();
                    for (String sym : rhs) {
                        if (terminals.contains(sym)) {
                            if (!terminalMap.containsKey(sym)) {
                                String newNT = newSymbol();
                                terminalMap.put(sym, newNT);
                                newProductions.put(newNT, new ArrayList<>(
                                        Collections.singletonList(Collections.singletonList(sym))));
                            }
                            newRhs.add(terminalMap.get(sym));
                        } else {
                            newRhs.add(sym);
                        }
                    }
                    updatedRules.add(newRhs);
                } else {
                    updatedRules.add(new ArrayList<>(rhs));
                }
            }
            newProductions.put(lhs, updatedRules);
        }
        productions = newProductions;

        Map<String, List<List<String>>> finalProductions = new LinkedHashMap<>();
        for (String lhs : productions.keySet()) {
            finalProductions.put(lhs, new ArrayList<>());
        }

        for (String lhs : productions.keySet()) {
            for (List<String> rhs : productions.get(lhs)) {
                if (rhs.size() <= 2) {
                    finalProductions.get(lhs).add(new ArrayList<>(rhs));
                } else {
                    String current = lhs;
                    List<String> remaining = new ArrayList<>(rhs);
                    while (remaining.size() > 2) {
                        String newNT = newSymbol();
                        if (!finalProductions.containsKey(newNT)) {
                            finalProductions.put(newNT, new ArrayList<>());
                        }
                        List<String> newRule = new ArrayList<>();
                        newRule.add(remaining.get(0));
                        newRule.add(newNT);
                        if (!finalProductions.containsKey(current)) {
                            finalProductions.put(current, new ArrayList<>());
                        }
                        finalProductions.get(current).add(newRule);
                        remaining = remaining.subList(1, remaining.size());
                        current = newNT;
                    }
                    finalProductions.get(current).add(new ArrayList<>(remaining));
                }
            }
        }
        productions = finalProductions;
        nonTerminals.addAll(productions.keySet());

        System.out.println("After CNF conversion:");
        printProductions();
        System.out.println("\nGrammar is now in Chomsky Normal Form");
    }

    public void printProductions() {
        for (String lhs : productions.keySet()) {
            List<List<String>> rules = productions.get(lhs);
            if (rules.isEmpty()) continue;
            System.out.print("  " + lhs + " -> ");
            List<String> rhsStrings = new ArrayList<>();
            for (List<String> rhs : rules) {
                rhsStrings.add(String.join(" ", rhs));
            }
            System.out.println(String.join(" | ", rhsStrings));
        }
    }

    public void normalize() {
        System.out.println("CNF NORMALIZATION - VARIANT 24");
        System.out.println("Initial grammar G=(V_N, V_T, P, S)");
        System.out.println("V_N = " + nonTerminals);
        System.out.println("V_T = " + terminals);
        System.out.println("S = " + startSymbol);
        System.out.println("Initial productions:");
        printProductions();

        eliminateEpsilonProductions();
        eliminateRenamings();
        eliminateInaccessibleSymbols();
        eliminateNonProductiveSymbols();
        convertToCNF();

        System.out.println("FINAL CNF GRAMMAR");
        System.out.println("V_N = " + nonTerminals);
        System.out.println("V_T = " + terminals);
        System.out.println("S = " + startSymbol);
        printProductions();
    }

    public static CNFNormalizer createVariant24() {
        Set<String> vn = new LinkedHashSet<>(Arrays.asList("S", "A", "B", "C"));
        Set<String> vt = new LinkedHashSet<>(Arrays.asList("a", "d"));

        Map<String, List<List<String>>> p = new LinkedHashMap<>();

        p.put("S", Arrays.asList(
                Arrays.asList("d", "B"),
                Arrays.asList("A")
        ));
        p.put("A", Arrays.asList(
                Arrays.asList("d"),
                Arrays.asList("d", "S"),
                Arrays.asList("a", "B", "d", "A", "B")
        ));
        p.put("B", Arrays.asList(
                Arrays.asList("a"),
                Arrays.asList("d", "A"),
                Arrays.asList("A"),
                Collections.singletonList("ε")
        ));
        p.put("C", Arrays.asList(
                Arrays.asList("A", "a")
        ));

        return new CNFNormalizer(vn, vt, p, "S");
    }

    public static void main(String[] args) {
        CNFNormalizer normalizer = createVariant24();
        normalizer.normalize();
    }
}