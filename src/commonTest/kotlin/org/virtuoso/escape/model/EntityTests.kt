package org.virtuoso.escape.model

import org.junit.jupiter.api.Assertions.*

/** @author Andrew Heuer
 */
class EntityTests {
    var proj: GameProjection? = null

    @BeforeEach
    fun pre() {
        proj = GameProjection()
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath()
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath()
        assertTrue(proj!!.login("dummy", "dummy"))
        GameState.instance().setCurrentMessage(null)
    }

    // -- Constructor Tests -- //
    @DisplayName("Should construct an actionless entity with default state")
    @Test
    fun constructActionlessEntityTest() {
        val entity = Entity("testEntity")

        assertEquals("testEntity", entity.id())
        assertNotNull(entity.state())
        assertEquals("testEntity", entity.state().id())
    }

    @DisplayName("Should construct an entity with multiple states and default to matching id")
    @Test
    fun constructEntityMultipleStatesDefaultIdTest() {
        val state1 = EntityState("state1")
        val state2 = EntityState("state2")

        val entity = Entity("state1", state2, state1)

        assertEquals("state1", entity.id())
        assertEquals("state1", entity.state().id())
    }

    @DisplayName("Should construct an entity with multiple states and default to first state if id missing")
    @Test
    fun constructEntityMultipleStatesDefaultFirstStateTest() {
        val state1 = EntityState("state1")
        val state2 = EntityState("state2")

        val entity = Entity("missingId", state1, state2)

        assertEquals("missingId", entity.id())
        assertEquals("state1", entity.state().id())
    }

    @DisplayName("Should construct a one-state entity with actions")
    @Test
    fun constructEntityWithActionsTest() {
        val attackExecuted: AtomicBoolean = AtomicBoolean(false)
        val attack: Action = Action { attackExecuted.set(true) }
        val inspect: Action = SetMessage("inspect")
        val interact: Action = SetMessage("interact")
        val input: TakeInput = TakeInput("", TakeInput.makeCases("cmd", Action {} as Action))

        val entity: Entity = Entity("actionEntity", attack, inspect, interact, input)

        assertEquals("actionEntity", entity.id())
        assertNotNull(entity.state())
        assertNotNull(entity.state().attackAction())
        assertNotNull(entity.state().inspectAction())
        assertNotNull(entity.state().interactAction())
        assertNotNull(entity.state().inputAction())

        entity.state().attackAction().execute()
        assertTrue(attackExecuted.get())
    }

    @DisplayName("Constructing an entity with a null id should throw NullPointerException")
    @Test
    fun constructWithNullIdTest() {
        assertThrows(NullPointerException::class.java, {
            Entity(null)
        })
    }

    // -- State Swap Tests -- //
    @DisplayName("Should swap states correctly for multiple scenarios")
    @ParameterizedTest(name = "Swap from {0} to {1}")
    @MethodSource
    fun swapStateTests(initialState: String?, newState: String, expected: String?) {
        val state1 = EntityState("state1")
        val state2 = EntityState("state2")
        val entity = Entity("state1", state1, state2)

        entity.swapState(newState)
        assertEquals(expected, entity.state().id())
    }

    @DisplayName("Swapping to a nonexistent state should throw an exception")
    @Test
    fun swapNonExistentState() {
        val state1 = EntityState("state1")
        val entity = Entity("myEntity", state1)

        assertThrows(RuntimeException::class.java, {
            entity.swapState("nonexistent")
        })
    }

    // -- Write Method Tests -- //
    @DisplayName("Should return id and current state in write()")
    @Test
    fun writeMethodTest() {
        val state1 = EntityState("state1")
        val state2 = EntityState("state2")

        val entity = Entity("state1", state1, state2)
        entity.swapState("state2")

        val result: Array<String?> = entity.write()
        assertEquals(2, result.size)
        assertEquals("state1", result[0])
        assertEquals("state2", result[1])
    }

    // -- id Tests -- //
    @DisplayName("Should return the correct id")
    @Test
    fun idMethodTest() {
        val entity = Entity("testState")
        val result: String? = entity.id()
        assertEquals("testState", result)
    }

    // -- State Retrieval Tests -- //
    @DisplayName("stateW should return the current state")
    @Test
    fun stateRetrievalTest() {
        val state = EntityState("myState")
        val entity = Entity("myEntity", state)

        assertEquals("myState", entity.state().id())
    }

    companion object {
        private fun swapStateTests(): Stream<Arguments?> {
            return Stream.of(Arguments.of("state1", "state2", "state2"), Arguments.of("state1", "state1", "state1"))
        }
    }
}
