package org.virtuoso.escape.model.data;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.Util;
import org.virtuoso.escape.model.account.AccountManager;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Treasure
 */
public class DataWriterTests {
    private static String stateData = """
            {}
            """;
    private static String accountData = """
            {}
            """;
    GameProjection projection;

    @BeforeEach
    void pre() throws Exception {
        Util.rebuildSingleton(GameState.class);
        Util.rebuildSingleton(AccountManager.class);
        DataWriter.ACCOUNTS_PATH = Paths.get(getClass()
                                                .getResource("/org/virtuoso/escape/model/data/accounts.json")
                                                .toURI())
                                        .toString();
        DataWriter.GAMESTATES_PATH = Paths.get(getClass()
                                                  .getResource("/org/virtuoso/escape/model/data/gamestates.json")
                                                  .toURI())
                                          .toString();
        DataLoader.ACCOUNTS_PATH = Paths.get(getClass()
                                                .getResource("/org/virtuoso/escape/model/data/accounts.json")
                                                .toURI())
                                        .toString();
        DataLoader.GAMESTATES_PATH = Paths.get(getClass()
                                                  .getResource("/org/virtuoso/escape/model/data/gamestates.json")
                                                  .toURI())
                                          .toString();
        try {
            Files.writeString(Path.of(DataWriter.ACCOUNTS_PATH), accountData);
            Files.writeString(Path.of(DataWriter.GAMESTATES_PATH), stateData);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't write to file.");
        }
        projection = new GameProjection();
    }

    @DisplayName("Should successfully write valid account and find current account's username upon load")
    @Test
    void testWritingValidAccount() {
        boolean isDummy = projection.createAccount("dummy", "dummy");
        DataWriter.writeAccount();
        assertTrue(isDummy);
        JSONObject accounts = DataLoader.loadAccounts();
        assertTrue(accounts.containsKey(GameState.instance().account().id().toString()));
        JSONObject dummyJSON =
                (JSONObject) accounts.get(GameState.instance().account().id().toString());
        assertEquals("dummy", dummyJSON.get("username"));
    }

    @DisplayName("Should fail to write null account and throw a NullPointerException")
    @Test
    void testWritingNullAccount() {
        assertThrows(NullPointerException.class, () -> projection.createAccount(null, null));
        assertThrows(NullPointerException.class, DataWriter::writeAccount);
    }

    @DisplayName("Should write account with empty username and password, and find it's empty username upon load")
    @Test
    void testWritingEmptyAccount() {
        projection.createAccount("", "");
        DataWriter.writeAccount();
        assertTrue(DataLoader.loadAccounts()
                             .containsKey(GameState.instance().account().id().toString()));
        JSONObject emptyDummyJSON = (JSONObject) DataLoader.loadAccounts()
                                                           .get(GameState.instance().account().id().toString());
        assertEquals("", emptyDummyJSON.get("username"));
    }

    @DisplayName("Should write multiple valid accounts and find current account's password upon load")
    @Test
    void testWritingMultipleValidAccounts() {
        projection.createAccount("dummy", "dummy");
        DataWriter.writeAccount();
        projection.createAccount("nulldummy", "nulldummy");
        DataWriter.writeAccount();
        projection.createAccount("emptydummy", "emptydummy");
        DataWriter.writeAccount();
        projection.createAccount("mrsdummy", "mrsdummy");
        DataWriter.writeAccount();
        assertEquals(4, DataLoader.loadAccounts().size());
        assertEquals(
                "3a77e04b2d960ac37396832feff4d1753cf29cd7",
                GameState.instance().account().hashedPassword());
    }

    @DisplayName("Should not write any accounts and confirm accounts.json remains empty")
    @Test
    void testWritingZeroAccounts() {
        try {
            DataWriter.writeAccount();
        } catch (Exception _) {
        }
        assertTrue(DataLoader.loadAccounts().isEmpty());
    }

    @DisplayName("Should write valid game state and find changed difficulty upon load")
    @Test
    void testWritingValidGameState() {
        projection.createAccount("mrsdummy", "mrsdummy");
        GameState.instance().setDifficulty(Difficulty.TRIVIAL);
        DataWriter.writeGameState();
        assertTrue(DataLoader.loadGameStates()
                             .containsKey(GameState.instance().account().id().toString()));
        JSONObject mrsdummyGS = (JSONObject) DataLoader.loadGameStates()
                                                       .get(GameState.instance().account().id().toString());
        assertEquals(
                Difficulty.TRIVIAL,
                Difficulty.valueOf(mrsdummyGS.get("difficulty").toString()));
    }

    @DisplayName("Should fail to write game state with null account and throw a NullPointerException")
    @Test
    void testWritingNullGameState() {
        assertThrows(NullPointerException.class, () -> projection.createAccount(null, null));
        assertThrows(NullPointerException.class, DataWriter::writeGameState);
    }
}
