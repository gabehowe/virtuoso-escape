package org.virtuoso.escape.model.data;

import static org.junit.jupiter.api.Assertions.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class DataLoaderTests {
	private static String stateData =
            """
            {
            "7766f361-af7a-4da5-b741-6867d1768d45": {
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
            },
            "d32ad7e6-570f-43cb-b447-82d4c8be293e": {
              "currentItems": ["sealed_clean_food_safe_hummus", "sunflower_seed_butter"],
              "difficulty": "VIRTUOSIC",
              "currentRoom": "storey_i_0",
              "currentEntity": "security",
              "currentFloor": "storey_i",
              "completedPuzzles": ["sandwich", "trash"],
              "time": 2524,
              "currentEntityStates": {
                "trash_can": "sans_hummus_trash_can",
		        "security": "security",
		        "narrator": "narrator_hint_1",
		        "almanac": "almanac_5",
		        "elephant_in_the_room": "elephant_in_the_room",
		        "joe_hardy": "sans_sandwich_joe",
		        "first_door": "first_door"
              },
              "hintsUsed": {"storey_iv": 1}
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
            "d32ad7e6-570f-43cb-b447-82d4c8be293e": {
              "highScore": {
                "difficulty": "VIRTUOSIC",
                "timeRemaining": null,
                "totalScore": null
              },
              "hashedPassword": "3a77e04b2d960ac37396832feff4d1753cf29cd7",
              "ttsOn": false,
              "username": "mrsdummy"
            },
            "2381cd7e-980b-4dc2-bf8c-29d0a39047d3": "explicitly not a JSONObject"
            }""";
	private static String languageData =
			"""
			{
			"dummyJr": {
			  "name": "dummyJr",
			  "attack": "You mistake dummyJr for a mannequin and try to kick it",
			  "inspect": "Mannequin-ish exterior. Suspiciously wiggly energy. Possibly sentient"
			},
			"emptyDummy": {}
			}
			""";

	@BeforeEach
	void pre() throws URISyntaxException {
		DataLoader.ACCOUNTS_PATH = Paths.get(getClass().getResource("/org/virtuoso/escape/model/data/accounts.json").toURI()).toString();
		DataLoader.GAMESTATES_PATH = Paths.get(getClass().getResource("/org/virtuoso/escape/model/data/gamestates.json").toURI()).toString();
		DataLoader.LANGUAGE_PATH = Paths.get(getClass().getResource("/org/virtuoso/escape/model/data/language.json").toURI()).toString();
		try {
			Files.writeString(Path.of(DataLoader.ACCOUNTS_PATH), accountData);
			Files.writeString(Path.of(DataLoader.GAMESTATES_PATH), stateData);
			Files.writeString(Path.of(DataLoader.LANGUAGE_PATH), languageData);
		} catch (Exception e){
			throw new RuntimeException("Couldn't write to file.");
		}
	}

	@DisplayName("Should successfully load accounts and find existing account with correct TTS setting")
	@Test
	void testValidAccountLoad(){
		JSONObject accounts = DataLoader.loadAccounts();
		assertNotNull(accounts);
		assertTrue(accounts.containsKey("d32ad7e6-570f-43cb-b447-82d4c8be293e"));
		JSONObject mrsdummyJSON = (JSONObject) accounts.get("d32ad7e6-570f-43cb-b447-82d4c8be293e");
		assertFalse((boolean) mrsdummyJSON.get("ttsOn"));
	}

	@DisplayName("Should not find invalid account key")
	@Test
	void testInvalidAccountLoad(){
		JSONObject accounts = DataLoader.loadAccounts();
		assertFalse(accounts.containsKey("7e4eca67-4326-4462-b5d4-5da4a8bca969"));
	}

	@DisplayName("Should return null for entries that are not valid JSONObjects")
	@Test
	void testNonJSONObjectAccountLoad() {
		JSONObject accounts = DataLoader.loadAccounts();
		assertNull(accounts.get("2381cd7e-980b-4dc2-bf8c-29d0a39047d3"));
	}

	@DisplayName("Should successfully load game language and find existing entity with correct name")
	@Test
	void testValidLanguageLoad(){
		Map<String, Map<String, String>> language = DataLoader.loadGameLanguage();
		assertNotNull(language);
		assertTrue(language.containsKey("dummyJr"));
		assertEquals("dummyJr", language.get("dummyJr").get("name"));
	}

	@DisplayName("Should return null when accessing invalid language key for an entity")
	@Test
	void testInvalidKeyLanguageLoad(){
		Map<String, String> dummyJr = DataLoader.loadGameLanguage().get("dummyJr");
		assertNull(dummyJr.get("introduce"));
	}

	@DisplayName("Should return an empty map for entities with no language entries")
	@Test
	void testEmptyJSONObjectLanguageLoad(){
		Map<String, Map<String, String>> language = DataLoader.loadGameLanguage();
		assertTrue(language.get("emptyDummy").isEmpty());
	}

	@DisplayName("Should successfully load items from game state for a specific account")
	@ParameterizedTest
    @CsvSource({
        "sealed_clean_food_safe_hummus, 0",
        "sunflower_seed_butter, 1",
    })
	void testValidGameStateLoad(String item, int num){
		JSONObject gameStates = DataLoader.loadGameStates();
		assertNotNull(gameStates);
		assertTrue(gameStates.containsKey("d32ad7e6-570f-43cb-b447-82d4c8be293e"));
		JSONObject mrsDummyGS = (JSONObject) gameStates.get("d32ad7e6-570f-43cb-b447-82d4c8be293e");
		JSONArray mrsDummyItems = (JSONArray) mrsDummyGS.get("currentItems");
		assertEquals(item, mrsDummyItems.get(num));
	}

	@DisplayName("Should return non-empty game states JSON")
	@Test
	void testGameStateLoadNotEmpty(){
		JSONObject gameStates = DataLoader.loadGameStates();
		assertFalse(gameStates.isEmpty());
	}
}