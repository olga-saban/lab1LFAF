# Laboratory Work #4
## Regular Expressions and String Generation

### Course: Formal Languages & Finite Automata
### Author: Saban Olga

---

## Theory

Regular expressions are formal tools used to describe patterns in strings. They are widely used in computer science for tasks such as lexical analysis, pattern matching, and validation of input data. A regular expression defines a set of strings over a given alphabet using operations such as concatenation, alternation, and repetition.

The most common operators in regular expressions include union, denoted by the symbol "|", which allows choosing between multiple symbols, concatenation which joins symbols together, and repetition operators such as "*" (zero or more repetitions), "+" (one or more repetitions), and "^n" (exact repetition). These operators allow the construction of complex patterns from simple building blocks.

Regular expressions are equivalent in expressive power to finite automata and regular grammars. They describe exactly the class of regular languages.

---

## Objectives

The objective of this laboratory work was to understand how regular expressions generate strings and how their structure influences the resulting language. Another goal was to implement algorithms that generate all possible strings defined by given regular expressions. The laboratory also aimed to simulate repetition operators and alternatives using programming constructs and to display the generated results.

---

## Implementation Description

The project was implemented in Java and consists of a single class called `RegexGenerator`. This class contains methods for generating strings based on three different regular expressions and for explaining the generation process.

A constant `MAX_REPEAT` is used to limit the number of repetitions for operators such as "*" and "+", ensuring that the generated output remains finite and manageable.

```java
static final int MAX_REPEAT = 5;
```

The `main` method controls the execution of the program. It sequentially processes each regular expression by displaying an explanation of the steps involved and then generating and printing the corresponding strings.

```java
public static void main(String[] args) {

    System.out.println("REGEX 1");
    explainR1();
    List<String> r1 = generateR1();
    printSample(r1);

    System.out.println("\nREGEX 2");
    explainR2();
    List<String> r2 = generateR2();
    printSample(r2);

    System.out.println("\nREGEX 3");
    explainR3();
    List<String> r3 = generateR3();
    printSample(r3);
}
```

The first regular expression has the form (S|T)(U|V)W*Y+24. The method `generateR1` constructs strings by iterating through all possible combinations of the alternatives and repetitions. Nested loops are used to simulate the behavior of the repetition operators "*" and "+".

```java
static List<String> generateR1() {
    List<String> result = new ArrayList<>();

    char[] first = {'S', 'T'};
    char[] second = {'U', 'V'};

    for (char a : first) {
        for (char b : second) {
            for (int w = 0; w <= MAX_REPEAT; w++) {
                for (int y = 1; y <= MAX_REPEAT; y++) {

                    StringBuilder sb = new StringBuilder();
                    sb.append(a).append(b);
                    sb.append("W".repeat(w));
                    sb.append("Y".repeat(y));
                    sb.append("24");

                    result.add(sb.toString());
                }
            }
        }
    }
    return result;
}
```

The second regular expression L(M|N)O^3P*Q(2|3) is implemented in the method `generateR2`. This method ensures that the symbol 'O' is repeated exactly three times while 'P' can appear any number of times within the defined limit.

```java
static List<String> generateR2() {
    List<String> result = new ArrayList<>();

    char[] mid = {'M', 'N'};
    char[] last = {'2', '3'};

    for (char m : mid) {
        for (int p = 0; p <= MAX_REPEAT; p++) {
            for (char l : last) {

                StringBuilder sb = new StringBuilder();
                sb.append("L");
                sb.append(m);
                sb.append("O".repeat(3));
                sb.append("P".repeat(p));
                sb.append("Q");
                sb.append(l);

                result.add(sb.toString());
            }
        }
    }
    return result;
}
```

The third regular expression R*S(T|U|V)W(X|Y|Z)^2 is implemented in the method `generateR3`. This method generates all possible strings by combining repetitions of 'R', a single symbol from the middle group, and all combinations of two symbols from the set {X, Y, Z}.

```java
static List<String> generateR3() {
    List<String> result = new ArrayList<>();

    char[] middle = {'T', 'U', 'V'};
    char[] xyz = {'X', 'Y', 'Z'};

    for (int r = 0; r <= MAX_REPEAT; r++) {
        for (char m : middle) {
            for (char x : xyz) {
                for (char y : xyz) {

                    StringBuilder sb = new StringBuilder();

                    sb.append("R".repeat(r));
                    sb.append("S");
                    sb.append(m);
                    sb.append("W");
                    sb.append(x).append(y);

                    result.add(sb.toString());
                }
            }
        }
    }
    return result;
}
```

Each regular expression also has a corresponding explanation method that prints the sequence of steps used to construct the strings. This helps clarify how each part of the regular expression contributes to the final result.

---

## Conclusions

The laboratory work demonstrated how regular expressions can be translated into algorithms that generate strings. By using loops and combinatorial logic, it is possible to simulate the behavior of operators such as alternation and repetition.

The implementation shows that even though regular expressions are abstract mathematical constructs, they can be effectively implemented using standard programming techniques. The generated outputs confirm that the expressions were correctly interpreted and that all valid combinations were produced within the defined limits.

This work also reinforces the connection between regular expressions and formal language theory, highlighting their practical application in software development.

---

## References

Course materials for Formal Languages & Finite Automata.  
Java Documentation: https://docs.oracle.com/javase/  