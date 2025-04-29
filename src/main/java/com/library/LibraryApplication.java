package com.library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LibraryApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
<<<<<<< HEAD
        FXMLLoader fxmlLoader = new FXMLLoader(LibraryApplication.class.getResource("/View/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Library 11 Points");
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                .getResource("/Image/ApplicationLogo.jpg")).toExternalForm()));
=======
        FXMLLoader fxmlLoader = new FXMLLoader(LibraryApplication.class.getResource("Views/LoginAndRegister.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 960, 640);
        stage.setTitle("Library 11 Points");
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                .getResource("Image/ApplicationLogo.jpg")).toExternalForm()));
>>>>>>> 4dfe4beec7ecdba9f655517bed17d8eff7b08fa1
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {launch();}



}