package org.virtuoso.escape.terminal;

import org.virtuoso.escape.model.*;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.virtuoso.escape.terminal.FunString.escape;


/**
 * @author gabri
 */
public class TerminalDriver {
    private boolean DEBUG = true;
    // Tree
    private record P<U, V> (U u, V v) {
        public static <U, V> P<U, V> of(U u, V v){
            return new P<>(u,v);
        }
    }

    // Avoid java's lack of belief in runnable type
    P<FunString, Runnable> fs_r(String s, Runnable r) {
        return P.of(new FunString(s), r);
    }

    P<FunString, Runnable> fs_r(FunString s, Runnable r) {
        return P.of(s, r);
    }

    @SafeVarargs
    final List<P<FunString, Runnable>> makeTuiActionMap(P<FunString, Runnable>... input) {
        return new ArrayList<>(Arrays.asList(input));
    }


    // Sequenced retains order.
    void createActionInterface(Scanner scanner, List<P<FunString, Runnable>> tuiAction, String status) {
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
            keyMap.put(key.toLowerCase(), P.of(name, pair.v));
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

    String validateInput(Scanner scanner, String prompt, Predicate<String> predicate) {
        String scanAttempt;
        while (true) {
            display(prompt);
            scanAttempt = scanner.nextLine().strip().toLowerCase();
            System.out.print(escape( MOVE_LINE) + escape(CLEAR_BELOW));
            if (predicate.test(scanAttempt)) break;
            System.out.print(escape("1A") + escape(CLEAR_BELOW));
        }
        return scanAttempt;
    }

    void tryLogin(Scanner scanner, GameProjection projection, BiPredicate<String, String> type) {
        String username, password;
        boolean flag = false;
        do {
            username = validateInput(scanner, "Enter your username:", i -> !i.isBlank());
            password = validateInput(scanner, "Enter your password:", i -> !i.isBlank());
            flag = type.test(username.strip(), password.strip());
            // Move to the second console line
            System.out.print(escape("2;1H") + escape(CLEAR_BELOW));
            if (!flag) display(new FunString("Failed to process login.").red().toString());
        } while (!flag);
        // logical negation:
        // If the choice is 0 and login
        // or the choice is 1 and createAccount, continue
    }

    void display(String display, Object... args) {
        // wrapper function allows for flair
        System.out.printf(display + "\n", args);
    }

    void pauseDisplay(Scanner scanner, String str, Object... args) {
        display(str, args);
        scanner.nextLine();
    }
    void typewriterDisplay(Scanner scanner, String str) {
        long rate = 60;
        for(char s: str.toCharArray()){
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

    void clearScreen() {
        System.out.print(escape(CLEAR_RESET) + escape(CLEAR));
    }

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

    void menu_ending(Scanner scanner, GameProjection projection) {
        List<String> contributors = new ArrayList<>(IntStream.range(0, 4).mapToObj(i -> GameInfo.instance().string("credits", "contributor_" + i)).toList());
        contributors = contributors.stream().map(it -> {
            var j = it.split("<");
            return j[0] + new FunString("<" + j[1]).italic().terminalColor(50);
        }).collect(Collectors.toList());
        Collections.shuffle(contributors);
        String formattedTime = GameState.instance().time().toMinutesPart() + ":"+ GameState.instance().time().toSecondsPart();
        String scoremsg = String.format(GameInfo.instance().string("credits","score"), formattedTime, GameState.instance().difficulty());
        List<String> msg = new ArrayList<>();
        msg.addAll(List.of(new FunString(scoremsg).purple().toString()));
        msg.addAll(List.of(GameInfo.instance().string("credits", "message").split("\n")));
        msg.add("Credits:");
        msg.addAll(contributors);
        for (String s : msg) {
            System.out.println(s);
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        pauseDisplay(scanner, "");

    }

    void menu_changeRoom(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap();
        for (Room room : projection.currentFloor().rooms()) {
            actions.add(P.of(new FunString(room.name()).bold(), () -> projection.pickRoom(room)));
        }
        actions.add(P.of(new FunString("Nevermind"), () -> {
        }));
        createActionInterface(scanner, actions, "Change room");
    }

    void menu_roomActions(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap();
        // It makes no sense to change rooms if there are no rooms to change to!
        if (projection.currentFloor().rooms().size() > 1) {
            actions.add(P.of(new FunString("Change room"), () -> this.menu_changeRoom(scanner, projection)));
        }
        for (Entity e : projection.currentRoom().entities()) {
            actions.add(P.of(new FunString(e.name()).italic(), () -> projection.pickEntity(e)));
        }
        actions.add(P.of(new FunString("Exit game"), () -> menu_exit(scanner, projection)));
        actions.add(P.of(new FunString("Options"), () -> menu_options(scanner, projection)));
        String prompt = projection.time().toMinutesPart() + ":" + projection.time().toSecondsPart() + "\n" + projection.currentRoom().introMessage();
        createActionInterface(scanner, actions, prompt);

    }

    void menu_entityAction(Scanner scanner, GameProjection projection) {
        projection.currentEntity().ifPresent(Entity::introduce);
        var actions = makeTuiActionMap(
                fs_r(new FunString("Interact").blue(), projection::interact),
                fs_r(new FunString("Inspect").green(), projection::inspect),
                fs_r(new FunString("Attack").red(), projection::attack),
                fs_r(new FunString("Speak").purple(), () -> projection.input(validateInput(scanner, "What would you like to say? ", _ -> true)))
                );
        if (!projection.currentItems().isEmpty()) actions.add(P.of(new FunString("Items"), () -> this.displayItems(scanner, projection)));

        actions.add(P.of(new FunString("Leave"), projection::leaveEntity));

        var itemsCache = projection.currentItems();
        createActionInterface(scanner, actions, projection.currentMessage().orElse(""));
        var newItems = new ArrayList<>(projection.currentItems());
        projection.currentMessage().ifPresent(i -> typewriterDisplay(scanner, i));
        if (newItems.size() > itemsCache.size()) {
            newItems.removeIf(itemsCache::contains);
            pauseDisplay(scanner, "You received " + newItems.getFirst().itemName() + ".");
        }
    }

    void menu_difficulty(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap();

        for (var diff : Difficulty.values()) {
            actions.add(P.of(new FunString(diff.name()).terminalColor(diff.ordinal() + 196), () -> projection.setDifficulty(diff)));
        }
        actions.add(P.of(new FunString("Nevermind"), () -> {}));
        createActionInterface(scanner, actions, "Choose difficulty");
    }
    void menu_debugSwitchFloor(Scanner scanner, GameProjection projection){
        var actions = makeTuiActionMap();
        for (Floor floor: GameInfo.instance().building()) {
            actions.add(P.of(new FunString(floor.id()).green(), () -> GameState.instance().setCurrentFloor(floor)));
        }
        actions.add(P.of(new FunString("Nevermind"), ()-> {}));
        createActionInterface(scanner, actions, "Pick floor");
    }
    void menu_debug(Scanner scanner, GameProjection projection){
        var actions = makeTuiActionMap(
                fs_r("Switch floor", () -> menu_debugSwitchFloor(scanner, projection))
        );
        actions.forEach((k) -> k.u.green());
        actions.add(P.of(new FunString("Nevermind"), ()-> {}));
        createActionInterface(scanner, actions, "");
    }

    void menu_options(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap(
                fs_r("Set difficulty", () -> menu_difficulty(scanner, projection)),
                fs_r("Nevermind", () -> {})
        );
        if (DEBUG){
            actions.addFirst(P.of(new FunString("DEBUG").green(), () -> menu_debug(scanner, projection)));
        }
        createActionInterface(scanner, actions, "Options");
    }

    void menu_exit(Scanner scanner, GameProjection projection) {
        projection.logout();
        display("Logged out.\nThanks for playing!");
        System.exit(1);
    }

    void gameLoop(Scanner scanner, GameProjection projection) {
        while (true) {
            if (projection.isEnded()){
                this.menu_ending(scanner,projection);
                projection.logout();
                return;
            }
            if (projection.currentEntity().isPresent()) menu_entityAction(scanner, projection);
            else menu_roomActions(scanner, projection);
        }
    }

    // See JEP 495
    void main() {
        Scanner scanner = new Scanner(System.in);
        GameProjection projection = new GameProjection();
        // Ensure these are initialized
        var actions = makeTuiActionMap(
                fs_r("Login", () -> tryLogin(scanner, projection, projection::login)),
                fs_r("Create Account", () -> tryLogin(scanner, projection, projection::createAccount))
        );


        createActionInterface(scanner, actions, "Welcome to Virtuoso Escape!");
        gameLoop(scanner, projection);
    }


    private static String CLEAR = "2J";
    private static String CLEAR_RESET = "H";
    private static String MOVE_LINE = "1A";
    private static String CLEAR_BELOW = "0J";

}
