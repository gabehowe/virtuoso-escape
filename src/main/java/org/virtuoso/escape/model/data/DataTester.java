package org.virtuoso.escape.model.data;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.AccountManager;

import java.util.Map;

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
		gP.createAccount("JSONJR.", "drake");

		gP.logout();
		gP.login("JSONJR.", "drake");

		//Map<String, Map<String, String>> language = DataLoader.loadGameLanguage();


	}
}
