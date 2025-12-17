@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package org.virtuoso.escape.model

import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import org.virtuoso.escape.TestHelper
import org.virtuoso.escape.model.account.Account
import org.virtuoso.escape.model.account.AccountManager
import org.virtuoso.escape.model.action.Actions
import org.virtuoso.escape.model.data.DataLoader

/** @author Andrew Heuer */
class EntityTests {
  private lateinit var proj: GameProjection

  @BeforeTest
  fun pre() {
    // Setup mock DataLoader
    DataLoader.FILE_READER = { path ->
      when {
        path.endsWith("accounts.json") -> "{}"
        path.endsWith("gamestates.json") -> "{}"
        path.endsWith("language.json") -> "{}"
        else -> throw IllegalArgumentException("Unknown path: $path")
      }
    }

    // Workaround for buggy createAccount: manually setup AccountManager
    val dummyUser = "dummy"
    val dummyPass = "dummy"
    val account = Account(dummyUser, dummyPass)

    // Use a valid GameState
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
    val am = AccountManager(accounts, gameStates)

    proj = GameProjection(TestHelper.FILE_READER(this::class), TestHelper.DUMMY_WRITER)
    // Login to set state
    proj.login(dummyUser, dummyPass)
  }

  // -- Constructor Tests -- //
  @Test
  fun constructActionlessEntityTest() {
    val entity = Entity("testEntity")

    assertEquals("testEntity", entity.id)
    assertNotNull(entity.state())
    assertEquals("testEntity", entity.state().id)
  }

  @Test
  fun constructEntityMultipleStatesDefaultIdTest() {
    val state1 = EntityState("state1")
    val state2 = EntityState("state2")

    val entity = Entity("state1", state2, state1)

    assertEquals("state1", entity.id)
    assertEquals("state1", entity.state().id)
  }

  @Test
  fun constructEntityMultipleStatesDefaultFirstStateTest() {
    val state1 = EntityState("state1")
    val state2 = EntityState("state2")

    val entity = Entity("missingId", state1, state2)

    assertEquals("missingId", entity.id)
    assertEquals("state1", entity.state().id)
  }

  @Test
  fun constructEntityWithActionsTest() {
    var attackExecuted = false
    val attack = { _: GameState -> attackExecuted = true }
    val inspect = Actions.setMessage("inspect")
    val interact = Actions.setMessage("interact")
    val input = Actions.takeInput(listOf("cmd" to { _: GameState -> }))

    val entity = Entity("actionEntity", attack, inspect, interact, input)

    assertEquals("actionEntity", entity.id)
    assertNotNull(entity.state())
    assertNotNull(entity.state().attackAction)
    assertNotNull(entity.state().inspectAction)
    assertNotNull(entity.state().interactAction)
    assertNotNull(entity.state().inputAction)

    entity.state().attackAction!!.invoke(proj.state)
    assertTrue(attackExecuted)
  }

  /*
      @Test
      fun constructWithNullIdTest() {
          // Kotlin non-nullable types makes this redundant/impossible without java reflection/interop abuse.
          // assertFailsWith<NullPointerException> { Entity(null) }
      }
  */
  // -- State Swap Tests -- //
  @Test
  fun swapStateTests() {
    // Unrolling parameterized test
    val cases = listOf(Pair("state1", "state2") to "state2", Pair("state1", "state1") to "state1")

    for ((setup, expected) in cases) {
      val (initial, newState) = setup

      // Note: Entity constructor logic: if id is present in states, default to it.
      // We want to test swapping.
      val state1 = EntityState("state1")
      val state2 = EntityState("state2")
      // Here we just need an entity with both states
      val entity = Entity("state1", state1, state2)
      // Ensure we start at 'initial' (which is state1 by constructor)
      // If initial was state2, we'd need to swap first?
      // The parameterized test says: initial state is somewhat irrelevant if we construct fresh.
      // But let's assume we want to call swapState(newState) and check expected.

      entity.swapState(newState)
      assertEquals(expected, entity.state().id)
    }
  }

  @Test
  fun swapNonExistentState() {
    val state1 = EntityState("state1")
    val entity = Entity("myEntity", state1)

    assertFailsWith<IllegalArgumentException> { entity.swapState("nonexistent") }
  }

  // -- id Tests -- //
  @Test
  fun idMethodTest() {
    val entity = Entity("testState")
    val result: String = entity.id
    assertEquals("testState", result)
  }

  // -- State Retrieval Tests -- //
  @Test
  fun stateRetrievalTest() {
    val state = EntityState("myState")
    val entity = Entity("myEntity", state)

    assertEquals("myState", entity.state().id)
  }

  // -- String Resource Tests -- //
  @Test
  fun stringMethodTest() {
    val state = EntityState("myState")
    val entity = Entity("myEntity", state)

    // Mock Language - but Language class is not an interface.
    // We need a real language instance or simple valid data.
    // Assuming Language parses a map.
    val langData = mapOf("myEntity" to mapOf("testKey" to "testValue"))
    val language = Language(langData)

    assertEquals("testValue", entity.string(language, "testKey"))

    // Behavior check: Entity.string throws NPE if not found (due to !!)
    assertFailsWith<NullPointerException> { entity.string(language, "missingKey") }
  }
}
