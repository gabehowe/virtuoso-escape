package org.virtuoso.escape.terminal;

import java.util.ArrayList;
import java.util.Scanner;
import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.Item;

/**
 * Human-verified tests for the terminal interface.
 *
 * @author gabri
 */
public class TerminalTests {
    GameProjection projection;
    Scanner scanner;

    /** Test game ending. */
    public void testEnding() {
        new TerminalDriver().menu_ending(scanner, projection);
    }

    public void testAlamanc() {
        var storey_i = GameInfo.instance().building().get(1);
        var sunflower_room = storey_i.rooms().get(1);
        var almanac = sunflower_room.entities().getFirst();
        GameState.instance().setCurrentFloor(storey_i);
        GameState.instance().setCurrentRoom(sunflower_room);
        GameState.instance().pickEntity(almanac);

        new TerminalDriver().gameLoop(scanner, projection);
    }

    public void testItems() {
        GameState.instance().clearItems();
        GameState.instance().addItem(Item.sealed_clean_food_safe_hummus);
        GameState.instance().addItem(Item.sunflower_seed_butter);
        GameState.instance().addItem(Item.left_bread);
        GameState.instance().addItem(Item.right_bread);
        new TerminalDriver().displayItems(scanner, projection);
    }

    /** Test typewriter text animation. */
    public void testTypewriterText() {
        new TerminalDriver()
                .typewriterDisplay(
                        scanner,
                        "Four score and seven years ago, our fathers set upon this continent a new nation, conceived"
                                + " in liberty and dedicated to the proposition that all men are created equal. Now we are"
                                + " engaged in a great civil war, testing whether that nation, or any nation so conceived"
                                + " and so dedicated, can long endure. We are now met on a great battlefield of that"
                                + " war.");
    }

    /** Test {@link FunString}. */
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
        //        testAlamanc();
        //        testItems();
        //        testEnding();
        //        testTypewriterText();
        //        testFunStrings();
    }
}
