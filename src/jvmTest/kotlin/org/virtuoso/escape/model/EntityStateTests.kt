package org.virtuoso.escape.model

import kotlin.test.*
import org.virtuoso.escape.TestHelper

/** @author Andrew */
class EntityStateTests {

  private lateinit var proj: GameProjection

  @BeforeTest
  fun pre() {
    // Setup mock DataLoader

    proj =
        GameProjection(
            { path ->
              when {
                path.endsWith("accounts.json") -> "{}"
                path.endsWith("gamestates.json") -> "{}"
                path.endsWith("language.json") -> "{}"
                else -> throw IllegalArgumentException("Unknown path: $path")
              }
            },
            TestHelper.DUMMY_WRITER,
        )
    // Create a dummy account to initialize state
    proj.createAccount("testUser", "password")
  }

  // -- Constructor Tests -- //
  @Test
  fun constructActionlessStateTest() {
    val state = EntityState("testState")

    assertEquals("testState", state.id, "ID should be set correctly")
    assertTrue(state.capabilities.attack, "Attack capability should be true")
    assertTrue(state.capabilities.inspect, "Inspect capability should be true")
    assertTrue(state.capabilities.interact, "Interact capability should be true")
    assertFalse(state.capabilities.input, "Input capability should be false")

    assertNotNull(state.attackAction, "Attack action should not be null")
    assertNotNull(state.inspectAction, "Inspect action should not be null")
    assertNotNull(state.interactAction, "Interact action should not be null")
    assertNull(state.inputAction, "Input action should be null")
  }

  @Test
  fun constructState5Param() {
    val attack = Actions.addPenalty(Severity.HIGH)
    val inspect = Actions.setFloor(Floor.StoreyII)
    val interact = Actions.setMessage("TestMessage")
    val input = Actions.takeInput(emptyList())

    val state = EntityState("testState", attack, inspect, interact, input)

    assertEquals("testState", state.id)
    assertSame(attack, state.attackAction)
    assertSame(inspect, state.inspectAction)
    assertSame(interact, state.interactAction)
    assertSame(input, state.inputAction)

    assertTrue(state.capabilities.attack)
    assertTrue(state.capabilities.inspect)
    assertTrue(state.capabilities.interact)
    assertTrue(state.capabilities.input)
  }

  @Test
  fun constructStateSomeNullParam() {
    val attack = Actions.addPenalty(Severity.HIGH)
    val interact = Actions.setMessage("TestMessage")

    val state = EntityState("testState", attack, null, interact, null)

    assertEquals("testState", state.id)
    assertSame(attack, state.attackAction)
    assertNull(state.inspectAction)
    assertSame(interact, state.interactAction)
    assertNull(state.inputAction)

    assertTrue(state.capabilities.attack)
    assertFalse(state.capabilities.inspect)
    assertTrue(state.capabilities.interact)
    assertFalse(state.capabilities.input)
  }

  @Test
  fun constructStateAllNullParam() {
    val state = EntityState("testState", null, null, null, null)

    assertEquals("testState", state.id)
    assertNull(state.attackAction)
    assertNull(state.inspectAction)
    assertNull(state.interactAction)
    assertNull(state.inputAction)

    assertFalse(state.capabilities.attack)
    assertFalse(state.capabilities.inspect)
    assertFalse(state.capabilities.interact)
    assertFalse(state.capabilities.input)
  }

  @Test
  fun constructState6Param() {
    val attack = Actions.addPenalty(Severity.HIGH)
    val inspect = Actions.setFloor(Floor.StoreyII)
    val interact = Actions.setMessage("InteractMessage")
    val input = Actions.takeInput(emptyList())
    val capabilities = EntityState.Capabilities(true, false, false, true)

    val state = EntityState("testState", attack, inspect, interact, input, capabilities)

    assertEquals("testState", state.id)
    assertSame(attack, state.attackAction)
    assertSame(inspect, state.inspectAction)
    assertSame(interact, state.interactAction)
    assertSame(input, state.inputAction)
    assertSame(capabilities, state.capabilities)

    assertTrue(state.capabilities.attack)
    assertFalse(state.capabilities.inspect)
    assertFalse(state.capabilities.interact)
    assertTrue(state.capabilities.input)
  }

  // -- Action Tests -- //
  // Replaces parameterized test
  @Test
  fun actionCallTest() {
    val testCases =
        listOf(
            Triple("interact", true, false),
            Triple("interact", false, false),
            Triple("attack", true, false),
            Triple("attack", false, false),
            Triple("inspect", true, false),
            Triple("inspect", false, false),
            Triple("interact", true, true),
            Triple("interact", false, true),
            Triple("attack", true, true),
            Triple("attack", false, true),
            Triple("inspect", true, true),
            Triple("inspect", false, true),
        )

    for ((methodName, hasAction, nullAction) in testCases) {
      var executed = false
      val mockAction = if (nullAction) { _: GameState -> executed = true } else null

      val testState =
          when (methodName) {
            "interact" ->
                EntityState(
                    "testState",
                    null,
                    null,
                    mockAction,
                    null,
                    EntityState.Capabilities(false, false, hasAction, false),
                )
            "attack" ->
                EntityState(
                    "testState",
                    mockAction,
                    null,
                    null,
                    null,
                    EntityState.Capabilities(hasAction, false, false, false),
                )
            "inspect" ->
                EntityState(
                    "testState",
                    null,
                    mockAction,
                    null,
                    null,
                    EntityState.Capabilities(false, hasAction, false, false),
                )
            else -> throw IllegalArgumentException("Unknown method: $methodName")
          }

      // Mock language response for getText calls inside actions
      // Since we mocked DataLoader returning {}, language will return <id/key>

      if (!hasAction) {
        assertFailsWith<IllegalStateException> {
          when (methodName) {
            "interact" -> testState.interact(proj.state)
            "attack" -> testState.attack(proj.state)
            "inspect" -> testState.inspect(proj.state)
          }
        }
      } else {
        when (methodName) {
          "interact" -> testState.interact(proj.state)
          "attack" -> testState.attack(proj.state)
          "inspect" -> testState.inspect(proj.state)
        }
        assertEquals(nullAction, executed)
      }
    }
  }

  // -- Name Tests -- //
  @Test
  fun nameMethodTest() {
    val state = EntityState("intro_squirrel")
    // NOTE: We cannot easily verify language string output without real language data.
    // But we check that it calls language.string
    val name = state.name(proj.language)
    assertEquals("<intro_squirrel/name>", name)
  }

  // -- Introduce Tests -- //
  @Test
  fun introduceMethodTest() {
    val state = EntityState("intro_squirrel")
    state.introduce(proj.state)
    val message = proj.state.message
    assertEquals("<intro_squirrel/introduce>", message)
  }

  // -- Equals Tests -- //
  @Test
  fun equalsMethodTest() {
    val state1 = EntityState("testState")
    val state2 = EntityState("testState")
    val state3 = EntityState("otherState")

    assertTrue(state1.equals(state2))
    assertTrue(state1.equals(state1))
    assertFalse(state1.equals(state3))
  }

  // -- id Tests -- //
  @Test
  fun idMethodTest() {
    val state = EntityState("testState")
    assertEquals("testState", state.id)
  }

  // -- Take Input Tests -- //
  @Test
  fun takeInputActionExistsTest() {
    var executed = false
    val inputAction = Actions.takeInput(listOf("cd" to { _: GameState -> executed = true }))

    val state = EntityState("computty_unblocked", null, null, null, inputAction)
    state.takeInput("cd", proj.state)

    assertTrue(executed)
  }

  @Test
  fun trashCanStartsWithHummus() {
    assertEquals(
        Floor.StoreyI.rooms.first().entities.first { it.id == "trash_can" }.state().id,
        "trash_can",
    )
  }
}
