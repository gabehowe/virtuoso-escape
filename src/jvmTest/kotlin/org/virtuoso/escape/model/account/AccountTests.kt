@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.account

import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.virtuoso.escape.model.Difficulty

class AccountTests {

  @Test
  fun testValidFirstConstructor() {
    val dummyJr = Account("dummyJr", "dummyJr")
    assertNotNull(dummyJr)
  }

  @Test
  fun testEmptyFirstConstructor() {
    val emptyDummy = Account("", "")
    assertEquals("", emptyDummy.username)
  }

  @Test
  fun testValidSecondConstructor() {
    val mrsDummy =
        Account("mrsdummy", Uuid.random(), Score(0.seconds, Difficulty.SUBSTANTIAL, 0L), "hashedPw")
    assertNotNull(mrsDummy)
  }

  @Test
  fun testValidHashPassword() {
    val hashedPassword = Account.hashPassword("dummy")
    assertEquals("dummy", hashedPassword)
  }

  @Test
  fun testUsername() {
    val dummy = Account("dummy", "dummy")
    assertNotNull(dummy.username)
  }

  @Test
  fun testHighScore() {
    val dummy = Account("dummy", "dummy")
    val newScore = Score(1800.seconds, Difficulty.VIRTUOSIC, 1800L)
    dummy.highScore = newScore
    assertNotEquals(Score(0.seconds, Difficulty.TRIVIAL, 0L), dummy.highScore)
    assertEquals(newScore, dummy.highScore)
  }

  @Test
  fun testHashedPassword() {
    val mrsDummy = Account("mrsdummy", "mrsdummy")
    assertNotNull(mrsDummy.hashedPassword)
  }

  @Test
  fun testUUID() {
    val mrsDummy = Account("mrsdummy", "mrsdummy")
    assertNotNull(mrsDummy.id)
  }
}
