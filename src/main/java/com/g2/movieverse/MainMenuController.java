package com.g2.movieverse;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {
    @FXML
    private Label welcomeBackLabel;
    @FXML
    private ScrollPane friendsPane;
    @FXML
    private VBox friendsVbox;
    @FXML
    private Text followingText;
    @FXML
    private Text followersText;
    @FXML
    private ScrollPane recomendedPane;
    @FXML
    private GridPane recomendedGpane;
    @FXML
    private ScrollPane releasePane;
    @FXML
    private GridPane releaseGpane;
    @FXML
    private ScrollPane topMoviesPane;
    @FXML
    private GridPane topMoviesGpane;
    @FXML
    private ScrollPane friendsMoviesPane;
    @FXML
    private GridPane friendsMoviesGpane;
    @FXML
    private Label recommendedText;
    @FXML
    private Text collectionsNumber;
    @FXML
    private GridPane top10Pane;

    private URL url;
    private ResourceBundle resourceBundle;

    private final int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 500;

    private Scene loadScene(String file, int width, int height) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(MovieVerseApplication.class.getResource(file));
        return new Scene(fxmlLoader.load(), width, height);
    }

    @FXML
    public void onSearchButtonPressed(ActionEvent event) throws IOException {
        Button searchButton = (Button)event.getSource();

        Scene scene = loadScene("searchMenu.fxml", DEFAULT_WIDTH, DEFAULT_HEIGHT);

        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void onCollectionButtonPressed(ActionEvent event) throws IOException {
        Button searchButton = (Button)event.getSource();

        Scene scene = loadScene("collectionMenu.fxml", DEFAULT_WIDTH, DEFAULT_HEIGHT);

        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void onFriendButtonPressed(ActionEvent event) throws IOException {
        Button searchButton = (Button)event.getSource();

        Scene scene = loadScene("friendsMenu.fxml", DEFAULT_WIDTH, DEFAULT_HEIGHT);

        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void onLogout(ActionEvent event) throws IOException {
        Button searchButton = (Button)event.getSource();
        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.url = url;
        this.resourceBundle = resourceBundle;
        try{
            onLoadUp();
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
        welcomeBackLabel.setText("Welcome Back: " + user);

        friendsVbox.getChildren().clear();
        ResultSet friendsSet = ConnectionManager.sendQuery(query);
        while (friendsSet.next()) {
            String nextFriend = friendsSet.getString(1);
            Label nextFriendLable = new Label(nextFriend);
            friendsVbox.getChildren().add(nextFriendLable);
        }
        friendsSet.close();
        String followerQuery = "select count(follower_username) from p320_02.following where follower_username = '" + user + "';";
        // Send the query
        ResultSet followerSet = ConnectionManager.sendQuery(followerQuery);
        followerSet.next();
        int followers = followerSet.getInt(1);
        followersText.setText("# of followers: " + followers);
        followerSet.close();

        String followeeQuery = "select count(followee_username) from p320_02.following where followee_username = '" + user + "';";
        // Send the query
        ResultSet followeeSet = ConnectionManager.sendQuery(followeeQuery);
        followeeSet.next();
        int following = followeeSet.getInt(1);
        followingText.setText("# following: " + following);
        followeeSet.close();

        // Get recommended movie
        recomendedGpane.getChildren().clear();
        recomendedGpane.getColumnConstraints().clear();
        recomendedGpane.getRowConstraints().clear();
        ColumnConstraints movieCol = new ColumnConstraints(200);
        ColumnConstraints lengthCol = new ColumnConstraints(50);
        ColumnConstraints ratingCol = new ColumnConstraints(50);
        recomendedGpane.getColumnConstraints().addAll(movieCol, lengthCol, ratingCol, ratingCol);
        recomendedGpane.add(new Label("Title"), 0, 0);
        recomendedGpane.add(new Label("MPAA"), 1, 0);
        recomendedGpane.add(new Label("Length"), 2, 0);
        recomendedGpane.add(new Label("Rating"), 3, 0);

        String recommendedQuery = "select A.genre_id, G.name "+
                "from (select A.genre_count/count(*)*100 as user_genre_percent, A.genre_id "+
                "from p320_02.user_movie UM, (select cast(count(*) as double precision) as genre_count, GM.genre_id "+
                "from p320_02.user_movie UM, p320_02.genre_movie GM " +
                "where UM.username = '"+ user +"' and UM.title = GM.title " +
                "group by GM.genre_id, GM.genre_id) as A "+
                "where UM.username = '"+ user +"' " +
                "group by A.genre_id, A.genre_count) as A, " +
                "(select cast(count(GM.genre_id) as double precision)/(select count(*) from p320_02.movie)*100 as total_genre_percent, GM.genre_id " +
                "from p320_02.genre_movie GM " +
                "group by GM.genre_id) as B, p320_02.genre as G "  +
                "where A.genre_id = B.genre_id and A.genre_id = G.genre_id "+
                "order by (A.user_genre_percent-B.total_genre_percent) desc "+
                "limit 1;";
        ResultSet rs = ConnectionManager.sendQuery(recommendedQuery);
        if (rs.next()) {
            int preferredGenre = rs.getInt(1);
            String preferredGenreName = rs.getString(2);

            recommendedText.setText(recommendedText.getText() + preferredGenreName + " we recommend:");
            rs.close();

            // Find movies to recommend
            String str = "select M.title, M.mpaa_rating, M.length, avg(UM.rating) "+
                    "from p320_02.movie M, p320_02.user_movie UM " +
                    "where M.title = UM.title and M.title in " +
                    "(select title from p320_02.genre_movie G where M.title = G.title and genre_id = " + preferredGenre + ") " +
                    "group by M.mpaa_rating, M.title " +
                    "order by avg(rating) desc;";

            rs = ConnectionManager.sendQuery(str);

            int i = 1;
            while (rs.next() && i - 1 < 10) {
                recomendedGpane.add(new Label(rs.getString(1)), 0, i);
                recomendedGpane.add(new Label(rs.getString(2)), 1, i);
                recomendedGpane.add(new Label(rs.getString(3) + "m"), 2, i);
                recomendedGpane.add(new Label(rs.getString(4)), 3, i);
                i++;
            }

            rs.close();
        } else {
            recommendedText.setText("Once you watch a movie, we'll try to recommend similar movies!");
            recomendedGpane.add(new Label("Watch some movies!"), 0, 1);
        }

        // Get top released movies this month
        releaseGpane.getChildren().clear();
        releaseGpane.getColumnConstraints().clear();
        releaseGpane.getRowConstraints().clear();
        releaseGpane.getColumnConstraints().addAll(movieCol, ratingCol, lengthCol, ratingCol);
        releaseGpane.add(new Label("Title"), 0, 0);
        releaseGpane.add(new Label("MPAA"), 1, 0);
        releaseGpane.add(new Label("Length"), 2, 0);
        releaseGpane.add(new Label("Rating"), 3, 0);

        // Get movies released this month and movies with ratings, order them in descending order
        // based on rating, then get, at most, the top 5
        String releaseQuery = "select M.title, M.mpaa_rating, M.length, avg(UM.rating) as rating from p320_02.movie M, p320_02.user_movie UM " +
                "where M.title = UM.title and EXTRACT(year from M.release_date) = EXTRACT (year from CURRENT_TIMESTAMP) " +
                "and EXTRACT (month from M.release_date) = EXTRACT (month from CURRENT_TIMESTAMP) " +
                "group by M.title " +
                "order by rating desc limit 5;";

        ResultSet releaseSet = ConnectionManager.sendQuery(releaseQuery);
        if (releaseSet.next()) {
            int i = 1;
            do {
                releaseGpane.add(new Label(releaseSet.getString(1)), 0, i);
                releaseGpane.add(new Label(releaseSet.getString(2)), 1, i);
                releaseGpane.add(new Label(releaseSet.getString(3) + "m"), 2, i);
                releaseGpane.add(new Label(releaseSet.getString(4)), 3, i);
                i++;
            } while (releaseSet.next() && i - 1 < 5);
            releaseSet.close();
        }
        else
        {
            releaseGpane.add(new Label("No rated movies this month!"), 0, 1);
        }

        // Get top movies in the last 90 days
        topMoviesGpane.getChildren().clear();
        topMoviesGpane.getColumnConstraints().clear();
        topMoviesGpane.getRowConstraints().clear();
        topMoviesGpane.getColumnConstraints().addAll(movieCol, ratingCol, lengthCol, ratingCol);
        topMoviesGpane.add(new Label("Title"), 0, 0);
        topMoviesGpane.add(new Label("MPAA"), 1, 0);
        topMoviesGpane.add(new Label("Length"), 2, 0);
        topMoviesGpane.add(new Label("Rating"), 3, 0);

        // Get movies that have the most last_watched dates by users in the last 90 days
        String topQuery = "select M.title, M.mpaa_rating, M.length, avg(UM.rating), count(M.title) as views from " +
                "p320_02.movie M, p320_02.user_movie UM " +
                "where M.title = UM.title and " +
                "UM.last_watched >= (CURRENT_DATE - INTERVAL '90 days') " +
                "group by M.title order by views desc limit 20;";

        ResultSet topSet = ConnectionManager.sendQuery(topQuery);
        if (topSet.next()) {
            int i = 1;
            do {
                topMoviesGpane.add(new Label(topSet.getString(1)), 0, i);
                topMoviesGpane.add(new Label(topSet.getString(2)), 1, i);
                topMoviesGpane.add(new Label(topSet.getString(3) + "m"), 2, i);
                topMoviesGpane.add(new Label(topSet.getString(4)), 3, i);
                i++;
            } while (topSet.next() && i - 1 < 20);
            topSet.close();
        }
        else
        {
            topMoviesGpane.add(new Label("No watched movies in 90 days!"), 0, 1);
        }

        // Get top movies in the last 90 days
        friendsMoviesGpane.getChildren().clear();
        friendsMoviesGpane.getColumnConstraints().clear();
        friendsMoviesGpane.getRowConstraints().clear();
        friendsMoviesGpane.getColumnConstraints().addAll(movieCol, ratingCol, lengthCol, ratingCol);
        friendsMoviesGpane.add(new Label("Title"), 0, 0);
        friendsMoviesGpane.add(new Label("MPAA"), 1, 0);
        friendsMoviesGpane.add(new Label("Length"), 2, 0);
        friendsMoviesGpane.add(new Label("Rating"), 3, 0);

        // Get movies that friends have rated most highly
        String friendMovieQuery = "select M.title, M.mpaa_rating, M.length, avg(UM.rating) " +
                "from p320_02.movie M, p320_02.user_movie UM " +
                "where M.title in ( "+
                    "select title " +
                    "from p320_02.user_movie UM " +
                    "where username in ( " +
                        "select followee_username " +
                        "from p320_02.following " +
                        "where follower_username = '"+user+"' and followee_username in ( " +
                            "select(follower_username) " +
                            "from p320_02.following "+
                            "where followee_username = '"+user+"')) "+
                            "group by title) and M.title = UM.title " +
                "group by M.title " +
                "order by avg(rating) desc " +
                "limit 20;";

        ResultSet friendMovieSet = ConnectionManager.sendQuery(friendMovieQuery);
            if (friendMovieSet.next()) {
                int i = 1;
                do {
                    friendsMoviesGpane.add(new Label(friendMovieSet.getString(1)), 0, i);
                    friendsMoviesGpane.add(new Label(friendMovieSet.getString(2)), 1, i);
                    friendsMoviesGpane.add(new Label(friendMovieSet.getString(3) + "m"), 2, i);
                    friendsMoviesGpane.add(new Label(friendMovieSet.getString(4)), 3, i);
                    i++;
                } while (friendMovieSet.next() && i - 1 < 20);
                friendMovieSet.close();
        }
        else
        {
            friendsMoviesGpane.add(new Label("No friend rated movies!"), 0, 1);
        }

        String getCollections = "select count(collection_id) from p320_02.collection where username = '" + user + "';";
        ResultSet collSet = ConnectionManager.sendQuery(getCollections);
        collSet.next();
        int collections = collSet.getInt(1);
        collectionsNumber.setText("# of Collections: " + collections);
        collSet.close();

        top10Pane.getChildren().clear();
        top10Pane.getRowConstraints().clear();
        top10Pane.getColumnConstraints().clear();
        top10Pane.getColumnConstraints().addAll(movieCol, lengthCol, ratingCol, ratingCol);

        top10Pane.add(new Label("Title"), 0, 0);
        top10Pane.add(new Label("MPAA"), 1, 0);
        top10Pane.add(new Label("Length"), 2, 0);
        top10Pane.add(new Label("Rating"), 3, 0);
        top10Pane.add(new Label("Times You Watched"), 4, 0);

        // Get movies that friends have rated most highly
        String top10Query = "select M.title, M.mpaa_rating, M.length, avg(UM.rating) as rating, count(UM.title) "+
                "from p320_02.movie M, p320_02.user_movie UM " +
                " where UM.title = M.title and UM.username = '" + user + "' group by M.title " +
                "order by count(UM.title) desc, rating desc limit 10;";

        ResultSet top10MovieSet = ConnectionManager.sendQuery(top10Query);
        if (top10MovieSet.next()) {
            int i = 1;
            do {
                top10Pane.add(new Label(top10MovieSet.getString(1)), 0, i);
                top10Pane.add(new Label(top10MovieSet.getString(2)), 1, i);
                top10Pane.add(new Label(top10MovieSet.getString(3) + "m"), 2, i);
                top10Pane.add(new Label(top10MovieSet.getString(4)), 3, i);
                top10Pane.add(new Label(top10MovieSet.getString(5)), 4, i);
                i++;
            } while (top10MovieSet.next() && i - 1 < 10);
            top10MovieSet.close();
        }
        else
        {
            friendsMoviesGpane.add(new Label("No friend rated movies!"), 0, 1);
        }
    }
}