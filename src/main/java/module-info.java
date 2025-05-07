module library {
    requires java.sql;
    requires mysql.connector.j;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;

    opens com.library to javafx.fxml;
    exports com.library;
}