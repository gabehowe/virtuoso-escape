package org.virtuoso.escape.model.account

import org.junit.jupiter.api.Assertions.*

/** @author gabri
 */
class AccountManagerTests {
    var proj: GameProjection? = null

    @BeforeEach
    fun pre() {
        proj = GameProjection()
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath()
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath()
        try {
            Files.writeString(Path.of(DataLoader.ACCOUNTS_PATH), accountData)
            Files.writeString(Path.of(DataLoader.GAMESTATES_PATH), stateData)
        } catch (e: Exception) {
            throw RuntimeException("couldn't write to file!")
        }
    }

    fun login(): Boolean {
        return proj.login("dummy", "dummy")
    }

    @DisplayName("Should successfully log in with valid credentials")
    @Test
    fun testLogin() {
        assertTrue(login())
        AccountManager.instance().logout()
    }

    @DisplayName("Should fail login with invalid credentials")
    @Test
    fun testBadLogin() {
        val account: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = AccountManager.instance().login("fake", "fake")
        assertFalse(account.isPresent())
    }

    @DisplayName("Should handle logout without login")
    @Test
    fun testBadLogout() {
        val account: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = AccountManager.instance().login("fake", "fake")
        AccountManager.instance().logout()
    }

    @DisplayName("Should create new account with unique username")
    @Test
    fun testNewAccount() {
        val account: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = AccountManager.instance().newAccount("novel", "novel")
        assertTrue(account.isPresent())
    }

    @DisplayName("Should successfully re-authenticate with existing account credentials")
    @Test
    fun testTryCreateExistingAccount() {
        val account: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = AccountManager.instance().newAccount("dummy", "dummy")
        assertTrue(account.isPresent())
    }

    @DisplayName("Should fail to create account with existing username")
    @Test
    fun testTryCreateCollidingAccount() {
        val account: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = AccountManager.instance().newAccount("dummy", "novel")
        assertFalse(account.isPresent())
    }

    @DisplayName("Should fail to create account with username exceeding maximum length")
    @Test
    fun testTryCreateLargeUsernameAccount() {
        val account: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            AccountManager.instance().newAccount("a".repeat(33), "novel")
        assertFalse(account.isPresent())
    }

    @DisplayName("Should fail to create account with password exceeding maximum length")
    @Test
    fun testTryCreateLargePasswordAccount() {
        val account: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            AccountManager.instance().newAccount("novel", "a".repeat(33))
        assertFalse(account.isPresent())
    }

    @DisplayName("Should return appropriate error message for invalid login information")
    @ParameterizedTest
    @CsvSource(
        ["dummy,wrong,Password input is invalid.", "wrong,wrong,Both username and password input is invalid.", "wrong,dummy,Username input is invalid."
        ]
    )
    fun testInvalidLoginInfo(username: String?, password: String?, expected: String?) {
        assertEquals(expected, AccountManager.instance().invalidLoginInfo(username, password))
    }

    @DisplayName("Should return null for invalid account data")
    @Test
    fun testInvalidAccountData() {
        // All these instanceofs are guaranteed to be true by accountloader -- It's impossible to test
        // these branches.
        assertNull(AccountManager.instance().accountExists("a", "a"))
    }

    @DisplayName("Should load game data after successful login")
    @Test
    fun testData() {
        login()
        assertTrue(proj.currentItems().isEmpty())
        // ... The rest should be left for a GameState test.
        AccountManager.instance().logout()
    }

    @DisplayName("Should return non-null AccountManager instance")
    @Test
    fun testInstance() {
        assertNotNull(AccountManager.instance())
        AccountManager.instance().logout()
    }

    @DisplayName("Should return non-null game states map")
    @Test
    fun testGameStates() {
        assertNotNull(AccountManager.instance().gameStates)
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
            "d32ad7e6-570f-43cb-b447-82d4c8be293e": "explicitly NOT a JSONObject"
            }
            """.trimIndent()
    }
}
