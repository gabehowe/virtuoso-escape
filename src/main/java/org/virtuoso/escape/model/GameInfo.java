package org.virtuoso.escape.model;

import org.virtuoso.escape.model.actions.*;
import org.virtuoso.escape.model.data.DataLoader;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.stream.Collector;
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
        // TODO: floor 2
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
        Room acornGrove_0 = new Room(new ArrayList<>(List.of(intro_squirrel, portal_squirrel)), "acorn_grove_0", this.string("acorn_grove_0", "intro"));
        return new Floor("acorn_grove", List.of(acornGrove_0));
    }

    //Floor One//
    private Floor floor1() {
        Entity trash_can = new Entity("trash_can", new GiveItem(Item.sealed_clean_food_safe_hummus), null, null, null);
        Room room_1400 = new Room(new ArrayList<>(List.of(trash_can)), "room_1400", this.string("room_1400", "intro"));

        Entity[] almanacs = almanacChain(5);
        Entity finalAlmanac = almanacs[almanacs.length-1];
		Room janitor_closet = new Room(new ArrayList<>(List.of(finalAlmanac)), "janitor_closet", this.string("janitor_closet", "intro"));

 		return new Floor("floor1", List.of(room_1400, janitor_closet));
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
                this.building.stream().filter(i -> Objects.equals(i.id(), "floor1")).findFirst().orElseThrow()
                             .rooms().stream().filter(i->Objects.equals(i.id(),"janitor_closet")).findFirst().orElseThrow();
                properRoom.entities().clear();
                properRoom.entities().add(newEntities[chain.length-1]);
                GameState.instance().pickEntity(newEntities[chain.length-1]);
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
                                () -> flips -1 != 0,
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
        Room floor3_0 = new Room(new ArrayList<>(List.of(man, sock_squirrel, computtyBlocked, microwave)), "floor3_0", this.string("floor3_0", "intro"));
        return new Floor("floor3", List.of(floor3_0));
    }

    private Entity makeComputtyChain() {
        // Whoops! JEP 126!
        Entity microwaveUnblocked = new Entity("microwave_unblocked", this::gameEnding_moral, null, this::gameEnding_immoral, null);
        var computtyTarLogic = new TakeInput("", TakeInput.makeCases(
                "rotx 16 code", new SwapEntities(microwaveUnblocked, "microwave_blocked"),
                "rotx 16 .*", new SetMessage(this, "computty", "no_file"),
                "rotx \\d+", new SetMessage(this, "computty", "failed_rotx"),
                "rotx.*", new SetMessage(this, "computty", "man_rotx")
                // ls
        ));
        var computtyTar = new Entity("computty_tar", null, null, null, computtyTarLogic);
        var computtyCdLogic = new TakeInput("", TakeInput.makeCases(
                "tar xvf code.tar$", new SwapEntities(computtyTar, "computty_cd"),
                "tar xvf c.*", new SetMessage(this, "computty", "no_file"),
                "tar.*", new SetMessage(this, "computty", "man_tar")
                // ls, cat
        ));
        Entity computtyCd = new Entity("computty_cd", null, null, null, computtyCdLogic);
        var computtyDefault = new TakeInput("", TakeInput.makeCases(
                "cd code", new SwapEntities(computtyCd, "computty"),
                "cd.*", new SetMessage(this, "computty", "no_file")
                // ls
        ));
        return new Entity("computty", null, null, null, computtyDefault);
    }

    //Ending//
    private void gameEnding_moral() {
        throw new RuntimeException("unimplemented!");
    }

    private void gameEnding_immoral() {
        throw new RuntimeException("unimplemented!");
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