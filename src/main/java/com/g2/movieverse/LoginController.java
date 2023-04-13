package com.g2.movieverse;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    protected void onLoginButtonPressed(ActionEvent event) throws IOException {
        // contact SQL server to validate login
        //for now, pretend like it was successful.
        try {
            String query = "select username, password, first_name, last_name," +
                    "email, last_access, creation_date from p320_02.user " +
                    "where username = '" + usernameField.getText().trim() + "' " +
                    "and password = '" + passwordField.getText().trim() + "';";

            // Send the query
            ResultSet rs = ConnectionManager.sendQuery(query);

            // Only do things if they logged in to an account
            if (rs.next()) {
                // First update the last_access date to current date
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("hh:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String updateQuery = "update p320_02.user set last_access = '" + dtf.format(now) + "' " +
                        "where username = '" + rs.getString(1) + "' " +
                        "and password = '" + rs.getString(2) + "';";
                ConnectionManager.sendUpdate(updateQuery);
                String updateQueryTime = "update p320_02.user set last_access_time = '" + dtfTime.format(now) + "' " +
                        "where username = '" + rs.getString(1) + "' " +
                        "and password = '" + rs.getString(2) + "';";
                ConnectionManager.sendUpdate(updateQueryTime);

                // Then set all user data to that of current account
                Stage.getWindows().get(0).setUserData(new String[] {rs.getString(1),
                        rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7)});

                rs.close();

                // Load the new scene
                FXMLLoader fxmlLoader = new FXMLLoader(MovieVerseApplication.class.getResource("mainmenu.fxml"));
                Scene mainmenuScene = new Scene(fxmlLoader.load(), 800, 500);

                // Get current window
                Stage stage = (Stage) Stage.getWindows().get(0);
                stage.setScene(mainmenuScene);
            }
            else
            {
                usernameField.setText("Login attempt failed.");
                passwordField.setText("");
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println("There was an error parsing the SQL statement");
        }
    }

    @FXML
    protected void onCreateAccountButtonPressed(ActionEvent event) throws IOException {
        // Load the new scene
        FXMLLoader fxmlLoader = new FXMLLoader(MovieVerseApplication.class.getResource("createAccount.fxml"));
        Scene createAccountScene = new Scene(fxmlLoader.load(), 800, 500);

        // Get current window
        Stage stage = (Stage) Stage.getWindows().get(0);
        stage.setScene(createAccountScene);
    }
}