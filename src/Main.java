import java.util.*;
import java.util.regex.*;

public class Main {

    enum TokenType {
        NUMBER,
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        LPAREN,
        RPAREN,
        WHITESPACE
    }

    static class Token {
        TokenType type;
        String value;

        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        public String toString() {
            return type + " : " + value;
        }
    }

    static class Lexer {

        static class TokenInfo {
            TokenType type;
            String regex;

            TokenInfo(TokenType type, String regex) {
                this.type = type;
                this.regex = regex;
            }
        }

        private static final List<TokenInfo> tokenInfos = new ArrayList<>();

        static {
            tokenInfos.add(new TokenInfo(TokenType.NUMBER, "\\d+"));
            tokenInfos.add(new TokenInfo(TokenType.PLUS, "\\+"));
            tokenInfos.add(new TokenInfo(TokenType.MINUS, "-"));
            tokenInfos.add(new TokenInfo(TokenType.MULTIPLY, "\\*"));
            tokenInfos.add(new TokenInfo(TokenType.DIVIDE, "/"));
            tokenInfos.add(new TokenInfo(TokenType.LPAREN, "\\("));
            tokenInfos.add(new TokenInfo(TokenType.RPAREN, "\\)"));
            tokenInfos.add(new TokenInfo(TokenType.WHITESPACE, "\\s+"));
        }

        public static List<Token> tokenize(String input) {
            List<Token> tokens = new ArrayList<>();

            while (!input.isEmpty()) {
                boolean match = false;

                for (TokenInfo info : tokenInfos) {
                    Pattern pattern = Pattern.compile("^(" + info.regex + ")");
                    Matcher matcher = pattern.matcher(input);

                    if (matcher.find()) {
                        match = true;
                        String value = matcher.group();

                        if (info.type != TokenType.WHITESPACE) {
                            tokens.add(new Token(info.type, value));
                        }

                        input = input.substring(value.length());
                        break;
                    }
                }

                if (!match) {
                    throw new RuntimeException("Unexpected character: " + input);
                }
            }

            return tokens;
        }
    }

    static abstract class ASTNode {}

    static class NumberNode extends ASTNode {
        int value;

        NumberNode(int value) {
            this.value = value;
        }
    }

    static class BinaryOpNode extends ASTNode {
        ASTNode left;
        Token operator;
        ASTNode right;

        BinaryOpNode(ASTNode left, Token operator, ASTNode right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    static class Parser {

        private List<Token> tokens;
        private int position = 0;

        Parser(List<Token> tokens) {
            this.tokens = tokens;
        }

        private Token currentToken() {
            if (position < tokens.size()) {
                return tokens.get(position);
            }
            return null;
        }

        private void eat(TokenType type) {
            if (currentToken() != null && currentToken().type == type) {
                position++;
            } else {
                throw new RuntimeException("Expected " + type);
            }
        }

        public ASTNode parse() {
            return expression();
        }

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
    }

    static void printAST(ASTNode node, String indent) {
        if (node instanceof NumberNode) {
            System.out.println(indent + "Number: " + ((NumberNode) node).value);
        } else if (node instanceof BinaryOpNode) {
            BinaryOpNode bin = (BinaryOpNode) node;
            System.out.println(indent + "Operator: " + bin.operator.value);
            printAST(bin.left, indent + "  ");
            printAST(bin.right, indent + "  ");
        }
    }

    public static void main(String[] args) {

        String input = "3 + 5 * (2 - 4)";

        List<Token> tokens = Lexer.tokenize(input);

        System.out.println("TOKENS:");
        for (Token t : tokens) {
            System.out.println(t);
        }

        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parse();

        System.out.println("\nAST:");
        printAST(ast, "");
    }
}