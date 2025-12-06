@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.virtuoso.escape.model.GameState
import org.virtuoso.escape.model.Language
import org.virtuoso.escape.model.account.Account
import org.virtuoso.escape.model.account.Score
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


/**
 * Loads data from JSON files.
 *
 * @author Bose
 * @author Treasure
 * @author Andrew
 */
object DataLoader {
    var ACCOUNTS_PATH: String = "json/accounts.json"
    var LANGUAGE_PATH: String = "json/language.json"
    var GAMESTATES_PATH: String = "json/gamestates.json"
    var FILE_READER: ((String) -> String)? = null

    /**
     * Load all accounts from accounts.json
     *
     * @return An id-account accounts map.
     */

    /**
     * Load all accounts from accounts.json
     *
     * @return An id-account accounts map.
     */
    fun loadAccounts(): Map<Uuid, Account> {
        val root = getJsonFile(ACCOUNTS_PATH) ?: return mutableMapOf()
        return Json.decodeFromJsonElement<Map<Uuid, Account>>(root)
    }

    /**
     * Loads language mapping from language.json
     *
     * @return A mapping of id: (id: string)
     */
    fun loadGameLanguage(): Language? {
        val root = getJsonFile(LANGUAGE_PATH) ?: return null
        return Language(Json.decodeFromJsonElement<Map<String, Map<String, String>>>(root))
    }

    /**
     * Loads all high scores from a file.
     *
     * @return A id-score mapping.
     */
    fun loadHighScores(): MutableMap<Uuid, Score> {
        val accounts = loadAccounts()
        return accounts.mapValues { it.value.highScore }.toMutableMap()
    }

    /**
     * Load all gamestates from gamestates.json
     *
     * @return An id-gamestate mapping.
     */
    fun loadGameStates(): MutableMap<Uuid, GameState> {
        val root = getJsonFile(GAMESTATES_PATH) ?: return mutableMapOf()
        return Json.decodeFromJsonElement(root)
    }

    /**
     * Parses a JSON file into a [JSONObject].
     *
     * @param path The file to parse
     * @return The loaded into a [JSONObject].
     */
    private fun getJsonFile(path: String): JsonObject? {
        return FILE_READER?.invoke(path)?.let { Json.decodeFromString(it) }
    }
}

typealias SerializableDuration = @Serializable(with= DurationAsLongSerializer::class) Duration
object DurationAsLongSerializer: KSerializer<Duration>{
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("org.virtuoso.escape.Duration", Duration.serializer().descriptor)

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeLong(value.inWholeSeconds)
    }

    override fun deserialize(decoder: Decoder): Duration {
        return decoder.decodeLong().seconds
    }

}
