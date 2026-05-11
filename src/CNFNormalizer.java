import java.util.*;

public class CNFNormalizer {
    static final String EPSILON = "eps";

    private Set<String> vn;
    private Set<String> vt;
    private Map<String, List<List<String>>> productions;
    private String start;

    public CNFNormalizer(Set<String> vn, Set<String> vt, Map<String, List<List<String>>> productions, String start) {
        this.vn = new LinkedHashSet<>(vn);
        this.vt = new LinkedHashSet<>(vt);
        this.productions = deepCopy(productions);
        this.start = start;
    }

    private Map<String, List<List<String>>> deepCopy(Map<String, List<List<String>>> original) {
        Map<String, List<List<String>>> copy = new LinkedHashMap<>();
        for (String key : original.keySet()) {
            List<List<String>> rhsCopy = new ArrayList<>();
            for (List<String> rhs : original.get(key)) {
                rhsCopy.add(new ArrayList<>(rhs));
            }
            copy.put(key, rhsCopy);
        }
        return copy;
    }

    public void printGrammar() {
        System.out.println("V_N = " + new ArrayList<>(vn));
        System.out.println("V_T = " + new ArrayList<>(vt));
        System.out.println("P = {");
        for (String head : productions.keySet()) {
            System.out.print("  " + head + " -> ");
            List<String> parts = new ArrayList<>();
            for (List<String> rhs : productions.get(head)) {
                parts.add(rhs.isEmpty() ? EPSILON : String.join("", rhs));
            }
            System.out.println(parts.isEmpty() ? EPSILON : String.join(" | ", parts));
        }
        System.out.println("}");
    }

    // --- Step 1: Eliminate epsilon productions ---
    public CNFNormalizer eliminateEpsilon() {
        // Find all nullable nonterminals
        Set<String> nullable = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String head : productions.keySet()) {
                if (nullable.contains(head)) continue;
                for (List<String> rhs : productions.get(head)) {
                    if (rhs.isEmpty()) {
                        nullable.add(head);
                        changed = true;
                        break;
                    }
                    boolean allNullable = true;
                    for (String s : rhs) {
                        if (!nullable.contains(s)) { allNullable = false; break; }
                    }
                    if (allNullable) {
                        nullable.add(head);
                        changed = true;
                        break;
                    }
                }
            }
        }

        // Build new productions: expand all combinations of nullable symbols
        Map<String, List<List<String>>> newProductions = new LinkedHashMap<>();
        for (String nt : vn) newProductions.put(nt, new ArrayList<>());

        for (String head : productions.keySet()) {
            for (List<String> rhs : productions.get(head)) {
                if (rhs.isEmpty()) continue; // skip original epsilon rules
                Set<List<String>> expansions = nullableExpansions(rhs, nullable);
                for (List<String> option : expansions) {
                    // keep empty option only for start symbol
                    if (!option.isEmpty() || head.equals(start)) {
                        if (!newProductions.get(head).contains(option)) {
                            newProductions.get(head).add(option);
                        }
                    }
                }
            }
        }

        // If start was nullable, add epsilon for start
        if (nullable.contains(start)) {
            List<String> eps = new ArrayList<>();
            if (!newProductions.get(start).contains(eps)) {
                newProductions.get(start).add(eps);
            }
        }

        productions = newProductions;
        return this;
    }

    /**
     * Generate all subsets of rhs by optionally dropping nullable symbols.
     * Matches Python backtrack logic: try skipping nullable first, then including.
     */
    private Set<List<String>> nullableExpansions(List<String> rhs, Set<String> nullable) {
        Set<List<String>> results = new HashSet<>();
        backtrackNullable(rhs, nullable, 0, new ArrayList<>(), results);
        return results;
    }

    private void backtrackNullable(List<String> rhs, Set<String> nullable, int index,
                                   List<String> current, Set<List<String>> results) {
        if (index == rhs.size()) {
            results.add(new ArrayList<>(current));
            return;
        }
        String symbol = rhs.get(index);
        // Option 1: skip symbol if it's nullable
        if (nullable.contains(symbol)) {
            backtrackNullable(rhs, nullable, index + 1, current, results);
        }
        // Option 2: include symbol
        current.add(symbol);
        backtrackNullable(rhs, nullable, index + 1, current, results);
        current.remove(current.size() - 1);
    }

    // --- Step 2: Eliminate unit (renaming) productions ---
    public CNFNormalizer eliminateUnit() {
        // Compute unit-closure for each nonterminal
        Map<String, Set<String>> closure = new LinkedHashMap<>();
        for (String nt : vn) closure.put(nt, unitClosure(nt));

        Map<String, List<List<String>>> newProductions = new LinkedHashMap<>();
        for (String head : vn) {
            newProductions.put(head, new ArrayList<>());
            for (String target : closure.get(head)) {
                for (List<String> rhs : productions.getOrDefault(target, new ArrayList<>())) {
                    // Skip unit productions (A -> B where B is nonterminal)
                    if (rhs.size() == 1 && vn.contains(rhs.get(0))) continue;
                    if (!newProductions.get(head).contains(rhs)) {
                        newProductions.get(head).add(new ArrayList<>(rhs));
                    }
                }
            }
        }
        productions = newProductions;
        return this;
    }

    private Set<String> unitClosure(String startNt) {
        Set<String> closure = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        closure.add(startNt);
        stack.push(startNt);
        while (!stack.isEmpty()) {
            String head = stack.pop();
            for (List<String> rhs : productions.getOrDefault(head, new ArrayList<>())) {
                if (rhs.size() == 1 && vn.contains(rhs.get(0))) {
                    String target = rhs.get(0);
                    if (!closure.contains(target)) {
                        closure.add(target);
                        stack.push(target);
                    }
                }
            }
        }
        return closure;
    }

    // --- Step 3: Eliminate inaccessible symbols ---
    public CNFNormalizer eliminateInaccessible() {
        Set<String> reachable = new HashSet<>();
        reachable.add(start);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String head : new HashSet<>(reachable)) {
                for (List<String> rhs : productions.getOrDefault(head, new ArrayList<>())) {
                    for (String symbol : rhs) {
                        if (vn.contains(symbol) && !reachable.contains(symbol)) {
                            reachable.add(symbol);
                            changed = true;
                        }
                    }
                }
            }
        }
        vn.retainAll(reachable);
        productions.keySet().retainAll(reachable);
        return this;
    }

    // --- Step 4: Eliminate nonproductive symbols ---
    public CNFNormalizer eliminateNonproductive() {
        Set<String> productive = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String head : productions.keySet()) {
                if (productive.contains(head)) continue;
                for (List<String> rhs : productions.get(head)) {
                    boolean ok = true;
                    for (String s : rhs) {
                        if (!(vt.contains(s) || productive.contains(s))) { ok = false; break; }
                    }
                    if (ok) { productive.add(head); changed = true; break; }
                }
            }
        }
        vn.retainAll(productive);
        Map<String, List<List<String>>> newProductions = new LinkedHashMap<>();
        for (String head : vn) {
            List<List<String>> filtered = new ArrayList<>();
            for (List<String> rhs : productions.getOrDefault(head, new ArrayList<>())) {
                boolean ok = true;
                for (String s : rhs) {
                    if (!(vt.contains(s) || vn.contains(s))) { ok = false; break; }
                }
                if (ok) filtered.add(rhs);
            }
            newProductions.put(head, filtered);
        }
        productions = newProductions;
        return this;
    }

    // --- Step 5: Convert to CNF ---
    private int counter = 0;

    private String fresh(String prefix) {
        if (!vn.contains(prefix)) {
            vn.add(prefix);
            return prefix;
        }
        String candidate;
        do { candidate = prefix + (++counter); } while (vn.contains(candidate));
        vn.add(candidate);
        return candidate;
    }

    public CNFNormalizer toCNF() {
        // Phase 1: Replace terminals in long rules with dedicated nonterminals T_x -> x
        Map<String, String> terminalMap = new LinkedHashMap<>();
        Map<String, List<List<String>>> afterTerminals = new LinkedHashMap<>();
        for (String nt : vn) afterTerminals.put(nt, new ArrayList<>());

        // Snapshot original productions before modifying vn
        Map<String, List<List<String>>> snapshot = deepCopy(productions);

        for (String head : snapshot.keySet()) {
            for (List<String> rhs : snapshot.get(head)) {
                if (rhs.size() <= 1) {
                    afterTerminals.get(head).add(new ArrayList<>(rhs));
                    continue;
                }
                List<String> replaced = new ArrayList<>();
                for (String symbol : rhs) {
                    if (vt.contains(symbol)) {
                        if (!terminalMap.containsKey(symbol)) {
                            String nt = fresh("T_" + symbol);
                            terminalMap.put(symbol, nt);
                            afterTerminals.put(nt, new ArrayList<>());
                            afterTerminals.get(nt).add(Collections.singletonList(symbol));
                        }
                        replaced.add(terminalMap.get(symbol));
                    } else {
                        replaced.add(symbol);
                    }
                }
                afterTerminals.get(head).add(replaced);
            }
        }

        // Phase 2: Break rules with 3+ symbols into binary rules using pair caching
        Map<String, List<List<String>>> finalP = new LinkedHashMap<>();
        for (String nt : vn) finalP.put(nt, new ArrayList<>());

        // Cache: pair of (A, B) -> nonterminal that produces AB
        Map<String, String> pairMap = new LinkedHashMap<>();

        for (String head : afterTerminals.keySet()) {
            for (List<String> rhs : afterTerminals.get(head)) {
                if (rhs.size() <= 2) {
                    if (!finalP.getOrDefault(head, Collections.emptyList()).contains(rhs)) {
                        finalP.computeIfAbsent(head, k -> new ArrayList<>()).add(new ArrayList<>(rhs));
                    }
                    continue;
                }

                // Binarize: fold from the right using pair caching
                List<String> symbols = new ArrayList<>(rhs);
                String currentHead = head;

                while (symbols.size() > 2) {
                    String first = symbols.remove(0);

                    if (symbols.size() == 2) {
                        // Cache the remaining pair
                        String pairKey = symbols.get(0) + "," + symbols.get(1);
                        String pairNt;
                        if (pairMap.containsKey(pairKey)) {
                            pairNt = pairMap.get(pairKey);
                        } else {
                            pairNt = fresh("X");
                            pairMap.put(pairKey, pairNt);
                            finalP.put(pairNt, new ArrayList<>());
                            finalP.get(pairNt).add(new ArrayList<>(symbols));
                        }
                        finalP.computeIfAbsent(currentHead, k -> new ArrayList<>())
                                .add(Arrays.asList(first, pairNt));
                        symbols.clear();
                        break;
                    } else {
                        // Not yet down to 2 remaining — create a fresh intermediate nonterminal
                        String newNt = fresh("X");
                        finalP.put(newNt, new ArrayList<>());
                        finalP.computeIfAbsent(currentHead, k -> new ArrayList<>())
                                .add(Arrays.asList(first, newNt));
                        currentHead = newNt;
                    }
                }

                if (symbols.size() == 2) {
                    finalP.computeIfAbsent(currentHead, k -> new ArrayList<>())
                            .add(new ArrayList<>(symbols));
                }
            }
        }

        productions = finalP;
        return this;
    }

    // --- Validation ---
    public void validateCNF() {
        List<String> issues = new ArrayList<>();
        for (String head : productions.keySet()) {
            if (!vn.contains(head)) {
                issues.add("Nonterminal " + head + " is not in V_N");
            }
            for (List<String> rhs : productions.get(head)) {
                for (String symbol : rhs) {
                    if (!vn.contains(symbol) && !vt.contains(symbol)) {
                        issues.add("Unknown symbol '" + symbol + "' in " + head + " -> " + rhs);
                    }
                }
                if (rhs.isEmpty()) {
                    if (!head.equals(start)) issues.add("Epsilon production not allowed: " + head);
                    continue;
                }
                if (rhs.size() == 1) {
                    if (!vt.contains(rhs.get(0)))
                        issues.add("Unit production not in CNF: " + head + " -> " + rhs);
                    continue;
                }
                if (rhs.size() == 2) {
                    if (!(vn.contains(rhs.get(0)) && vn.contains(rhs.get(1))))
                        issues.add("Binary production must be nonterminals: " + head + " -> " + rhs);
                    continue;
                }
                issues.add("Production too long for CNF: " + head + " -> " + rhs);
            }
        }
        System.out.println("\nCNF validation");
        if (issues.isEmpty()) System.out.println("CNF check passed: all productions follow CNF rules.");
        else {
            System.out.println("CNF check failed:");
            for (String issue : issues) System.out.println("  - " + issue);
        }
    }

    // --- Grammar factory for Variant 20 ---
    public static CNFNormalizer createVariant20() {
        Set<String> vn = new LinkedHashSet<>(Arrays.asList("S", "A", "B", "C", "D"));
        Set<String> vt = new LinkedHashSet<>(Arrays.asList("a", "b"));
        Map<String, List<List<String>>> p = new LinkedHashMap<>();
        p.put("S", Arrays.asList(
                Arrays.asList("a", "B"),
                Arrays.asList("b", "A"),
                Arrays.asList("A")));
        p.put("A", Arrays.asList(
                Arrays.asList("B"),
                Arrays.asList("S", "a"),
                Arrays.asList("b", "B", "A"),
                Arrays.asList("b")));
        p.put("B", Arrays.asList(
                Arrays.asList("b"),
                Arrays.asList("b", "S"),
                Arrays.asList("a", "D"),
                new ArrayList<>()));    // epsilon
        p.put("D", Arrays.asList(Arrays.asList("A", "A")));
        p.put("C", Arrays.asList(Arrays.asList("B", "a")));
        return new CNFNormalizer(vn, vt, p, "S");
    }

    public static void main(String[] args) {
        System.out.println("Lab 5: Chomsky Normal Form");
        System.out.println("Variant 20");

        CNFNormalizer g = createVariant20();
        System.out.println("\nOriginal Grammar");
        g.printGrammar();

        g.eliminateEpsilon();
        System.out.println("\nAfter eliminating epsilon productions");
        g.printGrammar();

        g.eliminateUnit();
        System.out.println("\nAfter eliminating renaming (unit productions)");
        g.printGrammar();

        g.eliminateInaccessible();
        System.out.println("\nAfter eliminating inaccessible symbols");
        g.printGrammar();

        g.eliminateNonproductive();
        System.out.println("\nAfter eliminating nonproductive symbols");
        g.printGrammar();

        g.toCNF();
        System.out.println("\nChomsky Normal Form");
        g.printGrammar();

        g.validateCNF();
    }
}