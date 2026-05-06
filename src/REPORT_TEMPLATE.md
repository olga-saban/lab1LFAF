# Laboratory Work 4
## Formal Languages & Finite Automata

**Topic:** Parser & Building an Abstract Syntax Tree  
**Author:** Saban Olga  
**Acknowledgements:** Vasile Drumea, Irina Cojuhari  
**Programming Language:** Java

---

# 1. Introduction

**Parsing** is the process of analyzing a sequence of tokens to determine its grammatical structure according to a given formal grammar. It is a fundamental step in the compilation pipeline, typically following lexical analysis. The result of parsing is most commonly a **parse tree** or an **Abstract Syntax Tree (AST)**, which encodes the syntactic relationships between elements of the source text.

An **Abstract Syntax Tree** is a hierarchical data structure where each node represents a syntactic construct of the source language. Unlike a concrete parse tree, an AST omits redundant syntactic details (such as parentheses and semicolons) and retains only the semantically meaningful structure.

Parsers are widely used in compilers and interpreters, static analysis tools, query engines, configuration processors

---

# 2. Theoretical Background

## 2.1 Lexical Analysis (Tokenization)

Before parsing can occur, the input string must be broken into **tokens** — the smallest meaningful units of the language. A **Lexer** (or tokenizer) reads the raw character stream and classifies substrings into token types using pattern matching, typically implemented with **regular expressions**.

For an arithmetic expression language, token types include:

| Token Type | Example |
|------------|---------|
| NUMBER     | `3`, `42` |
| PLUS       | `+` |
| MINUS      | `-` |
| MULTIPLY   | `*` |
| DIVIDE     | `/` |
| LPAREN     | `(` |
| RPAREN     | `)` |
| WHITESPACE | ` ` (skipped) |

## 2.2 Abstract Syntax Tree (AST)

An AST represents the syntactic structure of an expression as a tree. For example, the expression `3 + 5 * (2 - 4)` yields:

```
Operator: +
  Number: 3
  Operator: *
    Number: 5
    Operator: -
      Number: 2
      Number: 4
```

Each internal node is a binary operation; each leaf node is a numeric literal.

## 2.3 Recursive Descent Parsing

The parser implemented here uses **recursive descent** — a top-down parsing strategy where each grammar rule corresponds to a function. The grammar for arithmetic expressions follows standard operator precedence:

```
expression  → term (('+' | '-') term)*
term        → factor (('*' | '/') factor)*
factor      → NUMBER | '(' expression ')'
```

This grammar ensures that `*` and `/` bind more tightly than `+` and `-`.

---

# 3. Objectives

The objective of this laboratory work is to understand the process of parsing and to implement it in practice. This includes defining a set of token types, implementing lexical analysis using regular expressions, designing a set of classes to represent an Abstract Syntax Tree, and constructing a recursive descent parser that produces such a tree from an input expression.

---

# 4. Implementation

The solution is implemented entirely in a single Java file named `Main.java`. It includes all necessary components required to transform an input string into an Abstract Syntax Tree.

No external libraries were used.

---

## 4.1 TokenType Enum

```java
enum TokenType {
    NUMBER, PLUS, MINUS, MULTIPLY, DIVIDE,
    LPAREN, RPAREN, WHITESPACE
}
```

The enum provides a clean, type-safe way to categorize each token during lexical analysis.

---

## 4.2 Lexer

The `Lexer` class maintains a list of `TokenInfo` entries, each pairing a `TokenType` with its corresponding regular expression pattern.

```java
tokenInfos.add(new TokenInfo(TokenType.NUMBER,   "\\d+"));
tokenInfos.add(new TokenInfo(TokenType.PLUS,     "\\+"));
tokenInfos.add(new TokenInfo(TokenType.MINUS,    "-"));
tokenInfos.add(new TokenInfo(TokenType.MULTIPLY, "\\*"));
tokenInfos.add(new TokenInfo(TokenType.DIVIDE,   "/"));
tokenInfos.add(new TokenInfo(TokenType.LPAREN,   "\\("));
tokenInfos.add(new TokenInfo(TokenType.RPAREN,   "\\)"));
tokenInfos.add(new TokenInfo(TokenType.WHITESPACE, "\\s+"));
```

### Tokenization Process

The `tokenize()` method iterates through the input string. At each position, it tries to match the front of the remaining input against each pattern in order. When a match is found:
- If the token type is `WHITESPACE`, it is consumed silently (not added to the token list).
- Otherwise, a `Token` object is created and appended.
- The matched substring is removed from the front of the input.

If no pattern matches, a `RuntimeException` is thrown indicating an unexpected character.

---

## 4.3 AST Node Classes

Three classes represent nodes in the AST:

```java
abstract class ASTNode {}

class NumberNode extends ASTNode {
    int value;
}

class BinaryOpNode extends ASTNode {
    ASTNode left;
    Token operator;
    ASTNode right;
}
```

`NumberNode` is a leaf node representing an integer literal.
`BinaryOpNode` is an internal node with a left child, an operator token, and a right child.

---

## 4.4 Parser

The `Parser` class uses recursive descent to build the AST from the token list. It maintains a `position` cursor and exposes three parsing methods corresponding to the grammar rules:

### `expression()`

Handles `+` and `-` (lowest precedence):

```java
private ASTNode expression() {
    ASTNode node = term();
    while (currentToken() != null &&
           (currentToken().type == TokenType.PLUS ||
            currentToken().type == TokenType.MINUS)) {
        Token op = currentToken();
        eat(op.type);
        node = new BinaryOpNode(node, op, term());
    }
    return node;
}
```

### `term()`

Handles `*` and `/` (higher precedence):

```java
private ASTNode term() {
    ASTNode node = factor();
    while (currentToken() != null &&
           (currentToken().type == TokenType.MULTIPLY ||
            currentToken().type == TokenType.DIVIDE)) {
        Token op = currentToken();
        eat(op.type);
        node = new BinaryOpNode(node, op, factor());
    }
    return node;
}
```

### `factor()`

Handles numeric literals and parenthesized sub-expressions:

```java
private ASTNode factor() {
    Token token = currentToken();
    if (token.type == TokenType.NUMBER) {
        eat(TokenType.NUMBER);
        return new NumberNode(Integer.parseInt(token.value));
    }
    if (token.type == TokenType.LPAREN) {
        eat(TokenType.LPAREN);
        ASTNode node = expression();
        eat(TokenType.RPAREN);
        return node;
    }
    throw new RuntimeException("Unexpected token: " + token);
}
```

The `eat()` helper advances the position after verifying the current token matches the expected type.

---

## 4.5 AST Pretty-Printer

A recursive `printAST()` utility prints the tree with indentation to visualize the hierarchy:

```java
static void printAST(ASTNode node, String indent) {
    if (node instanceof NumberNode) {
        System.out.println(indent + "Number: " + ((NumberNode) node).value);
    } else if (node instanceof BinaryOpNode) {
        BinaryOpNode bin = (BinaryOpNode) node;
        System.out.println(indent + "Operator: " + bin.operator.value);
        printAST(bin.left,  indent + "  ");
        printAST(bin.right, indent + "  ");
    }
}
```

---

# 5. Example Execution

## Input

```
3 + 5 * (2 - 4)
```

## Token Output

```
NUMBER : 3
PLUS : +
NUMBER : 5
MULTIPLY : *
LPAREN : (
NUMBER : 2
MINUS : -
NUMBER : 4
RPAREN : )
```

Whitespace tokens are consumed silently and do not appear in the output.

## AST Output

```
Operator: +
  Number: 3
  Operator: *
    Number: 5
    Operator: -
      Number: 2
      Number: 4
```

This correctly reflects operator precedence: the subtraction `2 - 4` is grouped first (due to parentheses), then multiplied by `5`, then added to `3`.

---

# 6. Project Structure

```
Main.java
```

The file contains all classes:
- `TokenType`
- `Token`
- `Lexer` (with nested `TokenInfo`)
- `ASTNode`, `NumberNode`, `BinaryOpNode`
- `Parser`
- `Main`

---

# 7. Conclusions

This laboratory work demonstrates the complete pipeline of transforming raw input into a structured representation. Starting from lexical analysis, the input is converted into tokens, which are then processed by a parser to produce an Abstract Syntax Tree.  
The implementation highlights the practical application of recursive descent parsing and shows how operator precedence can be enforced through grammar design. The resulting AST provides a clean and meaningful representation of the input expression, forming a solid foundation for further stages such as interpretation or compilation.

---

# References

1. [Parsing – Wikipedia](https://en.wikipedia.org/wiki/Parsing)
2. [Abstract Syntax Tree – Wikipedia](https://en.wikipedia.org/wiki/Abstract_syntax_tree)