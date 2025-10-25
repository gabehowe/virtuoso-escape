package org.virtuoso.escape.model;

import org.virtuoso.escape.model.actions.Action;
import org.virtuoso.escape.model.actions.Default;
import org.virtuoso.escape.model.actions.TakeInput;

/**
 * An entity state that holds the state's behavior.
 *
 * @param id             The entity state id.
 * @param attackAction   The entity state attack behavior.
 * @param inspectAction  The entity state inspect behavior.
 * @param interactAction The entity state interact behavior.
 * @param inputAction    The entity state input behavior.
 * @param capabilities   What this entity will perform an action for.
 * @author Andrew
 */
public record EntityState(String id, Action attackAction, Action inspectAction, Action interactAction, TakeInput inputAction, Capabilities capabilities) {
    /**
     * Default constructor
     *
     * @param id The id of this entity.
     */
    public EntityState(String id) {
        this(id, new Default(), new Default(), new Default(), null);
    }

    /**
     * Create the entity with default capabilities
     *
     * @param id             The entity state id.
     * @param attackAction   The entity state attack behavior.
     * @param inspectAction  The entity state inspect behavior.
     * @param interactAction The entity state interact behavior.
     * @param inputAction    The entity state input behavior.
     */
    public EntityState(String id, Action attackAction, Action inspectAction, Action interactAction, TakeInput inputAction) {
        Capabilities capabilities = new Capabilities(
                attackAction == null,
                inspectAction == null,
                interactAction == null,
                inputAction == null
        );
        this(id, attackAction, inspectAction, interactAction, inputAction, capabilities);
    }

    /**
     * Get text with this entity's id.
     *
     * @param key The id of the text to get.
     * @return A resource string.
     */
    private String getText(String key) {
        try {
            return GameInfo.instance().string(this.id, key);
        } catch (Exception e) {
            return "[Missing text: " + key + "]";
        }
    }

    /**
     * Run the interact {@link Action}.
     */
    public void interact() {
        GameState.instance().setCurrentMessage(getText("interact"));

        if (interactAction != null) interactAction.execute();
    }

    /**
     * Run the attack {@link Action}.
     */
    public void attack() {
        GameState.instance().setCurrentMessage(getText("attack"));
        if (attackAction != null) attackAction.execute();
    }

    /**
     * Run the inspect {@link Action}.
     */
    public void inspect() {
        GameState.instance().setCurrentMessage(getText("inspect"));
        if (inspectAction != null) inspectAction.execute();
    }

    /**
     * The name of this entity.
     *
     * @return The name of this entity.
     */
    public String name() {
        return GameInfo.instance().string(this.id, "name");
    }

    /**
     * Display the introductory message.
     */
    public void introduce() {
        GameState.instance().setCurrentMessage(getText("introduce"));
    }

    /**
     * Check if this {@link EntityState} equals another.
     *
     * @param other The {@link EntityState} to compare.
     * @return {@code true} if this and other are the same, else {@code false}.
     */
    public boolean equals(EntityState other) {
        return this.id.equals(other.id);
    }

    /**
     * The id of this entity.
     *
     * @return The id of this entity.
     */
    public String id() {
        return this.id;
    }

    /**
     * Run an input string against the stored input action.
     *
     * @param input The input string to compare against.
     */
    public void takeInput(String input) {
        String message;
        if (GameInfo.instance().language().get(id) == null || GameInfo.instance().language().get(id).get("input_" + input) == null)
            message = "I couldn't understand '" + input + "'";
        else message = GameInfo.instance().string(id, "input_" + input);
        if (message != null) {
            GameState.instance().setCurrentMessage(message);
        }

        if (inputAction != null) {
            inputAction.withInput(input).execute();
        }
    }

}

record Capabilities(boolean attack, boolean inspect, boolean interact, boolean input) {

}
