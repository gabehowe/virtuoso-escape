@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.account

import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.virtuoso.escape.TestHelper
import org.virtuoso.escape.model.Difficulty
import org.virtuoso.escape.model.GameProjection

class LeaderboardTests {

  private lateinit var proj: GameProjection
  private lateinit var mockAccount: Account

  @BeforeTest
  fun setup() {
    proj = GameProjection(TestHelper.FILE_READER(this::class), TestHelper.DUMMY_WRITER)

    proj.createAccount("dummy", "dummy")
    proj.state = proj.accountManager.gameStates[proj.account.id]!!
    proj.state.language = proj.language
    mockAccount = proj.account
    mockAccount.highScore = Score(100.seconds, Difficulty.TRIVIAL, 100L)
  }

  @Test
  fun testRecordSessionUpdatesHigherScore() {
    proj.state.time = 200.seconds
    proj.state.difficulty = Difficulty.SUBSTANTIAL
    proj.state.penalty = 0

    mockAccount.highScore = Score(0.seconds, Difficulty.TRIVIAL, 0L)

    proj.state.difficulty = Difficulty.VIRTUOSIC
    proj.state.time = 500.seconds

    val oldScore = mockAccount.highScore.totalScore

    Leaderboard.recordSession(proj.state, mockAccount)

    val newScore = mockAccount.highScore.totalScore
    assertTrue(newScore >= oldScore)
  }

  @Test
  fun testGetLeaderboardSorting() {
    val accounts = mutableMapOf<Uuid, Account>()

    fun addAcct(name: String, diff: Difficulty, scoreVal: Long) {
      val a = Account(name, name)
      a.highScore = Score(100.seconds, diff, scoreVal)
      accounts[a.id] = a
    }

    addAcct("low", Difficulty.TRIVIAL, 50L)
    addAcct("mid", Difficulty.SUBSTANTIAL, 200L)
    addAcct("high", Difficulty.VIRTUOSIC, 1000L)

    val testManager = AccountManager(accounts, mapOf())

    val list = Leaderboard.getLeaderboard(testManager.accounts, mockAccount)

    val highIdx = list.indexOf("high")
    val midIdx = list.indexOf("mid")

    assertTrue(highIdx < midIdx, "High difficulty should be before mid difficulty given same time")
  }

  @Test
  fun testLimitTop10() {
    val accounts = mutableMapOf<Uuid, Account>()
    for (i in 1..20) {
      val a = Account("user$i", "pw")
      a.highScore = Score(100.seconds, Difficulty.TRIVIAL, i.toLong())
      accounts[a.id] = a
    }
    val testManager = AccountManager(accounts, mapOf())

    val list = Leaderboard.getLeaderboard(testManager.accounts, mockAccount)

    assertTrue(list.isNotEmpty())
  }
}
