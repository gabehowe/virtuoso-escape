package org.virtuoso.escape.gui;

import javafx.css.CssMetaData;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.virtuoso.escape.terminal.FunString;
import org.virtuoso.escape.terminal.TerminalDriver;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class KeyboardProcessor {
    public static void addKeyboardBindings(Node root) {
        Map<String, Labeled> keyMap = new LinkedHashMap<>();
        Set<Node> nodes = root.lookupAll(".logical-button");
        for (Node node : nodes) {
            if (node instanceof Labeled elem) {
                String key = null;
                String sourceKey = elem.getText();
                int index = 0;
                for (var c: sourceKey.toCharArray()){
                    key = String.valueOf(c);
                    if (key.matches("^\\w+$") && !keyMap.containsKey(key.toLowerCase())) break;
                    if (index == sourceKey.length()-1) {
                        index = -1;
                        break;
                    }
                    index++;
                }
                if (!sourceKey.startsWith("[") &&index != -1){
                    var leftDelimiter = new Label("[");
                    var rightDelimiter = new Label("]");
                    var preKey = new Label(sourceKey.substring(0, index));
                    var postKey = new Label(sourceKey.substring(index+1));
                    var keyText = new Label(key);
                    keyText.setStyle("-fx-underline:true;-fx-font-weight: 700;-fx-fill: white");

                    var flow = new TextFlow(leftDelimiter, preKey, keyText, postKey, rightDelimiter);
                    flow.getStyleClass().add("logical-button");
                    elem.setGraphic(flow);
                    elem.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
                keyMap.put(key.toLowerCase(), elem);
            }
        }
        root.setOnKeyPressed(e -> {
            for (var entry: keyMap.entrySet()) {
                if (Objects.equals(e.getText(), entry.getKey())) {
                    entry.getValue().getOnMouseClicked().handle(null);
                }
            }
        });
    }
}
