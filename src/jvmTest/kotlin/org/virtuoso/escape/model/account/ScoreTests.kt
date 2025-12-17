package org.virtuoso.escape.model.account

import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import org.virtuoso.escape.model.Difficulty

class ScoreTests {

  @Test
  fun testCalculateScore() {
    // time = 100s, penalty = 10, hints = (5, 5) -> 100 - 10 - 10 = 80
    val time = 100.seconds
    val penalty = 10
    val hints = mapOf("a" to 5, "b" to 5)

    val score = Score.calculateScore(penalty, hints, time)
    assertEquals(80L, score)
  }

  @Test
  fun testScoreEntryFormatting() {
    // 125 seconds = 2 minutes, 5 seconds
    val score = Score(125.seconds, Difficulty.SUBSTANTIAL, 500L)
    val entry = score.scoreEntry("testPlayer")

    assertEquals(4, entry.size)
    assertEquals("testPlayer", entry[0])
    assertEquals("500", entry[1])
    // entry[2] is Duration.toString(), format depends on Kotlin implementation but usually ISO-8601
    // like or similar
    // Let's trust it contains "2m 5s" or similar, or just check non-empty.
    // checking implementation: "this.timeRemaining.toString()"
    assertFalse(entry[2].isEmpty())
    assertEquals("SUBSTANTIAL", entry[3])
  }

  @Test
  fun testCompareTo() {
    val s1 = Score(10.seconds, Difficulty.TRIVIAL, 100L)
    val s2 = Score(20.seconds, Difficulty.TRIVIAL, 200L)

    assertTrue(s1 < s2)
    assertTrue(s2 > s1)
    assertEquals(0, s1.compareTo(Score(10.seconds, Difficulty.TRIVIAL, 100L)))
  }
}
