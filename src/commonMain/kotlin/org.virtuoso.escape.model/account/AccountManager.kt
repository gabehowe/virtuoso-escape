@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.account

import kotlinx.serialization.Serializable
import org.virtuoso.escape.model.Data
import org.virtuoso.escape.model.GameState
import org.virtuoso.escape.model.account.Account.Companion.hashPassword
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Manages user accounts.
 *
 * @author Treasure
 */
@Serializable
data class AccountManager(val accounts: Map<Uuid, Account>, val gameStates: Map<Uuid, GameState>) {
  /** Loads all user accounts and gamestates. */
  constructor() : this(Data.loadAccounts(), Data.loadGameStates())

  class AccountError(override val message: String) : Error(message)

  /**
   * Attempts to create an [Account] with the indicated username and password.
   *
   * @param username The username to attempt.
   * @param password The password to attempt.
   * @return the `Optional<Account>` if no user exists with this exact username-password
   *   combination, otherwise `Optional.empty`.
   */
  fun newAccount(username: String, password: String): Account {
    try {
      return login(username, password)
    } catch(_: AccountError){}

    if (this.accounts.values.any { it.username == username }) throw AccountError("Username is already taken.")
    if (username.length > 32) throw AccountError("Username is too long.")
    if (password.length > 32) throw AccountError("Password is too long.")
    return Account(username, password)
  }

  /** Logs the current user account out and writes its data. */
  fun logout(account: Account) = Data.writeAccount(account, this.accounts)

  /**
   * Attempts to log in with the indicated username and password.
   *
   * @param username The username to attempt.
   * @param password The password to attempt.
   * @return the `Optional<Account>` if username-password combination was valid, otherwise
   *   `Optional.empty`.
   */
  fun login(username: String, password: String): Account {
    return accounts.entries
      .firstOrNull { (_, v) ->
        v.username == username && v.hashedPassword == hashPassword(password)
      }
      ?.value ?: throw AccountError("Username or password is invalid.")
  }
}
