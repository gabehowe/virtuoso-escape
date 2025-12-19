package org.virtuoso.escape.model.account

import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import org.virtuoso.escape.model.Difficulty
import org.virtuoso.escape.model.GameState
import org.virtuoso.escape.model.SerializableDuration
import org.virtuoso.escape.model.toMicrowaveTime

/**
 * Leaderboard displays high scores per user accounts.
 *
 * @author Andrew
 * @author Bose
 */
object Leaderboard {
  /**
   * Update the user's high score.
   *
   * @param state The [GameState] to draw data from.
   * @param account The [Account] to draw data from.
   */
  fun recordSession(state: GameState, account: Account) {
    val newScore = state.score
    val currentHighScore = account.highScore
    if (newScore.totalScore > currentHighScore.totalScore) account.highScore = newScore
  }

  /**
   * Returns a list format version of the leaderboard
   *
   * @param account The account to place on the leaderboard.
   * @param accountManager
   * @return A list of every leaderboard element, row by row, left to right.
   */
  @OptIn(ExperimentalUuidApi::class)
  fun getLeaderboard(
      accounts: Map<Uuid, Account>,
      account: Account,
  ): MutableList<String> {
    val accountMap = accounts
    val allScores = accountMap.map { (_, v) -> v.highScore.scoreEntry(v.username) }.toMutableList()

    allScores.add(account.highScore.scoreEntry(account.username))
    val topScores =
        accountMap.values
            .sortedByDescending { it.highScore.difficulty.ordinal }
            .sortedByDescending { it.highScore.timeRemaining }
            .mapIndexed { i, v ->
              v.highScore.scoreEntry(v.username).toMutableList().apply { this.add(0, i.toString()) }
            }
            .take(10)
    return topScores.flatten().toMutableList()
  }
}

/**
 * Holds information about the users score.
 *
 * @param timeRemaining The time remaining when the user finished the game.
 * @param difficulty The difficulty the user was playing on when they finished the game.
 * @param totalScore The overall high score of a user.
 * @author gabri
 * @author Andrew
 */
@Serializable
data class Score(
    val timeRemaining: SerializableDuration,
    val difficulty: Difficulty,
    val totalScore: Long,
) {
  /**
   * Compare this score with another.
   *
   * @param other The other score to compare with
   */
  operator fun compareTo(other: Score): Int = this.totalScore.compareTo(other.totalScore)

  /**
   * Package a score for display.
   *
   * @param username The username to display.
   * @return The username, total score, time remaining, and difficulty in a list.
   */
  fun scoreEntry(username: String): List<String> {
    return listOf(
        username,
        this.totalScore.toString(),
        this.timeRemaining.toMicrowaveTime(),
        this.difficulty.name,
    )
  }

  companion object {
    /** Calculate a score from timeRemaining, difficulty, and GameState data. */
    fun calculateScore(penalty: Int, hintsUsed: Map<String, Int>, timeRemaining: Duration): Long {
      return timeRemaining.inWholeSeconds - penalty - hintsUsed.values.sum()
    }
  }
}
