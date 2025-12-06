package org.virtuoso.escape.model.data

import org.junit.jupiter.api.Assertions.*

/** @author Treasure
 */
class DataLoaderTests {
    @BeforeEach
    @Throws(URISyntaxException::class)
    fun pre() {
        DataLoader.ACCOUNTS_PATH = Paths.get(
            getClass()
                .getResource("/org/virtuoso/escape/model/data/accounts.json")
                .toURI()
        )
            .toString()
        DataLoader.GAMESTATES_PATH = Paths.get(
            getClass()
                .getResource("/org/virtuoso/escape/model/data/gamestates.json")
                .toURI()
        )
            .toString()
        DataLoader.LANGUAGE_PATH = Paths.get(
            getClass()
                .getResource("/org/virtuoso/escape/model/data/language.json")
                .toURI()
        )
            .toString()
        try {
            Files.writeString(Path.of(DataLoader.ACCOUNTS_PATH), accountData)
            Files.writeString(Path.of(DataLoader.GAMESTATES_PATH), stateData)
            Files.writeString(Path.of(DataLoader.LANGUAGE_PATH), languageData)
        } catch (e: Exception) {
            throw RuntimeException("Couldn't write to file.")
        }
    }

    @DisplayName("Should successfully load accounts and find existing account with correct TTS setting")
    @Test
    fun testValidAccountLoad() {
        val accounts: JSONObject = DataLoader.loadAccounts()
        assertNotNull(accounts)
        assertTrue(accounts.containsKey("d32ad7e6-570f-43cb-b447-82d4c8be293e"))
        val mrsdummyJSON: JSONObject = accounts.get("d32ad7e6-570f-43cb-b447-82d4c8be293e") as JSONObject
        assertFalse(mrsdummyJSON.get("ttsOn") as Boolean)
    }

    @DisplayName("Should not find invalid account key")
    @Test
    fun testInvalidAccountLoad() {
        val accounts: JSONObject = DataLoader.loadAccounts()
        assertFalse(accounts.containsKey("7e4eca67-4326-4462-b5d4-5da4a8bca969"))
    }

    @DisplayName("Should return null for entries that are not valid JSONObjects")
    @Test
    fun testNonJSONObjectAccountLoad() {
        val accounts: JSONObject = DataLoader.loadAccounts()
        assertNull(accounts.get("2381cd7e-980b-4dc2-bf8c-29d0a39047d3"))
    }

    @DisplayName("Should successfully load game language and find existing entity with correct name")
    @Test
    fun testValidLanguageLoad() {
        val language: Map<String?, Map<String?, String?>?>? = DataLoader.loadGameLanguage()
        assertNotNull(language)
        assertTrue(language!!.containsKey("dummyJr"))
        assertEquals("dummyJr", language.get("dummyJr")!!.get("name"))
    }

    @DisplayName("Should return null when accessing invalid language key for an entity")
    @Test
    fun testInvalidKeyLanguageLoad() {
        val dummyJr: Map<String?, String?>? = DataLoader.loadGameLanguage()!!.get("dummyJr")
        assertNull(dummyJr!!.get("introduce"))
    }

    @DisplayName("Should return an empty map for entities with no language entries")
    @Test
    fun testEmptyJSONObjectLanguageLoad() {
        val language: Map<String?, Map<String?, String?>?>? = DataLoader.loadGameLanguage()
        assertTrue(language!!.get("emptyDummy")!!.isEmpty())
    }

    @DisplayName("Should successfully load items from game state for a specific account")
    @ParameterizedTest
    @CsvSource(
        ["sealed_clean_food_safe_hummus, 0", "sunflower_seed_butter, 1"
        ]
    )
    fun testValidGameStateLoad(item: String?, num: Int) {
        val gameStates: JSONObject = DataLoader.loadGameStates()
        assertNotNull(gameStates)
        assertTrue(gameStates.containsKey("d32ad7e6-570f-43cb-b447-82d4c8be293e"))
        val mrsDummyGS: JSONObject = gameStates.get("d32ad7e6-570f-43cb-b447-82d4c8be293e") as JSONObject
        val mrsDummyItems: JSONArray = mrsDummyGS.get("currentItems") as JSONArray
        assertEquals(item, mrsDummyItems.get(num))
    }

    @DisplayName("Should return non-empty game states JSON")
    @Test
    fun testGameStateLoadNotEmpty() {
        val gameStates: JSONObject = DataLoader.loadGameStates()
        assertFalse(gameStates.isEmpty())
    }

    companion object {
        private val stateData = """
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
          
          """.trimIndent()
        private val accountData = """
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
            }
            """.trimIndent()
        private val languageData = """
			{
			"dummyJr": {
			  "name": "dummyJr",
			  "attack": "You mistake dummyJr for a mannequin and try to kick it",
			  "inspect": "Mannequin-ish exterior. Suspiciously wiggly energy. Possibly sentient"
			},
			"emptyDummy": {}
			}
			
			""".trimIndent()
    }
}
