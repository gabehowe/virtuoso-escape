package org.virtuoso.escape.model.action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

/**
 * Perform actions based on input-action mappings.
 *
 * @author gabri
 */
public class TakeInput implements Action {
    private final Map<String, Action> cases;
    private final Action default_;
    String input;

    /**
     * Create an action with a default input.
     *
     * @param input    A default input to try on execution.
     * @param cases    The input-action mapping.
     * @param default_ The default action to run if no other input matches.
     */
    public TakeInput(String input, SequencedMap<String, Action> cases, Action default_) {
        this.input = input;
        this.cases = cases;
        this.default_ = default_;
    }

    /**
     * Create an action without a default action.
     *
     * @param input A default input to try on execution.
     * @param cases The input-action mapping.
     */
    public TakeInput(String input, SequencedMap<String, Action> cases) {
        this(input, cases, null);
    }

    /**
     * Construct from cases.
     *
     * @param args The cases to use.
     */
    public TakeInput(Object... args) {
        this("", TakeInput.makeCases(args));
    }

    /**
     * Helper function for creating input-action mappings
     *
     * @param args Sequential pairs of input: Action
     * @return a SequencedMap for usage in the TakeInput constructor.
     */
    public static SequencedMap<String, Action> makeCases(Object... args) {
        assert args.length % 2 == 0 : "TakeInput::makeCases must be called with an even number of arguments!";
        SequencedMap<String, Action> map = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put((String) args[i], (Action) args[i + 1]);
        }
        return map;
    }

    /**
     * Run the current input against the mapping.
     */
    @Override
    public void execute() {
        for (Map.Entry<String, Action> tuple : cases.entrySet()) {
            if (!input.strip().toLowerCase().matches(tuple.getKey())) continue;
            tuple.getValue().execute();
            return;
        }
        if (this.default_ != null) this.default_.execute();
    }

    /**
     * Create this Action with a new input.
     *
     * @param input the input to test on.
     * @return this, but with the input.
     */
    public Action withInput(String input) {
        this.input = input;
        return this;
    }
}
