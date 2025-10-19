package org.virtuoso.escape.model;

import org.virtuoso.escape.model.actions.*;
import org.virtuoso.escape.model.data.DataLoader;

import java.util.*;
import java.util.function.Function;
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
        // TODO: floor 2
        this.building.add(floor1());
        this.building.add(floor3());
    }

    //Acorn Grove//
    private Floor acornGrove() {
        Entity intro_squirrel = new Entity("intro_squirrel", null, null, null, null);
        Entity portal_squirrel = new Entity("portal_squirrel",
                null,
                null,
                () -> GameState.instance().setCurrentFloor(this.building.get(1)
                ), null);
        Room acornGrove_0 = new Room(new ArrayList<>(List.of(intro_squirrel, portal_squirrel)), "acorn_grove_0", this.string("acorn_grove_0", "introduce"));
        return new Floor("acorn_grove", List.of(acornGrove_0));
    }

    //Floor One//
    private Floor floor1() {
        Entity trash_can = new Entity("trash_can", new GiveItem(Item.sealed_clean_food_safe_hummus), null, null, null);
        Room room_1400 = new Room(new ArrayList<>(List.of(trash_can)), "storey_i_0", this.string("storey_i_0", "introduce"));

        Entity[] almanacs = almanacChain(5);
        Entity finalAlmanac = almanacs[almanacs.length - 1];
        Room janitor_closet = new Room(new ArrayList<>(List.of(finalAlmanac)), "storey_i_1", this.string("storey_i_1", "introduce"));

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

    /**
     * Create an Entity linkedlist through 2^length pages.
     *
     * @param length The number of almanacs
     * @return An entity with actions holding references to the next entity.
     */
    private Entity[] almanacChain(int length) {
        final int PAGES = (int) Math.pow(2, length);
        final int LEFT_BREAD_PAGE = (int) (Math.random() * PAGES);

        Entity found_almanac = new Entity("found_almanac", null, null, null, null);

        // Create all objects with bad values
        Entity[] almanacChain = IntStream.range(0, length).mapToObj(_ -> new Entity("", null, null, null, null)).limit(length).toArray(Entity[]::new);
        for (int i = 0; i < length; i++) {
            // Copy data from makeAlmanac to stay in the same spot in memory
            int finalI = i;
            Function<Integer, Action> tp = (current) -> turnPage(finalI + 1, current, LEFT_BREAD_PAGE, found_almanac, almanacChain);
            almanacChain[i].absorb(makeAlmanac(PAGES, i, tp));
        }
        return almanacChain;
    }

    private Entity makeAlmanac(int pages, int flips, Function<Integer, Action> turnPage) {
        var j = IntStream.range(1, pages).boxed()
                         .collect(Collectors.toMap(String::valueOf, turnPage));
        return new Entity("almanac",
                new SetMessage(String.format(this.string("almanac", "attack"), flips + 1)),
                null,
                null,
                new TakeInput("", new LinkedHashMap<>(j)));
    }

    private Action turnPage(int flips, int currentPage, int correctPage, Entity foundPage, Entity[] chain) {
        Action swap = () -> {
            if (flips - 1 == 0) {
                Entity[] newEntities = almanacChain(chain.length);
                Room properRoom =
                        this.building.stream().filter(i -> Objects.equals(i.id(), "storey_i")).findFirst().orElseThrow()
                                     .rooms().stream().filter(i -> Objects.equals(i.id(), "storey_i_1")).findFirst().orElseThrow();
                properRoom.entities().clear();
                properRoom.entities().add(newEntities[chain.length - 1]);
                GameState.instance().pickEntity(newEntities[chain.length - 1]);
                return;
            }
            chain[chain.length - 1].absorb(chain[flips - 2]);
        };
        String guessesRemaining = String.format(this.string("almanac", "guesses_remaining"), flips - 1, flips);
        Action caseBreak = new SetMessage(this, "almanac", "break");
        Action caseOvershoot = new SetMessage(this.string("almanac", "too_high") + " " + guessesRemaining);
        Action caseUndershoot = new SetMessage(this.string("almanac", "too_low") + " " + guessesRemaining);
        Action caseFound = new Chain(
                new SetMessage(this, "almanac", "correct_page"),
                new GiveItem(Item.left_bread),
                () -> chain[flips - 1].absorb(foundPage),
                //Covers the case of getting the correct page on the last page
                new SwapEntities(foundPage, "broken_almanac"));
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

    //Floor Three/
    private Floor floor3() {
        // Basic info entity -- provide logic by adding dialogue in language.json
        Entity man = new Entity("man", null, null, null, null);
        var computtyBlocked = new Entity("computty_blocked", null, null, null, null);
        Entity sock_squirrel = new Entity("sock_squirrel", new SwapEntities(makeComputtyChain(), "computty_blocked"), null, null, null);
        Entity microwave = new Entity("microwave_blocked", null, null, null, null);
        Room floor3_0 = new Room(new ArrayList<>(List.of(man, sock_squirrel, computtyBlocked, microwave)), "storey_iii_0", this.string("storey_iii_0", "introduce"));
        return new Floor("storey_iii_0", List.of(floor3_0));
    }

    private Entity makeComputtyChain() {
        // Dichotomy: DRY violation or unreadable code?
        Function<String, Action> ttyStr = (string) -> new SetMessage(this, "computty", string);
        // Whoops! JEP 126!
        Entity microwaveUnblocked = new Entity("microwave_unblocked", this::gameEnding, null, this::gameEnding, null);
        var computtyTarLogic = new TakeInput("", TakeInput.makeCases(
                "rotx 16 code", new Chain(ttyStr.apply("good_rotx"),new SwapEntities(microwaveUnblocked, "microwave_blocked")),
                "rotx 16 .*", ttyStr.apply("no_file"),
                "rotx \\d+", ttyStr.apply("failed_rotx"),
                "rotx.*", ttyStr.apply("man_rotx"),
                "ls", ttyStr.apply("ls_tar"),
                "cat code", ttyStr.apply("cat_code"),
                "cat.*", ttyStr.apply("man_cat")
        ));
        var computtyTar = new Entity("computty_tar", ttyStr.apply("attack"), ttyStr.apply("inspect"), ttyStr.apply("interact"), computtyTarLogic);
        var computtyCdLogic = new TakeInput("", TakeInput.makeCases(
                "tar xvf code.tar$", new Chain(new SetMessage("code"),new SwapEntities(computtyTar, "computty_cd")),
                "tar xvf c.*", ttyStr.apply( "no_file"),
                "tar.*", ttyStr.apply( "man_tar"),
                "ls", ttyStr.apply("ls_cd"),
                "cat code.tar", ttyStr.apply("cat_tar"),
                "cat.*", ttyStr.apply("man_cat")
        ));
        Entity computtyCd = new Entity("computty_cd", ttyStr.apply("attack"), ttyStr.apply("inspect"), ttyStr.apply("interact"), computtyCdLogic);
        var computtyDefault = new TakeInput("", TakeInput.makeCases(
                "cd code", new SwapEntities(computtyCd, "computty"),
                "cd.*", ttyStr.apply( "no_file"),
                "ls", ttyStr.apply("ls_default"),
                "cat.*", ttyStr.apply("man_cat")
        ));
        return new Entity("computty", null, null, null, computtyDefault);
    }

    //Ending//
    private void gameEnding() {
        GameState.instance().end();
    }

    //Utils//
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