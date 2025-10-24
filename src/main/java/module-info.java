module org.virtuoso.escape {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires java.sql;
	requires freetts;


    opens org.virtuoso.escape.model to javafx.fxml;
    opens org.virtuoso.escape.gui to javafx.fxml;
    opens org.virtuoso.escape.terminal to javafx.fxml;
    exports org.virtuoso.escape.terminal;
    exports org.virtuoso.escape.gui;
}