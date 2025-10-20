package org.virtuoso.escape.terminal;

import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.terminal.TerminalDriver;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Human-verified tests for the terminal interface.
 * @author gabri
 */
public class TerminalTests {
    GameProjection projection;
    Scanner scanner;

    /**
     * Test game ending.
     */
    public void testEnding() {
        new TerminalDriver().menu_ending(scanner, projection);
    }

    /**
     * Test typewriter text animation.
     */
    public void testTypewriterText() {
        new TerminalDriver().typewriterDisplay(scanner, "Four score and seven years ago, our fathers set upon this continent a new nation, conceived in liberty and dedicated to the proposition that all men are created equal. Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived and so dedicated, can long endure. We are now met on a great battlefield of that war.");
    }

    /**
     * Test {@link FunString}.
     */
    public void testFunStrings() {
        var strings = new ArrayList<String>();
        var fs = new FunString("Basic text");
        strings.add(fs.toString());
        strings.add(fs.rawText());
        strings.add(fs.purple().toString());
        strings.add(fs.terminalColor(80).toString());
        strings.add(fs.italic().toString());
        strings.add(fs.bold().toString());
        fs = new FunString("Basic text");
        System.out.println(fs.styleCodes().stream().map(i -> i.substring(1)).toList());
        fs.replaceSubstring(6, fs.length(), new FunString("italic!").italic().purple());
        System.out.println(fs.styleCodes().stream().map(i -> i.substring(1)).toList());
        strings.add(fs.bold().toString());
        strings.forEach(System.out::println);
    }

    void main() {
        scanner = new Scanner(System.in);
        projection = new GameProjection();
        projection.login("j", "j");
        testEnding();
//        testTypewriterText();
//        testFunStrings();
    }
}
