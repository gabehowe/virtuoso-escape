package org.virtuoso.escape.model.account

import kotlinx.serialization.Serializable
import org.virtuoso.escape.model.Difficulty
import org.virtuoso.escape.model.GameState
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A user account containing UUID, username, password, and high score.
 *
 * @author Treasure
 * @author gabri
 */
@Serializable
@OptIn(ExperimentalUuidApi::class)
class Account(val username: String, var id: Uuid, var highScore: Score, val hashedPassword: String) {

    /**
     * Create an `Account` with the indicated username and password.
     *
     * @param username the username to be assigned.
     * @param password the password to be assigned.
     */
    constructor(username: String, password: String) : this(username, Uuid.random(), Score(0.seconds, Difficulty.SUBSTANTIAL, 0), hashPassword(password))

    /**
     * Print a string containing the UUID, the username, and the high score of the account.
     *
     * @return a string representation of the account information.
     */
    override fun toString(): String {
        return ("Account: {" + "id='" + this.id + "', " + "username='" + this.username + "', "
                + this.highScore.toString())
    }

    /** Recalculate the high score.  */
    fun updateHighScore(state: GameState) {
        val currentScore = Score(state.time, state.difficulty, Score.calculateScore(state.penalty, state.hintsUsed, state.countdown()))
        val oldScore = this.highScore
        if (currentScore > oldScore) this.highScore = Score(state.time, state.difficulty, currentScore.totalScore)
    }

    companion object {
        /**
         * Hashes the indicated password.
         *
         * TODO: Make this actually hash.
         * @param password the password to be hashed.
         * @return a string representation of the `hashedPassword`.
         */
        fun hashPassword(password: String): String {
            return password
        }
    }
}
