package org.virtuoso.escape.model

import org.junit.jupiter.api.Assertions.*

/**
 * Tests for GameState.java
 *
 * @author Bose
 * @author Andre
 */
class GameStateTests {
    private var state: GameState? = null
    private var proj: GameProjection? = null

    @BeforeEach
    @Throws(Exception::class)
    fun setup() {
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath()
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath()
        try {
            Files.writeString(Path.of(DataLoader.ACCOUNTS_PATH), accountData)
            Files.writeString(Path.of(DataLoader.GAMESTATES_PATH), stateData)
        } catch (e: Exception) {
            throw RuntimeException("couldn't write to file!")
        }
        proj = GameProjection()
        // Reset the singleton
        val instanceField: Field = GameState::class.java.getDeclaredField("instance")
        instanceField.setAccessible(true)
        instanceField.set(null, null)
        state = GameState.instance()
        proj!!.login("dummy", "dummy")

        //        injectPrivateField(state, "account", account);
        injectPrivateField(state, "time", Duration.ofSeconds(1000))
        injectPrivateField(state, "penalty", 0)
        injectPrivateField(state, "ended", false)
        injectPrivateField(state, "hintsUsed", HashMap())
        injectPrivateField(state, "completedPuzzles", HashSet())
        injectPrivateField(state, "currentItems", HashSet(Set.of()))
        injectPrivateField(state, "difficulty", Difficulty.SUBSTANTIAL)
        injectPrivateField(state, "startTime", System.currentTimeMillis())
        injectPrivateField(state, "currentEntity", null)
    }

    @Test
    @Throws(Exception::class)
    fun startWithLoadedData() {
        Util.rebuildSingleton(GameState::class.java)
        Util.rebuildSingleton(AccountManager::class.java)
        val projection = GameProjection()
        assertTrue(projection.login("dummy_loaded", "dummy"))
        assertTrue(projection.currentItems().contains(Item.keys))
        assertTrue(GameState.instance().completedPuzzles().contains("joe_hardy"))
    }

    @Test
    fun testSingletonIdentity() {
        val s1: GameState? = GameState.instance()
        val s2: GameState? = GameState.instance()
        assertSame(s1, s2)
    }

    @ParameterizedTest
    @CsvSource(["50, 100", "150, 150", "100, 100"])
    fun testUpdateHighScore(value: Int, expected: Int) {
        GameState.instance().end()
        GameState.instance().account().setHighScore(Score(null, null, 100L))
        GameState.instance().setTime(Duration.ofSeconds(value))
        GameState.instance().updateHighScore()
        assertEquals(expected, GameState.instance().account().highScore().totalScore)
    }

    @Test
    fun testAddAndClearItems() {
        val keycard: Item = Item.keys
        state.addItem(keycard)
        assertTrue(state!!.hasItem(keycard))

        // Duplicate creatrion for test
        state.addItem(keycard)
        assertEquals(1, state.currentItems().size())

        state.clearItems()
        assertFalse(state!!.hasItem(keycard))
    }

    @Test
    fun testPickAndSetEntity() {
        val e1 = Entity("id1")
        state.pickEntity(e1)
        assertTrue(state.currentEntity().isPresent())
        assertEquals(e1, state.currentEntity().get())

        val e2 = Entity("id2")
        state.setCurrentEntity(e2)
        assertEquals(e2, state.currentEntity().get())

        state!!.leaveEntity()
        assertTrue(state.currentEntity().isEmpty())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testTimeAndSetTime() {
        state.setTime(Duration.ofSeconds(2))
        val t1: Duration = state.time()
        Thread.sleep(10)
        val t2: Duration = state.time()
        assertTrue(t2.toMillis() < t1.toMillis() || t2.toMillis() === 0)
    }

    @Test
    fun testAddPenalty() {
        state.addPenalty(5)
        assertEquals(5, state.penalty())
        state.addPenalty(10)
        assertEquals(15, state.penalty())
    }

    @Test
    @Throws(Exception::class)
    fun testIncrementInitialTimeBoundaries() {
        val f: Field = GameState::class.java.getDeclaredField("initialTime")
        f.setAccessible(true)

        f.setLong(null, 7199L)
        state!!.incrementInitialTime()
        assertEquals(7259L, f.getLong(null))

        f.setLong(null, 7200L)
        state!!.incrementInitialTime()
        assertEquals(7200L, f.getLong(null))
    }

    @Test
    fun testSetAndGetDifficulty() {
        state!!.difficulty = Difficulty.VIRTUOSIC
        assertEquals(Difficulty.VIRTUOSIC, state.difficulty())
    }

    @Test
    fun testCurrentMessageConsumption() {
        state.setCurrentMessage("hello")
        val msg1: Optional<String?> = state.currentMessage()
        assertTrue(msg1.isPresent())
        assertEquals("hello", msg1.get())

        val msg2: Optional<String?> = state.currentMessage()
        assertTrue(msg2.isEmpty())
    }

    // completed
    @Test
    fun testAddCompletedPuzzleIdempotency() {
        state.addCompletedPuzzle("PuzzleA")
        state.addCompletedPuzzle("PuzzleA") // duplicate
        assertEquals(1, state.completedPuzzles().size())
        assertTrue(state.completedPuzzles().contains("PuzzleA"))
    }

    @Test
    fun testHintsUsed() {
        state!!.hintsUsed = "Level1"
        assertEquals(2, state.hintsUsed().get("Level1"))
        assertNull(state.hintsUsed().get("Level2"))
    }

    // high svore
    @Test
    fun testEndGame() {
        assertFalse(state!!.isEnded)
        state!!.end()
        assertTrue(state!!.isEnded)
    }

    @Test
    @Throws(Exception::class)
    fun testSetCurrentFloorResetsRoomAndEntity() {
        val floor1 = Floor("F1", LinkedList())
        val floor2 = Floor("F2", LinkedList())
        injectPrivateField(state, "currentFloor", floor1)
        val r1 = Room("R1", ArrayList(), "")
        floor1.rooms.add(r1)
        val r2 = Room("R2", ArrayList(), "")
        floor2.rooms.add(r2)

        state.setCurrentFloor(floor2)
        assertEquals(floor2, state.currentFloor())
        assertEquals(r2, state.currentRoom())
        assertTrue(state.currentEntity().isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testSetCurrentRoomDoesNotChangeFloor() {
        val floor = Floor("F", LinkedList())
        val r1 = Room("R1", ArrayList(), "")
        val r2 = Room("R2", ArrayList(), "")
        floor.rooms.add(r1)
        floor.rooms.add(r2)
        injectPrivateField(state, "currentFloor", floor)
        state.setCurrentRoom(r2)
        assertEquals(floor, state.currentFloor())
        assertEquals(r2, state.currentRoom())
    }

    @Test
    @Throws(Exception::class)
    fun testResetTimer() {
        val startTimeField: Field = GameState::class.java.getDeclaredField("startTime")
        startTimeField.setAccessible(true)
        val originalStartTime = startTimeField.get(state) as Long

        Thread.sleep(2)

        state!!.resetTimer()

        val newStartTime = startTimeField.get(state) as Long

        assertTrue(newStartTime > originalStartTime, "startTime should be updated to a more recent timestamp")
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
                    },
                  "d32ad7e6-570f-43cb-b447-82d4c8be293e": {
                    "currentItems": ["keys"],
                    "difficulty": "SUBSTANTIAL",
                    "currentRoom": "acorn_grove_0",
                    "currentFloor": "acorn_grove",
                    "completedPuzzles": ["joe_hardy"],
                    "time": 2680,
                    "currentEntityStates": {
                      "narrator": "narrator_hint_1",
                      "portal_squirrel": "portal_squirrel",
                      "intro_squirrel": "intro_squirrel"
                    },
                    "hintsUsed": {
                      "acorn_grove": 1
                    }
                  },
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
                    }
                  "d32ad7e6-570f-43cb-b447-82d4c8be293e": {
                    "highScore": {
                      "difficulty": "TRIVIAL",
                      "timeRemaining": null,
                      "totalScore": null
                    },
                    "ttsOn": true,
                    "hashedPassword": "829c3804401b0727f70f73d4415e162400cbe57b",
                    "username": "dummy_loaded"
                  },
                    }
                  
                  """.trimIndent()

        @Throws(Exception::class)
        private fun injectPrivateField(instance: Object, fieldName: String?, value: Object?) {
            val f: Field = instance.getClass().getDeclaredField(fieldName)
            f.setAccessible(true)
            f.set(instance, value)
        }
    }
}
