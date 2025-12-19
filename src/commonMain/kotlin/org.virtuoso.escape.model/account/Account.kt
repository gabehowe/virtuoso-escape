package org.virtuoso.escape.model.account

import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import org.virtuoso.escape.model.Data
import org.virtuoso.escape.model.Difficulty
import org.virtuoso.escape.model.GameState

/**
 * A user account containing UUID, username, password, and high score.
 *
 * @author Treasure
 * @author gabri
 */
@Serializable
@OptIn(ExperimentalUuidApi::class)
class Account(
    val username: String,
    var id: Uuid,
    var highScore: Score,
    val hashedPassword: String,
) {

  /** Logs the current user account out and writes its data. */
  fun logout(accounts: Map<Uuid, Account>) = Data.writeAccount(this, accounts)

  /**
   * Create an `Account` with the indicated username and password.
   *
   * @param username the username to be assigned.
   * @param password the password to be assigned.
   */
  constructor(
      username: String,
      password: String,
  ) : this(
      username,
      Uuid.random(),
      Score(0.seconds, Difficulty.SUBSTANTIAL, 0),
      hashPassword(password),
  )

  /** Recalculate the high score. */
  fun updateHighScore(state: GameState) {
    val currentScore =
        Score(
            state.time,
            state.difficulty,
            Score.calculateScore(state.penalty, state.hintsUsed, state.countdown()),
        )
    val oldScore = this.highScore
    if (currentScore > oldScore)
        this.highScore = Score(state.time, state.difficulty, currentScore.totalScore)
  }

  /** Account error is thrown when authentications fail. */
  class AccountError(override val message: String) : Error(message)

  companion object {
    /**
     * Hashes the indicated password.
     *
     * @param password the password to be hashed.
     * @return a string representation of the `hashedPassword`.
     *
     * TODO: Make this actually hash.
     */
    fun hashPassword(password: String): String = password

    /**
     * Attempts to create an [Account] with the indicated username and password.
     *
     * @param username The username to attempt.
     * @param password The password to attempt.
     * @return an [Account] with the corresponding username and password.
     * @throws [AccountError] If authentication fails.
     */
    fun newAccount(username: String, password: String, accounts: Map<Uuid, Account>): Account {
      try {
        return login(username, password, accounts)
      } catch (_: AccountError) {}

      if (accounts.values.any { it.username == username })
          throw AccountError("Username is already taken.")
      if (username.length > 32) throw AccountError("Username is too long.")
      if (password.length > 32) throw AccountError("Password is too long.")
      return Account(username, password)
    }

    /**
     * Attempts to log in with the indicated username and password.
     *
     * @param username The username to attempt.
     * @param password The password to attempt.
     * @return the `Optional<Account>` if username-password combination was valid, otherwise
     *   `Optional.empty`.
     */
    fun login(username: String, password: String, accounts: Map<Uuid, Account>): Account {
      return accounts.entries
          .firstOrNull { (_, v) ->
            v.username == username && v.hashedPassword == hashPassword(password)
          }
          ?.value ?: throw AccountError("Username or password is invalid.")
    }
  }
}
