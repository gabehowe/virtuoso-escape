package org.virtuoso.escape.model.data;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.Item;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.AccountManager;
import org.virtuoso.escape.model.account.Score;

import java.util.Map;

public class DataTester {
	private static DataLoader dl;
	private static JSONObject states;
	private static AccountManager am;

	static void main(String[] args) {
		/*GameState gs = GameState.instance();
		gs.setCurrentMessage("Hello");
		DataWriter.writeGameState();*/

		GameProjection gP = new GameProjection();
		System.out.println(gP.login("j", "drake"));
		//gP.login("Jay'sSon", "cat");

		/*gP.createAccount("JsonLaquermelonie", "dog");
		gP.createAccount("JSONJR.", "json123");

		gP.logout();
		gP.login("JSONJR.", "json123");*/

		//works
		Map<String, Map<String, String>> language = DataLoader.loadGameLanguage();
		System.out.println(language.get("intro_joe"));

		//works
		Map<String, Score> highScores = DataLoader.loadHighScores();
		System.out.println(highScores.get("39cccc63-6aa2-4832-a707-a9b23841ac9c").toString());

		//works. print pre changed data.
		JSONObject jGameState1 = (JSONObject) DataLoader.loadGameStates().get("39cccc63-6aa2-4832-a707-a9b23841ac9c");
		System.out.println("J's Game State: " + jGameState1);

		gP.setDifficulty(Difficulty.VIRTUOSIC);
		gP.addItem(Item.left_bread);

		gP.logout();

		//works. prints post changed data.
		JSONObject jGameState2 = (JSONObject) DataLoader.loadGameStates().get("39cccc63-6aa2-4832-a707-a9b23841ac9c");
		System.out.println("J's Game State: " + jGameState2);
	}
}
