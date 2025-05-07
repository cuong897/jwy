module library {
    requires java.sql;
    requires mysql.connector.j;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;
    requires com.google.api.services.books;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires javafx.media;

    opens com.library to javafx.fxml;
    exports com.library;
}