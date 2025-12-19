package org.virtuoso.escape.model

/**
 * A basic functional action interface.
 *
 * @author gabri
 */
typealias ActionType = (state: GameState) -> Unit

typealias InputActionType = (input: String, state: GameState) -> Unit

object Actions {

  /**
   * Penalize the player by removing time.
   *
   * @param severity The degree of penalty to award
   * @author Andrew
   */
  fun addPenalty(severity: Severity): ActionType {
    var points = 5
    points =
        when (severity) {
          Severity.HIGH -> points * 3
          Severity.MEDIUM -> points * 2
          Severity.LOW -> points
        }
    return {
      points =
          when (it.difficulty) {
            Difficulty.VIRTUOSIC -> points * 3
            Difficulty.SUBSTANTIAL -> points * 2
            Difficulty.TRIVIAL -> points
          }
      it.penalty -= points
    }
  }

  /**
   * Add a puzzle to the completed list. .
   *
   * @author Andrew
   */
  fun completePuzzle(puzzle: String): ActionType = { it.completedPuzzles.add(puzzle) }

  /**
   * Gives an item to the user.
   *
   * @param item The item to give to the user.
   * @author gabri
   */
  fun giveItem(item: Item): ActionType = { it.items.add(item) }

  /**
   * Move to a floor and clear items.
   *
   * @param floor The number of the floor to move to.
   * @author Andrew
   */
  fun setFloor(floor: Floor): ActionType = {
    it.floor = floor
    it.items.clear()
  }

  /**
   * @param message The message to give
   * @author gabri
   */
  fun setMessage(message: String): ActionType = { it.message = message }

  fun setMessage(resource: String, vararg namespaces: String): ActionType = {
    setMessage(it.language.get(resource, *namespaces) ?: "<${namespaces[0]}/$resource>")(it)
  }

  /**
   * Changes the state of an entity.
   *
   * @param entity The entity to change the state of.
   * @param newState The state to set the entity to.
   * @author gabri
   * @author Andrew
   */
  fun swapEntities(entity: String, newState: String): ActionType {
    return { state ->
      val entityToSwap =
          state.floor.rooms.flatMap { it.entities }.firstOrNull { it.id == entity }
              ?: throw IllegalArgumentException(
                  "Entity with id '$entity' not found in current floor."
              )
      entityToSwap.swapState(newState)
    }
  }

  fun takeInput(
      cases: List<Pair<String, ActionType>>,
      default: ActionType? = null,
  ): InputActionType {
    return lambda@{ input, state ->
      for (tuple in cases) {
        if (!input.trim().lowercase().matches(tuple.first.toRegex())) continue
        tuple.second(state)
        return@lambda
      }
      default?.invoke(state)
    }
  }

  fun chain(vararg actions: ActionType): ActionType = { state -> actions.forEach { it(state) } }

  fun conditional(
      supplier: (GameState) -> Boolean,
      if_: ActionType,
      else_: ActionType? = null,
  ): ActionType {
    return { state -> if (supplier(state)) if_(state) else else_?.invoke(state) }
  }
}

/**
 * The severity of a punishment.
 *
 * @author Andrew
 */
enum class Severity {
  HIGH,
  MEDIUM,
  LOW,
}
