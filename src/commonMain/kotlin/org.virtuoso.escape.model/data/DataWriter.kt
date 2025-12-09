package org.virtuoso.escape.model.data

import kotlin.uuid.ExperimentalUuidApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.virtuoso.escape.model.GameState
import org.virtuoso.escape.model.account.Account
import org.virtuoso.escape.model.account.AccountManager

/** @author Andrew */
@ExperimentalUuidApi
object DataWriter {
  var GAMESTATES_PATH: String = "json/gamestates.json"
  var ACCOUNTS_PATH: String = "json/accounts.json"
  var FILE_WRITER: ((String, String) -> Unit)? = null

  /** Write [GameState.instance] to a [DataWriter.GAMESTATES_PATH]. */
  fun writeGameState(state: GameState, account: Account, accountManager: AccountManager) {
    val allGameStatesMap = accountManager.gameStates.toMutableMap()
    if (!state.isEnded) allGameStatesMap[account.id] = state
    writeToFile(GAMESTATES_PATH, Json.encodeToJsonElement(allGameStatesMap))
  }

  /** Write the current account ([GameState.instance]) to [DataWriter.ACCOUNTS_PATH]. */
  fun writeAccount(account: Account, accountManager: AccountManager) {
    val allAccountsMap = accountManager.accounts.toMutableMap()
    allAccountsMap[account.id] = account
    writeToFile(ACCOUNTS_PATH, Json.encodeToJsonElement(allAccountsMap))
  }

  /**
   * Write a [JSONObject] to a file.
   *
   * @param filepath The file to write to.
   * @param json The object to write.
   */
  private fun writeToFile(filepath: String, json: JsonElement) {
    val data = Json.encodeToString(json)
    FILE_WRITER?.invoke(filepath, data) ?: println(data)
  }
}
