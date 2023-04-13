package com.g2.movieverse;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CreateCollection {

    private final int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 500;

    @FXML
    private TextField textfield_name;

    @FXML
    private Label error_label;

    @FXML
    public void onBackButtonPressed(ActionEvent event) throws IOException {
        Button searchButton = (Button)event.getSource();

        FXMLLoader fxmlLoader = new FXMLLoader(CollectionMenuController.class.getResource("collectionMenu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.setScene(scene);
    }

    public void onCreateButtonPressed(ActionEvent event) throws IOException, SQLException {
        Button createButton = (Button)event.getSource();

        String new_name = textfield_name.getText().trim();

        // Check for string inside the text
        if(new_name.equals("")){
            if(!(error_label.isVisible())){
                error_label.setVisible(true);
            }

        } else {

            String[] value = (String[])Stage.getWindows().get(0).getUserData();
            String user = value[0];

            String[] val = new String[2];
            val[1] = new_name;
            val[0] = user;
            Stage.getWindows().get(0).setUserData(val);

            String nameQ = "select name from p320_02.collection where username =  '" + user + "' AND name = '" + new_name + "';";
            ResultSet ns = ConnectionManager.sendQuery(nameQ);

            if(ns.next()){
               // do nothing
            } else {
                String insertQuery = "INSERT INTO p320_02.collection(username, name) VALUES ('" + user + "', '" + new_name + "');";
                ConnectionManager.sendUpdate(insertQuery);
            }

            ns.close();

            FXMLLoader fxmlLoader = new FXMLLoader(SingleCollectionController.class.getResource("singlecollection.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 500);

            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.setScene(scene);
        }
    }

}
