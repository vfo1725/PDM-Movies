package com.g2.movieverse;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FriendMenuController implements Initializable {
    @FXML
    private TextField followerField;
    @FXML
    private ScrollPane friendsScrollPane;
    @FXML
    private VBox friendsPane;
    @FXML
    private Button removeFollowerButton;
    @FXML
    private Button addFollowerButton;
    @FXML
    private TextField numFollowers;
    @FXML
    private TextField numFollowees;
    @FXML
    private Button goBackButton;

    private URL url;
    private ResourceBundle resourceBundle;
    private int followers;
    private int following;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.url = url;
        this.resourceBundle = resourceBundle;
        try{
            onLoadUp();
            numFollowers.setText("Number of Followers " + followers);
            numFollowees.setText("Number of Followees " + following);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void onLoadUp() throws SQLException {
        String[] values = (String[]) Stage.getWindows().get(0).getUserData();
        String user = values[0];
        String query = "select followee_username from p320_02.following where follower_username = '" + user + "'" +
                " and followee_username in(select(follower_username) from p320_02.following where followee_username = '" + user + "');";

        // Send the query
        friendsPane.getChildren().clear();
        ResultSet friendsSet = ConnectionManager.sendQuery(query);
        while(friendsSet.next()){
            String nextFriend = friendsSet.getString(1);
            Label nextFriendLable = new Label(nextFriend);
            friendsPane.getChildren().add(nextFriendLable);
        }
        friendsSet.close();
        String followerQuery = "select count(follower_username) from p320_02.following where follower_username = '" + user +"';";
        // Send the query
        ResultSet followerSet = ConnectionManager.sendQuery(followerQuery);
        followerSet.next();
        followers = followerSet.getInt(1);
        followerSet.close();

        String followeeQuery = "select count(followee_username) from p320_02.following where followee_username = '" + user +"';";
        // Send the query
        ResultSet followeeSet = ConnectionManager.sendQuery(followeeQuery);
        followeeSet.next();
        following = followeeSet.getInt(1);
        followeeSet.close();

    }

    @FXML
    public void onBackButtonPressed(ActionEvent event) throws IOException {
        Button searchButton = (Button)event.getSource();

        FXMLLoader fxmlLoader = new FXMLLoader(MovieVerseApplication.class.getResource("mainmenu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);

        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void onAddFollowingPressed(ActionEvent event) throws IOException {
        Button originalButton = (Button) event.getSource();
        String[] values = (String[]) Stage.getWindows().get(0).getUserData();
        String user = values[0];
        String query = "insert into p320_02.following(follower_username, followee_username) " +
                "values ('" + user + "', '" + followerField.getText().trim() + "')";

        // Send the query
        ConnectionManager.sendUpdate(query);
        initialize(url, resourceBundle);
    }



    @FXML
    public void onRemoveFollowingPressed(ActionEvent event) throws IOException {
        Button originalButton = (Button)event.getSource();
        String[] values = (String[]) Stage.getWindows().get(0).getUserData();
        String user = values[0];
        String query = "delete from p320_02.following" +
                " where follower_username = '" + user + "'" +
                " and followee_username = '" + followerField.getText().trim() + "'";

        // Send the query
        ConnectionManager.sendUpdate(query);
        initialize(url, resourceBundle);
    }
}
