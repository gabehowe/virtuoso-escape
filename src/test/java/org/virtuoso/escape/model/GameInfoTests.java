package org.virtuoso.escape.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.virtuoso.escape.model.data.DataLoader;

public class GameInfoTests {
    GameProjection proj;
    private static String stateData =
            """
            {"7766f361-af7a-4da5-b741-6867d1768d45": {
              "currentItems": [],
              "difficulty": "SUBSTANTIAL",
              "currentRoom": "acorn_grove_0",
              "currentEntity": null,
              "currentFloor": "acorn_grove",
              "completedPuzzles": [],
              "time": 2698,
              "currentEntityStates": {
                "narrator": "narrator_start",
                "portal_squirrel": "portal_squirrel",
                "intro_squirrel": "intro_squirrel"
              },
              "hintsUsed": {}
            }
            }
            """;
    private static String accountData =
            """
            {
            "7766f361-af7a-4da5-b741-6867d1768d45": {
              "highScore": {
                "difficulty": "TRIVIAL",
                "timeRemaining": null,
                "totalScore": null
              },
              "hashedPassword": "829c3804401b0727f70f73d4415e162400cbe57b",
              "ttsOn": true,
              "username": "dummy"
            },
            }""";

    @BeforeEach
    public void pre() {
        proj = new GameProjection();
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath();
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath();
        try {
            Files.writeString(Path.of(DataLoader.ACCOUNTS_PATH), accountData);
            Files.writeString(Path.of(DataLoader.GAMESTATES_PATH), stateData);
        } catch (Exception e) {
            throw new RuntimeException("couldn't write to file!");
        }
        proj.login("dummy", "dummy");
    }

    @Test
    public void testGameInfo() {
        // Test both cases -- create
        assertNotNull(GameInfo.instance());
        // get created
        assertNotNull(GameInfo.instance());
    }

    @Test
    public void testWrongFloorStateChange() {
        GameState.instance().setCurrentFloor(GameInfo.instance().building().getFirst());
        testJoeHardyNoItems();
    }

    @Test
    public void testMoveToFloor1() {
        var introRoom = GameInfo.instance().building().getFirst().rooms().getFirst();
        var gottfried = introRoom.entities().stream()
                .filter(entity -> entity.id().equals("portal_squirrel"))
                .toList()
                .getFirst();
        gottfried.state().interact();
        try {
            assertSame(
                    GameInfo.instance().building().get(1), GameState.instance().currentFloor());
        } catch (NullPointerException e) {
            fail();
        }
    }

    @Test
    public void testJoeHardyNoItems() {
        GameState.instance().clearItems();
        var hardyRoom = GameInfo.instance().building().get(1).rooms().getFirst();
        var hardy = hardyRoom.entities().stream()
                .filter(e -> e.id().equals("joe_hardy"))
                .findFirst();
        assertTrue(hardy.isPresent());
        var hardyPresent = hardy.get();
        hardyPresent.swapState("sans_sandwich_joe");
        assertEquals("sans_sandwich_joe", hardyPresent.state().id());
        hardyPresent.state().interact();
        // Should be no change.
        assertEquals("sans_sandwich_joe", hardyPresent.state().id());
    }

    @Test
    public void testJoeHardyWithItems() {
        GameState.instance().addItem(Item.left_bread);
        GameState.instance().addItem(Item.right_bread);
        GameState.instance().addItem(Item.sunflower_seed_butter);
        GameState.instance().addItem(Item.sealed_clean_food_safe_hummus);
        var hardyRoom = GameInfo.instance().building().get(1).rooms().getFirst();
        var hardy = hardyRoom.entities().stream()
                .filter(e -> e.id().equals("joe_hardy"))
                .findFirst();
        assertTrue(hardy.isPresent());
        var hardyPresent = hardy.get();
        hardyPresent.swapState("sans_sandwich_joe");
        assertEquals("sans_sandwich_joe", hardyPresent.state().id());
        hardyPresent.state().interact();
        // Should be sandwich joe.
        assertEquals("sandwich_joe", hardyPresent.state().id());
    }

    @Test
    public void testAlmanacFind() {
        var almanacRoom = GameInfo.instance().building().get(1).rooms().get(1);
        var almanac = almanacRoom.entities().getFirst();
        int low = 0, high = 32;
        Integer correct = null;
        for (int i = 0; i < 5; i++) {
            var guess = (high + low) / 2;
            almanac.state().takeInput(String.valueOf(guess));
            var msg = GameState.instance().currentMessage();
            assertTrue(msg.isPresent());
            if (msg.get().equals(GameInfo.instance().string("almanac", "too_low"))) {
                low = guess;
                continue;
            }
            if (msg.get().equals(GameInfo.instance().string("almanac", "too_high"))) {
                high = guess;
                continue;
            }
            if (msg.get().equals(GameInfo.instance().string("almanac", "correct_page"))) {
                correct = guess;
                break;
            }
            fail("Failed to find correct almanac page with binary search.");
        }
        assertNotNull(correct);
        System.out.println(correct);
    }

    @Test
    public void testAlmanacWrong() {
        var almanacRoom = GameInfo.instance().building().get(1).rooms().get(1);
        var almanac = almanacRoom.entities().getFirst();
        var firstState = almanac.state();
        // Correct value cannot be multiple inputs, ergo one must be wrong.
        firstState.takeInput("31");
        firstState.takeInput("30");
        var msg = GameState.instance().currentMessage().get();
        assertTrue(msg.equals(GameInfo.instance().string("almanac", "too_high"))
                || msg.equals(GameInfo.instance().string("almanac", "too_low")));
    }

    @Test
    public void testAlmanacBreak() {
        var almanacRoom = GameInfo.instance().building().get(1).rooms().get(1);
        var almanac = almanacRoom.entities().getFirst();
        var firstState = almanac.state();
        var guess = "31";
        firstState.takeInput(guess);
        // Ensure incorrect guess;
        if (GameState.instance()
                .currentMessage()
                .get()
                .equals(GameInfo.instance().string("almanac", "correct_page"))) {
            almanac.swapState("almanac_5");
            guess = "30";
        }
        for (int i = 0; i < 6; i++) {
            almanac.state().takeInput(guess);
        }
        var msg = GameState.instance().currentMessage().get();
        assertEquals(GameInfo.instance().string("almanac", "break"), msg);
    }

    private void narratorTest(String expectedResource, String state) {
        var floor = GameInfo.instance().building().get(1);
        var room = floor.rooms().getFirst();
        var maybeNarrator = room.entities().stream()
                .filter(it -> it.id().contains("narrator"))
                .findFirst();
        assertTrue(maybeNarrator.isPresent());
        var narrator = maybeNarrator.get();
        narrator.swapState(state);
        narrator.state().interact();
        var msg = GameState.instance().currentMessage().orElse(null);
        assertNotNull(msg);
        assertEquals(msg, GameInfo.instance().string("narrator", expectedResource));
    }

    @Test
    public void testFirstNarrator() {
        narratorTest("storey_i_hint_1", "narrator_start");
    }

    @Test
    public void testSecondNarrator() {
        narratorTest("storey_i_hint_2", "narrator_hint_1");
    }

    @Test
    public void testThirdNarrator() {
        narratorTest("hints_exhausted", "narrator_hint_2");
    }

    @Test
    public void testEnding() {
        var floor4 = GameInfo.instance().building().get(4);
        var finfoyer = floor4.rooms().getFirst();
        var microwave = finfoyer.entities().stream()
                .filter(i -> i.id().equals("microwave"))
                .findFirst()
                .get();
        microwave.swapState("microwave_unblocked");
        microwave.state().interact();
    }

    @Test
    public void testInvalidLanguageKey() {
        var inputId = "impossibly-never-ever-possible-exact-language-string";
        var result = GameInfo.instance().string(inputId, "");
        assertTrue(result.contains(inputId));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void floor3DoorKeyCheck(boolean has) {
        if (has) GameState.instance().addItem(Item.keys);
        var floor3 = GameInfo.instance().building().get(3);
        var room = floor3.rooms().getFirst();
        var exit_door = room.entities().stream()
                .filter(i -> i.id().equals("exit_door"))
                .findFirst()
                .get();
        exit_door.state().interact();
        var msg = GameState.instance().currentMessage().get();
        assertEquals(GameInfo.instance().string("exit_door", (has) ? "unlocked_msg" : "locked_msg"), msg);
    }
}
