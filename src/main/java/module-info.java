module library {
    requires java.sql;
    requires mysql.connector.j;
    requires javafx.fxml;
    requires javafx.controls;

    opens com.library.Controller to javafx.fxml;
    exports com.library;
}