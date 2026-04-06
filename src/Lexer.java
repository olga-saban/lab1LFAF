import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String text;
    private int pos;
    private char currentChar;

    public Lexer(String text) {
        this.text = text;
        this.pos = 0;
        this.currentChar = text.length() > 0 ? text.charAt(0) : '\0';
    }

    private void advance() {
        pos++;
        if (pos >= text.length()) {
            currentChar = '\0';
        } else {
            currentChar = text.charAt(pos);
        }
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    private String number() {
        StringBuilder result = new StringBuilder();
        boolean hasDot = false;

        while (Character.isDigit(currentChar) || currentChar == '.') {
            if (currentChar == '.') {
                if (hasDot) break;
                hasDot = true;
            }
            result.append(currentChar);
            advance();
        }

        return result.toString();
    }

    private String identifier() {
        StringBuilder result = new StringBuilder();

        while (Character.isLetter(currentChar)) {
            result.append(currentChar);
            advance();
        }

        return result.toString();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (currentChar != '\0') {

            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
                continue;
            }

            if (Character.isDigit(currentChar)) {
                String num = number();
                if (num.contains(".")) {
                    tokens.add(new Token(TokenType.FLOAT, num));
                } else {
                    tokens.add(new Token(TokenType.INTEGER, num));
                }
                continue;
            }

            if (Character.isLetter(currentChar)) {
                String id = identifier();

                switch (id) {
                    case "sin":
                        tokens.add(new Token(TokenType.SIN, id));
                        break;
                    case "cos":
                        tokens.add(new Token(TokenType.COS, id));
                        break;
                    default:
                        tokens.add(new Token(TokenType.IDENTIFIER, id));
                }
                continue;
            }

            switch (currentChar) {
                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+"));
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-"));
                    break;
                case '*':
                    tokens.add(new Token(TokenType.MUL, "*"));
                    break;
                case '/':
                    tokens.add(new Token(TokenType.DIV, "/"));
                    break;
                case '=':
                    tokens.add(new Token(TokenType.ASSIGN, "="));
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LPAREN, "("));
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RPAREN, ")"));
                    break;
                default:
                    throw new RuntimeException("Unknown character: " + currentChar);
            }

            advance();
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}