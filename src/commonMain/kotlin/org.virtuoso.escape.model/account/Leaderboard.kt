package org.virtuoso.escape.model.account

import org.virtuoso.escape.model.GameState
import kotlin.uuid.ExperimentalUuidApi

/**
 * Leaderboard Displays high scores per user accounts
 *
 * @author Andrew
 * @author Bose
 */
object Leaderboard {
    /**
     * Update the user's high score.
     *
     * @param username The user's username.
     */
    fun recordSession(state: GameState, account: Account) {
        val newScore = state.score
        val currentHighScore = account.highScore
        if (newScore.totalScore > currentHighScore.totalScore
        ) account.highScore = newScore
    }

    /**
     * Returns a list format version of the leaderboard
     *
     * @return A list of every leaderboard element, row by row, left to right.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun getLeaderboard(accountManager: AccountManager, state: GameState, account: Account): MutableList<String> {
        val accountsJson = accountManager.accounts
        val allScores = accountManager.accounts.map { (_, v) -> v.highScore.scoreEntry(v.username) }.toMutableList()

        allScores.add(account.highScore.scoreEntry(account.username))
        val topScores = accountsJson.values.sortedByDescending {
            it.highScore.difficulty.ordinal
        }.sortedByDescending { it.highScore.timeRemaining }
            .mapIndexed { i, v -> v.highScore.scoreEntry(v.username).toMutableList().apply { this.add(0, i.toString()) } }.take(10)
        return topScores.flatten().toMutableList()
    }

}
