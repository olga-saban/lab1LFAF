# Laboratory Work #4
## Regular Expressions and String Generation

### Course: Formal Languages & Finite Automata
### Author: Saban Olga

---

## Theory

Regular expressions are formal tools used to describe patterns in strings. They are widely used in computer science for tasks such as lexical analysis, pattern matching, and validation of input data. A regular expression defines a set of strings over a given alphabet using operations such as concatenation, alternation, and repetition.

The most common operators in regular expressions include union, denoted by the symbol "|", which allows choosing between multiple symbols, concatenation which joins symbols together, and repetition operators such as "*" (zero or more repetitions), "+" (one or more repetitions), and "{n}" (exact repetition). These operators allow the construction of complex patterns from simple building blocks.

Regular expressions are equivalent in expressive power to finite automata and regular grammars. They describe exactly the class of regular languages. To process them programmatically, a pattern can be parsed into an Abstract Syntax Tree (AST) whose nodes represent the structural operations of the expression.

---

## Objectives

The objective of this laboratory work was to understand how regular expressions generate strings and how their structure influences the resulting language. Another goal was to implement a recursive-descent parser that builds an AST from a pattern string. The laboratory also aimed to generate all possible strings defined by given regular expressions through AST traversal and to display the parsing steps alongside the generated results.

---

## Implementation Description

The project was implemented in Java and consists of a single class called `RegexGenerator`. Rather than hardcoding generation logic per expression, it uses a recursive-descent parser that builds an AST from any pattern string and generates strings by traversing that tree.

A constant `MAX_REPEAT` is used to limit the number of repetitions for operators such as `*` and `+`, ensuring that the generated output remains finite and manageable.

```java
static final int MAX_REPEAT = 5;
```

The `main` method iterates over all three regular expressions. For each one it creates a `RegexParser`, parses the pattern into an AST, prints the recorded parsing steps, and then generates and prints up to 50 strings.

```java
for (String regex : regexes) {
    System.out.println("\nREGEX: " + regex);
    RegexParser parser = new RegexParser(regex);
    Node ast = parser.parse();

    System.out.println("\nProcessing steps:");
    for (String step : parser.steps) {
        System.out.println(step);
    }

    System.out.println("\nGenerated strings:");
    List<String> results = ast.generate(MAX_REPEAT, 50);
    for (String s : results) {
        System.out.println(s);
    }
}
```

### AST Node Types

All nodes implement the `Node` interface which exposes a single `generate(int maxRepeat, int maxResults)` method. Four concrete types are defined:

- **Literal** – holds a single character and returns it as a one-element list.
- **Concat** – holds an ordered list of child nodes and builds its output by progressively cross-producting each child's string list.
- **Alternation** – holds multiple alternative child nodes and returns the union of all their outputs.
- **Repeat** – wraps a child node with a minimum and maximum count. It generates strings for every count from `min` to `min(max, MAX_REPEAT)` by repeatedly concatenating the child's output with itself.

String lists are combined using a helper that respects the `maxResults` cap:

```java
static List<String> concat(List<String> left, List<String> right, int maxResults) {
    List<String> result = new ArrayList<>();
    for (String a : left) {
        for (String b : right) {
            result.add(a + b);
            if (result.size() >= maxResults) return result;
        }
    }
    return result;
}
```

### Recursive-Descent Parser

The `RegexParser` class walks the pattern string using three mutually recursive methods that mirror the grammar of regular expressions. Each method also appends a description of its action to the `steps` list.

- `parseExpression()` handles alternation. It collects terms separated by `|` and wraps them in an `Alternation` node when there is more than one.
- `parseTerm()` handles concatenation. It collects consecutive factors until it hits `)`, `|`, or end-of-input, and wraps them in a `Concat` node.
- `parseFactor()` calls `parseBase()` then checks for a quantifier: `*` produces `Repeat(base, 0, MAX_REPEAT)`, `+` produces `Repeat(base, 1, MAX_REPEAT)`, and `{n}` produces `Repeat(base, n, n)`.
- `parseBase()` consumes a literal character or, on encountering `(`, recursively calls `parseExpression()` and expects the closing `)`.

```java
Node parseExpression() {
    List<Node> options = new ArrayList<>();
    options.add(parseTerm());
    while (current() == '|') {
        advance();
        steps.add("Build alternation");
        options.add(parseTerm());
    }
    if (options.size() == 1) return options.get(0);
    return new Alternation(options);
}
```

### Processed Regular Expressions

The three expressions exercise the full set of supported constructs:

1. `(S|T)(U|V)W*Y+24` — two alternation groups, a zero-or-more repeat, a one-or-more repeat, and literal digits.
2. `L(M|N)O{3}P*Q(2|3)` — an exact-repeat quantifier `{3}`, a zero-or-more repeat, and an alternation containing digit literals.
3. `R*S(T|U|V)W(X|Y|Z){2}` — a zero-or-more repeat at the start, a three-way alternation, and an exact-repeat `{2}` applied to another three-way alternation.

---

## Conclusions

The laboratory work demonstrated how regular expressions can be parsed into an Abstract Syntax Tree and how that tree can be traversed recursively to enumerate all strings belonging to the described language. Using a recursive-descent parser instead of hardcoded loops makes the implementation general: the same code handles any combination of alternation, concatenation, and repetition without requiring expression-specific methods.

The parsing step log makes the internal structure of each expression visible, reinforcing the connection between the textual notation and the underlying tree. This work also illustrates the practical relationship between regular expressions, formal language theory, and tree-based algorithm design.

---

## References

Course materials for Formal Languages & Finite Automata.  
Java Documentation: https://docs.oracle.com/javase/