package org.virtuoso.escape.model.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.Entity;
import org.virtuoso.escape.model.EntityState;
import org.virtuoso.escape.model.Floor;
import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.Item;
import org.virtuoso.escape.model.Room;
import org.virtuoso.escape.model.data.DataLoader;

/**
 * @author gabri
 * @author Andrew
 */
public class ActionTests {
    GameProjection proj;

    @BeforeEach
    public void pre() {
        proj = new GameProjection();
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath();
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath();
        assertTrue(proj.login("dummy", "dummy"));
    }

    @AfterEach
    public void post() {
        //        proj.logout();
    }

    @DisplayName("Should apply penalty based on difficulty and severity")
    @ParameterizedTest
    @MethodSource
    public void addPenaltyTest(Difficulty difficulty, Severity severity, int expectedDifference) {
        var pre = GameState.instance().penalty();
        var expectedPenalty = pre - expectedDifference;
        proj.setDifficulty(difficulty);
        new AddPenalty(severity).execute();
        assertEquals(GameState.instance().penalty(), expectedPenalty);
    }

    private static Stream<Arguments> addPenaltyTest() {
        return Stream.of(
                Arguments.of(Difficulty.VIRTUOSIC, Severity.HIGH, 5 * 3 * 3),
                Arguments.of(Difficulty.VIRTUOSIC, Severity.MEDIUM, 5 * 3 * 2),
                Arguments.of(Difficulty.VIRTUOSIC, Severity.LOW, 5 * 3 * 1),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.HIGH, 5 * 2 * 3),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.MEDIUM, 5 * 2 * 2),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.LOW, 5 * 2 * 1),
                Arguments.of(Difficulty.TRIVIAL, Severity.HIGH, 5 * 1 * 3),
                Arguments.of(Difficulty.TRIVIAL, Severity.MEDIUM, 5 * 1 * 2),
                Arguments.of(Difficulty.TRIVIAL, Severity.LOW, 5 * 1 * 1));
    }

    // @formatter:on

    @DisplayName("Should execute actions in sequence and use the last action's result")
    @Test
    public void chainTest() {
        new Chain(new SetMessage("initial"), new SetMessage("expected")).execute();
        assertEquals("expected", GameState.instance().currentMessage().get());
    }

    @DisplayName("Should execute correct action branch based on condition")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void conditionalTest(boolean value) {
        new Conditional(() -> value, new SetMessage(String.valueOf(true)), new SetMessage(String.valueOf(false)))
                .execute();
        assertEquals(
                String.valueOf(value), GameState.instance().currentMessage().get());
    }

    @DisplayName("Should handle null action branch when condition is false")
    @Test
    public void nullConditionalTest() {
        new Conditional(() -> false, new SetMessage(String.valueOf(true)), null).execute();
        assertFalse(GameState.instance().currentMessage().isPresent());
    }

    @DisplayName("Should execute default action without errors")
    @Test
    public void defaultTest() {
        new Default().execute();
        // Expect no error?
    }

	@DisplayName("Should give do nothing if the user is given a null item")
	@Test
	public void giveNullItemTest() {
		new GiveItem(null).execute();
		assertFalse(GameState.instance().currentItems().contains(null));
	}

	@DisplayName("Should add an item to currentItems")
	@ParameterizedTest
	@EnumSource(Item.class)
	// Runs once for every possible item, kind of overkill but cool nonetheless.
	public void giveAllItemsTest(Item item){
		assertNotNull(item);
		new GiveItem(item).execute();
		assertTrue(GameState.instance().currentItems().contains(item));
	}

	@DisplayName("Should not add the same item twice")
	@Test
	public void giveDoubleItemTest() {
		new GiveItem(Item.keys).execute();
		new GiveItem(Item.keys).execute();
		assertTrue(Collections.frequency(GameState.instance().currentItems(), Item.keys) == 1);
	}

	@DisplayName("Should add multiple different items")
	@Test
	public void giveDifferentItemsTest() {
		new GiveItem(Item.left_bread).execute();
		new GiveItem(Item.sealed_clean_food_safe_hummus).execute();
		assertTrue(GameState.instance().currentItems().size() == 2);
	}

	private static int TEST_FLOOR = 1;
	@DisplayName("Should change entity state")
	@ParameterizedTest
	@MethodSource
	public void swapEntityStateTest(Entity entity) throws NoSuchFieldException, IllegalAccessException{
		new SetFloor(TEST_FLOOR).execute();
		Map<String, EntityState> stateMap = getStateMap(entity);
		// Tests setting the state for each entity on a floor
		for (String stateString: stateMap.keySet()) {
			String originalState = entity.state().id();
			new SwapEntities(entity.id(), stateString).execute();
			Entity updated = GameState.instance().currentFloor().rooms().stream().flatMap(room -> room.entities().stream())
				.filter(e -> Objects.equals(e.id(), entity.id())).findFirst()
				.orElseThrow(() -> new AssertionError("Entity with id " + entity.id() + " not found in GameState!"));
				assertEquals(
					stateMap.get(stateString),
					updated.state(),
					"\nEntity id: " + entity.id()
					+ "\nFailed for state: " + stateString
					+ "\nOriginal state was: " + originalState
					+ "\nCurrent state is: " + updated.state().id() + "\n"
				);
		}
	}

	private static Stream<Entity> swapEntityStateTest() throws NoSuchFieldException, IllegalAccessException {
		new SetFloor(TEST_FLOOR).execute();

		List<Entity> entities = GameState.instance().currentFloor().rooms().stream().map(Room::entities).flatMap(List::stream).toList();

		if (entities.isEmpty()) {
			throw new IllegalStateException("No entities found in the current floor!");
		}

		multiStateEntity(entities);
		
		return entities.stream();
	}

	private static Map<String, EntityState> getStateMap(Entity entity) throws NoSuchFieldException, IllegalAccessException {
		Field stateField = Entity.class.getDeclaredField("states");
		stateField.setAccessible(true);
		return (Map<String, EntityState>) stateField.get(entity);
	}

	private static Entity multiStateEntity(List<Entity> entities){
		return entities.stream().filter(entity -> {
			try {
				Map<String, EntityState> stateMap = getStateMap(entity);
				return stateMap.size() > 1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).findFirst().orElseThrow(() -> new IllegalStateException("No entities with multiple states found!"));
	}

	@DisplayName("Should throw errors on null or missing entity or entity state arguments.")
	@ParameterizedTest
	@MethodSource
	public void invalidEntityStateSwapTest(String entity, String entityState) throws NoSuchFieldException, IllegalAccessException {
		new SetFloor(TEST_FLOOR).execute();
		assertThrows(
			IllegalArgumentException.class,() -> new SwapEntities(entity, entityState).execute(),
			"Expected SwapEntities to throw an exception, but it did not!"
    	);
	}

	private static Stream<Arguments> invalidEntityStateSwapTest() throws NoSuchFieldException, IllegalAccessException {
		new SetFloor(TEST_FLOOR).execute();
		Entity testEntity = GameState.instance().currentFloor().rooms().stream().map(Room::entities).flatMap(List::stream).findFirst()
		.orElseThrow(() -> new IllegalStateException("No entities found in the current floor!"));
		return Stream.of(
			Arguments.of(null, null),
			Arguments.of("fakeEntity", null),
			Arguments.of(null, "fakeState"),
			Arguments.of("fakeEntity", "fakeState"),
			Arguments.of(testEntity.id(), null),
			Arguments.of(testEntity.id(), "fakeState")
		);
	}

    @DisplayName("Should execute action based on matching input case")
    @ParameterizedTest
    @ValueSource(strings = {"one", "two", "three"})
    public void testTakeInput(String input) {
        new TakeInput("one", new SetMessage("one"), "two", new SetMessage("two"), "three", new SetMessage("three"))
                .withInput(input)
                .execute();
        var msg = GameState.instance().currentMessage();
        assertTrue(msg.isPresent());
        assertEquals(input, msg.get());
    }

    @DisplayName("Should execute default action when input does not match any case")
    @Test
    public void testDefaultTakeInput() {
        new TakeInput("", new LinkedHashMap<>(), new SetMessage("default"))
                .withInput(null)
                .execute();
        var msg = GameState.instance().currentMessage();
        assertTrue(msg.isPresent());
        assertEquals("default", msg.get());
    }

    @DisplayName("Should handle null default action when input does not match any case")
    @Test
    public void testNullDefaultTakeInput() {
        new TakeInput("", new LinkedHashMap<>(), null).withInput(null).execute();
        var msg = GameState.instance().currentMessage();
        assertFalse(msg.isPresent());
    }

    @DisplayName("Should throw error for non-even TakeInput arguments")
    @Test
    public void testNonEvenTakeInputMakeCasesArgumentLength() {
        try {
            new TakeInput("one").execute();
        } catch (AssertionError | IndexOutOfBoundsException e) {
            return; // Passed!
        }
        fail("Error should have been thrown!");
    }
}
