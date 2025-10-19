module org.virtuoso.escape.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires java.sql;


    opens org.virtuoso.escape.model to javafx.fxml;
    opens org.virtuoso.escape.gui to javafx.fxml;
    exports org.virtuoso.escape.gui;
    opens org.virtuoso.escape.terminal to javafx.fxml;
}