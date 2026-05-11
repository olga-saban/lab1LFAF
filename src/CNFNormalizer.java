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
        System.out.println("V_N = " + vn);
        System.out.println("V_T = " + vt);
        System.out.println("P = {");
        for (String head : productions.keySet()) {
            System.out.print("  " + head + " -> ");
            List<String> parts = new ArrayList<>();
            for (List<String> rhs : productions.get(head)) {
                parts.add(rhs.isEmpty() ? EPSILON : String.join("", rhs));
            }
            System.out.println(String.join(" | ", parts));
        }
        System.out.println("}");
    }

    public CNFNormalizer eliminateEpsilon() {
        Set<String> nullable = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String head : productions.keySet()) {
                if (nullable.contains(head)) continue;
                for (List<String> rhs : productions.get(head)) {
                    boolean allNullable = true;
                    for (String s : rhs) {
                        if (!nullable.contains(s)) {
                            allNullable = false;
                            break;
                        }
                    }
                    if (rhs.isEmpty() || allNullable) {
                        nullable.add(head);
                        changed = true;
                        break;
                    }
                }
            }
        }

        Map<String, List<List<String>>> newProductions = new LinkedHashMap<>();
        for (String head : vn) newProductions.put(head, new ArrayList<>());
        for (String head : productions.keySet()) {
            for (List<String> rhs : productions.get(head)) {
                if (rhs.isEmpty()) continue;
                Set<List<String>> expansions = nullableExpansions(rhs, nullable);
                for (List<String> option : expansions) {
                    if ((!option.isEmpty() || head.equals(start)) && !newProductions.get(head).contains(option)) {
                        newProductions.get(head).add(option);
                    }
                }
            }
        }
        if (nullable.contains(start) && !newProductions.get(start).contains(new ArrayList<>())) {
            newProductions.get(start).add(new ArrayList<>());
        }
        productions = newProductions;
        return this;
    }

    private Set<List<String>> nullableExpansions(List<String> rhs, Set<String> nullable) {
        Set<List<String>> results = new HashSet<>();
        backtrackNullable(rhs, nullable, 0, new ArrayList<>(), results);
        return results;
    }

    private void backtrackNullable(List<String> rhs, Set<String> nullable, int index, List<String> current, Set<List<String>> results) {
        if (index == rhs.size()) {
            results.add(new ArrayList<>(current));
            return;
        }
        String symbol = rhs.get(index);
        if (nullable.contains(symbol)) {
            backtrackNullable(rhs, nullable, index + 1, current, results);
        }
        current.add(symbol);
        backtrackNullable(rhs, nullable, index + 1, current, results);
        current.remove(current.size() - 1);
    }

    public CNFNormalizer eliminateUnit() {
        Map<String, Set<String>> closure = new LinkedHashMap<>();
        for (String nt : vn) closure.put(nt, unitClosure(nt));

        Map<String, List<List<String>>> newProductions = new LinkedHashMap<>();
        for (String head : vn) {
            newProductions.put(head, new ArrayList<>());
            for (String target : closure.get(head)) {
                for (List<String> rhs : productions.getOrDefault(target, new ArrayList<>())) {
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
        Stack<String> stack = new Stack<>();
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
                        if (!(vt.contains(s) || productive.contains(s))) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        productive.add(head);
                        changed = true;
                        break;
                    }
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
                    if (!(vt.contains(s) || vn.contains(s))) {
                        ok = false;
                        break;
                    }
                }
                if (ok) filtered.add(rhs);
            }
            newProductions.put(head, filtered);
        }
        productions = newProductions;
        return this;
    }

    private int counter = 0;
    private String fresh(String prefix) {
        String candidate = prefix;
        while (vn.contains(candidate)) candidate = prefix + (++counter);
        vn.add(candidate);
        return candidate;
    }

    public CNFNormalizer toCNF() {
        Map<String, String> terminalMap = new HashMap<>();
        Map<String, List<List<String>>> temp = new LinkedHashMap<>();
        for (String nt : vn) temp.put(nt, new ArrayList<>());

        for (String head : productions.keySet()) {
            for (List<String> rhs : productions.get(head)) {
                if (rhs.size() <= 1) {
                    temp.get(head).add(new ArrayList<>(rhs));
                    continue;
                }
                List<String> replaced = new ArrayList<>();
                for (String symbol : rhs) {
                    if (vt.contains(symbol)) {
                        if (!terminalMap.containsKey(symbol)) {
                            String nt = fresh("T_" + symbol);
                            terminalMap.put(symbol, nt);
                            temp.put(nt, new ArrayList<>());
                            temp.get(nt).add(Arrays.asList(symbol));
                        }
                        replaced.add(terminalMap.get(symbol));
                    } else replaced.add(symbol);
                }
                temp.get(head).add(replaced);
            }
        }

        Map<String, List<List<String>>> finalP = new LinkedHashMap<>();
        for (String nt : vn) finalP.put(nt, new ArrayList<>());
        for (String head : temp.keySet()) {
            for (List<String> rhs : temp.get(head)) {
                if (rhs.size() <= 2) {
                    finalP.get(head).add(new ArrayList<>(rhs));
                    continue;
                }
                String current = head;
                List<String> symbols = new ArrayList<>(rhs);
                while (symbols.size() > 2) {
                    String first = symbols.remove(0);
                    String next = fresh("X");
                    finalP.get(current).add(Arrays.asList(first, next));
                    current = next;
                }
                finalP.get(current).add(new ArrayList<>(symbols));
            }
        }
        productions = finalP;
        return this;
    }

    public void validateCNF() {
        List<String> issues = new ArrayList<>();
        for (String head : productions.keySet()) {
            for (List<String> rhs : productions.get(head)) {
                if (rhs.isEmpty()) {
                    if (!head.equals(start)) issues.add("Invalid epsilon: " + head);
                    continue;
                }
                if (rhs.size() == 1) {
                    if (!vt.contains(rhs.get(0))) issues.add("Invalid unit: " + head + " -> " + rhs);
                } else if (rhs.size() == 2) {
                    if (!(vn.contains(rhs.get(0)) && vn.contains(rhs.get(1)))) issues.add("Binary rule must be NTs: " + head + " -> " + rhs);
                } else issues.add("Rule too long: " + head + " -> " + rhs);
            }
        }
        if (issues.isEmpty()) System.out.println("\nCNF validation passed.");
        else {
            System.out.println("\nCNF validation failed:");
            for (String issue : issues) System.out.println(" - " + issue);
        }
    }

    public static CNFNormalizer createVariant20() {
        Set<String> vn = new LinkedHashSet<>(Arrays.asList("S", "A", "B", "C", "D"));
        Set<String> vt = new LinkedHashSet<>(Arrays.asList("a", "b"));
        Map<String, List<List<String>>> p = new LinkedHashMap<>();
        p.put("S", Arrays.asList(Arrays.asList("a", "B"), Arrays.asList("b", "A"), Arrays.asList("A")));
        p.put("A", Arrays.asList(Arrays.asList("B"), Arrays.asList("S", "a"), Arrays.asList("b", "B", "A"), Arrays.asList("b")));
        p.put("B", Arrays.asList(Arrays.asList("b"), Arrays.asList("b", "S"), Arrays.asList("a", "D"), new ArrayList<>()));
        p.put("D", Arrays.asList(Arrays.asList("A", "A")));
        p.put("C", Arrays.asList(Arrays.asList("B", "a")));
        return new CNFNormalizer(vn, vt, p, "S");
    }

    public static void main(String[] args) {
        System.out.println("Lab 5: Chomsky Normal Form");
        CNFNormalizer g = createVariant20();
        System.out.println("\nOriginal Grammar");
        g.printGrammar();

        g.eliminateEpsilon().eliminateUnit().eliminateInaccessible().eliminateNonproductive().toCNF();

        System.out.println("\nChomsky Normal Form");
        g.printGrammar();
        g.validateCNF();
    }
}