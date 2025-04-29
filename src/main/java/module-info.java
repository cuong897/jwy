module library {
    requires java.sql;
    requires mysql.connector.j;
<<<<<<< HEAD
    requires javafx.fxml;
    requires javafx.controls;

    opens com.library.Controller to javafx.fxml;
=======
    requires javafx.graphics;
    requires javafx.fxml;

    opens com.library to javafx.fxml;
>>>>>>> 4dfe4beec7ecdba9f655517bed17d8eff7b08fa1
    exports com.library;
}