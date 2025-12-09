@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model

import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import org.virtuoso.escape.model.account.Account
import org.virtuoso.escape.model.account.AccountManager
import org.virtuoso.escape.model.data.DataLoader
import org.virtuoso.escape.model.data.DataWriter

/**
 * The game facade.
 *
 * @author Treasure
 */
class GameProjection(fileReader: ((String) -> String), fileWriter: (String, String) -> Unit) {
  init {
    DataLoader.FILE_READER = fileReader
    DataWriter.FILE_WRITER = fileWriter
  }

  val accountManager: AccountManager = AccountManager()
  lateinit var state: GameState
  lateinit var account: Account
  val language = DataLoader.loadGameLanguage()!!

  /**
   * Initialize and attempt login.
   *
   * @param username The username to attempt.
   * @param password The password to attempt.
   * @return `true` if the username-password combination was valid, otherwise `false`.
   */
  fun login(username: String, password: String): Boolean {
    val currentAccount =
        this.accountManager.login(username, password)?.let {
          account = it
          state = accountManager.gameStates[account.id]!!
          state.language = language
        }
    return currentAccount != null
  }

  /**
   * Attempt to create an account.
   *
   * @param username The username to attempt.
   * @param password The password to attempt.
   * @return `true` if no user exists with this exact username-password combination, otherwise
   *   `false`.
   */
  fun createAccount(username: String, password: String): Boolean {
    this.accountManager.newAccount(username, password)?.let {
      account = it
      this.state = GameState()
      state.language = language
      return true
    }
    return false
  }

  /** Log the current user out and write data. */
  @OptIn(ExperimentalUuidApi::class)
  fun logout() {
    DataWriter.writeAccount(account, accountManager)
    DataWriter.writeGameState(state, account, accountManager)
  }

  /**
   * The room the user is in.
   *
   * @return The room the user is in.
   */
  fun currentRoom(): Room {
    return this.state.room
  }

  /**
   * Change the current room.
   *
   * @param room The room to change to.
   */
  fun pickRoom(room: Room) {
    this.state.room = room
  }

  /**
   * The currently focused entity.
   *
   * @return The [<] if the current entity is non-null, otherwise null.
   */
  fun currentEntity(): Entity? {
    return this.state.entity
  }

  /**
   * The current floor.
   *
   * @return The current floor.
   */
  fun currentFloor(): Floor {
    return this.state.floor
  }

  /**
   * Consume the current message.
   *
   * @return [<] if the message is non-null, otherwise null.
   */
  fun currentMessage(): String? {
    return this.state.message
  }

  /**
   * The full inventory of the user.
   *
   * @return A list of items.
   */
  fun currentItems(): MutableList<Item> {
    return this.state.items.toMutableList()
  }

  /**
   * Set the difficulty to `difficulty`
   *
   * @param difficulty The [Difficulty] to change to.
   */
  fun setDifficulty(difficulty: Difficulty) {
    this.state.difficulty = difficulty
  }

  /**
   * Focus `entity`
   *
   * @param entity The [Entity] to focus.
   */
  fun pickEntity(entity: Entity?) {
    this.state.entity = (entity)
  }

  /** Unfocus the current entity. */
  fun leaveEntity() {
    this.state.leaveEntity()
  }

  /**
   * The time remaining on the countdown.
   *
   * @return The time remaining on the countdown.
   */
  fun time(): Duration {
    return this.state.countdown()
  }

  /** Reset time remaining on the countdown. */
  fun resetTimer() {
    this.state.resetTimer()
  }

  /** Increments the `initialTime` by 1 minute if it is less than 2 hours. */
  fun incrementInitialTime() {
    this.state.incrementInitialTime()
  }

  /**
   * Interact with the currently focused entity.
   *
   * @throws java.util.NoSuchElementException if no entity is focused.
   */
  fun interact() {
    currentEntity()!!.state()!!.interact(state)
  }

  /**
   * Attack the currently focused entity.
   *
   * @throws java.util.NoSuchElementException if no entity is focused.
   */
  fun attack() {
    currentEntity()!!.state()!!.attack(state)
  }

  /**
   * Inspect the currently focused entity.
   *
   * @throws java.util.NoSuchElementException if no entity is focused.
   */
  fun inspect() {
    currentEntity()!!.state()!!.inspect(state)
  }

  /**
   * Speak to the current entity.
   *
   * @param input The input to give to the entity.
   * @throws java.util.NoSuchElementException if no entity is focused.
   */
  fun input(input: String) {
    currentEntity()!!.state()!!.takeInput(input, state)
  }

  /**
   * The capabilities of the current entity -- whether it supports an action.
   *
   * @return The capabilities of the entity.
   */
  fun capabilities(): EntityState.Capabilities {
    return currentEntity()!!.state()!!.capabilities
  }

  val isEnded: Boolean
    /**
     * Whether the game is ended.
     *
     * @return `true` if the game has ended, otherwise `false`.
     */
    get() = this.state.isEnded
}
