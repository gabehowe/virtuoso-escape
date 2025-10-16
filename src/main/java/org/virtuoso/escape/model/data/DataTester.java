package org.virtuoso.escape.model.data;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.account.AccountManager;

public class DataTester {
	private static DataLoader dl;
	private static JSONObject states;
	private static AccountManager am;

	static void main(String[] args) {
//		GameState gs = GameState.instance();
//		gs.setCurrentMessage("Hello");
//		DataWriter.writeGameState();

		GameProjection gP = new GameProjection();
		gP.login("JsonLaquermelonie", "dog");
		//gP.login("Jay'sSon", "cat");


		//gP.newAccount("JsonLaquermelonie", "dog");
		gP.createAccount("JSONJR.", "cat");
		gP.login("JSONJR.", "cat"); //accounts.json never updates. results in null
	}
}
