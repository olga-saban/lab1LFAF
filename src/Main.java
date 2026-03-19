import java.util.List;

public class Main {
    public static void main(String[] args) {
        String input = "x = sin(3.14) + cos(2) * 5";

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}