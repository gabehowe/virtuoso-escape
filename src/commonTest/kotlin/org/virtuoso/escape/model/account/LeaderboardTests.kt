package org.virtuoso.escape.model.account

import org.junit.jupiter.api.Assertions.*

/**
 * Tests for Leaderboard.java
 *
 * @author Bose
 * @author Andrew
 */
class LeaderboardTests {
    private val originalOut: PrintStream? = System.out
    private var outContent: ByteArrayOutputStream? = null

    private var mockGameState: GameState? = null
    private var mockAccount: Account? = null
    private var mockAccountManager: AccountManager? = null

    private var fakeAccountsJson: JSONObject? = null

    @BeforeEach
    @Throws(Exception::class)
    fun setup() {
        Util.rebuildSingleton(GameState::class.java)
        Util.rebuildSingleton(GameInfo::class.java)
        Util.rebuildSingleton(AccountManager::class.java)

        outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        mockAccount = Account(
            "dummy",
            "dummy",
            UUID.randomUUID(),
            Score(Duration.ofSeconds(100), Difficulty.TRIVIAL, 100L),
            false
        )

        mockGameState = GameState.instance()
        injectPrivateField(GameState::class.java, "account", mockGameState, mockAccount)
        injectPrivateField(GameState::class.java, "difficulty", mockGameState, Difficulty.SUBSTANTIAL)
        injectPrivateField(GameState::class.java, "time", mockGameState, Duration.ofSeconds(200))

        fakeAccountsJson = JSONObject()
        fakeAccountsJson.putAll(buildAccountJson("dummy", "TRIVIAL", 100L, 100L))

        mockAccountManager = AccountManager.instance()
        mockAccountManager!!.accounts.putAll(fakeAccountsJson)
    }

    @AfterEach
    fun teardown() {
        System.setOut(originalOut)
    }

    @Test
    @DisplayName("Should update high score when new score is higher")
    fun testRecordSessionUpdatesHigherScore() {
        val old: Long = mockAccount.highScore().totalScore
        Leaderboard.recordSession("dummy")
        assertTrue(mockAccount.highScore().totalScore >= old)
    }

    @Test
    @DisplayName("Should not update high score when new score is lower")
    @Throws(Exception::class)
    fun testRecordSessionDoesNotOverwriteWithLowerScore() {
        injectPrivateField(GameState::class.java, "time", mockGameState, Duration.ofSeconds(1))
        val old: Long = mockAccount.highScore().totalScore
        Leaderboard.recordSession("dummy")
        assertEquals(old, mockAccount.highScore().totalScore)
    }

    @Test
    @DisplayName("Should correctly format time as MM:SS")
    fun testFormattedTime() {
        val entry: Leaderboard.ScoreEntry = ScoreEntry("user", 999L, 125L, "TRIVIAL")
        assertEquals("02:05", entry.getFormattedTime())
    }

    @Test
    @DisplayName("Should print leaderboard correctly with single user")
    fun testShowLeaderboardSingleUser() {
        Leaderboard.showLeaderboard()
        val output = outContent.toString()
        assertTrue(output.contains("Top 5 Leaderboard"))
        assertTrue(output.contains("USERNAME"))
        assertTrue(output.contains("dummy"))
    }

    @Test
    @DisplayName("Should not throw when AccountManager has no accounts")
    fun testShowLeaderboardEmptyAccountManager() {
        mockAccountManager!!.accounts.clear()
        assertDoesNotThrow(Leaderboard::showLeaderboard)
    }

    @Test
    @DisplayName("Should sort multiple users correctly by score")
    fun testLeaderboardSortingMultipleUsers() {
        val multiple: JSONObject = JSONObject()
        multiple.putAll(buildAccountJson("low", "TRIVIAL", 50L, 50L))
        multiple.putAll(buildAccountJson("mid", "SUBSTANTIAL", 120L, 200L))
        multiple.putAll(buildAccountJson("high", "VIRTUOSIC", 300L, 1000L))
        multiple.putAll(buildAccountJson("tieA", "SUBSTANTIAL", 60L, 500L))
        multiple.putAll(buildAccountJson("tieB", "VIRTUOSIC", 90L, 500L))

        mockAccountManager!!.accounts.clear()
        mockAccountManager!!.accounts.putAll(multiple)

        Leaderboard.showLeaderboard()
        val output = outContent.toString()
        assertTrue(output.contains("high"))
    }

    @Test
    @DisplayName("Should safely skip entries with missing fields")
    fun testMissingFieldsSkipped() {
        val incomplete: JSONObject = JSONObject()
        val user: JSONObject = JSONObject()
        user.put("username", "broken")
        incomplete.put(UUID.randomUUID().toString(), user)

        mockAccountManager!!.accounts.putAll(incomplete)

        assertDoesNotThrow(Leaderboard::showLeaderboard)
        assertTrue(outContent.toString().contains("Top 5 Leaderboard"))
    }

    @Test
    @DisplayName("Should handle null totalScore without throwing")
    fun testNullTotalScoreHandled() {
        val corrupted: JSONObject = JSONObject()
        val hs: JSONObject = JSONObject()
        hs.put("difficulty", "TRIVIAL")
        hs.put("timeRemaining", 100L)
        hs.put("totalScore", null)

        val acct: JSONObject = JSONObject()
        acct.put("username", "nullscore")
        acct.put("highScore", hs)
        corrupted.put(UUID.randomUUID().toString(), acct)

        mockAccountManager!!.accounts.putAll(corrupted)

        assertDoesNotThrow(Leaderboard::showLeaderboard)
    }

    @Test
    @DisplayName("Should return correctly ordered leaderboard list")
    @Throws(Exception::class)
    fun testGetLeaderboardOrdering() {
        mockAccountManager!!.accounts.clear()

        val multiple: JSONObject = JSONObject()
        multiple.putAll(buildAccountJson("steve", "TRIVIAL", 120L, 100L))
        multiple.putAll(buildAccountJson("joe", "SUBSTANTIAL", 150L, 300L))
        multiple.putAll(buildAccountJson("frank", "VIRTUOSIC", 200L, 500L))
        mockAccountManager!!.accounts.putAll(multiple)

        val flat: List<String?> = Leaderboard.getLeaderboard()
        assertEquals(16, flat.size())
        assertEquals("frank", flat.get(2))
    }

    @Test
    @DisplayName("Should include current session even when stored accounts are empty")
    @Throws(Exception::class)
    fun testGetLeaderboardIncludesCurrentSessionOnly() {
        mockAccountManager!!.accounts.clear()

        injectPrivateField(GameState::class.java, "time", mockGameState, Duration.ofSeconds(95))
        injectPrivateField(GameState::class.java, "difficulty", mockGameState, Difficulty.SUBSTANTIAL)

        val flat: List<String?> = Leaderboard.getLeaderboard()
        assertFalse(flat.isEmpty())
        assertEquals("dummy", flat.get(2))
    }

    @Test
    @DisplayName("Should skip leaderboard entries with non-positive remaining time")
    fun testGetLeaderboardSkipsZeroTime() {
        mockAccountManager!!.accounts.clear()

        val bad: JSONObject = JSONObject()
        val hs: JSONObject = JSONObject()
        hs.put("difficulty", "TRIVIAL")
        hs.put("timeRemaining", 0L)
        hs.put("totalScore", 300L)

        val acct: JSONObject = JSONObject()
        acct.put("username", "zeroTime")
        acct.put("highScore", hs)

        bad.put(UUID.randomUUID().toString(), acct)
        mockAccountManager!!.accounts.putAll(bad)

        val flat: List<String?> = Leaderboard.getLeaderboard()
        assertFalse(flat.contains("zeroTime"))
    }

    @Test
    @DisplayName("Should not throw when highScore exists but totalScore is null")
    @Throws(Exception::class)
    fun testRecordSessionNullHighScoreDoesNotThrow() {
        val brokenScore = Score(Duration.ofSeconds(100), Difficulty.TRIVIAL, null)
        injectPrivateField(Account::class.java, "highScore", mockAccount, brokenScore)
        assertDoesNotThrow({ Leaderboard.recordSession("dummy") })
    }

    @Test
    @DisplayName("Should not include entries with negative remaining time in printed leaderboard")
    fun testShowLeaderboardSkipsNegativeTime() {
        mockAccountManager!!.accounts.clear()

        val bad: JSONObject = JSONObject()
        val hs: JSONObject = JSONObject()
        hs.put("difficulty", "SUBSTANTIAL")
        hs.put("timeRemaining", -50L)
        hs.put("totalScore", 900L)

        val acct: JSONObject = JSONObject()
        acct.put("username", "negativeTime")
        acct.put("highScore", hs)

        mockAccountManager!!.accounts.put(UUID.randomUUID().toString(), acct)

        Leaderboard.showLeaderboard()
        assertFalse(outContent.toString().contains("negativeTime"))
    }

    @Test
    @DisplayName("Should limit leaderboard to top 10 entries")
    fun testGetLeaderboardLimitTop10() {
        mockAccountManager!!.accounts.clear()

        for (i in 1..20) {
            val entry: JSONObject = buildAccountJson("user" + i, "TRIVIAL", 100L + i, i.toLong())
            mockAccountManager!!.accounts.putAll(entry)
        }

        val flat: List<String?> = Leaderboard.getLeaderboard()
        assertEquals(10, flat.size() / 4)
    }

    @Test
    @DisplayName("Should sort by score then difficulty when scores tie")
    fun testGetLeaderboardDifficultySorting() {
        mockAccountManager!!.accounts.clear()

        val a: JSONObject = buildAccountJson("easy", "TRIVIAL", 100L, 500L)
        val b: JSONObject = buildAccountJson("hard", "VIRTUOSIC", 100L, 500L)

        mockAccountManager!!.accounts.putAll(a)
        mockAccountManager!!.accounts.putAll(b)

        val flat: List<String?> = Leaderboard.getLeaderboard()
        assertEquals("hard", flat.get(2))
    }

    @Test
    @DisplayName("Should skip entry when highScore is not a JSONObject")
    fun testHighScoreNotJSONObjectSkipped() {
        mockAccountManager!!.accounts.clear()

        val acct: JSONObject = JSONObject()
        acct.put("username", "badHS")
        acct.put("highScore", "notJson")
        mockAccountManager!!.accounts.put(UUID.randomUUID().toString(), acct)

        val result: List<String?> = Leaderboard.getLeaderboard()
        assertFalse(result.contains("badHS"))
    }

    @Test
    @DisplayName("Should skip entry missing timeRemaining")
    fun testMissingTimeRemainingSkipped() {
        mockAccountManager!!.accounts.clear()

        val hs: JSONObject = JSONObject()
        hs.put("difficulty", "TRIVIAL")
        hs.put("totalScore", 500L)

        val acct: JSONObject = JSONObject()
        acct.put("username", "missingTime")
        acct.put("highScore", hs)

        mockAccountManager!!.accounts.put(UUID.randomUUID().toString(), acct)

        val flat: List<String?> = Leaderboard.getLeaderboard()
        assertFalse(flat.contains("missingTime"))
    }

    @Test
    @DisplayName("Should skip entry missing totalScore")
    fun testMissingTotalScoreSkipped() {
        mockAccountManager!!.accounts.clear()

        val hs: JSONObject = JSONObject()
        hs.put("difficulty", "TRIVIAL")
        hs.put("timeRemaining", 100L)

        val acct: JSONObject = JSONObject()
        acct.put("username", "missingScore")
        acct.put("highScore", hs)

        mockAccountManager!!.accounts.put(UUID.randomUUID().toString(), acct)

        val flat: List<String?> = Leaderboard.getLeaderboard()
        assertFalse(flat.contains("missingScore"))
    }

    companion object {
        @Throws(Exception::class)
        private fun injectPrivateField(clazz: Class<*>, fieldName: String?, instance: Object?, value: Object?) {
            val f: Field = clazz.getDeclaredField(fieldName)
            f.setAccessible(true)
            f.set(instance, value)
        }

        private fun buildAccountJson(username: String?, difficulty: String?, timeRem: Long?, score: Long?): JSONObject {
            val hs: JSONObject = JSONObject()
            hs.put("difficulty", difficulty)
            hs.put("timeRemaining", timeRem)
            hs.put("totalScore", score)

            val acct: JSONObject = JSONObject()
            acct.put("username", username)
            acct.put("highScore", hs)

            val container: JSONObject = JSONObject()
            container.put(UUID.randomUUID().toString(), acct)
            return container
        }
    }
}
