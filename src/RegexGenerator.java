import java.util.*;

public class RegexGenerator {
    static final int MAX_REPEAT = 5;

    public static void main(String[] args) {
        String[] regexes = {
                "(S|T)(U|V)W*Y+24",
                "L(M|N)O{3}P*Q(2|3)",
                "R*S(T|U|V)W(X|Y|Z){2}"
        };

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
    }

    interface Node {
        List<String> generate(int maxRepeat, int maxResults);
    }

    static class Literal implements Node {
        String value;

        Literal(String value) {
            this.value = value;
        }

        public List<String> generate(int maxRepeat, int maxResults) {
            return List.of(value);
        }
    }

    static class Concat implements Node {
        List<Node> parts;

        Concat(List<Node> parts) {
            this.parts = parts;
        }

        public List<String> generate(int maxRepeat, int maxResults) {
            List<String> result = new ArrayList<>();
            result.add("");

            for (Node node : parts) {
                List<String> next = node.generate(maxRepeat, maxResults);
                result = concat(result, next, maxResults);
            }
            return result;
        }
    }

    static class Alternation implements Node {
        List<Node> options;

        Alternation(List<Node> options) {
            this.options = options;
        }

        public List<String> generate(int maxRepeat, int maxResults) {
            List<String> result = new ArrayList<>();
            for (Node node : options) {
                result.addAll(node.generate(maxRepeat, maxResults));
            }
            return result;
        }
    }

    static class Repeat implements Node {
        Node child;
        int min;
        int max;

        Repeat(Node child, int min, int max) {
            this.child = child;
            this.min = min;
            this.max = max;
        }

        public List<String> generate(int maxRepeat, int maxResults) {
            List<String> result = new ArrayList<>();
            int limit = Math.min(max, maxRepeat);
            List<String> base = child.generate(maxRepeat, maxResults);

            for (int count = min; count <= limit; count++) {
                List<String> current = new ArrayList<>();
                current.add("");
                for (int i = 0; i < count; i++) {
                    current = concat(current, base, maxResults);
                }
                result.addAll(current);
            }
            return result;
        }
    }

    static class RegexParser {
        String pattern;
        int pos = 0;
        List<String> steps = new ArrayList<>();

        RegexParser(String pattern) {
            this.pattern = pattern;
        }

        Node parse() {
            return parseExpression();
        }

        // expression -> term ('|' term)*
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

        // term -> factor+
        Node parseTerm() {
            List<Node> factors = new ArrayList<>();
            while (current() != '\0' && current() != ')' && current() != '|') {
                factors.add(parseFactor());
            }

            if (factors.size() == 1) return factors.get(0);
            steps.add("Build concatenation");
            return new Concat(factors);
        }

        // factor -> base (* + {m})
        Node parseFactor() {
            Node base = parseBase();

            if (current() == '*') {
                advance();
                steps.add("Apply repeat *");
                return new Repeat(base, 0, MAX_REPEAT);
            }
            if (current() == '+') {
                advance();
                steps.add("Apply repeat +");
                return new Repeat(base, 1, MAX_REPEAT);
            }
            if (current() == '{') {
                advance();
                int number = readNumber();
                expect('}');
                steps.add("Apply repeat {" + number + "}");
                return new Repeat(base, number, number);
            }
            return base;
        }

        // base -> literal | (expression)
        Node parseBase() {
            if (current() == '(') {
                advance();
                Node node = parseExpression();
                expect(')');
                steps.add("Close group");
                return node;
            }

            char ch = current();
            advance();
            steps.add("Read literal '" + ch + "'");
            return new Literal(String.valueOf(ch));
        }

        // =========================
        // HELPERS
        // =========================
        char current() {
            return (pos >= pattern.length()) ? '\0' : pattern.charAt(pos);
        }

        void advance() {
            pos++;
        }

        void expect(char c) {
            if (current() != c) throw new RuntimeException("Expected " + c);
            advance();
        }

        int readNumber() {
            StringBuilder sb = new StringBuilder();
            while (Character.isDigit(current())) {
                sb.append(current());
                advance();
            }
            return Integer.parseInt(sb.toString());
        }
    }

    // =========================
    // CONCAT HELPER
    // =========================
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
}