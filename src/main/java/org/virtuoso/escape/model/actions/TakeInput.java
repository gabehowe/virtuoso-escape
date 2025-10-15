package org.virtuoso.escape.model.actions;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class TakeInput implements Action {
    String input;
    private final Map<String, Action> cases;
    private final Action default_;

    public TakeInput(String input, Map<String, Action> cases, Action default_) {
        this.input = input;
        this.cases = cases;
        this.default_ = default_;
    }

    public TakeInput(String input, Map<String, Action> cases) {
        this(input, cases, null);
    }


    @Override
    public void execute() {
        for (Map.Entry<String, Action> tuple : cases.entrySet()) {
            if (!input.strip().toLowerCase().equals(tuple.getKey())) continue;
            tuple.getValue().execute();
            return;
        }
        this.default_.execute();
    }

    public Action withInput(String input) {
        this.input = input;
        return this;
    }
}
