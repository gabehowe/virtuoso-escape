package org.virtuoso.escape.model.data;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.Item;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.AccountManager;
import org.virtuoso.escape.model.account.Score;

import java.time.Duration;
import java.util.Map;

/**
 * Tests for {@link DataLoader} and {@link DataWriter}
 * @author Andrew
 * @author Treasure
 */
public class DataTester {
	private static DataLoader dl;
	private static JSONObject states;
	private static AccountManager am;

	static void main(String[] args) {
		GameState gs = GameState.instance();

		GameProjection gP = new GameProjection();
		//System.out.println(gP.createAccount("q", "q")); //Account already exists so returns false
		//gP.login("Jay'sSon", "cat");

		//works
		System.out.println(gP.login("f", "q")); //Outputs "Username input is invalid"
		System.out.println(gP.login("q", "t")); //Outputs "Password input is invalid"

		/*gP.createAccount("JsonLaquermelonie", "dog");
		gP.createAccount("JSONJR.", "json123");*/

		/*gP.logout();
		gP.login("JSONJR.", "json123");*/

		//works
		Map<String, Map<String, String>> language = DataLoader.loadGameLanguage();
		System.out.println(language.get("intro_joe"));

		//works
		Map<String, Score> qHighScores1 = DataLoader.loadHighScores();
		System.out.println(qHighScores1.get("5531f883-41fd-4f3c-b3f7-a1ef6d3c82e6").toString());

		//works. print pre changed data.
		JSONObject qGameState1 = (JSONObject) DataLoader.loadGameStates().get("5531f883-41fd-4f3c-b3f7-a1ef6d3c82e6");
		System.out.println("Q's Game State: " + qGameState1);

//		gP.setDifficulty(Difficulty.VIRTUOSIC);
//		gP.addItem(Item.left_bread);
//
//		gs.setTime(Duration.ofSeconds(2000));
//		gs.end();
//
//		gP.logout();

		//works. prints post changed data.
		Map<String, Score> qHighScores2 = DataLoader.loadHighScores();
		System.out.println(qHighScores2.get("5531f883-41fd-4f3c-b3f7-a1ef6d3c82e6").toString());

		JSONObject qGameState2 = (JSONObject) DataLoader.loadGameStates().get("5531f883-41fd-4f3c-b3f7-a1ef6d3c82e6");
		System.out.println("Q's Game State: " + qGameState2);
	}
}
