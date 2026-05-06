# Laboratory Work 4
## Formal Languages & Finite Automata

**Topic:** Chomsky Normal Form  
**Variant:** 24  
**Author:** Cretu Dumitru  
**Course:** Formal Languages & Finite Automata  
**Professors:** Vasile Drumea, Irina Cojuhari  
**Programming Language:** Java

---

## 1. Introduction

In formal language theory, **Chomsky Normal Form (CNF)** is a standardized way of expressing any context-free grammar (CFG). Named after linguist Noam Chomsky, CNF imposes strict structural constraints on grammar productions, which makes many theoretical proofs and parsing algorithms significantly simpler to reason about.

Any context-free grammar that does not generate the empty string can be transformed into an equivalent grammar in CNF without changing the language it generates. This transformation is a foundational technique in compiler design, parsing theory, and the study of formal languages.

This laboratory work implements the complete CNF normalization pipeline in Java, applying it to the grammar defined in Variant 24.

---

## 2. Theoretical Background

### 2.1 Context-Free Grammar (CFG)

A context-free grammar is a 4-tuple **G = (V_N, V_T, P, S)** where:

- **V_N** – a finite set of non-terminal symbols
- **V_T** – a finite set of terminal symbols
- **P** – a finite set of production rules of the form A → α, where A ∈ V_N and α ∈ (V_N ∪ V_T)*
- **S** – the start symbol, S ∈ V_N

### 2.2 Chomsky Normal Form

A grammar is said to be in **Chomsky Normal Form** if every production rule is of exactly one of the following two forms:

- **A → BC** — the right-hand side consists of exactly two non-terminals
- **A → a** — the right-hand side consists of exactly one terminal

Where A, B, C ∈ V_N and a ∈ V_T.

### 2.3 Normalization Steps

Any CFG can be converted to CNF through the following sequence of transformations:

1. **Eliminate ε-productions** — remove all rules of the form A → ε, replacing them with all combinations that account for the nullable symbol.
2. **Eliminate unit (renaming) productions** — remove all rules of the form A → B (where B is a single non-terminal), substituting the productions of B directly into A.
3. **Eliminate inaccessible symbols** — remove all non-terminals and their associated rules that cannot be reached from the start symbol.
4. **Eliminate non-productive symbols** — remove all non-terminals that can never derive a string of terminals.
5. **Convert to binary form (CNF)** — replace terminals in long productions with new non-terminals (e.g., X → a), and binarize rules of length ≥ 3 by introducing intermediate non-terminals.

---

## 3. Variant 24 – Grammar Definition

The grammar assigned for Variant 24 is:

**G = (V_N, V_T, P, S)**

### Non-terminals

```
V_N = { S, A, B, C }
```

### Terminals

```
V_T = { a, d }
```

### Start Symbol

```
S
```

### Productions

| # | Rule |
|---|------|
| 1 | S → d B |
| 2 | S → A |
| 3 | A → d |
| 4 | A → d S |
| 5 | A → a B d A B |
| 6 | B → a |
| 7 | B → d A |
| 8 | B → A |
| 9 | B → ε |
| 10 | C → A a |

---

## 4. Objectives

1. Learn about Chomsky Normal Form (CNF) and understand its formal definition.
2. Get familiar with the normalization pipeline: ε-elimination, unit production removal, inaccessibility pruning, non-productivity pruning, and binarization.
3. Implement a method for normalizing an input grammar by the rules of CNF:
    - The implementation is encapsulated in a dedicated `CNFNormalizer` class with a clean, reusable API.
    - The functionality is executed and tested on Variant 24.
    - **Bonus:** The implementation accepts any grammar via a general constructor, not only the Variant 24 grammar.

---

## 5. Implementation

### 5.1 Project Structure

The implementation is contained in a single Java file:

```
CNFNormalizer.java
```

The `CNFNormalizer` class provides:

- A **general constructor** accepting any grammar `(V_N, V_T, P, S)`.
- A **static factory method** `createVariant24()` for the specific grammar of this variant.
- Public methods for each normalization step.
- A `normalize()` orchestrator that runs all five steps in sequence.

### 5.2 Data Representation

Productions are stored as:

```java
Map<String, List<List<String>>> productions
```

Each key is a non-terminal string. Each value is a list of right-hand sides, where every right-hand side is a list of symbol strings. This makes the structure easy to iterate, mutate, and deep-copy across all transformation steps.

### 5.3 Step 1 – Eliminate ε-productions

```java
public void eliminateEpsilonProductions()
```

**Algorithm:**

1. Iteratively find all **nullable** symbols — those that can derive ε directly (`B → ε`) or transitively (if all symbols in a right-hand side are nullable).
2. For every production containing a nullable symbol, generate all combinations with that symbol present or absent (powerset over nullable positions).
3. Add any new combinations not already present.
4. Remove all ε-productions (`A → ε`) from the grammar.

**Applied to Variant 24:**

- `B` is nullable (via `B → ε`).
- Productions containing `B` gain extra variants with `B` omitted.
- `B → ε` is then removed.

**Result (selected):**

```
S → d B | d
A → a B d A B | a B d A | a d A B | a d A
B → a | d A | A
```

### 5.4 Step 2 – Eliminate Renamings (Unit Productions)

```java
public void eliminateRenamings()
```

**Algorithm:**

A unit production is any rule of the form `A → B` where B is a single non-terminal. The algorithm repeatedly scans for such rules, replaces them with all productions reachable from B, and removes the original unit rule. This continues until no unit productions remain.

**Applied to Variant 24:**

- `S → A` is eliminated: S inherits all of A's productions.
- `B → A` is eliminated: B inherits all of A's productions.

**Result (selected):**

```
S → d B | d | d S | a B d A B | a B d A | a d A B | a d A
B → a | d A | d | d S | a B d A B | a B d A | a d A B | a d A
```

### 5.5 Step 3 – Eliminate Inaccessible Symbols

```java
public void eliminateInaccessibleSymbols()
```

**Algorithm:**

Starting from the start symbol `S`, perform a reachability traversal through all productions. Any non-terminal (and its rules) that cannot be reached from `S` is removed entirely.

**Applied to Variant 24:**

- `C` is never referenced in any production reachable from `S`.
- `C → A a` is removed.

**Result:** The grammar is reduced to `{ S, A, B }` as active non-terminals.

### 5.6 Step 4 – Eliminate Non-productive Symbols

```java
public void eliminateNonProductiveSymbols()
```

**Algorithm:**

A symbol is **productive** if it can eventually derive a string consisting solely of terminals. Initialize the productive set with all terminals, then iteratively add non-terminals whose every production right-hand side consists entirely of productive symbols. Remove all non-productive symbols and any rules containing them.

**Applied to Variant 24:**

All remaining symbols (`S`, `A`, `B`) are productive. No changes occur in this step.

### 5.7 Step 5 – Convert to CNF

```java
public void convertToCNF()
```

This step runs in two phases:

**Phase A – Terminal substitution in long rules:**

For any production of length ≥ 2, replace each terminal symbol `a` with a fresh non-terminal `X_i` and add the production `X_i → a`. This ensures that all non-unit productions consist entirely of non-terminals.

```
X0 → d
X1 → a
```

**Phase B – Binarization:**

For any production of length ≥ 3, introduce intermediate non-terminals to break it into a cascade of binary rules:

```
A → B1 B2 B3 B4
```

becomes:

```
A  → B1 X2
X2 → B2 X3
X3 → B3 B4
```

**Result (selected):**

```
S → X0 B | d | X0 S | X1 X2 | X1 X5 | X1 X7 | X1 X9
A → d | X0 S | X1 X10 | X1 X13 | X1 X15 | X1 X17
B → a | X0 A | d | X0 S | X1 X18 | X1 X21 | X1 X23 | X1 X25
X0 → d
X1 → a
X2 → B X3
X3 → X0 X4
X4 → A B
...
```

Every production is now either `A → BC` or `A → a`. ✓

### 5.8 Key Code Snippets

**Constructor and factory method:**

```java
public CNFNormalizer(Set<String> nonTerminals, Set<String> terminals,
                     Map<String, List<List<String>>> productions, String startSymbol) {
    this.nonTerminals = new LinkedHashSet<>(nonTerminals);
    this.terminals = new LinkedHashSet<>(terminals);
    this.productions = deepCopy(productions);
    this.startSymbol = startSymbol;
}

public static CNFNormalizer createVariant24() {
    Set<String> vn = new LinkedHashSet<>(Arrays.asList("S", "A", "B", "C"));
    Set<String> vt = new LinkedHashSet<>(Arrays.asList("a", "d"));
    // ... productions defined here
    return new CNFNormalizer(vn, vt, p, "S");
}
```

**Nullable combination generator (used in Step 1):**

```java
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
                newResult.add(new ArrayList<>(existing)); // version without nullable symbol
            }
        }
        result = newResult;
    }
    return result;
}
```

**Binarization loop (used in Step 5):**

```java
while (remaining.size() > 2) {
    String newNT = newSymbol();
    finalProductions.get(current).add(Arrays.asList(remaining.get(0), newNT));
    remaining = remaining.subList(1, remaining.size());
    current = newNT;
}
finalProductions.get(current).add(new ArrayList<>(remaining));
```

---

## 6. Results and Testing

### 6.1 Compilation and Execution

```bash
javac CNFNormalizer.java
java CNFNormalizer
```

### 6.2 Step-by-step Output

**Initial Grammar:**

```
S → d B | A
A → d | d S | a B d A B
B → a | d A | A | ε
C → A a
```

**After Step 1 (ε-elimination):**

```
S → d B | A | d
A → d | d S | a B d A B | a B d A | a d A B | a d A
B → a | d A | A
C → A a
```

**After Step 2 (unit production removal):**

```
S → d B | d | d S | a B d A B | a B d A | a d A B | a d A
A → d | d S | a B d A B | a B d A | a d A B | a d A
B → a | d A | d | d S | a B d A B | a B d A | a d A B | a d A
C → A a
```

**After Step 3 (inaccessible symbols removed):**

```
S → d B | d | d S | a B d A B | a B d A | a d A B | a d A
A → d | d S | a B d A B | a B d A | a d A B | a d A
B → a | d A | d | d S | a B d A B | a B d A | a d A B | a d A
```
> `C` and its production `C → A a` have been removed.

**After Step 4 (non-productive symbols):**

No changes — all symbols are productive.

**After Step 5 (CNF conversion):**

```
S  → X0 B | d | X0 S | X1 X2 | X1 X5 | X1 X7 | X1 X9
A  → d | X0 S | X1 X10 | X1 X13 | X1 X15 | X1 X17
B  → a | X0 A | d | X0 S | X1 X18 | X1 X21 | X1 X23 | X1 X25
X0 → d
X1 → a
X2 → B X3
X3 → X0 X4
X4 → A B
X5 → B X6
X6 → X0 A
X7 → X0 X8
X8 → A B
X9 → X0 A
... (auxiliary binarization symbols X10–X25)
```

All productions satisfy CNF: every rule is either `A → BC` or `A → a`. ✓

### 6.3 Verification Checklist

| Check | Result |
|-------|--------|
| No ε-productions remain | ✓ |
| No unit productions remain | ✓ |
| No inaccessible symbols remain | ✓ |
| No non-productive symbols remain | ✓ |
| Every production is `A → BC` or `A → a` | ✓ |
| Language generated is unchanged | ✓ |

### 6.4 Bonus – General Grammar Support

The class accepts any grammar through its public constructor. Example of using it with a custom grammar:

```java
Set<String> vn = new LinkedHashSet<>(Arrays.asList("S", "X", "Y"));
Set<String> vt = new LinkedHashSet<>(Arrays.asList("a", "b"));
Map<String, List<List<String>>> p = new LinkedHashMap<>();
p.put("S", Arrays.asList(Arrays.asList("X", "Y"), Arrays.asList("a")));
p.put("X", Arrays.asList(Arrays.asList("a"), Collections.singletonList("ε")));
p.put("Y", Arrays.asList(Arrays.asList("b")));

CNFNormalizer custom = new CNFNormalizer(vn, vt, p, "S");
custom.normalize();
```

---

## 7. Conclusions

In this laboratory work:

- The five-step CNF normalization pipeline was studied and fully understood.
- The pipeline was implemented in Java as a clean, self-contained `CNFNormalizer` class with a reusable API that accepts any context-free grammar (satisfying the bonus requirement).
- The implementation was applied to the Variant 24 grammar and verified to correctly produce a grammar in Chomsky Normal Form.
- Key observations from the normalization of Variant 24:
    - `B → ε` caused `B` to be nullable, triggering multiple new productions in Step 1.
    - Unit productions `S → A` and `B → A` propagated all of A's rules into S and B in Step 2.
    - Non-terminal `C` was found to be inaccessible from `S` and was cleanly pruned in Step 3.
    - All symbols were productive, so Step 4 made no changes.
    - Step 5 introduced 26 auxiliary non-terminals (X0–X25) to achieve full binarization.

The resulting CNF grammar preserves the language generated by the original grammar and is suitable for use with algorithms such as the CYK (Cocke–Younger–Kasami) parser.

---

## 8. References

1. [Chomsky Normal Form – Wikipedia](https://en.wikipedia.org/wiki/Chomsky_normal_form)
2. Hopcroft, J. E., Motwani, R., Ullman, J. D. – *Introduction to Automata Theory, Languages, and Computation*, 3rd ed.
3. Course materials – Formal Languages & Finite Automata, Technical University of Moldova