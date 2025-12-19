@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package org.virtuoso.escape.model

import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import org.virtuoso.escape.TestHelper
import org.virtuoso.escape.model.account.Account
import org.virtuoso.escape.model.account.AccountManager

class GameProjectionTests {
  private lateinit var proj: GameProjection
  private val testUser = "dummy"
  private val testPass = "dummy"

  @BeforeTest
  fun setup() {
    // Setup AccountManager with one existing user
    val account = Account(testUser, testPass)
    val floor = Floor.StoreyI
    val roomId = floor.rooms.first().id
    val gameState =
        GameState(
            currentFloor = floor,
            currentRoomId = roomId,
            entityId = null,
            items = mutableSetOf(),
            time = 100.seconds,
            hintsUsed = mutableMapOf(),
            completedPuzzles = mutableSetOf(),
            difficulty = Difficulty.TRIVIAL,
            penalty = 0,
        )

    val accounts = mapOf(account.id to account)
    val gameStates = mapOf(account.id to gameState)
    // GameState requires language to be set usually by GameProjection or manual injection
    // But GameProjection sets it on login.

    val am = AccountManager(accounts, gameStates)
    proj = GameProjection(TestHelper.FILE_READER(this::class), TestHelper.DUMMY_WRITER)
  }

  @Test
  fun testLoginSuccess() {
    assertTrue(proj.login(testUser, testPass))
    assertEquals(testUser, proj.account.username)
    assertNotNull(proj.state)
    // Verify language is set
    assertNotNull(proj.state.language)
  }

  @Test
  fun testLoginFailure() {
    assertFailsWith<AccountManager.AccountError>{ proj.login("nonExistent", "password") }
    assertFailsWith<AccountManager.AccountError>{ proj.login(testUser, "wrongPassword") }
  }

  @Test
  fun testCreateAccountNewUserFailsDueToBug() {
    // Doc: "Attempt to create an account... return true if no user exists..."
    // Implementation Bug: newAccount returns new Account, but AccountManager is immutable/not
    // updated.
    // GameProjection expects it in map -> NPE.
    assertTrue(proj.createAccount("newUser", "newPass"))
    assertFalse(proj.state.isEnded)
  }

  @Test
  fun testCreateAccountExistingUser() {
    // If user exists, it actually acts as login
    assertTrue(proj.createAccount(testUser, testPass))
    assertEquals(testUser, proj.account.username)
  }

  @Test
  fun testLogout() {
    proj.login(testUser, testPass)
    proj.logout()
  }

  @Test
  fun testDelegatedProperties() {
    proj.login(testUser, testPass)

    // Initial state check
    assertNotNull(proj.currentFloor())
    assertNotNull(proj.currentRoom())
    assertNull(proj.currentEntity())

    // Room change
    val room =
        proj
            .currentFloor()
            .rooms
            .last() // Use last to ensure it's different if possible, or just checking setter
    proj.pickRoom(room)
    assertEquals(room, proj.currentRoom())

    // Difficulty
    proj.setDifficulty(Difficulty.VIRTUOSIC)
    assertEquals(Difficulty.VIRTUOSIC, proj.state.difficulty)
  }

  @Test
  fun testEntityInteractions() {
    proj.login(testUser, testPass)

    var interacted = false
    var attacked = false
    var inspected = false
    var inputReceived = ""

    val state =
        EntityState(
            "testState",
            { attacked = true },
            { inspected = true },
            { interacted = true },
            Actions.takeInput(listOf("test" to { inputReceived = "test" })),
        )
    val entity = Entity("testEntity", state)

    // Should fail when no entity selected
    assertFailsWith<NullPointerException> { proj.interact() }
    assertFailsWith<NullPointerException> { proj.attack() }
    assertFailsWith<NullPointerException> { proj.inspect() }
    assertFailsWith<NullPointerException> { proj.input("test") }

    proj.pickEntity(entity)
    assertEquals(entity, proj.currentEntity())

    proj.interact()
    assertTrue(interacted)

    proj.attack()
    assertTrue(attacked)

    proj.inspect()
    assertTrue(inspected)

    proj.input("test")
    assertEquals("test", inputReceived)

    assertNotNull(proj.capabilities())

    proj.leaveEntity()
    assertNull(proj.currentEntity())
  }

  @Test
  fun testItems() {
    proj.login(testUser, testPass)
    assertTrue(proj.currentItems().isEmpty(), "Expected no items.")

    proj.state.items.add(Item.Keys)
    assertTrue(proj.currentItems().any { it == Item.Keys }, "Expected keys")
  }

  @Test
  fun testTime() {
    proj.login(testUser, testPass)

    proj.resetTimer()
    assertTrue(proj.time() > 0.seconds)

    proj.incrementInitialTime()
  }

  @Test
  fun testCurrentMessage() {
    proj.login(testUser, testPass)

    proj.state.message = "Hello"
    assertEquals("Hello", proj.currentMessage())
  }

  @Test
  fun testIsEnded() {
    proj.login(testUser, testPass)
    assertFalse(proj.isEnded)

    proj.state.isEnded = true
    assertTrue(proj.isEnded)
  }
}
