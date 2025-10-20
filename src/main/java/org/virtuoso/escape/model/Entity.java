package org.virtuoso.escape.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.virtuoso.escape.model.actions.Action;
import org.virtuoso.escape.model.actions.TakeInput;

/**
 * A finite state machine that holds all possible states for an entity to be in and which state it is currently in.
 * @author Andrew
 */
public class Entity {
	private String id;
	private Map<String,EntityState> states;
	private String currentState;

	/**
	 * Constructs and entity with multiple states.
	 * @param id The ID of the entity you want to construct.
	 * @param entity_states The states you want the entity to have,
	 * the default state will be a state with the name of the the entity id or the first state argument.
	 */
	public Entity(String id, EntityState... entity_states) {
		this.id = id;
		this.states = Arrays.stream(entity_states).collect(Collectors.toMap(EntityState::id, state -> state));
		currentState = this.states.containsKey(id) ? id : entity_states[0].id();
	}

	/**
	 * Constructs a one-state entity and its state.
	 * @param id The entity id.
	 * @param attackAction The entity attack behavior.
	 * @param inspectAction The entity inspect behavior.
	 * @param interactAction The entity interact behavior.
	 * @param inputAction The entity input behavior.
	 */
	public Entity(String id, Action attackAction, Action inspectAction, Action interactAction, TakeInput inputAction) {
		this.id = id;
		this.states = Map.of(id, new EntityState(id, attackAction, inspectAction, interactAction, inputAction));
		this.currentState = id;
	}

	/**
	 * Gets the current entity state.
	 * @return The current state of the entity.
	 */
	public EntityState state(){
		return states.get(currentState);
	}

	/**
	 * Changes the state of an entity.
	 * @param newState The name of the state to swap to.
	 */
	public void swapState(String newState){
		this.currentState = newState;
	}

	public String id(){
		return this.id;
	}

}
