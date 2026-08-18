package UI;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

class ConsoleUtils {
    private final Scanner in;
    private final PrintStream out;

    public ConsoleUtils(InputStream in, PrintStream out) {
        this.in = new Scanner(in);
        this.out = out;
    }

    public void println(String text) {
        out.println(text);
    }

    public void println() {
        out.println();
    }

    public String readLine(String prompt) {
        out.print(prompt);
        return in.nextLine().trim();
    }

    public Integer readInt(String prompt) {
        String input = readLine(prompt);

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            out.println("\"" + input + "\" is not a valid number.");
            return null;
        }
    }

    public Integer pickFromList(List<String> labels, String prompt, String itemNoun) {
        for (int i = 0; i < labels.size(); i++) {
            out.println((i + 1) + ". " + labels.get(i));
        }

        Integer selection = readInt(prompt);
        if (selection == null) return null;

        if (selection < 1 || selection > labels.size()) {
            out.println("There is no " + itemNoun + " numbered " + selection + ".");
            return null;
        }

        return selection - 1;
    }

    public static String formatDecimal(double value) {
        return String.format("%.2f", value);
    }
}
