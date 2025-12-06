@file:OptIn(ExperimentalUuidApi::class)
package org.virtuoso.escape.model

import org.virtuoso.escape.TestHelper
import org.virtuoso.escape.model.account.Score
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

class GameStateTests {
    private lateinit var proj: GameProjection
    private lateinit var state: GameState

    @BeforeTest
    fun setup() {
        TestHelper.setupDataLoader(this::class)
        proj = GameProjection()
        proj.login("dummy", "dummy")
        state = proj.state
        
        // Reset state values for testing
        state.items.clear()
        state.time = 1000.seconds
        state.penalty = 0
        state.isEnded = false
        state.hintsUsed.clear()
        state.completedPuzzles.clear()
        state.difficulty = Difficulty.SUBSTANTIAL
        state.resetTimer()
        state.leaveEntity()
    }

    @Test
    fun startWithLoadedData() {
        val proj2 = GameProjection()
        assertTrue(proj2.login("dummy_loaded", "dummy"))
        assertEquals(proj2.account.username, "dummy_loaded")
        assertContains(proj2.currentItems(), Item.Keys)
        assertContains(proj2.state.completedPuzzles, "joe_hardy")
    }

    @Test
    fun testUpdateHighScore() {
        proj.state.end()
        proj.account.highScore = Score(100.seconds, Difficulty.TRIVIAL, 100L)
        proj.state.time = 50.seconds 
        val score = proj.state.score
        assertNotNull(score)
    }

    @Test
    fun testAddAndClearItems() {
        val keycard = Item.Keys
        state.items.add(keycard)
        assertTrue(state.hasItem(keycard))

        state.items.add(keycard)
        assertEquals(1, state.items.size)

        state.items.clear()
        assertFalse(state.hasItem(keycard))
    }

    @Test
    fun testPickAndSetEntity() {
        val e1 = Entity("id1")
        val room = state.room
        room.entities.add(e1)
        
        proj.pickEntity(e1)
        assertNotNull(proj.currentEntity())
        assertEquals(e1, proj.currentEntity())

        val e2 = Entity("id2")
        proj.pickEntity(e2)
        assertEquals(e2, proj.currentEntity())

        proj.leaveEntity()
        assertNull(proj.currentEntity())
    }

    @Test
    fun testTimeAndSetTime() {
        state.time = 2.seconds
        val t1 = state.time
        assertEquals(2.seconds, t1)
    }

    @Test
    fun testAddPenalty() {
        state.penalty = 5
        assertEquals(5, state.penalty)
        state.penalty = 15
        assertEquals(15, state.penalty)
    }

    @Test
    fun testIncrementInitialTimeBoundaries() {
        val oldInit = GameState.initialTime
        
        GameState.initialTime = 7199L
        state.incrementInitialTime()
        assertEquals(7259L, GameState.initialTime)

        GameState.initialTime = 7200L
        state.incrementInitialTime()
        assertEquals(7200L, GameState.initialTime)
        
        GameState.initialTime = oldInit
    }

    @Test
    fun testSetAndGetDifficulty() {
        state.difficulty = Difficulty.VIRTUOSIC
        assertEquals(Difficulty.VIRTUOSIC, state.difficulty)
    }

    @Test
    fun testCurrentMessageConsumption() {
        state.message = "hello"
        val msg1 = state.message
        assertEquals("hello", msg1)

        val msg2 = state.message
        assertNull(msg2)
    }

    @Test
    fun testAddCompletedPuzzleIdempotency() {
        state.completedPuzzles.add("PuzzleA")
        state.completedPuzzles.add("PuzzleA") 
        assertEquals(1, state.completedPuzzles.size)
        assertTrue(state.completedPuzzles.contains("PuzzleA"))
    }

    @Test
    fun testHintsUsed() {
        state.hintsUsed["Level1"] = 2
        assertEquals(2, state.hintsUsed["Level1"])
        assertNull(state.hintsUsed["Level2"])
    }

    @Test
    fun testEndGame() {
        assertFalse(state.isEnded)
        state.end()
        assertTrue(state.isEnded)
    }

    @Test
    fun testSetCurrentFloorResetsRoomAndEntity() {
        val floor1 = Floor.StoreyI
        val floor2 = Floor.StoreyII
        
        state.floor = floor1
        
        state.floor = floor2
        assertEquals(floor2, state.floor)
        assertEquals(floor2.rooms.first(), state.room)
        assertNull(state.entity)
    }

    @Test
    fun testSetCurrentRoomDoesNotChangeFloor() {
        val floor = Floor.StoreyI
        val r1 = floor.rooms[0]
        val r2 = floor.rooms[1]
        
        state.floor = floor
        state.room = r2
        assertEquals(floor, state.floor)
        assertEquals(r2, state.room)
    }

    @Test
    fun testResetTimer() {
        state.resetTimer()
    }


}
