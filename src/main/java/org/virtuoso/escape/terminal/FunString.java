package org.virtuoso.escape.terminal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holder for decorated strings for terminal output.
 *
 * @param content The child nodes to output, FunString or char
 * @param styleCodes The console style codes to prefix to the string.
 * @param resetCodes The console style reset codes to suffix to the string.
 * @author gabri
 */
public record FunString(List<Object /*FunString, char*/> content, List<String> styleCodes, List<String> resetCodes) {

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

    /**
     * Create an {@link FunString} from a {@link String}.
     *
     * @param content the string to create this with.
     */
    public FunString(String content) {
        this(new ArrayList<>(FunString.stringChars(content)), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Clone another {@link FunString}.
     *
     * @param funString the {@link FunString} to clone.
     */
    public FunString(FunString funString) {
        this(
                new ArrayList<>(funString.content.stream().toList()),
                new ArrayList<>(funString.styleCodes),
                new ArrayList<>(funString.resetCodes));
    }

    /**
     * Create a {@link FunString} from a {@link List} of {@link FunString} and char.
     *
     * @param strs a {@link List} of {@link FunString} and {@code char}.
     */
    public FunString(List<Object> strs) {
        this(new ArrayList<>(strs), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Create a terminal escape code.
     *
     * @param innerCode the inner part of the code.
     * @return An escaped ANSI terminal code.
     */
    public static String escape(String innerCode) {
        return String.format("\033[%s", innerCode);
    }

    /**
     * Join {@link Iterable<FunString>} by a delimiter. Similar to {@link String#join(CharSequence, CharSequence...)}
     *
     * @param delimiter The string to delimit by.
     * @param funStrings The FunStrings to join
     * @return the joined object.
     */
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

    /**
     * Convert a string into a list of characters.
     *
     * @param s the string to convert.
     * @return a list of characters.
     */
    private static List<Character> stringChars(String s) {
        return s.chars().mapToObj(c -> (char) c).toList();
    }

    /**
     * Convert this to a string with escape codes.
     *
     * @return A string with escape codes.
     */
    @Override
    public String toString() {
        var result = new StringBuilder();
        var substring =
                String.join("", this.content.stream().map(Object::toString).toList());
        result.append(substring); // will automatically call toString on everything
        result.insert(0, String.join("", styleCodes));
        result.append(String.join("", resetCodes));
        return result.toString();
    }

    /**
     * Returns the length of the string without escape codes.
     *
     * @return the visual length.
     */
    public int length() {
        return this.rawText().length();
    }

    /**
     * The text content without escape codes.
     *
     * @return A string with no escape codes.
     */
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

    /**
     * Add another {@link FunString} to the end of this.
     *
     * @param fs the object to append.
     */
    public FunString add(FunString fs) {
        this.content.add(fs);
        return this;
    }

    /**
     * Add another {@link Character} to this.
     *
     * @param s The object to append.
     */
    public FunString add(Character s) {
        this.content.add(s);
        return this;
    }

    /**
     * Add another {@link String} to this.
     *
     * @param s The object to append.
     */
    public FunString add(String s) {
        this.content.addAll(stringChars(s));
        return this;
    }

    /**
     * Replaces part of this object with {@code toReplace}
     *
     * @param start The index to start replacing at.
     * @param end The index to stop replacing before.
     * @param toReplace The {@link FunString} to replace the substring with.
     * @apiNote The behavior when {@link FunString}s collide is unknown.
     */
    // doesn't support FunString collisions
    public void replaceSubstring(int start, int end, FunString toReplace) {
        for (int i = end - 1; i >= start; i--) {
            this.content.remove(i);
        }
        this.content.add(start, toReplace);
    }

    /**
     * Add an underline decoration to this.
     *
     * @return This with an underline decoration.
     */
    public FunString underline() {
        this.styleCodes.add(UNDERLINE);
        this.resetCodes.add(UNDERLINE_OFF);
        return this;
    }

    /**
     * Add an italic decoration to this.
     *
     * @return This with an italic decoration.
     */
    public FunString italic() {
        this.styleCodes.add(ITALIC);
        this.resetCodes.add(ITALIC_OFF);
        return this;
    }

    /**
     * Add a bold decoration to this.
     *
     * @return This with a bold decoration.
     */
    public FunString bold() {
        this.styleCodes.add(BOLD);
        this.resetCodes.add(BOLD_OFF);
        return this;
    }

    /**
     * Make this red.
     *
     * @return This, but red.
     */
    public FunString red() {
        this.styleCodes.add(RED_FG);
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }

    /**
     * Make this blue.
     *
     * @return This, but blue.
     */
    public FunString blue() {
        this.styleCodes.add(BLUE_FG);
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }

    /**
     * Make this green.
     *
     * @return This, but green.
     */
    public FunString green() {
        this.styleCodes.add(GREEN_FG);
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }

    /**
     * Make this purple.
     *
     * @return This, but purple.
     */
    public FunString purple() {
        this.styleCodes.add(PURPLE_FG);
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }

    /**
     * Color this with an 8-bit terminal color.
     *
     * @param color A color index from 0-256.
     * @return This, but colored.
     */
    public FunString terminalColor(int color) {
        assert color > 0 && color < 256;
        this.styleCodes.add(escape("38;5;" + color + "m"));
        this.resetCodes.add(DEFAULT_FG);
        return this;
    }
}
