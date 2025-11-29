package org.virtuoso.escape.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.virtuoso.escape.model.action.Action;
import org.virtuoso.escape.model.action.TakeInput;

/**
 * A finite state machine that holds all possible states for an entity to be in and which state it is currently in.
 *
 * @author Andrew
 */
public class Entity {
    private String id;
    private Map<String, EntityState> states;
    private String currentState;

    /**
     * Construct an actionless entity.
     *
     * @param id The ID of the entity.
     */
    public Entity(String id) {
        this(id, new EntityState(id));
    }

    /**
     * Construct an entity with multiple states.
     *
     * @param id The ID of the entity to change.
     * @param entity_states The states the entity has, the default state will be a state with the name of the entity id
     *     or the first state argument.
     */
    public Entity(String id, EntityState... entity_states) {
        this.id = id;
        this.states = Arrays.stream(entity_states).collect(Collectors.toMap(EntityState::id, state -> state));
        currentState = this.states.containsKey(id) ? id : entity_states[0].id();
    }

    /**
     * Construct a one-state entity and its state.
     *
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
     * Get the current entity state.
     *
     * @return The current state of the entity.
     */
    public EntityState state() {
        return states.get(currentState);
    }

    /**
     * Change the state of an entity.
     *
     * @param newState The name of the state to swap to.
     */
    public void swapState(String newState) {
        if (newState == null || !states.containsKey(newState)) {
            throw new IllegalArgumentException(
                    "Entity '" + this.id + "' cannot be swapped to non-existent state: '" + newState + "'");
        }
        this.currentState = newState;
    }

    /**
     * The id of the entity.
     *
     * @return The id of the entity.
     */
    public String id() {
        return this.id;
    }

    /**
     * Try to get a string from the current state, but try to get it based on the parent ID in the event that it fails.
     *
     * @param resource The id of the resource to attempt to get.
     * @return The string resource from the Entity, current state, or placeholder.
     */
    public String string(String resource) {
        return GameInfo.instance()
                .searchString(resource, this.id(), this.state().id());
    }

    /**
     * Get the id and state id of the entity of data saving.
     *
     * @return The id and state id of an entity.
     */
    public String[] write() {
        return new String[] {this.id, this.currentState};
    }
}
