package org.virtuoso.escape.model.action;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.data.DataLoader;

public class ActionTests {
    @BeforeEach
    public void pre(){
        var j = new GameProjection();
        var accountsPath = getClass().getResource("mock_accounts.json").getPath();
        DataLoader.ACCOUNTS_PATH = accountsPath;
    }
    @AfterEach
    public void post() {

    }

    @Test
    public void addPenaltyTest() {
    }

}
