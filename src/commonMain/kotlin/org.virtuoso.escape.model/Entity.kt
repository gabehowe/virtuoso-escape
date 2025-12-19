package org.virtuoso.escape.model

/**
 * A finite state machine that holds all possible states for an entity to be in and which state it
 * is currently in.
 *
 * @author Andrew
 */
class Entity(val id: String, val states: Map<String, EntityState>, var currentState: String) {

  /**
   * Construct an actionless entity.
   *
   * @param id The ID of the entity.
   */
  constructor(id: String) : this(id, EntityState(id))

  /**
   * Construct an entity with multiple states.
   *
   * @param id The ID of the entity to change.
   * @param entityStates The states the entity has, the default state will be a state with the name
   *   of the entity id or the first state argument.
   */
  constructor(
      id: String,
      vararg entityStates: EntityState,
  ) : this(
      id,
      entityStates.associateBy { it.id },
      if (entityStates.any { it.id == id }) id else entityStates[0].id,
  )

  /**
   * Construct a one-state entity and its state.
   *
   * @param id The entity id.
   * @param attackAction The entity attack behavior.
   * @param inspectAction The entity inspect behavior.
   * @param interactAction The entity interact behavior.
   * @param inputAction The entity input behavior.
   */
  constructor(
      id: String,
      attackAction: ActionType?,
      inspectAction: ActionType?,
      interactAction: ActionType?,
      inputAction: InputActionType?,
  ) : this(
      id,
      mapOf(id to EntityState(id, attackAction, inspectAction, interactAction, inputAction)),
      id,
  )

  /**
   * Get the current entity state.
   *
   * @return The current state of the entity.
   */
  fun state(): EntityState {
    return states[currentState]
        ?: throw NoSuchElementException("${this.id} $currentState ${this.states.keys}")
  }

  /**
   * Change the state of an entity.
   *
   * @param newState The name of the state to swap to.
   */
  fun swapState(newState: String) {
    require(states.containsKey(newState)) {
      "Entity '${this.id}' cannot be swapped to non-existent state: '${newState}'"
    }
    this.currentState = newState
  }

  /**
   * Try to get a string from the current state, but try to get it based on the parent ID in the
   * event that it fails.
   *
   * @param resource The id of the resource to attempt to get.
   * @return The string resource from the Entity, current state, or placeholder.
   */
  fun string(language: Language, resource: String): String = language[resource, this.id, this.state().id]!!
}
