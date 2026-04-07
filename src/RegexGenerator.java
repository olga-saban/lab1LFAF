import java.util.*;

public class RegexGenerator {

    static final int MAX_REPEAT = 5;

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

    // (S|T)(U|V)W*Y+24
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

    // L(M|N)O^3P*Q(2|3)
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

    // R*S(T|U|V)W(X|Y|Z)^2
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

    static void explainR1() {
        System.out.println("Processing steps:");
        System.out.println("1. Choose one symbol from (S | T)");
        System.out.println("2. Choose one symbol from (U | V)");
        System.out.println("3. Repeat 'W' from 0 to " + MAX_REPEAT + " times (*)");
        System.out.println("4. Repeat 'Y' from 1 to " + MAX_REPEAT + " times (+)");
        System.out.println("5. Append constant '24'");
        System.out.println("6. Combine all parts into final string\n");
    }

    static void explainR2() {
        System.out.println("Processing steps:");
        System.out.println("1. Start with 'L'");
        System.out.println("2. Choose one symbol from (M | N)");
        System.out.println("3. Append 'O' exactly 3 times (^3)");
        System.out.println("4. Repeat 'P' from 0 to " + MAX_REPEAT + " times (*)");
        System.out.println("5. Append 'Q'");
        System.out.println("6. Choose one symbol from (2 | 3)");
        System.out.println("7. Combine all parts into final string\n");
    }

    static void explainR3() {
        System.out.println("Processing steps:");
        System.out.println("1. Repeat 'R' from 0 to " + MAX_REPEAT + " times (*)");
        System.out.println("2. Append 'S'");
        System.out.println("3. Choose one symbol from (T | U | V)");
        System.out.println("4. Append 'W'");
        System.out.println("5. Choose two symbols from (X | Y | Z) (^2)");
        System.out.println("6. Combine all parts into final string\n");
    }

    static void printSample(List<String> list) {
        for (String s : list) {
            System.out.println(s);
        }
    }
}