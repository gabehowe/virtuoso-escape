package org.virtuoso.escape.model;

import org.virtuoso.escape.model.action.*;
import org.virtuoso.escape.model.data.DataLoader;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @author Andrew
 * @author Bose
 * @author gabri
 */
public class GameInfo {
    private static GameInfo instance;
    private Map<String, Map<String, String>> language = Map.of();
    private List<Floor> building = new ArrayList<Floor>();

    /**
     * Construct this singleton.
     */
    private GameInfo() {
        this.language = DataLoader.loadGameLanguage();
        // todo: add other floors

        this.building.add(acornGrove());
        this.building.add(floor1());
        this.building.add(floor2());
        this.building.add(floor3());
        this.building.add(floor4());
    }

    /**
     * The global singleton.
     *
     * @return The global singleton.
     */
    public static GameInfo instance() {
        if (instance == null)
            instance = new GameInfo();
        return instance;
    }

    /**
     * Making the narrator.
     * @param floorId, unique id per floor
     * @return the narrator
     */
     private Entity makeNarrator(String floorId) {
        // Utility function to easily create the message setter
        Function<String, Action> narratorMsg = (stringId) -> new SetMessage(this, "narrator", stringId);

        //No left
        EntityState hintsUsedUp = new EntityState (
                "narrator_hint_2",
                narratorMsg.apply("attack"),
                narratorMsg.apply("inspect"),
                narratorMsg.apply("hints_exhausted"),
                null
        );

        // 1 hint given
        String hint2Id = floorId + "_hint_2";
        Action giveHint2 = new Chain(
        narratorMsg.apply(hint2Id), // Give the specific hint text
                () -> GameState.instance().setHintsUsed(floorId, 2), // Record the hint
                new SwapEntities("narrator", "narrator_hint_2") // Swap to final state
        );
        EntityState hint1Given = new EntityState(
                "narrator_hint_1",
                narratorMsg.apply("attack"),
                narratorMsg.apply("inspect"),
                giveHint2, // Interact action: Give Hint 2 and move to final state
                null
        );

        // No hints given
        String hint1Id = floorId + "_hint_1";
        Action giveHint1 = new Chain(
                narratorMsg.apply(hint1Id), // Give the specific hint text
                () -> GameState.instance().setHintsUsed(floorId, 1), // Record the hint
                new SwapEntities("narrator", "narrator_hint_1") // Swap to hint1Given state
        );
        EntityState start = new EntityState(
                "narrator_start",
                narratorMsg.apply("attack"),
                narratorMsg.apply("inspect"),
                giveHint1, // Interact action: Give Hint 1 and move to next state
                null
        );

        //hi gabe
        return new Entity("narrator", start, hint1Given, hintsUsedUp);
}


    //Acorn Grove//

    /**
     * Build the Acorn Grove.
     *
     * @return The Acorn Grove.
     */
    private Floor acornGrove() {
        Entity intro_squirrel = new Entity("intro_squirrel", new Default(), new Default(), new Default(), null);
        Entity portal_squirrel = new Entity("portal_squirrel", new Default(), new Default(), new Chain(new CompletePuzzle("portal"), new SetFloor(1)), null);
        Entity narrator = makeNarrator("acorn_grove");
        Room acornGrove_0 = new Room("acorn_grove_0", new ArrayList<>(List.of(intro_squirrel, portal_squirrel, narrator)), this.string("acorn_grove_0", "introduce"));
        return new Floor("acorn_grove", List.of(acornGrove_0));
    }

    //Floor One//

    /**
     * Build M. Bert Storey floor 1.
     *
     * @return M. Bert Storey floor 1.
     */
    private Floor floor1() {
        Entity door = new Entity("first_door", new Default(), new Default(), new SetFloor(2), null);
        Entity narrator= makeNarrator("storey_i");
        EntityState hummus_trash_can = new EntityState("trash_can", new Chain(new GiveItem(Item.sealed_clean_food_safe_hummus), new CompletePuzzle("trash"), new SwapEntities("trash_can", "sans_hummus_trash_can")), new Default(), new Default(), null);
        EntityState sans_hummus_trash_can = new EntityState("sans_hummus_trash_can", new Default(), new Default(), new Default(), null);
        Entity trash_can = new Entity("trash_can", hummus_trash_can, sans_hummus_trash_can);

        Entity joeHardy = joeHardy();

        EntityState hummus_elephant = new EntityState("elephant_in_the_room", new Default(), new Default(), new Chain(new GiveItem(Item.sunflower_seed_butter), new CompletePuzzle("elephant"), new SwapEntities("elephant_in_the_room", "sans_butter_elephant")), null);
        EntityState sans_butter_elephant = new EntityState("sans_butter_elephant", new Default(), new Default(), new Default(), null);
        Entity elephant = new Entity("elephant_in_the_room", hummus_elephant, sans_butter_elephant);

        Room room_1400 = new Room("storey_i_0", new ArrayList<>(List.of(joeHardy, trash_can, elephant, door, narrator)), this.string("storey_i_0", "introduce"));

        Entity almanac = makeAlmanacs(5);
        Room janitor_closet = new Room(this,"storey_i_1", almanac);

        Entity securityBread = new Entity("security",
                new Default(),
                new Default(),
                new Default(),
                new TakeInput("",
                        TakeInput.makeCases(".*(?<!w)right.*", new Chain(new SetMessage(this, "security", "right_answer"), new GiveItem(Item.right_bread), new CompletePuzzle("right")),
                                ".*", new Chain(new AddPenalty(Severity.LOW), new SetMessage(this, "security", "non_right_answer")))));
        Room hallway = new Room(this, "storey_i_2",securityBread);


        return new Floor("storey_i", List.of(room_1400, janitor_closet, hallway));
    }

    /**
     * Create Joe Hardy for floor 1.
     *
     * @return Joe Hardy.
     */
    private Entity joeHardy() {
        Predicate<Item[]> hasItems = (i) -> Arrays.stream(i).map(GameState.instance()::hasItem).reduce((a, b) -> a && b).get();
        EntityState sandwichJoe = new EntityState("sandwich_joe", new Default(), new Default(), new Default(), null);
        EntityState sansSandwichJoe = new EntityState("sans_sandwich_joe", new Default(), new Default(), new Conditional(
                () -> hasItems.test(new Item[]{Item.left_bread, Item.right_bread, Item.sunflower_seed_butter, Item.sealed_clean_food_safe_hummus}),
                new Chain(new SetMessage(this, "sans_sandwich_joe", "interact_sandwich"),
                        new SwapEntities("joe_hardy", "sandwich_joe")
                )
        ), null);
        EntityState introJoe = new EntityState("intro_joe", new Default(), new Default(), new Chain(new CompletePuzzle("sandwich"), new SwapEntities("joe_hardy", "sans_sandwich_joe")), null);
        return new Entity("joe_hardy", introJoe, sansSandwichJoe, sandwichJoe);
    }

    /**
     * Returns almanacs for a binary search puzzle
     *
     * @param length The number of guesses
     * @return An entity with states for each almanac guess
     */
    private Entity makeAlmanacs(int length) {
        final int PAGES = (int) Math.pow(2, length);
        final int CORRECT_PAGE = (int) (Math.random() * PAGES);

        ArrayList<EntityState> almanacStates = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            int current_i = i;
            Map<String, Action> map = IntStream.range(1, PAGES).boxed()
                                               .collect(Collectors.toMap(j -> String.valueOf(j), j -> turnPage(length - current_i, j, length, CORRECT_PAGE)));
            LinkedHashMap<String, Action> linkedMap = new LinkedHashMap<String, Action>(map);
            Function<String, Action> alm = (stringId) -> new SetMessage(this, "almanac", stringId);
            almanacStates.add(new EntityState("almanac_" + (length - i), alm.apply("attack"), alm.apply("inspect"), alm.apply("interact"), new TakeInput("", linkedMap)));

        }
        almanacStates.add(new EntityState("found_almanac"));
        return new Entity("almanac", almanacStates.toArray(new EntityState[0]));
    }

    /**
     * Test the user input page.
     *
     * @param flips       Attempts remaining
     * @param currentPage The page attempted.
     * @param maxFlips    The total number of attemptable flips.
     * @param correctPage The correct, desired page
     * @return An Action to be run later.
     */
    private Action turnPage(int flips, int currentPage, int maxFlips, int correctPage) {
        Action swap = flips > 1 ? new SwapEntities("almanac", "almanac_" + (flips - 1)) :
                new SwapEntities("almanac", "almanac_" + maxFlips);

        Action caseBreak = new Chain(new AddPenalty(Severity.MEDIUM), new SetMessage(this, "almanac", "break"));
        var guesses_remaining = String.format(this.string("almanac", "guesses_remaining"), (flips-1), flips);
        Action caseOvershoot = new SetMessage(this.string("almanac", "too_high") + " " +guesses_remaining);
        Action caseUndershoot = new SetMessage(this.string("almanac", "too_low") + " " +guesses_remaining);
        Action caseFound = new Chain(
                new SetMessage(this, "almanac", "correct_page"),
                new GiveItem(Item.left_bread),
				new CompletePuzzle("almanac"),
                new SwapEntities("almanac", "found_almanac"));

        Action evaluatePage = new Conditional(
                () -> currentPage > correctPage,
                caseOvershoot,
                new Conditional(
                        () -> currentPage < correctPage,
                        new Conditional(
                                () -> flips - 1 != 0,
                                caseUndershoot,
                                caseBreak
                        ),
                        caseFound
                ));
        return new Chain(swap, evaluatePage);
    }

    //Floor Two//

    /**
     * Build M. Bert Storey floor 2.
     *
     * @return M. Bert Storey floor 2.
     */
    private Floor floor2() {
        Room doorRoom = new Room("storey_ii_1", new ArrayList<>(), this.string("storey_ii_1", "introduce"));
        Entity narrator = makeNarrator("storey_ii");
        Action shuffle = () -> Collections.shuffle(doorRoom.entities());
        Entity door1 = createDoorChain(3, shuffle);
        Action failDoor = new Chain(new AddPenalty(Severity.MEDIUM), new SwapEntities("door1", "door1_2"), shuffle, GameState.instance()::leaveEntity);
        Entity door2 = new Entity("door2", new Default(), new Default(), failDoor, null);
        Entity door3 = new Entity("door3", new Default(), new Default(), failDoor, null);
        doorRoom.entities().addAll(List.of(door1, door2, door3, narrator));
        shuffle.execute();

        Entity pitcherPlant = new Entity("pitcher_plant");
        Room plantOffice = new Room(this, "storey_ii_0", pitcherPlant);
        return new Floor("storey_ii", List.of(plantOffice, doorRoom));
    }

    /**
     * Create a linked list of proper doors.
     *
     * @param length  The number of doors in the linked list.
     * @param shuffle The function to shuffle doors.
     * @return A single door with an internal link to the next door.
     */
    private Entity createDoorChain(int length, Action shuffle) {
        EntityState[] door1 = new EntityState[length];
        Function<String, Action> sm = (stringId) -> new SetMessage(this, "door1", stringId);
        EntityState door1_final = new EntityState("door1_0", sm.apply("attack"), sm.apply("inspect"), new Chain(new CompletePuzzle("doors"), new SetMessage(this, "door1", "final_door"), new SetFloor(3)), null);
        door1[length - 1] = door1_final;
        for (int i = 1; i < length; i++) {
            EntityState next = new EntityState("door1_" + i, sm.apply("attack"), sm.apply("inspect"), new Chain(
                    new SwapEntities("door1", "door1_" + (i - 1)),
                    GameState.instance()::leaveEntity,
                    shuffle,
                    sm.apply("interact")
            ), null);
            door1[length - (i + 1)] = next;
        }
        return new Entity("door1", door1);
    }

    //Floor Three//
    /**
     * Build M. Bert Storey floor 3.
     *
     * @return M. Bert Storey floor 3.
     */
    private Floor floor3() {
        Entity narrator=makeNarrator("storey_iii");
        //hi treasure
        Entity sparrowAmbassador = new Entity("sparrow_ambassador",
                new Default(),
                new Default(),
                new Default(),
                new TakeInput(
                        "(?:sparrow )?help", new SetMessage(this, "sparrow_ambassador", "hint_help"),
                        ".*", new SetMessage(this, "sparrow_ambassador", "hint_general")
                ));

        Function<String, Action> puzzleMsg = (string) -> new SetMessage(this, "box_riddle", string);

        EntityState box_open = new EntityState("box_open", new Default(), new Default(),
                new Chain(
                        puzzleMsg.apply("solved"),
                        new GiveItem(Item.keys),
						new CompletePuzzle("boxes")
                ), null);

        var boxLogicSuccess = new TakeInput("", TakeInput.makeCases(
                "(?:box )?open", new Chain(new SwapEntities("box_riddle", "box_open"), puzzleMsg.apply("step_success")),
                ".*", new Chain(puzzleMsg.apply("step_wrong"), new SwapEntities("box_riddle", "box_start"))
        ));
        EntityState box_success = new EntityState("box_success", new Default(), new Default(), null, boxLogicSuccess);

        var boxLogicFollow = new TakeInput("", TakeInput.makeCases(
                "(?:box )?follow", new Chain(new SwapEntities("box_riddle", "box_success"), puzzleMsg.apply("step_follow")),
                ".*", new Chain(puzzleMsg.apply("step_wrong"), new SwapEntities("box_riddle", "box_start"))
        ));
        EntityState box_step1 = new EntityState("box_step1", new Default(), new Default(), new Default(), boxLogicFollow);

        var boxLogicStart = new TakeInput("", TakeInput.makeCases(
                "(?:box )?start", new Chain(new SwapEntities("box_riddle", "box_step1"), puzzleMsg.apply("step_start")),
                ".*", puzzleMsg.apply("step_wrong")
        ));
        EntityState box_start = new EntityState("box_start", puzzleMsg.apply("attack"), puzzleMsg.apply("inspect"), puzzleMsg.apply("interact"), boxLogicStart);

        Entity puzzleBox = new Entity("box_riddle", box_start, box_step1, box_success, box_open);
        Function<String, Action> doorMsg = (string) -> new SetMessage(this, "exit_door", string);
        Predicate<Item[]> hasKeys = (i) -> Arrays.stream(i).map(GameState.instance()::hasItem).reduce((a, b) -> a && b).get();

        Action goToFloor4 = new Chain(
                doorMsg.apply("unlocked_success"),
                new SetFloor(4)
        );
        EntityState doorUnlocked = new EntityState("exit_door_unlocked",
                new Default(),
                doorMsg.apply("inspect_unlocked"),
                goToFloor4,
                null
        );

        Action checkKeysAndSwap = new Conditional(
                () -> hasKeys.test(new Item[]{Item.keys}),
                new Chain(
                        doorMsg.apply("unlocked_msg"),
                        new SwapEntities("exit_door", "exit_door_unlocked")
                ),
                doorMsg.apply("locked_msg")
        );
        EntityState doorLocked = new EntityState("exit_door_locked",
                new Default(),
                doorMsg.apply("inspect_locked"),
                checkKeysAndSwap,
                null
        );

        Entity exitDoor = new Entity("exit_door", doorLocked, doorUnlocked);

        Room boxRoom = new Room("storey_iii_0", new ArrayList<>(List.of(sparrowAmbassador, puzzleBox, exitDoor, narrator)), this.string("storey_iii_0", "introduce"));
        return new Floor("storey_iii", List.of(boxRoom));
    }


    //Floor Four//
    private Floor floor4() {
        // Basic info entity -- provide logic by adding dialogue in language.json
        Entity man = man();
        Entity narrator=makeNarrator("storey_iv");

        Entity computty = makeComputtyLogic();
        Entity sock_squirrel = new Entity("sock_squirrel", new Chain(new CompletePuzzle("unblock"), new SwapEntities("computty", "computty_unblocked")), new Default(), new Default(), null);
        EntityState microwave_blocked = new EntityState("microwave_blocked", new Default(), new Default(), new Default(), null);
        // Whoops! JEP 126!
        EntityState microwaveUnblocked = new EntityState("microwave_unblocked", this::gameEnding, new Default(), this::gameEnding, null);
        Entity microwave = new Entity("microwave", microwave_blocked, microwaveUnblocked);
        Room floor4 = new Room("storey_iv", new ArrayList<>(List.of(man, sock_squirrel, computty, microwave, narrator)), this.string("storey_iv", "introduce"));
        return new Floor("storey_iv", List.of(floor4));
    }

    /**
     * Create the man entity.
     *
     * @return the man entity.
     */
    private Entity man() {
        Function<String, Action> manMsg = (stringId) -> new SetMessage(this, "man", stringId);
        Entity man = new Entity("man", new Default(), new Default(), new Default(),
                new TakeInput(
                        "(?:man )?help", manMsg.apply("input_help"),
                        "(?:man )?man", manMsg.apply("input_man"),
                        "(?:man )?ls", manMsg.apply("input_ls"),
                        "(?:man )?cd", manMsg.apply("input_cd"),
                        "(?:man )?tar", manMsg.apply("input_tar"),
                        "(?:man )?rotx", manMsg.apply("input_rotx")));
        return man;
    }

    /**
     * Create CompuTTY -- a terminal entity.
     *
     * @return CompuTTY.
     */
    private Entity makeComputtyLogic() {
        // Dichotomy: DRY violation or unreadable code?
        var computtyBlocked = new EntityState("computty_blocked");
        Function<String, Action> ttyStr = (string) -> new SetMessage(this, "computty_unblocked", string);
        var computtyTarLogic = new TakeInput("", TakeInput.makeCases(
                "rotx 16 code", new Chain(ttyStr.apply("good_rotx"), new CompletePuzzle("CompuTTY"), new SwapEntities("microwave", "microwave_unblocked")),
                "rotx 16 .*", ttyStr.apply("no_file"),
                "rotx \\d+.*", ttyStr.apply("failed_rotx"),
                "rotx.*", ttyStr.apply("man_rotx"),
                "ls", ttyStr.apply("ls_tar"),
                "cat code", ttyStr.apply("cat_code"),
                "cat.*", ttyStr.apply("man_cat"),
                "tar xvf .*", ttyStr.apply("no_file"),
                "cd.*", ttyStr.apply("no_file")
        ));
        var computtyTar = new EntityState("computty_tar", ttyStr.apply("attack"), ttyStr.apply("inspect"), ttyStr.apply("interact"), computtyTarLogic);
        var computtyCdLogic = new TakeInput("", TakeInput.makeCases(
                "tar xvf code.tar$", new Chain(new SetMessage("code"), new SwapEntities("computty", "computty_tar")),
                "tar xvf .*", ttyStr.apply("no_file"),
                "tar.*", ttyStr.apply("man_tar"),
                "ls", ttyStr.apply("ls_cd"),
                "cat code.tar", ttyStr.apply("cat_tar"),
                "cat.*", ttyStr.apply("man_cat"),
                "cd.*", ttyStr.apply("no_file"),
                "rotx \\d+.*", ttyStr.apply("no_file"),
                "rotx.*", ttyStr.apply("man_rotx")
        ));
        var computtyCd = new EntityState("computty_cd", ttyStr.apply("attack"), ttyStr.apply("inspect"), ttyStr.apply("interact"), computtyCdLogic);
        var computtyDefault = new TakeInput("", TakeInput.makeCases(
                "cd code", new Chain(new SwapEntities("computty", "computty_cd"), ttyStr.apply("input_cd")),
                "cd.*", ttyStr.apply("no_file"),
                "ls", ttyStr.apply("ls_default"),
                "cat.*", ttyStr.apply("man_cat"),
                "tar.*", ttyStr.apply("man_tar"),
                "rotx \\d+.*", ttyStr.apply("no_file"),
                "rotx.*", ttyStr.apply("man_rotx")
        ));
        var computtyUnblocked = new EntityState("computty_unblocked", ttyStr.apply("attack"), ttyStr.apply("attack"), ttyStr.apply("interact"), computtyDefault);

        return new Entity("computty", computtyBlocked, computtyUnblocked, computtyTar, computtyCd);
    }

    //Ending//

    /**
     * End the game.
     */
    private void gameEnding() {
        GameState.instance().end();
    }

    //Utils//


    /**
     * Get a string resource from {@link GameInfo#language()} safely.
     *
     * @param id       The parent of the string resource.
     * @param stringId The id of the string resource.
     * @return A string resource.
     */
    public String string(String id, String stringId) {
        if (!language.containsKey(id) || !language.get(id).containsKey(stringId)) return "[" + id + "/" + stringId + "]"; // Default behavior for string
        return language.get(id).get(stringId);
    }

    /**
     * The language parent-(id-resource) mapping..
     *
     * @return The language mapping.
     */
    public Map<String, Map<String, String>> language() {
        return language;
    }

    /**
     * The list of {@link Floor} in the building.
     *
     * @return The list of {@link Floor} in the building.
     */
    public List<Floor> building() {
        return this.building;
    }

}
