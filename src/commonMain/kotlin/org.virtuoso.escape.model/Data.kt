@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import org.virtuoso.escape.model.Data.ACCOUNTS_PATH
import org.virtuoso.escape.model.account.Account

/**
 * Loads data from JSON files.
 *
 * @author Bose
 * @author Treasure
 * @author Andrew
 */
object Data {
  var ACCOUNTS_PATH: String = "json/accounts.json"
  var LANGUAGE_PATH: String = "json/language.json"
  var GAMESTATES_PATH: String = "json/gamestates.json"
  var FILE_READER: ((String) -> String)? = null

  /**
   * Load all accounts from accounts.json
   *
   * @return An id-account accounts map.
   */
  fun loadAccounts(): Map<Uuid, Account> =
      getJsonFile(ACCOUNTS_PATH)?.let(Json::decodeFromJsonElement) ?: mutableMapOf()

  /**
   * Loads language mapping from [LANGUAGE_PATH]
   *
   * @return A mapping of id: (id: [String])
   */
  fun loadGameLanguage(): Language? =
      getJsonFile(LANGUAGE_PATH)
          ?.let { Json.decodeFromJsonElement<Map<String, Map<String, String>>>(it) }
          ?.let { Language(it) }

  /**
   * Load all gamestates from [GAMESTATES_PATH].
   *
   * @return An id-gamestate mapping.
   */
  fun loadGameStates(): MutableMap<Uuid, GameState> =
      getJsonFile(GAMESTATES_PATH)?.let(Json::decodeFromJsonElement) ?: mutableMapOf()

  /**
   * Parses a JSON file into a [JsonObject].
   *
   * @param path The file to parse
   * @return The loaded into a [JsonObject].
   */
  private fun getJsonFile(path: String): JsonObject? =
      FILE_READER?.invoke(path)?.let { Json.decodeFromString(it) }

  lateinit var FILE_WRITER: ((String, String) -> Unit)

  /**
   * Write a GameState to [GAMESTATES_PATH].
   *
   * @param state The [GameState] to write.
   * @param account The account to save the [GameState] under.
   * @param gameStates The states to write with.
   */
  fun writeGameState(state: GameState, account: Account, gameStates: Map<Uuid, GameState>) {
    val allGameStatesMap = gameStates.toMutableMap()
    if (!state.isEnded) allGameStatesMap[account.id] = state
    writeToFile(GAMESTATES_PATH, Json.encodeToJsonElement(allGameStatesMap))
  }

  /**
   * Write an account to [ACCOUNTS_PATH].
   *
   * @param account The account to write.
   * @param accounts The accounts to write with.
   */
  fun writeAccount(account: Account, accounts: Map<Uuid, Account>) {
    val allAccountsMap = accounts.toMutableMap()
    allAccountsMap[account.id] = account
    writeToFile(ACCOUNTS_PATH, Json.encodeToJsonElement(allAccountsMap))
  }

  /**
   * Write a [JsonElement] to a file.
   *
   * @param filepath The file to write to.
   * @param json The object to write.
   */
  private fun writeToFile(filepath: String, json: JsonElement) {
    val data = Json.encodeToString(json)
    FILE_WRITER.invoke(filepath, data)
  }
}

typealias SerializableDuration = @Serializable(with = DurationAsLongSerializer::class) Duration

object DurationAsLongSerializer : KSerializer<Duration> {
  override val descriptor: SerialDescriptor
    get() = SerialDescriptor("org.virtuoso.escape.Duration", Duration.serializer().descriptor)

  override fun serialize(encoder: Encoder, value: Duration) =
      encoder.encodeLong(value.inWholeSeconds)

  override fun deserialize(decoder: Decoder): Duration = decoder.decodeLong().seconds
}
