@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package org.virtuoso.escape.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.virtuoso.escape.model.account.Score
import org.virtuoso.escape.model.data.SerializableDuration
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * The current state of the game. Holds most mutable data.
 *
 * @author Andrew
 */

@Serializable(with= GameState.Serializer::class)
class GameState @OptIn(ExperimentalTime::class) constructor(
    currentFloor: Floor,
    currentRoomId: String,
    entityId: String?,
    val items: MutableSet<Item>,
    var time: SerializableDuration,
    var hintsUsed: MutableMap<String, Int>,
    val completedPuzzles: MutableSet<String>,
    var difficulty: Difficulty,
    var penalty: Int,
    private var startTime: Long = Clock.System.now().toEpochMilliseconds(),
    var isEnded: Boolean = false,
    currentMessage: String? = null
) {
    var floor: Floor = currentFloor
        set(value) {
            field = value
            this.entity = null
            this.room = field.rooms.first()
        }
    var message: String? = currentMessage
        get() {
            val c = field
            field = null
            return c
        }
    var room: Room = currentFloor.rooms.first { it.id == currentRoomId }
        set(value) {
            field = value
            this.entity = null
        }
    val score: Score
        get() {
            return Score(
                this.time, this.difficulty,
                Score.calculateScore(this.penalty, this.hintsUsed, this.time)
            )
        }
    var entity: Entity? = if (entityId == null) entityId else room.entities.first {it.id == entityId}

    @Transient lateinit var language: Language

    /**
     * Whether the inventory contains `item`.
     *
     * @param item The item to check for.
     * @return `true` if the inventory contains the item, otherwise `false`.
     */
    fun hasItem(item: Item): Boolean {
        return items.contains(item)
    }

    /**
     * The time remaining on the countdown.
     *
     * @return The time remaining on the countdown.
     */
    fun countdown(): SerializableDuration {
        val delta = Clock.System.now().toEpochMilliseconds() - this.startTime
        return time.minus(delta.milliseconds)
    }

    /** Resets the countdown *  */
    fun resetTimer() {
        this.startTime = Clock.System.now().toEpochMilliseconds()
    }

    /** Increments the `initialTime` by 1 minute if it is less than 2 hours.  */
    fun incrementInitialTime() {
        if (initialTime < 7200) initialTime += 60
    }

    fun leaveEntity() {
        this.entity =null
    }
    fun end() {
        this.isEnded = true
    }

    companion object {
        var initialTime: Long = 2700

    }

    @Serializable
    data class Surrogate(
        val currentItems: MutableSet<Item>,
        val difficulty: Difficulty,
        val currentRoom: String,
        val currentFloor: Floor,
        val completedPuzzles: MutableSet<String>,
        val time: SerializableDuration,
        val currentEntityStates: Map<String, String>,
        val hintsUsed: MutableMap<String, Int>,
        val penalty: Int,
        val currentEntity: String?
    )

    object Serializer : KSerializer<GameState> {
        override val descriptor: SerialDescriptor
            get() = SerialDescriptor("org.virtuoso.escape.GameState", Surrogate.serializer().descriptor)

        override fun serialize(encoder: Encoder, value: GameState) {
            val surrogate = Surrogate(
                value.items,
                value.difficulty,
                value.room.id,
                value.floor,
                value.completedPuzzles,
                value.time,
                value.floor.rooms.flatMap { it.entities }.associate { it.id to it.currentState },
                value.hintsUsed,
                value.penalty,
                value.entity?.id
            )
            encoder.encodeSerializableValue(Surrogate.serializer(), surrogate)
        }

        override fun deserialize(decoder: Decoder): GameState {
            val surrogate = decoder.decodeSerializableValue(Surrogate.serializer())

            return GameState(
                surrogate.currentFloor,
                surrogate.currentRoom,
                surrogate.currentEntity,
                surrogate.currentItems,
                surrogate.time,
                surrogate.hintsUsed,
                surrogate.completedPuzzles,
                surrogate.difficulty,
                surrogate.penalty,
            ).apply {
                this.floor.rooms.flatMap { it.entities }.map { entity ->
                    surrogate.currentEntityStates[entity.id]?.let { newState -> entity.swapState(newState) }
                }
            }
        }
    }
}
