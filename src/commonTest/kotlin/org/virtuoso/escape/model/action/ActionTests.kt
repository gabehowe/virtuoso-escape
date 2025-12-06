package org.virtuoso.escape.model.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.virtuoso.escape.model.Entity
import org.virtuoso.escape.model.EntityState
import org.virtuoso.escape.model.Item

/**
 * @author gabri
 * @author Andrew
 */
class ActionTests {
    var proj: GameProjection? = null

    @BeforeEach
    fun pre() {
        proj = GameProjection()

        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath()
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath()
        assertTrue(proj.login("dummy", "dummy"))
    }

    @AfterEach
    fun post() {
        //        proj.logout();
    }

    @DisplayName("Should apply penalty based on difficulty and severity")
    @ParameterizedTest
    @MethodSource
    fun addPenaltyTest(difficulty: Difficulty, severity: Severity?, expectedDifference: Int) {
        val pre: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameState.instance().penalty()
        val expectedPenalty: Int = pre - expectedDifference
        proj.setDifficulty(difficulty)
        AddPenalty(severity).execute()
        assertEquals(GameState.instance().penalty(), expectedPenalty)
    }

    // @formatter:on
    @DisplayName("Should execute actions in sequence and use the last action's result")
    @Test
    fun chainTest() {
        Chain(SetMessage("initial"), SetMessage("expected")).execute()
        assertEquals("expected", GameState.instance().currentMessage().get())
    }

    @DisplayName("Should execute correct action branch based on condition")
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun conditionalTest(value: Boolean) {
        Conditional({ value }, SetMessage(String.valueOf(true)), SetMessage(String.valueOf(false)))
            .execute()
        assertEquals(
            String.valueOf(value), GameState.instance().currentMessage().get()
        )
    }

    @DisplayName("Should handle null action branch when condition is false")
    @Test
    fun nullConditionalTest() {
        Conditional({ false }, SetMessage(String.valueOf(true)), null).execute()
        assertFalse(GameState.instance().currentMessage().isPresent())
    }

    @DisplayName("Should execute default action without errors")
    @Test
    fun defaultTest() {
        Default().execute()
        // Expect no error?
    }

    @DisplayName("Should give do nothing if the user is given a null item")
    @Test
    fun giveNullItemTest() {
        GiveItem(null).execute()
        assertFalse(GameState.instance().currentItems().contains(null))
    }

    @DisplayName("Should add an item to currentItems")
    @ParameterizedTest
    @EnumSource(Item::class) // Runs once for every possible item, kind of overkill but cool nonetheless.
    fun giveAllItemsTest(item: Item?) {
        assertNotNull(item)
        GiveItem(item).execute()
        assertTrue(GameState.instance().currentItems().contains(item))
    }

    @DisplayName("Should not add the same item twice")
    @Test
    fun giveDoubleItemTest() {
        GiveItem(Item.keys).execute()
        GiveItem(Item.keys).execute()
        assertTrue(Collections.frequency(GameState.instance().currentItems(), Item.keys) === 1)
    }

    @DisplayName("Should add multiple different items")
    @Test
    fun giveDifferentItemsTest() {
        GiveItem(Item.left_bread).execute()
        GiveItem(Item.sealed_clean_food_safe_hummus).execute()
        assertTrue(GameState.instance().currentItems().size() === 2)
    }

    @ParameterizedTest(name = "Should change entity state to all possible options for each entity. {index}")
    @MethodSource
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun swapEntityStateTest(entityId: String?) {
        SetFloor(TEST_FLOOR).execute()
        val entity: Entity = GameState.instance().currentFloor().rooms.stream()
            .flatMap({ room -> room.entities.stream() })
            .filter({ e -> Objects.equals(e.id(), entityId) })
            .findFirst()
            .orElseThrow({ AssertionError("Entity with id " + entityId + " not found in GameState!") })

        val stateMap = getStateMap(entity)
        // Tests setting the state for each entity on a floor
        for (stateString in stateMap.keySet()) {
            val currentEntity: Entity = GameState.instance().currentFloor().rooms.stream()
                .flatMap({ room -> room.entities.stream() })
                .filter({ e -> Objects.equals(e.id(), entityId) })
                .findFirst()
                .orElseThrow({ AssertionError("Entity with id " + entityId + " not found during loop!") })
            val originalState: String? = currentEntity.state().id()
            SwapEntities(entity.id(), stateString).execute()
            val updated: Entity = GameState.instance().currentFloor().rooms.stream()
                .flatMap({ room -> room.entities.stream() })
                .filter({ e -> Objects.equals(e.id(), entity.id()) })
                .findFirst()
                .orElseThrow(
                    { AssertionError("Entity with id " + entity.id() + " not found in GameState!") })
            assertEquals(
                stateMap.get(stateString),
                updated.state(),
                ("\nEntity id: " + entity.id()
                        + "\nFailed for state: " + stateString
                        + "\nOriginal state was: " + originalState
                        + "\nCurrent state is: " + updated.state().id() + "\n")
            )
        }
    }

    @DisplayName("Should throw errors on null or missing entity or entity state arguments.")
    @ParameterizedTest
    @MethodSource
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun invalidEntityStateSwapTest(entity: String?, entityState: String?) {
        SetFloor(TEST_FLOOR).execute()
        assertThrows(
            IllegalArgumentException::class.java,
            { SwapEntities(entity, entityState).execute() },
            "Expected SwapEntities to throw an exception, but it did not!"
        )
    }

    @DisplayName("Should execute action based on matching input case")
    @ParameterizedTest
    @ValueSource(strings = ["one", "two", "three"])
    fun testTakeInput(input: String?) {
        TakeInput("one", SetMessage("one"), "two", SetMessage("two"), "three", SetMessage("three"))
            .withInput(input)
            .execute()
        val msg: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameState.instance().currentMessage()
        assertTrue(msg.isPresent())
        assertEquals(input, msg.get())
    }

    @DisplayName("Should execute default action when input does not match any case")
    @Test
    fun testDefaultTakeInput() {
        TakeInput("", LinkedHashMap(), SetMessage("default"))
            .withInput(null)
            .execute()
        val msg: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameState.instance().currentMessage()
        assertTrue(msg.isPresent())
        assertEquals("default", msg.get())
    }

    @DisplayName("Should handle null default action when input does not match any case")
    @Test
    fun testNullDefaultTakeInput() {
        TakeInput("", LinkedHashMap(), null).withInput(null).execute()
        val msg: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = GameState.instance().currentMessage()
        assertFalse(msg.isPresent())
    }

    @DisplayName("Should throw error for non-even TakeInput arguments")
    @Test
    fun testNonEvenTakeInputMakeCasesArgumentLength() {
        try {
            TakeInput("one").execute()
        } catch (e: AssertionError) {
            return  // Passed!
        } catch (e: IndexOutOfBoundsException) {
            return
        }
        fail("Error should have been thrown!")
    }

    companion object {
        @BeforeAll
        @Throws(Exception::class)
        fun setup() {
            Util.rebuildSingleton(AccountManager::class.java)
        }

        private fun addPenaltyTest(): Stream<Arguments?> {
            return Stream.of(
                Arguments.of(Difficulty.VIRTUOSIC, Severity.HIGH, 5 * 3 * 3),
                Arguments.of(Difficulty.VIRTUOSIC, Severity.MEDIUM, 5 * 3 * 2),
                Arguments.of(Difficulty.VIRTUOSIC, Severity.LOW, 5 * 3 * 1),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.HIGH, 5 * 2 * 3),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.MEDIUM, 5 * 2 * 2),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.LOW, 5 * 2 * 1),
                Arguments.of(Difficulty.TRIVIAL, Severity.HIGH, 5 * 1 * 3),
                Arguments.of(Difficulty.TRIVIAL, Severity.MEDIUM, 5 * 1 * 2),
                Arguments.of(Difficulty.TRIVIAL, Severity.LOW, 5 * 1 * 1)
            )
        }

        private const val TEST_FLOOR = 1

        @Throws(NoSuchFieldException::class, IllegalAccessException::class)
        private fun swapEntityStateTest(): Stream<String?> {
            SetFloor(TEST_FLOOR).execute()

            val entities: List<Entity?> = GameState.instance().currentFloor().rooms.stream()
                .map(Room::entities)
                .flatMap(List::stream)
                .toList()

            check(!entities.isEmpty()) { "No entities found in the current floor!" }

            multiStateEntity(entities)

            return entities.stream().map(Entity::id)
        }

        @Throws(NoSuchFieldException::class, IllegalAccessException::class)
        private fun getStateMap(entity: Entity?): Map<String?, EntityState?> {
            val stateField: Field = Entity::class.java.getDeclaredField("states")
            stateField.setAccessible(true)
            return stateField.get(entity) as Map<String?, EntityState?>
        }

        private fun multiStateEntity(entities: List<Entity?>): Entity {
            return entities.stream()
                .filter({ entity ->
                    try {
                        val stateMap = getStateMap(entity)
                        return@filter stateMap.size() > 1
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                })
                .findFirst()
                .orElseThrow({ IllegalStateException("No entities with multiple states found!") })
        }

        @Throws(NoSuchFieldException::class, IllegalAccessException::class)
        private fun invalidEntityStateSwapTest(): Stream<Arguments?> {
            SetFloor(TEST_FLOOR).execute()
            val testEntity: Entity = GameState.instance().currentFloor().rooms.stream()
                .map(Room::entities)
                .flatMap(List::stream)
                .findFirst()
                .orElseThrow({ IllegalStateException("No entities found in the current floor!") })
            return Stream.of(
                Arguments.of(null, null),
                Arguments.of("fakeEntity", null),
                Arguments.of(null, "fakeState"),
                Arguments.of("fakeEntity", "fakeState"),
                Arguments.of(testEntity.id(), null),
                Arguments.of(testEntity.id(), "fakeState")
            )
        }
    }
}
