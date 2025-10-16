package org.virtuoso.escape.model.actions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.Consumer;

public class TakeInput implements Action {
    String input;
    private final Map<String, Action> cases;
    private final Action default_;

    public TakeInput(String input, SequencedMap<String, Action> cases, Action default_) {
        this.input = input;
        this.cases = cases;
        this.default_ = default_;
    }

    public TakeInput(String input, SequencedMap<String, Action> cases) {
        this(input, cases, null);
    }

    public static SequencedMap<String, Action> makeCases(Object... args){
        assert args.length % 2 == 0: "TakeInput::makeCases must be called with an even number of arguments!";
        SequencedMap<String, Action> map = new LinkedHashMap<>();
        for (int i = 0; i< args.length; i += 2){
            map.put((String) args[i], (Action) args[i+1]);
        }
        return map;
    };


    @Override
    public void execute() {
        for (Map.Entry<String, Action> tuple : cases.entrySet()) {
            if (!input.strip().toLowerCase().matches(tuple.getKey())) continue;
            tuple.getValue().execute();
            return;
        }
        if (this.default_ != null) this.default_.execute();
    }

    public Action withInput(String input) {
        this.input = input;
        return this;
    }
}
