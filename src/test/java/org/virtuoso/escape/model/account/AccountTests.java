package org.virtuoso.escape.model.account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameProjection;

import java.time.Duration;
import java.util.UUID;

/**
 * @author Treasure
 */
class AccountTests {
	GameProjection proj;

	@DisplayName("Should return non-null account given valid username and password")
	@Test
	void testValidFirstConstructor(){
		Account dummyJr = new Account("dummyJr", "dummyJr");
		assertNotNull(dummyJr);
	}

	@DisplayName("Should return empty username")
	@Test
	void testEmptyFirstConstructor(){
		Account emptyDummy = new Account("", "");
		assertEquals("", emptyDummy.username());
	}

	@DisplayName("Should throw an IllegalArgumentException on attempt to create account with null username and password")
	@Test
	void testNullFirstConstructor(){
		assertThrows(IllegalArgumentException.class, () -> new Account(null, null));
	}

	@DisplayName("Should return non-null account given valid parameters")
	@Test
	void testValidSecondConstructor(){
		Account mrsDummy = new Account("mrsdummy", "mrsdummy", UUID.randomUUID(), new Score(null, Difficulty.SUBSTANTIAL, null), false);
		assertNotNull(mrsDummy);
	}

	@DisplayName("Should throw an IllegalArgumentException on attempt to create account with null parameters")
	@Test
	void testNullSecondConstructor(){
		assertThrows(IllegalArgumentException.class, () -> new Account(null, null, null, null, false));
	}

	@DisplayName("Should successfully hash a valid password")
	@Test
	void testValidHashPassword(){
		String hashedPassword = Account.hashPassword("dummy");
		assertNotNull(hashedPassword);
		assertEquals("829c3804401b0727f70f73d4415e162400cbe57b", hashedPassword);
	}

	@DisplayName("Should throw an IllegalArgumentException on attempt to hash a null password")
	@Test
	void testNullHashPassword(){
		assertThrows(IllegalArgumentException.class, () -> Account.hashPassword(null));
	}

	@DisplayName("Should successfully update TTS setting to true")
	@Test
	void testTTSSetting() {
		Account mrsDummy = new Account("mrsdummy", "mrsdummy", UUID.randomUUID(), new Score(null, Difficulty.SUBSTANTIAL, null), false);
		mrsDummy.SetTtsOn(true);
		assertTrue(mrsDummy.ttsOn());
	}

	@DisplayName("Should return non-null username")
	@Test
	void testUsername(){
		Account dummy = new Account("dummy", "dummy");
		assertNotNull(dummy.username());
	}

	@DisplayName("Should not equal previous score after new high score is set")
	@Test
	void testHighScore(){
		Account dummy = new Account("dummy", "dummy");
		dummy.setHighScore(new Score(Duration.ofSeconds(1800), Difficulty.VIRTUOSIC, 1800L));
		assertNotEquals(new Score(null, Difficulty.TRIVIAL, null), dummy.highScore());
	}

	@DisplayName("Should return non-null hashed password")
	@Test
	void testHashedPassword(){
		Account mrsDummy = new Account("mrsdummy", "mrsdummy");
		assertNotNull(mrsDummy.hashedPassword());
	}

	@DisplayName("Should return non-null UUID")
	@Test
	void testUUID(){
		Account mrsDummy = new Account("mrsdummy", "mrsdummy");
		assertNotNull(mrsDummy.id());
	}
}