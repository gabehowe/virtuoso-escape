package org.virtuoso.escape.model.account

import kotlinx.serialization.Serializable
import org.virtuoso.escape.model.Difficulty
import org.virtuoso.escape.model.data.SerializableDuration
import org.virtuoso.escape.model.toMicrowaveTime
import kotlin.time.Duration

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
data class Score(val timeRemaining: SerializableDuration, val difficulty: Difficulty, val totalScore: Long) {

    operator fun compareTo(other: Score): Int {
        return this.totalScore.compareTo(other.totalScore)
    }

    fun scoreEntry(username: String): List<String> {
        return listOf(username, this.totalScore.toString(), this.timeRemaining.toString(), this.difficulty.name)
    }

    companion object {
        /** Calculate a score from timeRemaining, difficulty, and GameState data.  */
        fun calculateScore(penalty: Int, hintsUsed: Map<String, Int>, timeRemaining: Duration): Long {
            return timeRemaining.inWholeSeconds - penalty - hintsUsed.values.sum()
        }
    }
}
