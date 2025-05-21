package com.example.splayerbeta;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("mediaPlayer.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("SPlayer Media Player");

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/iconMEDIA.png")));
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }

        // Set the window to maximiz (full screen window)
        stage.setMaximized(true);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lwtheme", "dark");
        System.out.println("Running main()");
        launch(args);
    }
}