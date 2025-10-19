package terminal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


// Holder for decorated strings for terminal output.
public record FunString(List<Object /*FunString, char*/> content, List<String> styleCodes, List<String> resetCodes) {

    public static String escape(String innerCode) {
        return String.format("\033[%s", innerCode);
    }

    public FunString(String content) {
        this(new ArrayList<>(FunString.stringChars(content)), new ArrayList<>(), new ArrayList<>());
    }

    public FunString(FunString funString) {
        this(new ArrayList<>(funString.content.stream().toList()), new ArrayList<>(funString.styleCodes), new ArrayList<>(funString.resetCodes));
    }

    public FunString(List<Object> strs) {
        this(new ArrayList<>(strs), new ArrayList<>(), new ArrayList<>());
    }

    public static FunString join(String delimiter, Iterable<FunString> funStrings) {
        Iterator<FunString> iterator = funStrings.iterator();
        assert iterator.hasNext();
        FunString accumulator = new FunString(List.of(iterator.next()));
        iterator.forEachRemaining(i -> {
            accumulator.add(delimiter);
            accumulator.add(i);
        });
        return accumulator;
    }

    @Override
    public String toString() {
        var result = new StringBuilder();
        var substring = String.join("", this.content.stream().map(Object::toString).toList());
        result.append(substring); // will automatically call toString on everything
        result.insert(0, String.join("", styleCodes));
        result.append(String.join("", resetCodes));
        return result.toString();
    }

    public int length() {
        return this.rawText().length();
    }

    public String rawText() {
        var result = new StringBuilder();
        for (Object i : this.content) {
            // JEP 394
            if (i instanceof FunString fs) {
                result.append(fs.rawText());
            } else {
                result.append(i.toString());
            }
        }
        return result.toString();
    }

    public void add(FunString fs) {
        this.content.add(fs);
    }

    public void add(Character s) {
        this.content.add(s);
    }

    public void add(String s) {
        this.content.addAll(this.stringChars(s));
    }

    // doesn't support FunString collisions
    public void replaceSubstring(int start, int end, FunString toReplace) {
        for (int i = end-1; i >= start; i--) {
            this.content.remove(i);
        }
        this.content.add(start, toReplace);
    }

    public FunString underline() {
        this.styleCodes.add(UNDERLINE);
        this.resetCodes.add(UNDERLINE_OFF);
        return this;
    }

    public FunString italic() {
        this.styleCodes.add(ITALIC);
        this.resetCodes.add(ITALIC_OFF);
        return this;
    }

    public FunString bold() {
        this.styleCodes.add(BOLD);
        this.resetCodes.add(BOLD_OFF);
        return this;
    }

    public FunString red() {
        this.styleCodes.add(RED_FG);
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }

    public FunString blue() {
        this.styleCodes.add(BLUE_FG);
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }

    public FunString green() {
        this.styleCodes.add(GREEN_FG);
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }

    public FunString purple() {
        this.styleCodes.add(PURPLE_FG);
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }
    public FunString terminalColor(int color) {
        assert color > 0 && color < 256;
        this.styleCodes.add(escape("38;5;" + color + "m"));
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }

    private static List<Character> stringChars(String s) {
        return s.chars().mapToObj(c -> (char) c).toList();
    }

    private static String RED_FG = escape("31m");
    private static String BLUE_FG = escape("38;5;44m");
    private static String GREEN_FG = escape("38;5;76m");
    private static String PURPLE_FG = escape("38;5;201m");
    private static String DEFAULT_FG = escape("39m");
    private static String BOLD = escape("1m"); // for controls
    private static String BOLD_OFF = escape("22m");

    private static String UNDERLINE = escape("4m"); // for controls
    private static String UNDERLINE_OFF = escape("24m");
    private static String RESET = escape("0m");
    private static String ITALIC = escape("3m"); // for entities
    private static String ITALIC_OFF = escape("23m");

}
