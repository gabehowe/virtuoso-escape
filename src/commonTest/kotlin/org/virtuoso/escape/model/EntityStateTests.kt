package org.virtuoso.escape.model

import org.junit.jupiter.api.Assertions.*
import org.virtuoso.escape.model.action.Severity

/** @author Andrew
 */
class EntityStateTests {
    var proj: GameProjection? = null

    @BeforeEach
    fun pre() {
        proj = GameProjection()
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath()
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath()
        assertTrue(proj!!.login("dummy", "dummy"))
    }

    // -- Constructor Tests -- //
    @DisplayName("Should construct and actionless entity state from an id")
    @Test
    fun constructActionlessStateTest() {
        val state = EntityState("testState")

        assertEquals("testState", state.id(), "ID should be set correctly")
        assertTrue(state.capabilities().attack, "Attack capability should be true")
        assertTrue(state.capabilities().inspect, "Inspect capability should be true")
        assertTrue(state.capabilities().interact, "Interact capability should be true")
        assertFalse(state.capabilities().input, "Input capability should be false")

        assertNotNull(state.attackAction(), "Attack action should not be null")
        assertNotNull(state.inspectAction(), "Inspect action should not be null")
        assertNotNull(state.interactAction(), "Interact action should not be null")
        assertNull(state.inputAction(), "Input action should be null")
    }

    @DisplayName("Should throw an error when constructed with a null id")
    @Test
    fun constructStateNullIdTest() {
        assertThrows(NullPointerException::class.java, {
            EntityState(null)
        })
    }

    @DisplayName("Should construct an entity state with 5 parameters")
    @Test
    fun constructState5Param() {
        val attack: Action = AddPenalty(Severity.HIGH)
        val inspect: Action = SetFloor(2)
        val interact: Action = SetMessage("TestMessage")
        val input: TakeInput = TakeInput()

        val state = EntityState("testState", attack, inspect, interact, input)

        assertEquals("testState", state.id())
        assertSame(attack, state.attackAction())
        assertSame(inspect, state.inspectAction())
        assertSame(interact, state.interactAction())
        assertSame(input, state.inputAction())

        assertTrue(state.capabilities().attack)
        assertTrue(state.capabilities().inspect)
        assertTrue(state.capabilities().interact)
        assertTrue(state.capabilities().input)
    }

    @DisplayName("Should construct an entity state with some null parameters")
    @Test
    fun constructStateSomeNullParam() {
        val attack: Action = AddPenalty(Severity.HIGH)
        val interact: Action = SetMessage("TestMessage")

        val state = EntityState("testState", attack, null, interact, null)

        assertEquals("testState", state.id())
        assertSame(attack, state.attackAction())
        assertNull(state.inspectAction())
        assertSame(interact, state.interactAction())
        assertNull(state.inputAction())

        assertTrue(state.capabilities().attack)
        assertFalse(state.capabilities().inspect)
        assertTrue(state.capabilities().interact)
        assertFalse(state.capabilities().input)
    }

    @DisplayName("Should construct an entity state with all null parameters")
    @Test
    fun constructStateAllNullParam() {
        val state = EntityState("testState", null, null, null, null)

        assertEquals("testState", state.id())
        assertNull(state.attackAction())
        assertNull(state.inspectAction())
        assertNull(state.interactAction())
        assertNull(state.inputAction())

        assertFalse(state.capabilities().attack)
        assertFalse(state.capabilities().inspect)
        assertFalse(state.capabilities().interact)
        assertFalse(state.capabilities().input)
    }

    @DisplayName("Should construct an entity state using the 6-parameter canonical constructor")
    @Test
    fun constructState6Param() {
        val attack: Action = AddPenalty(Severity.HIGH)
        val inspect: Action = SetFloor(2)
        val interact: Action = SetMessage("InteractMessage")
        val input: TakeInput = TakeInput()
        val capabilities = EntityState.Capabilities(true, false, false, true)

        val state = EntityState("testState", attack, inspect, interact, input, capabilities)

        assertEquals("testState", state.id())
        assertSame(attack, state.attackAction())
        assertSame(inspect, state.inspectAction())
        assertSame(interact, state.interactAction())
        assertSame(input, state.inputAction())
        assertSame(capabilities, state.capabilities())

        assertTrue(state.capabilities().attack)
        assertFalse(state.capabilities().inspect)
        assertFalse(state.capabilities().interact)
        assertTrue(state.capabilities().input)
    }

    // -- Get Text Tests -- //
    @ParameterizedTest(name = "Will get text from entity state: Test {index} => key: {0}, expected: {1}")
    @MethodSource
    @Throws(Exception::class)
    fun getTextTest(key: String?, expected: String?) {
        val state = EntityState("welcome")

        val result = getText(state, key)

        assertEquals(expected, result)
    }

    // -- Action Tests -- //
    @ParameterizedTest(name = "Will call actions through interact, attack and inspect : Test {index} => method: {0}, key: {1}, hasAction: {2}, nullAction: {3}")
    @MethodSource
    @Throws(Exception::class)
    fun actionCallTest(methodName: String, key: String?, hasAction: Boolean, nullAction: Boolean) {
        val mockAction: Action?
        val executed: AtomicBoolean = AtomicBoolean(false)
        mockAction = if (nullAction) Action? { executed.set(true) } else null

        val testState =
            when (methodName) {
                "interact" -> EntityState(
                    "testState",
                    null,
                    null,
                    mockAction,
                    null,
                    EntityState.Capabilities(false, false, hasAction, false)
                )

                "attack" -> EntityState(
                    "testState",
                    mockAction,
                    null,
                    null,
                    null,
                    EntityState.Capabilities(hasAction, false, false, false)
                )

                "inspect" -> EntityState(
                    "testState",
                    null,
                    mockAction,
                    null,
                    null,
                    EntityState.Capabilities(false, hasAction, false, false)
                )

                else -> throw IllegalArgumentException("Unknown method: " + methodName)
            }

        GameState.instance().setCurrentMessage(null)

        val method: Method = EntityState::class.java.getMethod(methodName)
        if (!hasAction) {
            assertThrows(Exception::class.java, { method.invoke(testState) })
            return
        } else method.invoke(testState)

        val expectedMessage = getText(testState, key)

        assertEquals(expectedMessage, GameState.instance().currentMessage().get())
        assertEquals(nullAction, executed.get())
    }

    // -- Name Tests -- //
    @DisplayName("Should return the entity's name")
    @Test
    @Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class)
    fun nameMethodTest() {
        val state = EntityState("intro_squirrel")
        val name = state.name()
        val expectedName = getText(state, "name")
        assertEquals(expectedName, name)
    }

    @DisplayName("Should return fallback for name if missing")
    @Test
    fun nameMethodMissingTest() {
        val state = EntityState("missingName")
        val name = state.name()
        assertEquals("<missingName/name>", name)
    }

    // -- Introduce Tests -- //
    @DisplayName("Should display the introductory message")
    @Test
    @Throws(Exception::class)
    fun introduceMethodTest() {
        val state = EntityState("intro_squirrel")
        state.introduce()
        val expectedMessage = getText(state, "introduce")
        assertEquals(expectedMessage, GameState.instance().currentMessage().get())
    }

    @DisplayName("Should handle introduce with missing text gracefully")
    @Test
    fun introduceMethodMissingTest() {
        val state = EntityState("missingIntroduce")
        state.introduce()
        val message: String? = GameState.instance().currentMessage().get()
        assertEquals("<missingIntroduce/introduce>", message)
    }

    // -- Equals Tests -- //
    @DisplayName("Should correctly compare equality of two entity states by id")
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
    @DisplayName("Should return the correct id")
    @Test
    fun idMethodTest() {
        val state = EntityState("testState")
        val result: String? = state.id()
        assertEquals("testState", result)
    }

    // -- Take Input Tests -- //
    @DisplayName("Should handle takeInput correctly when input action exists")
    @Test
    fun takeInputActionExistsTest() {
        val executed: AtomicBoolean = AtomicBoolean(false)
        val inputAction: TakeInput = TakeInput("", TakeInput.makeCases("cd", Action { executed.set(true) } as Action))

        val state = EntityState("computty_unblocked", null, null, null, inputAction)
        state.takeInput("cd")

        assertTrue(executed.get())
        assertTrue(GameState.instance().currentMessage().get()!!.equals(""))
    }

    @DisplayName("Should handle takeInput correctly when input action is null")
    @Test
    fun takeInputActionNullTest() {
        val state = EntityState(
            "testState", null, null, null, null, EntityState.Capabilities(false, false, false, true)
        )
        state.takeInput("unknownInput")
        val message: String = GameState.instance().currentMessage().get()
        assertTrue(message.contains("I couldn't understand 'unknownInput'"))
    }

    @DisplayName("Should handle takeInput when input action is null and key exists")
    @Test
    fun takeInputNullActionKeyExistsTest() {
        val state = EntityState(
            "computty_unblocked", null, null, null, null, EntityState.Capabilities(false, false, false, true)
        )
        state.takeInput("cd")
        val message: String? = GameState.instance().currentMessage().get()
        assertEquals(GameInfo.instance().string("computty_unblocked", "input_cd"), message)
    }

    companion object {
        private val textTest: Stream<Arguments?>
            get() = Stream.of(
                Arguments.of("welcome", "Welcome to Virtuoso Escape!"),
                Arguments.of("missingText", "<welcome/missingText>"),
                Arguments.of(null, "<welcome/null>")
            )

        @Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class)
        private fun getText(state: EntityState?, key: String?): String? {
            val getTextMethod: Method = EntityState::class.java.getDeclaredMethod("getText", String::class.java)
            getTextMethod.setAccessible(true)
            return getTextMethod.invoke(state, key) as String?
        }

        private fun actionCallTest(): Stream<Arguments?> {
            return Stream.of(
                Arguments.of("interact", "interact", true, false),
                Arguments.of("interact", "interact", false, false),
                Arguments.of("attack", "attack", true, false),
                Arguments.of("attack", "attack", false, false),
                Arguments.of("inspect", "inspect", true, false),
                Arguments.of("inspect", "inspect", false, false),
                Arguments.of("interact", "interact", true, true),
                Arguments.of("interact", "interact", false, true),
                Arguments.of("attack", "attack", true, true),
                Arguments.of("attack", "attack", false, true),
                Arguments.of("inspect", "inspect", true, true),
                Arguments.of("inspect", "inspect", false, true)
            )
        }
    }
}
