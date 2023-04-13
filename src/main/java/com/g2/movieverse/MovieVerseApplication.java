package com.g2.movieverse;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class MovieVerseApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MovieVerseApplication.class.getResource("login.fxml"));
        Scene loginScene = new Scene(fxmlLoader.load(), 500, 300);
        stage.setTitle("MovieVerse!");
        stage.setScene(loginScene);
        stage.show();
        stage.setOnCloseRequest(we -> ConnectionManager.closeConnection());

        // Establish connection to DB
        ConnectionManager.openConnection();
    }

    public static void main(String[] args) {
        launch();
    }
}