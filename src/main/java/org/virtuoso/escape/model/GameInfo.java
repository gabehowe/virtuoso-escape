package org.virtuoso.escape.model;

import org.virtuoso.escape.model.actions.*;
import org.virtuoso.escape.model.data.DataLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.stream.IntStream;
import java.util.stream.Collectors;


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
		Room acornGrove_0 = new Room(List.of(intro_squirrel, portal_squirrel), "acorn_grove_0", this.string("acorn_grove_0", "intro"));
		return new Floor("acorn_grove", List.of(acornGrove_0));
    }

	//Floor One//
	private Floor floor1() {
		Entity trash_can = new Entity("trash_can", new GiveItem(Item.sealed_clean_food_safe_hummus), null, null, null);

		Room room_1400 = new Room(List.of(trash_can), "room_1400", this.string("room_1400", "intro"));

		final int PAGES = 32;
		final Random rand = new Random();
		final int LEFT_BREAD_PAGE = rand.nextInt(PAGES);

		Entity found_almanac = new Entity("found_almanac", null, null, null, null);
		Entity brokenAlmanac = new Entity("broken_almanac", null, new Chain(new RemoveTime(Severity.HIGH), new GiveItem(Item.left_bread), new SwapEntities(found_almanac, "broken_almanac")), null, null);

		Entity almanac_1 = makeAlmanac(1, PAGES, LEFT_BREAD_PAGE, brokenAlmanac, found_almanac);
		Entity almanac_2 = makeAlmanac(2, PAGES, LEFT_BREAD_PAGE, almanac_1, found_almanac);
		Entity almanac_3 = makeAlmanac(3, PAGES, LEFT_BREAD_PAGE, almanac_2, found_almanac);
		Entity almanac_4 = makeAlmanac(4, PAGES, LEFT_BREAD_PAGE, almanac_3, found_almanac);
		Entity almanac_5 = makeAlmanac(5, PAGES, LEFT_BREAD_PAGE, almanac_4, found_almanac);
		almanac_1 = makeAlmanac(1, PAGES, LEFT_BREAD_PAGE, almanac_5, found_almanac);

		
		Room janitor_closet = new Room(List.of(almanac_5), "janitor_closet", this.string("janitor_closet", "intro"));
 		return new Floor("one", List.of(room_1400, janitor_closet));
	}

	private Entity makeAlmanac (int flips, int pages, int correct_page, Entity nextPage, Entity foundPage) {
		Map<String,Action> map = IntStream.range(1,pages).boxed()
			.collect(Collectors.toMap( i -> String.valueOf(i), i -> (Action) turnPage(flips, i, correct_page, nextPage, foundPage)));
		LinkedHashMap<String,Action> linkedMap = new LinkedHashMap<String,Action>(map);
		return new Entity ("almanac_" + String.valueOf(flips), null, null, null, new TakeInput("" , linkedMap));
	}

	private Action turnPage(int flips, int currentPage, int correctPage, Entity nextPage, Entity foundPage) {
		return new Chain(new SwapEntities(nextPage, "almanac_" + String.valueOf(flips)), 
			new Conditional(() ->  currentPage > correctPage, new SetMessage("too_high_"+String.valueOf(flips-1)), 
			new Conditional(() -> currentPage < correctPage, new SetMessage("too_low_"+String.valueOf(flips-1)), 
			new Chain(new SetMessage("correct_page"), new GiveItem(Item.left_bread), 
			new SwapEntities(foundPage, "almanac_" + String.valueOf(flips-1)),
			//Covers the case of getting the correct page on the last page
			new SwapEntities(foundPage, "broken_almanac")))));
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
        Room floor3_0 = new Room(List.of(man, sock_squirrel, computtyBlocked, microwave), "floor3_0", this.string("floor3_0", "intro"));
        return new Floor("floor3", List.of(floor3_0));
    }

    private Entity makeComputtyChain() {
        // Whoops! JEP 126!
        Entity microwaveUnblocked = new Entity("microwave_unblocked", this::gameEnding_moral, null, this::gameEnding_immoral, null);
        var computtyTarLogic = new TakeInput("", TakeInput.makeCases(
                "rotx 16 code", new SwapEntities(microwaveUnblocked, "microwave_blocked"),
                "rotx 16 .*", new SetMessage(this,"computty", "no_file"),
                "rotx \\d+", new SetMessage(this,"computty", "failed_rotx"),
                "rotx.*", new SetMessage(this,"computty", "man_rotx")
                // ls
        ));
        var computtyTar = new Entity("computty_tar", null, null, null, computtyTarLogic);
        var computtyCdLogic = new TakeInput("", TakeInput.makeCases(
                "tar xvf code.tar$", new SwapEntities(computtyTar, "computty_cd"),
                "tar xvf c.*", new SetMessage(this,"computty", "no_file"),
                "tar.*", new SetMessage(this,"computty", "man_tar")
                // ls, cat
        ));
        Entity computtyCd = new Entity("computty_cd", null, null, null, computtyCdLogic);
        var computtyDefault = new TakeInput("", TakeInput.makeCases(
                "cd code", new SwapEntities(computtyCd, "computty"),
                "cd.*", new SetMessage(this,"computty", "no_file")
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

	public List<Floor> building(){
		return this.building;
	};

}