package org.virtuoso.escape.model.account

import org.junit.jupiter.api.Assertions.*

/** @author Treasure
 */
internal class AccountTests {
    @DisplayName("Should return non-null account given valid username and password")
    @Test
    fun testValidFirstConstructor() {
        val dummyJr = Account("dummyJr", "dummyJr")
        assertNotNull(dummyJr)
    }

    @DisplayName("Should return empty username")
    @Test
    fun testEmptyFirstConstructor() {
        val emptyDummy = Account("", "")
        assertEquals("", emptyDummy.username())
    }

    @DisplayName("Should throw an IllegalArgumentException on attempt to create account with null username and password")
    @Test
    fun testNullFirstConstructor() {
        assertThrows(IllegalArgumentException::class.java, { Account(null, null) })
    }

    @DisplayName("Should return non-null account given valid parameters")
    @Test
    fun testValidSecondConstructor() {
        val mrsDummy: Account = Account(
            "mrsdummy", "mrsdummy", UUID.randomUUID(), Score(null, Difficulty.SUBSTANTIAL, null), false
        )
        assertNotNull(mrsDummy)
    }

    @DisplayName("Should throw an IllegalArgumentException on attempt to create account with null parameters")
    @Test
    fun testNullSecondConstructor() {
        assertThrows(IllegalArgumentException::class.java, { Account(null, null, null, null, false) })
    }

    @DisplayName("Should successfully hash a valid password")
    @Test
    fun testValidHashPassword() {
        val hashedPassword: String? = Account.hashPassword("dummy")
        assertNotNull(hashedPassword)
        assertEquals("829c3804401b0727f70f73d4415e162400cbe57b", hashedPassword)
    }

    @DisplayName("Should throw an IllegalArgumentException on attempt to hash a null password")
    @Test
    fun testNullHashPassword() {
        assertThrows(IllegalArgumentException::class.java, { Account.hashPassword(null) })
    }

    @DisplayName("Should successfully update TTS setting to true")
    @Test
    fun testTTSSetting() {
        val mrsDummy: Account = Account(
            "mrsdummy", "mrsdummy", UUID.randomUUID(), Score(null, Difficulty.SUBSTANTIAL, null), false
        )
        mrsDummy.SetTtsOn(true)
        assertTrue(mrsDummy.ttsOn())
    }

    @DisplayName("Should return non-null username")
    @Test
    fun testUsername() {
        val dummy = Account("dummy", "dummy")
        assertNotNull(dummy.username())
    }

    @DisplayName("Should not equal previous score after new high score is set")
    @Test
    fun testHighScore() {
        val dummy = Account("dummy", "dummy")
        dummy.highScore = Score(Duration.ofSeconds(1800), Difficulty.VIRTUOSIC, 1800L)
        assertNotEquals(Score(null, Difficulty.TRIVIAL, null), dummy.highScore())
    }

    @DisplayName("Should return non-null hashed password")
    @Test
    fun testHashedPassword() {
        val mrsDummy = Account("mrsdummy", "mrsdummy")
        assertNotNull(mrsDummy.hashedPassword())
    }

    @DisplayName("Should return non-null UUID")
    @Test
    fun testUUID() {
        val mrsDummy = Account("mrsdummy", "mrsdummy")
        assertNotNull(mrsDummy.id())
    }
}
