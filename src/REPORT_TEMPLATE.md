# Laboratory work 1  
## Formal Languages & Finite Automata  

**Topic:** Intro to Formal Languages. Regular Grammars. Finite Automata  
**Variant:** 24  
**Programming Language:** Java  

---

# 1. Introduction

A **formal language** is a mathematically defined system used to describe valid strings constructed from a given alphabet. Unlike natural languages, formal languages are strictly defined by production rules and structural constraints.

The fundamental components of a formal language are:

- **Alphabet (Σ)** – a finite set of symbols.
- **Vocabulary** – the set of valid words formed using the alphabet.
- **Grammar (G)** – a set of production rules that define how strings are generated.

Formal languages are widely used in programming languages, compilers, parsers, and automata theory.

---

# 2. Theoretical Background

## 2.1 Regular Grammar

A grammar is called **regular** if all productions are of one of the following forms:

- A → aB  
- A → a  

Where:
- A and B are non-terminals  
- a is a terminal symbol  

Regular grammars are equivalent to **Finite Automata**.

---

# 3. Variant 24 – Grammar Definition

## Non-terminals (VN)

VN = {S, A, C, D}

## Terminals (VT)

Although the initial statement specifies `{a, b}`, the production rules contain the symbol `d`. Therefore:

VT = {a, b, d}

## Productions (P)

S → aA
A → bS
A → dD
D → bC
C → a
C → bA
D → aD

## Start Symbol

S

---

# 4. Proof that the Grammar is Regular

Each production is of the form:

- A → aB  
- A → a  

There is at most one non-terminal on the right-hand side and it appears at the end of the production.

Therefore, the grammar is **right-linear**, which means it is a **regular grammar**.

---

# 5. Implementation

The solution is implemented in Java using three classes:

- `Grammar`
- `FiniteAutomaton`
- `Main`

No external libraries were used.

---

# 5.1 Grammar Class

The `Grammar` class contains:

- Set of non-terminals (VN)
- Set of terminals (VT)
- Production rules (P)
- Start symbol (S)

## Methods Implemented

### `generateString()`

- Starts from the start symbol `S`
- Randomly selects production rules
- Generates valid strings
- Stops when a terminal-only production is reached

This method is called 5 times in `Main`.

---

### `toFiniteAutomaton()`

Converts the grammar into a deterministic finite automaton.

Each non-terminal becomes a state.

If a production has the form:

A → aB

Then:

δ(A, a) = B

If a production has the form:

A → a

Then:

δ(A, a) = F

Where `F` is an additional final state.

---

# 5.2 Finite Automaton Definition

After conversion:

## States (Q)

Q = {S, A, C, D, F}

## Alphabet (Σ)

Σ = {a, b, d}

## Initial State

q0 = S

## Final States

F = {F}

## Transition Function (δ)

| From | Symbol | To |
|------|--------|----|
| S | a | A |
| A | b | S |
| A | d | D |
| D | b | C |
| D | a | D |
| C | b | A |
| C | a | F |

---

# 5.3 `stringBelongToLanguage()`

This method:

1. Starts from initial state `S`
2. Reads the input string symbol by symbol
3. Moves according to the transition function
4. Rejects the string if:
   - A symbol is not in the alphabet
   - No transition exists for the current state and symbol
5. Accepts the string only if the final state is reached

---

# 6. Example Execution

## Generated Strings Example

1: adba
2: abadba
3: adaba
4: ababdba
5: adaaabbdba

## Example Test

Input:

adba

Execution:

S --a--> A
A --d--> D
D --b--> C
C --a--> F

Output:

String ACCEPTED.

---

# 7. Project Structure

Main.java

The file contains:

- Grammar class
- FiniteAutomaton class
- Main class

---

# 8. How to Run

Compile:

javac Main.java

Run:

java Main

---

# 9. Conclusions

In this laboratory work:

- A regular grammar was defined and analyzed.
- The grammar was implemented in Java.
- A function for generating valid strings was created.
- The grammar was successfully converted into a finite automaton.
- A method was implemented to verify whether a string belongs to the language.

This laboratory demonstrates the equivalence between **regular grammars and finite automata**, and provides a practical implementation of theoretical concepts from formal language theory.
