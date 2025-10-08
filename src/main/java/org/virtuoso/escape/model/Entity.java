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

//TODO: add getLanguage method

	public void attack() {
		return GameInfo.getInstance().getLanguage(this.id).get("attack");
	}

	public void inspect() {
		return GameInfo.getInstance().getLanguage(this.id).get("inspect");
	}

	public void introduce() {
		return GameInfo.getInstance().getLanguage(this.id).get("introduce");
	}

	public boolean equals() {
		if (this.id.equals(id)) {
			return true;
		} else {
			return false;
		}
	}

	public String getId(){
		return this.id;
	}
	

	public String getName() {
		return GameInfo.getInstance().getLanguage(this.id).get("name");
	}

	public void takeInput(String input) {
		this.inputAction.withInput(input).execute();
	}
}
