@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.account

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import org.virtuoso.escape.model.GameState
import org.virtuoso.escape.model.account.Account.Companion.hashPassword
import org.virtuoso.escape.model.data.DataLoader
import org.virtuoso.escape.model.data.DataWriter

/**
 * Manages user accounts.
 *
 * @author Treasure
 */
@Serializable
data class AccountManager(val accounts: Map<Uuid, Account>, val gameStates: Map<Uuid, GameState>) {
  /** Loads all user accounts and gamestates. */
  constructor() : this(DataLoader.loadAccounts(), DataLoader.loadGameStates())

  /**
   * Attempts to log in with the indicated username and password.
   *
   * @param username The username to attempt.
   * @param password The password to attempt.
   * @return the `Optional<Account>` if username-password combination was valid, otherwise
   *   `Optional.empty`.
   */
  fun login(username: String, password: String): Account? {
    return accountExists(username, password)
  }

  /**
   * Attempts to create an [Account] with the indicated username and password.
   *
   * @param username The username to attempt.
   * @param password The password to attempt.
   * @return the `Optional<Account>` if no user exists with this exact username-password
   *   combination, otherwise `Optional.empty`.
   */
  fun newAccount(username: String, password: String): Account? {
    var usernameExists = false

    val account = login(username, password)
    if (account != null) return account

    for (id in this.accounts.keys) {
      val value = this.accounts[id]!!
      if (value.username == username) usernameExists = true
    }
    if (!usernameExists && username.length <= 32 && password.length <= 32) {
      val newAccount = Account(username, password)
      return newAccount
    }
    return null
  }

  /** Logs the current user account out and writes its data. */
  fun logout(account: Account) {
    DataWriter.writeAccount(account, this)
  }

  /**
   * Displays which information was incorrect when attempt to log in fails.
   *
   * @param username the username to be checked.
   * @param password the password to be checked.
   * @return the respective string output based on which information was incorrect.
   */
  fun invalidLoginInfo(username: String, password: String): String {
    var usernameCount = 0
    var passwordCount = 0
    val hashedPassword: String = Account.hashPassword(password)
    for (id in this.accounts.keys) {
      val value = this.accounts[id]!!
      if (value.username == username) usernameCount++
      else if (value.hashedPassword == hashedPassword) passwordCount++
    }
    return if (usernameCount == 0 && passwordCount == 0)
        "Both username and password input is invalid."
    else if (usernameCount > 0) "Password input is invalid." else "Username input is invalid."
  }

  /**
   * Checks to see if an `Account` exists given a username and password.
   *
   * @param username the username to be checked.
   * @param password the password to be checked.
   * @return the `Account` with the indicated username and password if .
   */
  fun accountExists(username: String, password: String): Account? {
    return accounts.entries
        .firstOrNull { (_, v) ->
          v.username == username && v.hashedPassword == hashPassword(password)
        }
        ?.value
  }
}
