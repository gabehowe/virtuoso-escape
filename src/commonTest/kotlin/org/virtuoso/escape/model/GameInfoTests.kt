package org.virtuoso.escape.model

import org.junit.jupiter.api.Assertions.*

/** @author gabri
 */
class GameInfoTests {
    var proj: GameProjection? = null

    @BeforeEach
    @Throws(Exception::class)
    fun pre() {
        proj = GameProjection()
        Util.rebuildSingleton(GameInfo::class.java)
        Util.rebuildSingleton(GameState::class.java)
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath()
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath()
        try {
            Files.writeString(Path.of(DataLoader.ACCOUNTS_PATH), accountData)
            Files.writeString(Path.of(DataLoader.GAMESTATES_PATH), stateData)
        } catch (e: Exception) {
            throw RuntimeException("couldn't write to file!")
        }
        proj!!.login("dummy", "dummy")
    }

    @AfterEach
    fun post() {
        proj!!.logout()
    }

    @DisplayName("Should return non-null GameInfo instance")
    @Test
    fun testGameInfo() {
        // Test both cases -- create
        assertNotNull(GameInfo.instance())
        // get created
        assertNotNull(GameInfo.instance())
    }

    @DisplayName("Should handle state change on wrong floor")
    @Test
    fun testWrongFloorStateChange() {
        GameState.instance().setCurrentFloor(GameInfo.instance().building.getFirst())
        testJoeHardyNoItems()
    }

    @DisplayName("Should move to floor 1 after portal squirrel interaction")
    @Test
    fun testMoveToFloor1() {
        val introRoom: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            GameInfo.instance().building.getFirst().rooms.getFirst()
        val gottfried: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = introRoom.entities.stream()
            .filter({ entity -> entity.id().equals("portal_squirrel") })
            .toList()
            .getFirst()
        gottfried.state().interact()
        try {
            assertSame(
                GameInfo.instance().building.get(1), GameState.instance().currentFloor()
            )
        } catch (e: NullPointerException) {
            fail()
        }
    }

    @DisplayName("Should keep Joe Hardy in same state when no items present")
    @Test
    fun testJoeHardyNoItems() {
        GameState.instance().clearItems()
        val floor: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameInfo.instance().building.get(1)
        val hardyRoom: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = floor.rooms.getFirst()
        GameState.instance().setCurrentFloor(floor)
        GameState.instance().setCurrentRoom(hardyRoom)
        val hardy: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = hardyRoom.entities.stream()
            .filter({ e -> e.id().equals("joe_hardy") })
            .findFirst()
        assertTrue(hardy.isPresent())
        val hardyPresent: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = hardy.get()
        hardyPresent.swapState("sans_sandwich_joe")
        assertEquals("sans_sandwich_joe", hardyPresent.state().id())
        hardyPresent.state().interact()
        // Should be no change.
        assertEquals("sans_sandwich_joe", hardyPresent.state().id())
    }

    @DisplayName("Should change Joe Hardy to sandwich state when all items present")
    @Test
    fun testJoeHardyWithItems() {
        GameState.instance().addItem(Item.left_bread)
        GameState.instance().addItem(Item.right_bread)
        GameState.instance().addItem(Item.sunflower_seed_butter)
        GameState.instance().addItem(Item.sealed_clean_food_safe_hummus)
        val floor: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameInfo.instance().building.get(1)
        val hardyRoom: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = floor.rooms.getFirst()
        GameState.instance().setCurrentFloor(floor)
        GameState.instance().setCurrentRoom(hardyRoom)
        val hardy: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = hardyRoom.entities.stream()
            .filter({ e -> e.id().equals("joe_hardy") })
            .findFirst()
        assertTrue(hardy.isPresent())
        val hardyPresent: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = hardy.get()
        hardyPresent.swapState("sans_sandwich_joe")
        assertEquals("sans_sandwich_joe", hardyPresent.state().id())
        hardyPresent.state().interact()
        // Should be sandwich joe.
        assertEquals("sandwich_joe", hardyPresent.state().id())
    }

    private fun narratorTest(expectedResource: String?, state: String?) {
        val floor: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameInfo.instance().building.get(1)
        val room: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = floor.rooms.getFirst()
        val maybeNarrator: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = room.entities.stream()
            .filter({ it -> it.id().contains("narrator") })
            .findFirst()
        assertTrue(maybeNarrator.isPresent())
        val narrator: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = maybeNarrator.get()
        narrator.swapState(state)
        narrator.state().interact()
        val msg: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameState.instance().currentMessage().orElse(null)
        assertNotNull(msg)
        assertEquals(msg, GameInfo.instance().string("narrator", expectedResource))
    }

    @DisplayName("Should display first narrator hint message")
    @Test
    fun testFirstNarrator() {
        narratorTest("storey_i_hint_1", "narrator_start")
    }

    @DisplayName("Should display second narrator hint message")
    @Test
    fun testSecondNarrator() {
        narratorTest("storey_i_hint_2", "narrator_hint_1")
    }

    @DisplayName("Should display hints exhausted message on third narrator interaction")
    @Test
    fun testThirdNarrator() {
        narratorTest("hints_exhausted", "narrator_hint_2")
    }

    @DisplayName("Should trigger game ending when interacting with unblocked microwave")
    @Test
    fun testEnding() {
        val floor4: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameInfo.instance().building.get(4)
        val finfoyer: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = floor4.rooms.getFirst()
        val microwave: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = finfoyer.entities.stream()
            .filter({ i -> i.id().equals("microwave") })
            .findFirst()
            .get()
        microwave.swapState("microwave_unblocked")
        microwave.state().interact()
    }

    @DisplayName("Should return key in result when language key is invalid")
    @Test
    fun testInvalidLanguageKey() {
        val inputId = "impossibly-never-ever-possible-exact-language-string"
        val result: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameInfo.instance().string(inputId, "")
        assertTrue(result.contains(inputId))
    }

    @DisplayName("Should display appropriate message based on whether player has keys for floor 3 door")
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun floor3DoorKeyCheck(has: Boolean) {
        if (has) GameState.instance().addItem(Item.keys)
        val floor3: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameInfo.instance().building.get(3)
        GameState.instance().setCurrentFloor(floor3)
        val room: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = floor3.rooms.getFirst()
        val exit_door: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = room.entities.stream()
            .filter({ i -> i.id().equals("exit_door") })
            .findFirst()
            .get()
        exit_door.state().interact()
        val msg: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameState.instance().currentMessage().get()
        assertEquals(GameInfo.instance().string("exit_door", if (has) "unlocked_msg" else "locked_msg"), msg)
    }

    @Nested
    internal inner class AlmanacTests {
        var almanac: Entity? = null

        @BeforeEach
        fun pre() {
            val floor: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameInfo.instance().building.get(1)
            val almanacRoom: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = floor.rooms.get(1)
            GameState.instance().setCurrentFloor(floor)
            GameState.instance().setCurrentRoom(almanacRoom)
            almanac = almanacRoom.entities.getFirst()
            almanac!!.swapState("almanac_5")
        }

        @DisplayName("Should find correct almanac page using binary search")
        @Test
        fun testAlmanacFind() {
            var low = 0
            var high = 32
            var correct: Integer? = null
            for (i in 0..4) {
                val guess = (high + low) / 2
                almanac!!.state()!!.takeInput(String.valueOf(guess))
                val msg: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameState.instance().currentMessage()
                assertTrue(msg.isPresent())
                if (msg.get().contains(GameInfo.instance().string("almanac", "too_low"))) {
                    low = guess
                    continue
                }
                if (msg.get().contains(GameInfo.instance().string("almanac", "too_high"))) {
                    high = guess
                    continue
                }
                if (msg.get().contains(GameInfo.instance().string("almanac", "correct_page"))) {
                    correct = guess
                    break
                }
                fail("Failed to find correct almanac page with binary search.")
            }
            assertNotNull(correct)
            System.out.println(correct)
        }

        @DisplayName("Should return 'too high' or 'too low' message for incorrect almanac guess")
        @Test
        fun testAlmanacWrong() {
            val firstState = almanac!!.state()
            // Correct value cannot be multiple inputs, ergo one must be wrong.
            firstState!!.takeInput("31")
            firstState.takeInput("30")
            val msg: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameState.instance().currentMessage().get()
            assertTrue(
                msg.contains(GameInfo.instance().string("almanac", "too_high"))
                        || msg.contains(GameInfo.instance().string("almanac", "too_low"))
            )
        }

        @DisplayName("Should break almanac after too many incorrect guesses")
        @Test
        fun testAlmanacBreak() {
            val firstState = almanac!!.state()
            var guess = "31"
            firstState!!.takeInput(guess)
            // Ensure incorrect guess;
            if (GameState.instance()
                    .currentMessage()
                    .get()
                    .contains(GameInfo.instance().string("almanac", "correct_page"))
            ) {
                guess = "30"
            }
            almanac!!.swapState("almanac_5")
            for (i in 0..4) {
                almanac!!.state()!!.takeInput(guess)
            }
            val msg: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameState.instance().currentMessage().get()
            assertTrue(msg.contains(GameInfo.instance().string("almanac", "break")), "Invalid:" + msg)
        }
    }

    companion object {
        private val stateData = """
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
            }
            """.trimIndent()
    }
}
