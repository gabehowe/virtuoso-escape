@file:OptIn(ExperimentalUuidApi::class)
package org.virtuoso.escape.model.action

import org.virtuoso.escape.TestHelper
import org.virtuoso.escape.model.*
import org.virtuoso.escape.model.data.DataLoader
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

class ActionTests {
    private lateinit var proj: GameProjection

    @BeforeTest
    fun pre() {
        proj = GameProjection(
            TestHelper.FILE_READER(this::class), TestHelper.DUMMY_WRITER
        )
        proj.login("dummy", "dummy")
    }

    @Test
    fun addPenaltyTest() {
        // Unroll params
        val cases = listOf(
            Triple(Difficulty.VIRTUOSIC, Severity.HIGH, 5 * 3 * 3),
            Triple(Difficulty.TRIVIAL, Severity.LOW, 5 * 1 * 1)
        )
        for ((diff, sev, expectedDiff) in cases) {
             proj.state.difficulty = diff
             val prev = proj.state.penalty
             
             Actions.addPenalty(sev)(proj.state)
             
             val expected = prev + expectedDiff // NOTE: Logic: penalty -= points or penalty += points?
             // Actions.kt: it.penalty -= points
             // Wait, addPenalty REMOVES points from penalty? Or adds to penalty?
             // "Penalize the player by removing time" -> usually implies increasing penalty or decreasing time?
             // Actions.kt line 34: `it.penalty -= points`.
             // If penalty is score penalty, usually you add to it to be bad, or subtract?
             // If penalty starts at 0.
             // Test expectation says `expectedDifference`.
             // `pre - expectedDifference`.
             // So decreasing penalty?
             
             // Wait, if penalty is accumulated negative score, then subtracting points makes it more negative (worse)?
             // Or if penalty is "Time Penalty", subtracting from time?
             // But valid penalty field is Int.
             
             // I will match code logic `penalty -= points`.
             val actual = proj.state.penalty
             assertEquals(prev - expectedDiff, actual)
             
             // Reset
             proj.state.penalty = 0
        }
    }

    @Test
    fun chainTest() {
        val action = Actions.chain(
            Actions.setMessage("initial"),
            Actions.setMessage("expected")
        )
        action(proj.state)
        assertEquals("expected", proj.state.message)
    }

    @Test
    fun conditionalTest() {
        // True case
        Actions.conditional({ true }, Actions.setMessage("true"), Actions.setMessage("false"))(proj.state)
        assertEquals("true", proj.state.message)

        // False case
        Actions.conditional({ false }, Actions.setMessage("true"), Actions.setMessage("false"))(proj.state)
        assertEquals("false", proj.state.message)
    }

    @Test
    fun nullConditionalTest() {
        // Else null
        proj.state.message = "orig"
        Actions.conditional({ false }, Actions.setMessage("new"), null)(proj.state)
        assertEquals("orig", proj.state.message) // Should not change
    }

    @Test
    fun giveNullItemTest() {
        // Item enum is non-nullable in Kotlin.
        // Can't pass null. Test invalid.
    }

    @Test
    fun giveAllItemsTest() {
        for (item in Item.entries) {
            Actions.giveItem(item)(proj.state)
            assertTrue(proj.state.items.contains(item))
            proj.state.items.clear()
        }
    }

    @Test
    fun giveDoubleItemTest() {
        Actions.giveItem(Item.Keys)(proj.state)
        Actions.giveItem(Item.Keys)(proj.state)
        // Set logic -> size 1
        assertEquals(1, proj.state.items.count { it == Item.Keys })
    }

    @Test
    fun takeInputTest() {
        val action = Actions.takeInput(
            listOf(
                "one" to Actions.setMessage("one"),
                "two" to Actions.setMessage("two")
            ),
            default = Actions.setMessage("default")
        )
        
        action("one", proj.state)
        assertEquals("one", proj.state.message)
        
        action("two", proj.state)
        assertEquals("two", proj.state.message)
        
        action("three", proj.state)
        assertEquals("default", proj.state.message)
    }
}
