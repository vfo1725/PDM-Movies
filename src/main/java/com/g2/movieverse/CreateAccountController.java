package com.g2.movieverse;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateAccountController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;

    public void createAccount() {
        try{
            // create new account
            String query = "select username from p320_02.user " +
                    "where username = '" + usernameField.getText().trim() + "';";

            // Send the query
            ResultSet rs = ConnectionManager.sendQuery(query);
            if (rs.next())
            {
                // account exists. Throw error
                usernameField.setText("Username taken. Select a different username.");
                passwordField.setText("");
                rs.close();
                return;
            }

            rs.close();

            // username free. Create account
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("hh:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            query = "insert into p320_02.user(username, password, first_name, last_name, email, last_access, " +
                    "creation_date, last_access_time) values ('" + usernameField.getText().trim() + "','" +
                    passwordField.getText().trim() + "', '" + firstNameField.getText().trim() +  "','" +
                    lastNameField.getText().trim() + "','" + emailField.getText().trim() + "','" +
                    dtf.format(now) + "','" + dtf.format(now) + "','" + dtfTime.format(now) + "'" +
                    ");";

            ConnectionManager.sendUpdate(query);

            String getQuery = "select username, password, first_name, last_name," +
                    "email, last_access, creation_date from p320_02.user " +
                    "where username = '" + usernameField.getText().trim() + "' " +
                    "and password = '" + passwordField.getText().trim() + "';";

            // Send the query
            rs = ConnectionManager.sendQuery(getQuery);
            rs.next();
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

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
