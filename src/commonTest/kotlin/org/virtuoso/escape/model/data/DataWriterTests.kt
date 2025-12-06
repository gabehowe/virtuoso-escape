package org.virtuoso.escape.model.data

import org.junit.jupiter.api.Assertions.*
import org.virtuoso.escape.model.account.Account

/** @author Treasure
 */
class DataWriterTests {
    var projection: GameProjection? = null

    @BeforeEach
    @Throws(Exception::class)
    fun pre() {
        Util.rebuildSingleton(GameState::class.java)
        Util.rebuildSingleton(AccountManager::class.java)
        DataWriter.ACCOUNTS_PATH = Paths.get(
            getClass()
                .getResource("/org/virtuoso/escape/model/data/accounts.json")
                .toURI()
        )
            .toString()
        DataWriter.GAMESTATES_PATH = Paths.get(
            getClass()
                .getResource("/org/virtuoso/escape/model/data/gamestates.json")
                .toURI()
        )
            .toString()
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
        try {
            Files.writeString(Path.of(DataWriter.ACCOUNTS_PATH), accountData)
            Files.writeString(Path.of(DataWriter.GAMESTATES_PATH), stateData)
        } catch (e: Exception) {
            throw RuntimeException("Couldn't write to file.")
        }
        projection = GameProjection()
    }

    @DisplayName("Should successfully write valid account and find current account's username upon load")
    @Test
    fun testWritingValidAccount() {
        val isDummy: Boolean = projection.createAccount("dummy", "dummy")
        DataWriter.writeAccount()
        assertTrue(isDummy)
        val accounts: JSONObject = DataLoader.loadAccounts()
        assertTrue(accounts.containsKey(GameState.instance().account().id().toString()))
        val dummyJSON: JSONObject =
            accounts.get(GameState.instance().account().id().toString()) as JSONObject
        assertEquals("dummy", dummyJSON.get("username"))
    }

    @DisplayName("Should fail to write null account and throw a NullPointerException")
    @Test
    fun testWritingNullAccount() {
        assertThrows(NullPointerException::class.java, { projection.createAccount(null, null) })
        assertThrows(
            NullPointerException::class.java,
            { obj: DataWriter?, account: Account, accountManager: AccountManager -> obj!!.writeAccount(account, accountManager) })
    }

    @DisplayName("Should write account with empty username and password, and find it's empty username upon load")
    @Test
    fun testWritingEmptyAccount() {
        projection.createAccount("", "")
        DataWriter.writeAccount()
        assertTrue(
            DataLoader.loadAccounts()
                .containsKey(GameState.instance().account().id().toString())
        )
        val emptyDummyJSON: JSONObject = DataLoader.loadAccounts()
            .get(GameState.instance().account().id().toString()) as JSONObject
        assertEquals("", emptyDummyJSON.get("username"))
    }

    @DisplayName("Should write multiple valid accounts and find current account's password upon load")
    @Test
    fun testWritingMultipleValidAccounts() {
        projection.createAccount("dummy", "dummy")
        DataWriter.writeAccount()
        projection.createAccount("nulldummy", "nulldummy")
        DataWriter.writeAccount()
        projection.createAccount("emptydummy", "emptydummy")
        DataWriter.writeAccount()
        projection.createAccount("mrsdummy", "mrsdummy")
        DataWriter.writeAccount()
        assertEquals(4, DataLoader.loadAccounts().size())
        assertEquals(
            "3a77e04b2d960ac37396832feff4d1753cf29cd7",
            GameState.instance().account().hashedPassword()
        )
    }

    @DisplayName("Should not write any accounts and confirm accounts.json remains empty")
    @Test
    fun testWritingZeroAccounts() {
        try {
            DataWriter.writeAccount()
        } catch (unused: Exception) {
        }
        assertTrue(DataLoader.loadAccounts().isEmpty())
    }

    @DisplayName("Should write valid game state and find changed difficulty upon load")
    @Test
    fun testWritingValidGameState() {
        projection.createAccount("mrsdummy", "mrsdummy")
        GameState.instance().setDifficulty(Difficulty.TRIVIAL)
        DataWriter.writeGameState()
        assertTrue(
            DataLoader.loadGameStates()
                .containsKey(GameState.instance().account().id().toString())
        )
        val mrsdummyGS: JSONObject = DataLoader.loadGameStates()
            .get(GameState.instance().account().id().toString()) as JSONObject
        assertEquals(
            Difficulty.TRIVIAL,
            Difficulty.valueOf(mrsdummyGS.get("difficulty").toString())
        )
    }

    @DisplayName("Should fail to write game state with null account and throw a NullPointerException")
    @Test
    fun testWritingNullGameState() {
        assertThrows(NullPointerException::class.java, { projection.createAccount(null, null) })
        assertThrows(
            NullPointerException::class.java,
            { obj: DataWriter?, state: GameState, account: Account, accountManager: AccountManager -> obj!!.writeGameState(state, account, accountManager) })
    }

    companion object {
        private val stateData = """
            {}
            
            """.trimIndent()
        private val accountData = """
            {}
            
            """.trimIndent()
    }
}
