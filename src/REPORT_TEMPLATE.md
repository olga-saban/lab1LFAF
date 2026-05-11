# Laboratory Work 5
## Formal Languages & Finite Automata

**Topic:** Chomsky Normal Form  
**Variant:** 20  
**Author:** Saban Olga  
**Course:** Formal Languages & Finite Automata  
**Programming Language:** Java

---

## 1. Introduction

In formal language theory, **Chomsky Normal Form (CNF)** is a standardized way of expressing any context-free grammar (CFG). Named after linguist Noam Chomsky, CNF imposes strict structural constraints on grammar productions, which makes many theoretical proofs and parsing algorithms significantly simpler to reason about.

Any context-free grammar that does not generate the empty string can be transformed into an equivalent grammar in CNF without changing the language it generates. This transformation is a foundational technique in compiler design, parsing theory, and the study of formal languages.

This laboratory work implements the complete CNF normalization pipeline in Java, applying it to the grammar defined in Variant 20.

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
2. **Eliminate unit productions** — remove all rules of the form A → B (where B is a single non-terminal), substituting the productions of B directly into A.
3. **Eliminate inaccessible symbols** — remove all non-terminals and their associated rules that cannot be reached from the start symbol.
4. **Eliminate non-productive symbols** — remove all non-terminals that can never derive a string of terminals.
5. **Convert to binary form (CNF)** — replace terminals in long productions with new non-terminals, and binarize rules of length ≥ 3 by introducing intermediate non-terminals.

---

## 3. Variant 20 – Grammar Definition

The grammar assigned for Variant 20 is:

**G = (V_N, V_T, P, S)**

### Non-terminals

```
V_N = { S, A, B, C, D }
```

### Terminals

```
V_T = { a, b }
```

### Start Symbol

```
S
```

### Productions

| # | Rule |
|---|------|
| 1 | S → a B |
| 2 | S → b A |
| 3 | S → A |
| 4 | A → B |
| 5 | A → S a |
| 6 | A → b B A |
| 7 | A → b |
| 8 | B → b |
| 9 | B → b S |
| 10 | B → a D |
| 11 | B → ε |
| 12 | D → A A |
| 13 | C → B a |

---

## 4. Objectives

1. Learn about Chomsky Normal Form (CNF) and understand its formal definition.
2. Get familiar with the normalization pipeline: ε-elimination, unit production removal, inaccessibility pruning, non-productivity pruning, and binarization.
3. Implement a method for normalizing an input grammar by the rules of CNF:
    - The implementation is encapsulated in a dedicated `CNFNormalizer` class with a clean, reusable API.
    - The functionality is executed and tested on Variant 20.
    - **Bonus:** The implementation accepts any grammar via a general constructor, not only the Variant 20 grammar.

---

## 5. Implementation

### 5.1 Project Structure

The implementation is contained in a single Java file:

```
CNFNormalizer.java
```

The `CNFNormalizer` class provides:

- A **general constructor** accepting any grammar `(V_N, V_T, P, S)`.
- A **static factory method** `createVariant20()` for the specific grammar of this variant.
- Public methods for each normalization step: `eliminateEpsilon()`, `eliminateUnit()`, `eliminateInaccessible()`, `eliminateNonproductive()`, and `toCNF()`.
- A `validateCNF()` method that checks the final grammar for correctness.

All methods return `this`, allowing them to be chained:

```java
g.eliminateEpsilon().eliminateUnit().eliminateInaccessible().eliminateNonproductive().toCNF();
```

### 5.2 Data Representation

Productions are stored as:

```java
Map<String, List<List<String>>> productions
```

Each key is a non-terminal string. Each value is a list of right-hand sides, where every right-hand side is a list of symbol strings. This makes the structure easy to iterate, mutate, and deep-copy across all transformation steps.

### 5.3 Step 1 – Eliminate ε-productions (`eliminateEpsilon`)

**Algorithm:**

1. Iteratively find all **nullable** symbols — those that can derive ε directly (`B → ε`) or transitively (if all symbols in a right-hand side are nullable).
2. For every production containing a nullable symbol, generate all combinations with that symbol present or absent using backtracking (`backtrackNullable`). The backtracking first tries skipping the nullable symbol, then includes it.
3. Remove all ε-productions. If the start symbol was nullable, re-add `S → ε`.

**Applied to Variant 20:**

- `B` is nullable via `B → ε`.
- Productions containing `B` gain extra variants with `B` omitted.
- `B → ε` is then removed.
- Since `S → A → B → ε` forms a nullable chain, `S` is also nullable, so `S → ε` is retained.

**Result:**

```
S → a | aB | bA | b | A | eps
A → B | a | Sa | bA | b | bB | bBA
B → b | bS | a | aD
C → a | Ba
D → A | AA
```

### 5.4 Step 2 – Eliminate Unit Productions (`eliminateUnit`)

**Algorithm:**

For each non-terminal, compute the **unit closure** — the set of all non-terminals reachable via chains of unit productions — using a stack-based traversal. Then replace each non-terminal's rules with all non-unit productions reachable through that closure.

**Applied to Variant 20:**

- `S → A` is a unit production. The unit closure of `S` includes `A` and, transitively, `B`. So `S` absorbs all non-unit productions of `A` and `B`.
- `A → B` is a unit production. The unit closure of `A` includes `B`, so `A` absorbs all non-unit productions of `B`.
- `D → A` (introduced in step 1 via `D → AA` with `A` nullable) is also resolved.

**Result:**

```
S → a | Sa | bA | b | bB | bBA | bS | aD | aB | eps
A → a | Sa | bA | b | bB | bBA | bS | aD
B → b | bS | a | aD
C → a | Ba
D → a | Sa | bA | b | bB | bBA | bS | aD | AA
```

### 5.5 Step 3 – Eliminate Inaccessible Symbols (`eliminateInaccessible`)

**Algorithm:**

Starting from `S`, perform a reachability traversal through all productions. Any non-terminal that cannot be reached from `S` is removed from `V_N` and its rules are dropped.

**Applied to Variant 20:**

- `C` is never referenced in any production reachable from `S`.
- `C → a` and `C → Ba` are removed.
- Active non-terminals after this step: `{ S, A, B, D }`.

**Result:**

```
V_N = [S, A, B, D]
S → a | Sa | bA | b | bB | bBA | bS | aD | aB | eps
A → a | Sa | bA | b | bB | bBA | bS | aD
B → b | bS | a | aD
D → a | Sa | bA | b | bB | bBA | bS | aD | AA
```

### 5.6 Step 4 – Eliminate Non-productive Symbols (`eliminateNonproductive`)

**Algorithm:**

A symbol is **productive** if it can eventually derive a string of terminals. Iteratively mark non-terminals productive when all symbols in at least one of their right-hand sides are productive. Remove any non-terminal that is never marked, along with all rules containing it.

**Applied to Variant 20:**

All remaining symbols (`S`, `A`, `B`, `D`) are productive. No changes occur in this step.

### 5.7 Step 5 – Convert to CNF (`toCNF`)

This step runs in two phases:

**Phase A – Terminal substitution in long rules:**

For any production of length ≥ 2, replace each terminal `t` with a fresh non-terminal `T_t` and add the rule `T_t → t`. This ensures all non-unit productions contain only non-terminals.

```
T_a → a
T_b → b
```

**Phase B – Binarization:**

For any production of length ≥ 3, introduce intermediate non-terminals to break it into a cascade of binary rules. The implementation uses a **pair cache** (`pairMap`) so that identical pairs of non-terminals reuse the same intermediate symbol rather than generating duplicates:

```
A → B1 B2 B3  becomes  A → B1 X
                        X → B2 B3
```

In this grammar, the only rule requiring binarization is `bBA` (after terminal substitution: `T_b B A`), which produces the auxiliary non-terminal `X`:

```
X → BA
```

Fresh non-terminal names are generated by `fresh(String prefix)`, which appends an incrementing counter if the candidate name already exists in `V_N`.

### 5.8 CNF Validation (`validateCNF`)

After conversion, `validateCNF()` checks every production and reports any violations:

- ε-productions only allowed for the start symbol.
- Unit productions must point to a terminal.
- Binary productions must consist of two non-terminals.
- No production may have length > 2.

```
CNF check passed: all productions follow CNF rules.
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
V_N = [S, A, B, C, D]
V_T = [a, b]
P = {
  S -> aB | bA | A
  A -> B | Sa | bBA | b
  B -> b | bS | aD | eps
  D -> AA
  C -> Ba
}
```

**After Step 1 (ε-elimination):**

```
V_N = [S, A, B, C, D]
V_T = [a, b]
P = {
  S -> a | aB | bA | b | A | eps
  A -> B | a | Sa | bA | b | bB | bBA
  B -> b | bS | a | aD
  C -> a | Ba
  D -> A | AA
}
```

**After Step 2 (unit production removal):**

```
V_N = [S, A, B, C, D]
V_T = [a, b]
P = {
  S -> a | Sa | bA | b | bB | bBA | bS | aD | aB | eps
  A -> a | Sa | bA | b | bB | bBA | bS | aD
  B -> b | bS | a | aD
  C -> a | Ba
  D -> a | Sa | bA | b | bB | bBA | bS | aD | AA
}
```

**After Step 3 (inaccessible symbols removed):**

`C` and its productions are removed. Active non-terminals: `{ S, A, B, D }`.

```
V_N = [S, A, B, D]
V_T = [a, b]
P = {
  S -> a | Sa | bA | b | bB | bBA | bS | aD | aB | eps
  A -> a | Sa | bA | b | bB | bBA | bS | aD
  B -> b | bS | a | aD
  D -> a | Sa | bA | b | bB | bBA | bS | aD | AA
}
```

**After Step 4 (non-productive symbols):**

No changes — all symbols are productive.

**After Step 5 (CNF conversion):**

```
V_N = [S, A, B, D, T_a, T_b, X]
V_T = [a, b]
P = {
  S -> a | ST_a | T_bA | b | T_bB | T_bX | T_bS | T_aD | T_aB | eps
  A -> a | ST_a | T_bA | b | T_bB | T_bX | T_bS | T_aD
  B -> b | T_bS | a | T_aD
  D -> a | ST_a | T_bA | b | T_bB | T_bX | T_bS | T_aD | AA
  T_a -> a
  T_b -> b
  X -> BA
}
```

Every production is either `A → BC` or `A → a`. The only auxiliary non-terminals introduced are `T_a`, `T_b`, and `X`.

### 6.3 Verification Checklist

| Check | Result |
|-------|--------|
| No ε-productions remain (except S) | ✓ |
| No unit productions remain | ✓ |
| No inaccessible symbols remain | ✓ |
| No non-productive symbols remain | ✓ |
| Every production is `A → BC` or `A → a` | ✓ |
| Language generated is unchanged | ✓ |

---

## 7. Conclusions

In this laboratory work:

- The five-step CNF normalization pipeline was studied and fully implemented.
- The pipeline is implemented in Java as a clean, self-contained `CNFNormalizer` class with a reusable API that accepts any context-free grammar.
- The implementation was applied to the Variant 20 grammar and verified to correctly produce a grammar in Chomsky Normal Form.
- Key observations from the normalization of Variant 20:
    - `B → ε` caused `B` to be nullable, which transitively made `A` and `S` nullable as well, triggering multiple new productions in Step 1, including the retention of `S → ε`.
    - Unit productions `S → A`, `A → B`, and `D → A` propagated their targets' rules into the source non-terminals in Step 2.
    - Non-terminal `C` was found to be inaccessible from `S` and was pruned in Step 3.
    - All remaining symbols were productive, so Step 4 made no changes.
    - Step 5 introduced auxiliary non-terminals `T_a`, `T_b`, and `X` (for the pair `BA`) to achieve full binarization. A pair cache ensures identical symbol pairs share the same intermediate non-terminal.

The resulting CNF grammar preserves the language generated by the original grammar and is suitable for use with algorithms such as the CYK (Cocke–Younger–Kasami) parser.

---

## 8. References

1. [Chomsky Normal Form – Wikipedia](https://en.wikipedia.org/wiki/Chomsky_normal_form)
2. Hopcroft, J. E., Motwani, R., Ullman, J. D. – *Introduction to Automata Theory, Languages, and Computation*, 3rd ed.
3. Course materials – Formal Languages & Finite Automata, Technical University of Moldova