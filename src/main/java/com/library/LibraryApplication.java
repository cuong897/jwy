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
        FXMLLoader fxmlLoader = new FXMLLoader(LibraryApplication.class.getResource("/com/library/View/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 749, 555);
        stage.setTitle("Library");
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                .getResource("/Image/ApplicationLogo.jpg")).toExternalForm()));
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {launch();}



}