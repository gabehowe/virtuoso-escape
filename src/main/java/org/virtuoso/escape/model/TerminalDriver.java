package org.virtuoso.escape.model;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;


public class TerminalDriver {
    // Tree

    // Avoid java's lack of belief in runnable type
    Map.Entry<FunString, Runnable> fs_r(String s, Runnable r) {
        return Map.entry(new FunString(s), r);
    }

    Map.Entry<FunString, Runnable> fs_r(FunString s, Runnable r) {
        return Map.entry(s, r);
    }

    @SafeVarargs
    final SequencedMap<FunString, Runnable> makeTuiActionMap(Map.Entry<FunString, Runnable>... input) {
        SequencedMap<FunString, Runnable> map = new LinkedHashMap<>();
        Arrays.stream(input).forEachOrdered(i -> map.put(i.getKey(), i.getValue()));
        return map;
    }

    ;

    // Sequenced retains order.
    void createActionInterface(Scanner scanner, SequencedMap<FunString, Runnable> tuiAction) {
        assert !tuiAction.isEmpty();
        Map<String, Map.Entry<FunString, Runnable>> keyMap = new LinkedHashMap<>();
        // Create a unique key (or group of keys) to press for each action.
        for (Map.Entry<FunString, Runnable> pair : tuiAction.entrySet()) {
            String key;
            String sourceKey = pair.getKey().rawText();
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
            ;
            FunString name = new FunString(pair.getKey());
            FunString small = new FunString(key).underline();

            name.replaceSubstring(index, index + width, small);
            keyMap.put(key.toLowerCase(), Map.entry(name, pair.getValue()));
        }
        Set<String> validResponses = keyMap.keySet();
        var prompts = keyMap.values().stream().map(Map.Entry::getKey).toList();
        String delim = " ░ ";
        FunString prompt = FunString.join(delim, prompts);
        // If long, cut into two lines for readability -- We can only assume terminal width because of java's cross-platform nature.
        if (prompt.length() > 75) {
            prompt = FunString.join(delim, prompts.subList(0, prompts.size() / 2));
            prompt.add("\n");
            prompt.add(FunString.join(delim, prompts.subList(prompts.size() / 2, prompts.size())));
        }

        String response = validateInput(scanner, prompt.toString(), validResponses::contains);
        keyMap.get(response).getValue().run();
    }

    String validateInput(Scanner scanner, String prompt, Predicate<String> predicate) {
        String scanAttempt;
        do {
            display(prompt);
            scanAttempt = scanner.nextLine().strip().toLowerCase();
        }
        while (!predicate.test(scanAttempt));
        return scanAttempt;
    }


    void tryLogin(Scanner scanner, GameProjection projection, BiPredicate<String, String> type) {
        String username, password;
        boolean flag = false;
        do {
            username = validateInput(scanner, "Enter your username:", i -> !i.isBlank());
            password = validateInput(scanner, "Enter your password:", i -> !i.isBlank());
            flag = type.test(username.strip(), password.strip());
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

    void changeRoom(Scanner scanner, GameProjection projection) {
        var actions = makeTuiActionMap();
        for (Room room : projection.currentFloor().rooms()) {
            actions.put(new FunString(room.name()).bold(), () -> projection.pickRoom(room));
        }
        actions.put(new FunString("Nevermind"), () -> {});
        createActionInterface(scanner, actions);
    }

    void roomActions(Scanner scanner, GameProjection projection) {
        display(projection.currentRoom().introMessage());
        SequencedMap<FunString, Runnable> actions = makeTuiActionMap(
        );
        // It makes no sense to change rooms if there are no rooms to change to!
        if (projection.currentFloor().rooms().size() > 1) {
            actions.put(new FunString("Change room"), () -> this.changeRoom(scanner, projection));
        }
        for (Entity e : projection.currentRoom().entities()) {
            actions.put(new FunString(e.name()).italic().bold(), () -> projection.pickEntity(e));
        }
        actions.put(new FunString("Exit game"), () -> exit(scanner, projection));
        createActionInterface(scanner, actions);

    }

    void pickEntityAction(Scanner scanner, GameProjection projection) {
        projection.currentEntity().ifPresent(Entity::introduce);
        display(projection.currentMessage());
        var actions = makeTuiActionMap(
                fs_r("Interact", projection::interact),
                fs_r("Inspect", projection::inspect),
                fs_r("Attack", projection::attack),
                fs_r("Speak", () -> projection.input(validateInput(scanner, "What would you like to say? ", _ -> true))),
                fs_r("Leave", projection::leaveEntity)
        );

        createActionInterface(scanner, actions);
        display(projection.currentMessage());
    }


    void gameLoop(Scanner scanner, GameProjection projection) {
        while (true) { // TODO(gabri) come up with an end condition
            if (projection.currentEntity().isPresent()) pickEntityAction(scanner, projection);
            else roomActions(scanner, projection);
        }
    }

    void exit(Scanner scanner, GameProjection projection) {
        projection.logout();
        display("Logged out.\nThanks for playing!");
        System.exit(1);
    }

    // See JEP 495
    void main() {
        Scanner scanner = new Scanner(System.in);
        GameProjection projection = new GameProjection();
        // TODO(gabri) add modus for difficulty selection.
        var actions = makeTuiActionMap(
                fs_r("Login", () -> tryLogin(scanner, projection, projection::login)),
                fs_r("Create Account", () -> tryLogin(scanner, projection, projection::createAccount))
        );


        createActionInterface(scanner, actions);
        gameLoop(scanner, projection);
    }


    // Holder for decorated strings for terminal output.
    private record FunString(List<Object /*FunString, char*/> content, List<String> styleCodes, List<String> resetCodes) {

        public FunString(String content) {
            this(new ArrayList<>(FunString.stringChars(content)), new ArrayList<>(), new ArrayList<>());
        }

        public FunString(FunString funString) {
            this(new ArrayList<>(funString.content.stream().toList()), new ArrayList<>(funString.styleCodes), new ArrayList<>(funString.resetCodes));
        }
        public FunString(List<Object> strs ) {
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
            for (int i = start; i < end; i++) {
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

        private static List<Character> stringChars(String s) {
            return s.chars().mapToObj(c -> (char) c).toList();
        }

        private static String RED_FG = escape("34");
        private static String DEFAULT_FG = escape("39");
        private static String BOLD = escape("1"); // for controls
        private static String BOLD_OFF = escape("22");

        private static String UNDERLINE = escape("4"); // for controls
        private static String UNDERLINE_OFF = escape("24");
        private static String RESET = escape("0");
        private static String ITALIC = escape("3"); // for entities
        private static String ITALIC_OFF = escape("23");

        private static String escape(String innerCode) {
            return String.format("\033[%sm", innerCode);
        }
    }
}
