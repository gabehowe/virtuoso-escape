package org.virtuoso.escape.terminal;

import org.virtuoso.escape.model.*;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.AccountManager;
import org.virtuoso.escape.model.account.Leaderboard;
import org.virtuoso.escape.speech.SpeechPlayer;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.virtuoso.escape.terminal.FunString.escape;


/**
 * Creates and runs the game.
 *
 * @author gabri
 */
public class TerminalDriver {
    private boolean DEBUG = true;
    private final Leaderboard leaderboard = new Leaderboard();

    /**
     * A simple tuple class.
     *
     * @param u   The first element.
     * @param v   The second element
     * @param <U> The type of the first element.
     * @param <V> The type of the second element.
     */
    private record P<U, V>(U u, V v) {
        /**
         * Shorthand for creating a pair.
         *
         * @param u   The first element.
         * @param v   The second element
         * @param <U> The type of the first element.
         * @param <V> The type of the second element.
         * @return a pair of {@code u} and {@code v}.
         */
        public static <U, V> P<U, V> of(U u, V v) {
            return new P<>(u, v);
        }
    }

    /**
     * Shorthand to narrow casting to r to Runnable.
     *
     * @param s The string for a name:action mapping.
     * @param r The action for a name:action mapping.
     * @return A pair of s and r.
     */
    // Avoid java's lack of belief in runnable type
    P<FunString, Runnable> fs_r(String s, Runnable r) {
        return P.of(new FunString(s), r);
    }

    /**
     * Shorthand to narrow casting to r to Runnable.
     *
     * @param s The string for a name:action mapping.
     * @param r The action for a name:action mapping.
     * @return A pair of s and r.
     */
    P<FunString, Runnable> fs_r(FunString s, Runnable r) {
        return P.of(s, r);
    }

    /**
     * Create a list of pairings.
     *
     * @param input Pairings.
     * @return A list of pairings.
     */
    @SafeVarargs
    final List<P<FunString, Runnable>> makeTuiActionMap(P<FunString, Runnable>... input) {
        return new ArrayList<>(Arrays.asList(input));
    }

    /**
     * Create a terminal with unique keys for action name: action pairings.
     *
     * @param scanner   The scanner to request input on.
     * @param tuiAction A list of name, action pairings. Note this is not an injective mapping.
     * @param status    A prompt to display before the actions.
     */
    // Sequenced retains order.
    void createActionInterface(Scanner scanner, List<P<FunString, Runnable>> tuiAction, String status) {
        SpeechPlayer.instance().playSoundbite(status);
        assert !tuiAction.isEmpty();
        Map<String, P<FunString, Runnable>> keyMap = new LinkedHashMap<>();
        // Create a unique key (or group of keys) to press for each action.
        for (P<FunString, Runnable> pair : tuiAction) {
            String key;
            String sourceKey = pair.u.rawText();
            int index = 0;
            int width = 1;
            while (true) { // Try to get a unique key.
                key = sourceKey.substring(index, index + width);
                if (key.matches("^\\w+$") && !keyMap.containsKey(key.toLowerCase())) break;
                if (index == sourceKey.length() - width) {
                    index = 0;
                    width++; // Try to get a wider key if no unique key can be found.
                }
                index++;
            }

            FunString name = new FunString(pair.u);
            FunString small = new FunString(key).underline().bold();

            name.replaceSubstring(index, index + width, small);
            keyMap.put(key.toLowerCase(), fs_r(name, pair.v));
        }
        Set<String> validResponses = keyMap.keySet();
        var prompts = keyMap.values().stream().map(P::u).toList();
        String delim = " ■ ";
        FunString prompt = FunString.join(delim, prompts);
        // If long, cut into two lines for readability -- We can only assume terminal width because of java's cross-platform nature.
        if (prompt.length() > 125) {
            prompt = FunString.join(delim, prompts.subList(0, prompts.size() / 2));
            prompt.add("\n");
            prompt.add(FunString.join(delim, prompts.subList(prompts.size() / 2, prompts.size())));
        }

        clearScreen();
        display(status);
        String response = validateInput(scanner, prompt.toString(), validResponses::contains);
        keyMap.get(response).v.run();
    }

    /**
     * Request user input until they provide valid input.
     *
     * @param scanner   The scanner to request input on.
     * @param prompt    The thing to ask the user.
     * @param predicate The function that validates the string.
     * @return Valid input.
     */
    String validateInput(Scanner scanner, String prompt, Predicate<String> predicate) {
        String scanAttempt;
        while (true) {
            display(prompt);
            scanAttempt = scanner.nextLine().strip().toLowerCase();
            System.out.print(escape(MOVE_LINE) + escape(CLEAR_BELOW));
            if (predicate.test(scanAttempt)) break;
            System.out.print(escape("1A") + escape(CLEAR_BELOW));
        }
        return scanAttempt;
    }

    /**
     * Request a valid username and password from the user.
     *
     * @param scanner       The scanner to request for input on.
     * @param authenticator The function to try to authenticate on, usually create account or login.
     * @param signal        the signal, represented as a character, that tells whether it was an attempt to log in or create an account.
     */
    void tryLogin(Scanner scanner, BiPredicate<String, String> authenticator, char signal) {
        String username, password;
        boolean flag = false;
        do {
            username = validateInput(scanner, "Enter your username:", i -> !i.isBlank());
            password = validateInput(scanner, "Enter your password:", i -> !i.isBlank());
            flag = authenticator.test(username.strip(), password.strip());
            // Move to the second console line
            System.out.print(escape("2;1H") + escape(CLEAR_BELOW));
            if (!flag) {
                display((new FunString(AccountManager.instance().getInvalidLoginInfo(username, password, signal))).red().toString());
            }
        } while (!flag);
        // logical negation:
        // If the choice is 0 and login
        // or the choice is 1 and createAccount, continue
    }

    /**
     * Display something to the user.
     *
     * @param display The format string to display.
     * @param args    Variables for the format string.
     */
    void display(String display, Object... args) {
        // wrapper function allows for flair
        System.out.printf(display + "\n", args);
    }

    /**
     * Display something to the user and wait for carriage return.
     *
     * @param scanner The scanner to wait for input on.
     * @param str     The format string to display
     * @param args    Variables for the format string.
     */
    void pauseDisplay(Scanner scanner, String str, Object... args) {
        display(str, args);
        scanner.nextLine();
    }

    /**
     * Animate a display to the user by showing each character printed sequentially, then wait.
     *
     * @param scanner The scanner to request input on.
     * @param str     The string to typewrite.
     */
    void typewriterDisplay(Scanner scanner, String str) {
        SpeechPlayer.instance().playSoundbite(str);
        long rate = 60;
        for (char s : str.toCharArray()) {
            System.out.print(s);
            try {
                Thread.sleep(1000 / rate);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println();
        scanner.nextLine();
    }

    /**
     * Clear the terminal window.
     */
    void clearScreen() {
        System.out.print(escape(CLEAR_RESET) + escape(CLEAR));
    }

    /**
     * Display all user items.
     *
     * @param scanner    The scanner to wait on.
     * @param projection The source for the items.
     */
    void displayItems(Scanner scanner, GameProjection projection) {
        List<String> names = projection.currentItems().stream().map(Item::itemName).toList();
        if (names.isEmpty()) {
            pauseDisplay(scanner, "You have no items.");
            return;
        }
        List<String> lines = new ArrayList<>();
        int padwidth = (names.stream().map(String::length)).max(Integer::compare).get();
        for (int i = 0; i < names.size(); i += 2) {
            Function<String, String> j = (str) -> String.format("1✖ %-" + padwidth + "s", str);
            String left = j.apply(names.get(i) + ((i + 1 == names.size()) ? "" : ","));
            String right = (i + 1 == names.size()) ? "" : j.apply(names.get(i + 1));
            lines.add(left + " " + right);
        }
        pauseDisplay(scanner, "You have: \n" + String.join("\n", lines));
    }

    /**
     * Present the end of the game, record score, and display the leaderboard.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void menu_ending(Scanner scanner, GameProjection projection) {
        // --- Display Credits/End Message ---
        List<String> contributors = new ArrayList<>(IntStream.range(0, 4).mapToObj(i -> GameInfo.instance().string("credits", "contributor_" + i)).toList());
        contributors = contributors.stream().map(it -> {
            var j = it.split("<");
            return j[0] + new FunString("<" + j[1]).italic().terminalColor(50);
        }).collect(Collectors.toList());
        Collections.shuffle(contributors);
        String formattedTime = String.format("%02d:%02d", GameState.instance().time().toMinutesPart(), GameState.instance().time().toSecondsPart());
        String scoremsg = String.format(GameInfo.instance().string("credits", "score"), formattedTime, GameState.instance().difficulty());
        List<String> msg = new ArrayList<>();
        msg.add(new FunString(scoremsg).purple().toString());
        msg.addAll(List.of(GameInfo.instance().string("credits", "message").split("\n")));
        msg.add("Credits:");
        msg.addAll(contributors);

        // Loop to display the credits line by line
        for (String s : msg) {
            System.out.println(s);
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        pauseDisplay(scanner, "");

        Account currentAccount = GameState.instance().account();

        String usernameToRecord = (currentAccount != null)
                ? currentAccount.username()
                : "Guest";

        leaderboard.recordSession(usernameToRecord);

        // Show
        leaderboard.showLeaderboard();
        pauseDisplay(scanner, "Press enter to logout");
        projection.logout();
    }

    /**
     * Ask the user to change rooms.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void menu_changeRoom(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap();
        for (Room room : projection.currentFloor().rooms()) {
            actions.add(fs_r(new FunString(room.name()).bold(), () -> projection.pickRoom(room)));
        }
        actions.add(fs_r(new FunString("Nevermind"), () -> {
        }));
        createActionInterface(scanner, actions, "Change room");
    }

    /**
     * Ask the user to perform room-specific actions.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void menu_roomActions(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap();
        // It makes no sense to change rooms if there are no rooms to change to!
        if (projection.currentFloor().rooms().size() > 1) {
            actions.add(fs_r(new FunString("Change room"), () -> this.menu_changeRoom(scanner, projection)));
        }
        for (Entity e : projection.currentRoom().entities()) {
            actions.add(fs_r(new FunString(e.state().name()).italic(), () -> projection.pickEntity(e)));
        }
        actions.add(fs_r(new FunString("Exit game"), () -> exit(scanner, projection)));
        actions.add(fs_r(new FunString("Options"), () -> menu_options(scanner, projection)));
        String prompt = String.format("%02d:%02d\n%s", projection.time().toMinutesPart(), projection.time().toSecondsPart(), projection.currentRoom()
                                                                                                                                       .introMessage());
        createActionInterface(scanner, actions, prompt);

    }

    /**
     * Ask the user to perform entity-specific actions.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void menu_entityAction(Scanner scanner, GameProjection projection) {
        projection.currentEntity().ifPresent(e -> e.state().introduce());
        var actions = makeTuiActionMap(
        );
        var capabilities = projection.capabilities();
        if (capabilities.interact()) actions.add(fs_r(new FunString("Interact").blue(), projection::interact));
        if (capabilities.inspect()) actions.add(fs_r(new FunString("Inspect").green(), projection::inspect));
        if (capabilities.inspect()) actions.add(fs_r(new FunString("Attack").red(), projection::attack));
        if (capabilities.input())
            actions.add(fs_r(new FunString("Speak").purple(), () -> projection.input(validateInput(scanner, "What would you like to say? ", _ -> true))));
        if (!projection.currentItems().isEmpty())
            actions.add(fs_r(new FunString("Items"), () -> this.displayItems(scanner, projection)));

        actions.add(fs_r(new FunString("Leave"), projection::leaveEntity));

        var itemsCache = projection.currentItems();
        createActionInterface(scanner, actions, projection.currentMessage().orElse(""));
        var newItems = new ArrayList<>(projection.currentItems());
        projection.currentMessage().ifPresent(i -> typewriterDisplay(scanner, i));
        if (newItems.size() > itemsCache.size()) {
            newItems.removeIf(itemsCache::contains);
            System.out.print("\007");
            pauseDisplay(scanner, "You received " + newItems.getFirst().itemName() + ".");
        }
    }

    /**
     * Ask the user to change the difficulty.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void menu_difficulty(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap();

        for (var diff : Difficulty.values()) {
            actions.add(fs_r(new FunString(diff.name()).terminalColor(diff.ordinal() + 196), () -> projection.setDifficulty(diff)));
        }
        actions.add(fs_r(new FunString("Nevermind"), () -> {
        }));
        createActionInterface(scanner, actions, "Choose difficulty");
    }

    /**
     * Debug floor switching menu. Is inaccessible if {@link TerminalDriver#DEBUG} variable is {@code false}.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void menu_debugSwitchFloor(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap();
        for (Floor floor : GameInfo.instance().building()) {
            actions.add(fs_r(new FunString(floor.id()).green(), () -> GameState.instance().setCurrentFloor(floor)));
        }
        actions.add(fs_r(new FunString("Nevermind"), () -> {
        }));
        createActionInterface(scanner, actions, "Pick floor");
    }

    /**
     * Debug options. Is inaccessible if  {@link TerminalDriver#DEBUG} variable is {@code false}.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void menu_debug(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap(
                fs_r("Switch floor", () -> menu_debugSwitchFloor(scanner, projection))
        );
        actions.forEach((k) -> k.u.green());
        actions.add(fs_r(new FunString("Nevermind"), () -> {
        }));
        createActionInterface(scanner, actions, "");
    }

    /**
     * Ask the user to select an option to change.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void menu_options(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap(
                fs_r("Set difficulty", () -> menu_difficulty(scanner, projection)),
                fs_r("Nevermind", () -> {
                })
        );
        if (DEBUG) {
            actions.addFirst(fs_r(new FunString("DEBUG").green(), () -> menu_debug(scanner, projection)));
        }
        createActionInterface(scanner, actions, "Options");
    }

    /**
     * Exit the game.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void exit(Scanner scanner, GameProjection projection) {
        projection.logout();
        display("Logged out.\nThanks for playing!");
        System.exit(1);
    }

    /**
     * Continuously ask the user for context-specific input.
     *
     * @param scanner    The scanner to request input on.
     * @param projection The source for data.
     */
    void gameLoop(Scanner scanner, GameProjection projection) {
        while (true) {
            if (projection.isEnded()) {
                // If the game is over (due to win or time run out), call menu_ending.
                // menu_ending now handles: display, recording, leaderboard, pause, and logout.
                this.menu_ending(scanner, projection);
                return; // Exit the game loop
            }
            if (projection.currentEntity().isPresent()) menu_entityAction(scanner, projection);
            else menu_roomActions(scanner, projection);
        }
    }

    /**
     * Program entrance.
     */
    // See JEP 495
    void main() {
        Scanner scanner = new Scanner(System.in);
        GameProjection projection = new GameProjection();
        // Ensure these are initialized
        var actions = makeTuiActionMap(
                fs_r("Login", () -> tryLogin(scanner, projection::login, 'l')),
                fs_r("Create Account", () -> tryLogin(scanner, projection::createAccount, 'c'))
        );
        createActionInterface(scanner, actions, "Welcome to Virtuoso Escape!");
        gameLoop(scanner, projection);
    }


    private static String CLEAR = "2J";
    private static String CLEAR_RESET = "H";
    private static String MOVE_LINE = "1A";
    private static String CLEAR_BELOW = "0J";

}
