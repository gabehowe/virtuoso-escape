package org.virtuoso.escape.model

/**
 * An entity state that holds the state's behavior.
 *
 * @param id The entity state id.
 * @param attackAction The entity state attack behavior.
 * @param inspectAction The entity state inspect behavior.
 * @param interactAction The entity state interact behavior.
 * @param inputAction The entity state input behavior.
 * @param capabilities What this entity will perform an action for.
 * @author Andrew
 */
class EntityState(
    val id: String,
    val attackAction: ActionType?,
    val inspectAction: ActionType?,
    val interactAction: ActionType?,
    val inputAction: InputActionType?,
    val capabilities: Capabilities,
) {
  /**
   * Construct an entity state with an id and default actions.
   *
   * @param id The id of this entity.
   */
  constructor(id: String) : this(id, {}, {}, {}, null)

  /**
   * Create the entity with default capabilities
   *
   * @param id The entity state id.
   * @param attackAction The entity state attack behavior.
   * @param inspectAction The entity state inspect behavior.
   * @param interactAction The entity state interact behavior.
   * @param inputAction The entity state input behavior.
   */
  constructor(
      id: String,
      attackAction: ActionType?,
      inspectAction: ActionType?,
      interactAction: ActionType?,
      inputAction: ((String, GameState) -> Unit)?,
  ) : this(
      id,
      attackAction,
      inspectAction,
      interactAction,
      inputAction,
      Capabilities(
          attackAction != null,
          inspectAction != null,
          interactAction != null,
          inputAction != null,
      ),
  )

  /**
   * Get text with this entity's id.
   *
   * @param key The id of the text to get.
   * @return A resource string.
   */
  private fun getText(language: Language, key: String): String = language.string(this.id, key)

  /** Run the interact [ActionType]. */
  fun interact(state: GameState) {
    check(this.capabilities.interact)
    state.message = getText(state.language, "interact")
    interactAction?.invoke(state)
  }

  /** Run the attack [ActionType]. */
  fun attack(state: GameState) {
    check(this.capabilities.attack)
    state.message = getText(state.language, "attack")
    attackAction?.invoke(state)
  }

  /** Run the inspect [ActionType]. */
  fun inspect(state: GameState) {
    check(this.capabilities!!.inspect)
    state.message = getText(state.language, "inspect")
    inspectAction?.invoke(state)
  }

  /**
   * The name of this entity.
   *
   * @return The name of this entity.
   */
  fun name(language: Language): String {
    return language.string(this.id, "name")
  }

  /** Display the introductory message. */
  fun introduce(state: GameState) {
    state.message = getText(state.language, "introduce")
  }

  /**
   * Check if this [EntityState] equals another.
   *
   * @param other The [EntityState] to compare.
   * @return `true` if this and other are the same, else `false`.
   */
  fun equals(other: EntityState): Boolean = this.id == other.id

  /**
   * Run an input string against the stored input action.
   *
   * @param input The input string to compare against.
   */
  fun takeInput(input: String, state: GameState) {
    check(this.capabilities.input)
    val message =
        if (state.language["input_$input", id] == null) "I couldn't understand '$input'"
        else state.language.string(id, "input_$input")
    state.message = message
    inputAction?.invoke(input, state)
  }

  /**
   * The action capabilities of the entity -- Can it be spoken to? Used to only show relevant
   * actions to the user.
   *
   * @param attack If the entity can be attacked to.
   * @param inspect If the entity can be inspected.
   * @param interact If the entity can be interacted with.
   * @param input If the entity can be spoken with.
   */
  data class Capabilities(
      val attack: Boolean,
      val inspect: Boolean,
      val interact: Boolean,
      val input: Boolean,
  )
}
