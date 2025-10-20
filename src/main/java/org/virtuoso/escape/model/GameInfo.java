package org.virtuoso.escape.model;

import org.virtuoso.escape.model.actions.*;
import org.virtuoso.escape.model.data.DataLoader;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @author Andrew
 * @author Gabri
 */
public class GameInfo {
    private static GameInfo instance;
    private Map<String, Map<String, String>> language = Map.of();
    private List<Floor> building = new ArrayList<Floor>();

    public static GameInfo instance() {
        if (instance == null)
            instance = new GameInfo();
        return instance;
    }

    private GameInfo() {
        this.language = DataLoader.loadGameLanguage();
        // todo: add other floors

        this.building.add(acornGrove());
        this.building.add(floor1());
        this.building.add(floor2());
        this.building.add(floor3());
    }

    //Acorn Grove//
    private Floor acornGrove() {
        Entity intro_squirrel = new Entity("intro_squirrel", null, null, null, null);
        Entity portal_squirrel = new Entity("portal_squirrel", null, null, new SetFloor(1), null);
        Room acornGrove_0 = new Room(new ArrayList<>(List.of(intro_squirrel, portal_squirrel)), "acorn_grove_0", this.string("acorn_grove_0", "introduce"));
        return new Floor("acorn_grove", List.of(acornGrove_0));
    }

    //Floor One//
    private Floor floor1() {
        Entity door = new Entity("first_door", null, null, new SetFloor(2), null);
        Entity trash_can = new Entity("trash_can", new GiveItem(Item.sealed_clean_food_safe_hummus), null, null, null);
        Entity joeHardy = joeHardy();
        Entity elephant = new Entity("elephant_in_the_room", null, null, new GiveItem(Item.sunflower_seed_butter), null);
        Room room_1400 = new Room(new ArrayList<>(List.of(joeHardy,trash_can, elephant, door)), "storey_i_0", this.string("storey_i_0", "introduce"));

		Entity almanac = makeAlmanacs(4);
        Room janitor_closet = new Room(List.of(almanac), "storey_i_1", this.string("storey_i_1", "introduce"));

        Entity securityBread = new Entity("security",
                null,
                null,
                null,
                new TakeInput("",
                        TakeInput.makeCases(".*(?<!w)right.*", new Chain(new SetMessage(this, "security","right_answer"), new GiveItem(Item.right_bread)),
                                ".*", new SetMessage(this, "security", "non_right_answer"))));
        Room hallway = new Room(new ArrayList<>(List.of(securityBread)), "storey_i_2", this.string("storey_i_2", "introduce"));


        return new Floor("storey_i", List.of(room_1400, janitor_closet, hallway));
    }

    private Entity joeHardy() {
        Predicate<Item[]> hasItems = (i) -> Arrays.stream(i).map(GameState.instance()::hasItem).reduce((a, b) -> a&&b).get();
        EntityState sandwichJoe = new EntityState("sandwich_joe", null, null, null, null);
        EntityState sansSandwichJoe = new EntityState("sans_sandwich_joe", null, null, new Conditional(
                () -> hasItems.test(new Item[]{Item.left_bread, Item.right_bread, Item.sunflower_seed_butter, Item.sealed_clean_food_safe_hummus}),
                new Chain(new SetMessage(this, "sans_sandwich_joe", "interact_sandwich"),
                        new SwapEntities("joe_hardy", "sandwich_joe")
                        )
        ), null);
        EntityState introJoe = new EntityState("intro_joe", null, null, new SwapEntities("joe_hardy", "sandwich_joe"), null);
        return new Entity("joe_hardy", introJoe, sansSandwichJoe, sandwichJoe);
    }

    /**
     * Create an Entity linkedlist through 2^length pages.
     *
     * @param length The number of almanacs
     * @return An entity with actions holding references to the next entity.
     */
    private Entity makeAlmanacs(int length) {
        final int PAGES = (int) Math.pow(2,length);
        final int CORRECT_PAGE = (int) (Math.random() * PAGES);

		ArrayList<EntityState> almanacStates = new ArrayList<EntityState>();
        for (int i = 0; i < length; i++) {
			int current_i = i;
            Map<String,Action> map = IntStream.range(1,PAGES).boxed()
			.collect(Collectors.toMap(j -> String.valueOf(j), j -> turnPage(length-current_i, j, length, CORRECT_PAGE)));
			LinkedHashMap<String,Action> linkedMap = new LinkedHashMap<String,Action>(map);
			almanacStates.add(new EntityState(String.valueOf(length-i), null, null, null, new TakeInput("", linkedMap)));

        }
		almanacStates.add(new EntityState("found", null, null, null, null));
		return new Entity("almanac", almanacStates.toArray(new EntityState[0]));
    }

    private Action turnPage(int flips, int currentPage, int max_flips, int correctPage) {
        Action swap = flips > 1 ? new SwapEntities("almanac", String.valueOf(flips-1)) : 
		new SwapEntities("almanac", String.valueOf(max_flips));

        Action caseBreak = new SetMessage(this, "almanac", "break");
        Action caseOvershoot = new SetMessage(this.string("almanac", "too_high") + " " + String.valueOf(flips-1));
        Action caseUndershoot = new SetMessage(this.string("almanac", "too_low") + " " + String.valueOf(flips-1));
        Action caseFound = new Chain(
                new SetMessage(this, "almanac", "correct_page"),
                new GiveItem(Item.left_bread),
                new SwapEntities("almanac", "found"));

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
    // TODO add floor 2
    private Floor floor2() {
        Room doorRoom = new Room(new ArrayList<>(),"storey_ii_1", this.string("storey_ii_1", "introduce"));
        Action shuffle =  () -> Collections.shuffle(doorRoom.entities());
        Entity door1 = createDoorChain(3, shuffle);
        Action failDoor = new Chain(new SwapEntities("door1","3"), shuffle, GameState.instance()::leaveEntity);
        Entity door2 = new Entity("door2", null, null, failDoor, null);
        Entity door3 = new Entity("door3", null, null, failDoor, null);
        doorRoom.entities().addAll(List.of(door1, door2, door3));
        shuffle.execute();

        Entity pitcherPlant = new Entity("pitcher_plant", null, null, null, null);
        Room plantOffice = new Room(new ArrayList<>(List.of(pitcherPlant)), "storey_ii_0", this.string("storey_ii_0", "introduce"));
        return new Floor("storey_ii", List.of(plantOffice, doorRoom));
    }

    private Entity createDoorChain(int length, Action shuffle) {
        EntityState[] door1 = new EntityState[length];
        Function<String, Action> sm = (stringId) -> new SetMessage(this, "door1", stringId);
        EntityState door1_final = new EntityState("door1_0", sm.apply("attack"), sm.apply("inspect"), new Chain(new SetMessage(this, "door1", "final_door"),this::nextFloor), null);
        door1[length-1] = door1_final;
        for (int i = 1; i < length; i++) {
            EntityState next = new EntityState("door1_" + i, sm.apply("attack"), sm.apply("inspect"), new Chain(
                    new SwapEntities("door1", "door1_" +(i-1)),
                    GameState.instance()::leaveEntity,
                    shuffle,
                    sm.apply("interact")
                    ), null);
            door1[length-(i+1)] = next;
        }
        return new Entity("door1", door1);
    }

    //Floor Three/
    private Floor floor3() {
        // Basic info entity -- provide logic by adding dialogue in language.json
        Entity man = man();

        Entity computty = makeComputtyLogic();
        Entity sock_squirrel = new Entity("sock_squirrel", new SwapEntities("computty", "computty_unblocked"), null, null, null);
        EntityState microwave_blocked = new EntityState("microwave_blocked", null, null, null, null);
	    // Whoops! JEP 126!
        EntityState microwaveUnblocked = new EntityState("microwave_unblocked", this::gameEnding, null, this::gameEnding, null);
		Entity microwave = new Entity("microwave", microwave_blocked, microwaveUnblocked);
        Room floor3_0 = new Room(new ArrayList<>(List.of(man, sock_squirrel, computty, microwave)), "storey_iii_0", this.string("storey_iii_0", "introduce"));
        return new Floor("storey_iii_0", List.of(floor3_0));
    }

    private Entity man() {
        Function<String, Action> manMsg = (stringId) -> new SetMessage(this, "man", stringId);
        Entity man = new Entity("man", null, null, null,
                new TakeInput(
                        "(?:man )?help", manMsg.apply("input_help"),
                        "(?:man )?man", manMsg.apply("input_man"),
                        "(?:man )?ls", manMsg.apply("input_ls"),
                        "(?:man )?cd", manMsg.apply("input_cd"),
                        "(?:man )?tar", manMsg.apply("input_tar"),
                        "(?:man )?rotx", manMsg.apply("input_rotx")));
        return man;
    }

    private Entity makeComputtyLogic() {
        // Dichotomy: DRY violation or unreadable code?
		var computtyBlocked = new EntityState("computty_blocked", null, null, null, null);
        Function<String, Action> ttyStr = (string) -> new SetMessage(this, "computty", string);
        var computtyTarLogic = new TakeInput("", TakeInput.makeCases(
                "rotx 16 code", new Chain(ttyStr.apply("good_rotx"),new SwapEntities("microwave", "microwave_unblocked")),
                "rotx 16 .*", ttyStr.apply("no_file"),
                "rotx \\d+", ttyStr.apply("failed_rotx"),
                "rotx.*", ttyStr.apply("man_rotx"),
                "ls", ttyStr.apply("ls_tar"),
                "cat code", ttyStr.apply("cat_code"),
                "cat.*", ttyStr.apply("man_cat")
        ));
        var computtyTar = new EntityState("computty_tar", ttyStr.apply("attack"), ttyStr.apply("inspect"), ttyStr.apply("interact"), computtyTarLogic);
        var computtyCdLogic = new TakeInput("", TakeInput.makeCases(
                "tar xvf code.tar$", new Chain(new SetMessage("code"),new SwapEntities("computty", "computty_tar")),
                "tar xvf c.*", ttyStr.apply( "no_file"),
                "tar.*", ttyStr.apply( "man_tar"),
                "ls", ttyStr.apply("ls_cd"),
                "cat code.tar", ttyStr.apply("cat_tar"),
                "cat.*", ttyStr.apply("man_cat")
        ));
        var computtyCd = new EntityState("computty_cd", ttyStr.apply("attack"), ttyStr.apply("inspect"), ttyStr.apply("interact"), computtyCdLogic);
        var computtyDefault = new TakeInput("", TakeInput.makeCases(
                "cd code", new Chain(new SwapEntities("computty", "computty_cd"), ttyStr.apply("input_cd")),
                "cd.*", ttyStr.apply( "no_file"),
                "ls", ttyStr.apply("ls_default"),
                "cat.*", ttyStr.apply("man_cat")
        ));
		var computtyUnblocked = new EntityState("computty_unblocked", ttyStr.apply("attack"), ttyStr.apply("attack"), ttyStr.apply("interact"), computtyDefault);

        return new Entity("computty", computtyBlocked, computtyUnblocked, computtyTar, computtyCd);
    }

    //Ending//
    private void gameEnding() {
        GameState.instance().end();
    }

    //Utils//
    private void nextFloor(){
        int currentIndex = this.building.indexOf(GameState.instance().currentFloor());
        GameState.instance().setCurrentFloor(this.building.get(currentIndex+1));
    }
    public String string(String id, String stringId) {
        if (!language.containsKey(id) || !language.get(id).containsKey(stringId)) return "[" + id + "/" + stringId + "]"; // Default behavior for string
        return language.get(id).get(stringId);
    }

    public Map<String, Map<String, String>> language() {
        return language;
    }

    public List<Floor> building() {
        return this.building;
    }

}