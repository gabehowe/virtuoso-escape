package org.virtuoso.escape.model;

import org.virtuoso.escape.model.actions.Action;
import org.virtuoso.escape.model.actions.TakeInput;

public class Entity {
	private String id;
	private Action attackAction;
	private Action inspectAction;
	private Action interactAction;
	private TakeInput inputAction;

	public Entity(String id, Action attackAction, Action inspectAction, Action interactAction, TakeInput inputAction) {
		this.id = id;
		this.attackAction = attackAction;
		this.inspectAction = inspectAction;
		this.interactAction = interactAction;
		this.inputAction = inputAction;
	}

	public void absorb(Entity other){
		this.id = other.id;
		this.attackAction = other.attackAction;
		this.inspectAction = other.inspectAction;
		this.interactAction = other.interactAction;
		this.inputAction = other.inputAction;
	}

	private String getText(String key) {
		try {
			return GameInfo.instance().string(this.id, key);
		} catch (Exception e) {
			return "[Missing text: " + key + "]";
		}
	}

	public void interact() {
		GameState.instance().setCurrentMessage(getText("interact"));

		if (interactAction != null) interactAction.execute();
	}

	public void attack() {
		GameState.instance().setCurrentMessage(getText("attack"));
		if (attackAction != null) attackAction.execute();
	}

	public void inspect() {
		GameState.instance().setCurrentMessage(getText("inspect"));
		if (inspectAction != null) inspectAction.execute();
	}

	public String name() {
		return GameInfo.instance().string(this.id, "name");
	}

	public void introduce() {
		GameState.instance().setCurrentMessage(getText("introduce"));
	}

	public boolean equals(Entity other) {
		return this.id.equals(other.id);
	}

	public String id() {
		return this.id;
	}

	public void takeInput(String input) {
		String message = GameInfo.instance().string(this.id, "input_" + input);
		if (message != null) {
			GameState.instance().setCurrentMessage(message);
		}

		if (inputAction != null) {
			inputAction.withInput(input).execute();
		}
	}

}
