package org.virtuoso.escape.model;

import java.util.function.BooleanSupplier;
import org.virtuoso.escape.model.actions.Action;
import org.virtuoso.escape.model.actions.TakeInput;

public class Entity{
	private String id;
	private Action attackAction;
	private Action inspectAction;
	private Action interactAction;
	private TakeInput inputAction;

	public Entity(String id, Action attackAction, Action inspectAction, Action interactAction) {
		this.id = id;
		this.attackAction = attackAction;
		this.inspectAction = inspectAction;
		this.interactAction = interactAction;
	}


	public void attack() {
        GameState.instance().setCurrentMessage(GameInfo.instance().string(this.id,"attack"));
	}

	public void inspect() {
	 GameState.instance().setCurrentMessage(GameInfo.instance().string(this.id,"inspect"));
	}

	public void introduce() {
		GameState.instance().setCurrentMessage(GameInfo.instance().string(this.id,"introduce"));
	}

	public boolean equals(Entity other) {
        return this.id.equals(other.id);
	}

	public String id(){
		return this.id;
	}
	

	public String name() {
		return GameInfo.instance().string(this.id, "name");
	}

	public void takeInput(String input) {
		this.inputAction.withInput(input).execute();
	}
}
