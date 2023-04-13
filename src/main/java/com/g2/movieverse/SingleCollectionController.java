package com.g2.movieverse;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.fxml.FXML;

public class SingleCollectionController implements Initializable {

    private final int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 500;

    @FXML
    private TextField collectionName;
    @FXML
    private GridPane collectionGridPane;
    @FXML
    private GridPane movieGridPane;
    @FXML
    private ScrollPane collectionScrollPane;
    @FXML
    private ScrollPane movieScrollPane;
    @FXML
    private Button collectionNameUpdateButton;
    @FXML
    private Button movieSearchButton;
    @FXML
    private TextField collectionSearchBar;

    private int collectionID = -1;

    private URL url;
    private ResourceBundle resource;

    private Label titleLabel = new Label("Movie Name"),
            runtimeLabel = new Label("Length"),
            ratingLabel = new Label("Rating"),
            castMemberLabel = new Label("Cast Members"),
            directorLabel = new Label("Director"),
            userRatingLabel = new Label("User Rating"),
            addLabel = new Label("Add");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.url = url;
        this.resource = resourceBundle;

        try{
            onLoadUp();
            // COLLECTION
            collectionScrollPane.setContent(collectionGridPane);
            ColumnConstraints movieCol = new ColumnConstraints(400);
            ColumnConstraints lastPlayedCol = new ColumnConstraints(130);
            ColumnConstraints playCol = new ColumnConstraints(100);
            ColumnConstraints ratingCol = new ColumnConstraints(70);
            ColumnConstraints deleteCol = new ColumnConstraints(100);
            collectionGridPane.getColumnConstraints().addAll(movieCol, lastPlayedCol, playCol,
                    ratingCol, deleteCol);


            // SEARCH
            Border border = new Border(new BorderStroke(
                    Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(0),
                    BorderStroke.THIN));
            titleLabel.setBorder(border);
            runtimeLabel.setBorder(border);
            ratingLabel.setBorder(border);
            castMemberLabel.setBorder(border);
            directorLabel.setBorder(border);
            userRatingLabel.setBorder(border);
            addLabel.setBorder(border);

            movieGridPane.getChildren().clear();
            movieGridPane.getColumnConstraints().clear();
            movieGridPane.getRowConstraints().clear();
            movieGridPane.setVgap(5);
            ColumnConstraints movieSearchCol = new ColumnConstraints(300);
            ColumnConstraints lengthCol = new ColumnConstraints(50);
            ColumnConstraints ratingSearchCol = new ColumnConstraints(50);
            ColumnConstraints directorCol = new ColumnConstraints(100);
            ColumnConstraints castMemberCol = new ColumnConstraints(150);
            ColumnConstraints userRatingCol = new ColumnConstraints(75);
            ColumnConstraints addCol = new ColumnConstraints(75);
            movieGridPane.getColumnConstraints().addAll(movieSearchCol, lengthCol,
                    ratingSearchCol, directorCol, castMemberCol, userRatingCol, addCol);

            String values[] = (String[]) Stage.getWindows().get(0).getUserData();
            String user = values[0];
            String collection_name = values[1];
            String getId = "select collection_id from p320_02.collection where name = '" + collection_name + "' " +
                    "and username = '" + user +  "';";
            ResultSet is = ConnectionManager.sendQuery(getId);

            if (is.next()) {
                int coll_id = is.getInt(1);
                collectionID = coll_id;
            }
            is.close();

        } catch (IOException | SQLException ioe) {
            ioe.printStackTrace();
        }
    }

    @FXML
    public void onBackButtonPressed(ActionEvent event) throws IOException {
        Button searchButton = (Button)event.getSource();

        FXMLLoader fxmlLoader = new FXMLLoader(CollectionMenuController.class.getResource("collectionMenu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void onUpdateButtonPressed(ActionEvent event) throws IOException, SQLException {
        String values[] = (String[]) Stage.getWindows().get(0).getUserData();
        String user = values[0];
        String name = values[1];

        String getId = "select collection_id from p320_02.collection where username = '" + user + "' AND name = '" + name + "';";
        ResultSet is = ConnectionManager.sendQuery(getId);

        if (is.next()) {
            int coll_id = is.getInt(1);
            collectionID = coll_id;
            String new_name = collectionName.getText().trim();

            if(new_name.equals("")){
                String error = "Collection Name cannot be empty";
                collectionName.setText(error);
            }else {
                String updateName = "UPDATE p320_02.COLLECTION SET NAME = '" + new_name + "' WHERE collection_id = " + coll_id + ";";
                ConnectionManager.sendUpdate(updateName);
            }
        }
    }

    public void onLoadUp() throws IOException, SQLException {
        String values[] = (String[]) Stage.getWindows().get(0).getUserData();
        String user = values[0];
        String coll_name = values[1];

        collectionName.setText(coll_name);
        collectionGridPane = new GridPane();

        Label header1 = new Label("Movie Title");
        Label header2 = new Label("Date Last Played");
        Label header3 = new Label("Rating");

        collectionGridPane.add(header1, 0,0);
        collectionGridPane.add(header2, 1, 0);
        collectionGridPane.add(header3, 2, 0);
        int i = 1;

        String getId = "select collection_id from p320_02.collection where username = '" + user +
                "' and name = '" + coll_name + "';";
        ResultSet is = ConnectionManager.sendQuery(getId);

        if (is.next()) {
            int coll_id = is.getInt(1);
            String moviesQuery = "select title from p320_02.movie_collection where collection_id = " + coll_id + ";";


            try {
                ResultSet ts = ConnectionManager.sendQuery(moviesQuery);
                while(ts.next()){

                    // DATE WATCHED FIELD

                    Label title = new Label(ts.getString(1));

                    String prQuery = "select last_watched, rating from p320_02.user_movie where username = '" + user +
                            "' AND title = '" + title.getText() + "';";
                    ResultSet lprs = ConnectionManager.sendQuery(prQuery);

                    Label lastWatched = new Label("Not Watched");
                    if (lprs.next())
                    {
                        lastWatched = new Label(lprs.getString(1));
                    }

                    // PLAY A MOVIE

                    Button play = new Button("Play");
                    Label finalLastWatched = lastWatched;
                    play.setOnAction(buttonEvent -> {
                        String title_name = title.getText();
                        String todayDate = null;

                        String getTodayDate = "select last_access from p320_02.user where username = '" + user + "';";
                        ResultSet tds = ConnectionManager.sendQuery(getTodayDate);
                        try {
                            tds.next();
                            todayDate = tds.getString(1);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        String getMovieDate = "select last_watched from p320_02.user_movie where username = '" + user + "' AND title = '" + title_name + "';";
                        ResultSet mds = ConnectionManager.sendQuery(getMovieDate);

                        String query = null;
                        try {
                            if (mds.next()) {
                                String movieDate = mds.getString(1);
                                if (!(todayDate.equals(movieDate))) {
                                    query = "UPDATE p320_02.user_movie" + " SET last_watched = '" + todayDate + "' WHERE username = '" + user + "' AND title = '" + title_name + "';";
                                }
                            } else {
                                query = "insert into p320_02.user_movie(username, title, last_watched, rating) values " +
                                        "('" + user + "', '" + title_name + "', '"+ todayDate +"', null);";
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        try {
                            tds.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        try {
                            mds.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        if (query != null) {
                            ConnectionManager.sendUpdate(query);
                            finalLastWatched.setText(todayDate);
                            initialize(url, resource);
                        }
                    });

                    // RATING

                    ComboBox rating = new ComboBox();
                    rating.getItems().add("1");
                    rating.getItems().add("2");
                    rating.getItems().add("3");
                    rating.getItems().add("4");
                    rating.getItems().add("5");


                    String seeRating = "select rating from p320_02.user_movie where username = '" + user + "' AND title = '" + title.getText() + "';";
                    ResultSet rds = ConnectionManager.sendQuery(seeRating);

                    if(rds.next()){
                        int current_rating = rds.getInt(1);
                        if(current_rating != 0 ) {
                            if(!(lastWatched.getText().trim().equals("Not Watched"))) {
                                rating.setPromptText(String.valueOf(current_rating));
                            }
                        }
                        else{
                            rating.setPromptText("NONE");
                        }
                    }

                    rating.setOnAction(
                            comboBoxEvent -> {
                                int new_rating = Integer.parseInt((String) rating.getValue());
                                String ratingQuery = "update p320_02.user_movie set rating = " + new_rating + " where username = '" +user +"' AND title = '" + title.getText() +"';";
                                ConnectionManager.sendUpdate(ratingQuery);
                                rating.setPromptText(String.valueOf(new_rating));
                                initialize(url, resource);

                            }
                    );

                    // DELETE FROM COLLECTION

                    Button delete = new Button("Delete");
                    delete.setOnAction(buttonEvent -> {
                        String delQuery = "delete from p320_02.movie_collection where collection_id = '" + coll_id + "' AND title = '" + title.getText() + "';";
                        ConnectionManager.sendUpdate(delQuery);
                        initialize(url, resource);
                    });

                    collectionGridPane.add(title, 0, i);
                    collectionGridPane.add(lastWatched, 1, i);
                    collectionGridPane.add(rating, 2, i);
                    collectionGridPane.add(play, 3, i);
                    collectionGridPane.add(delete, 4, i);
                    i++;
                    lprs.close();
                }
                ts.close();
                is.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void onMovieSearch()
    {
        try {
            String query = null;
            ResultSet rs = null;

            // See if search is empty
            if (collectionSearchBar.getText().trim().equals("")) {
                return;
            }

            query = "select title, length, mpaa_rating, release_date from p320_02.movie M " +
                    "where M.title like '%" + collectionSearchBar.getText() + "%' ";
            // Send the query
            rs = ConnectionManager.sendQuery(query);

            // Load the results
            movieGridPane.getChildren().clear();
            movieGridPane.add(titleLabel,0, 0);
            movieGridPane.add(runtimeLabel, 1, 0);
            movieGridPane.add(ratingLabel, 2, 0);
            movieGridPane.add(directorLabel, 3, 0);
            movieGridPane.add(castMemberLabel, 4, 0);
            movieGridPane.add(userRatingLabel, 5, 0);
            movieGridPane.add(addLabel, 6, 0);
            int i = 1;

            while (rs.next())
            {
                Label title = new Label(rs.getString(1));
                Label length = new Label(rs.getString(2) + " mins");
                Label rating = new Label(rs.getString(3));

                if (rs.getString(1).contains("\'"))
                    continue;

                movieGridPane.add(title, 0, i);
                movieGridPane.add(length, 1, i);
                movieGridPane.add(rating, 2, i);
                i++;
            }

            rs.close();

            // Now the fun part.
            // Getting the cast members, directors, ratings
            int row = 1;
            ObservableList<Node> allChildren = movieGridPane.getChildren();
            ArrayList<Node> children = new ArrayList<>();

            // workaround where allChildren updates as you enter the children
            for (Node node : allChildren)
                children.add(node);

            for (i = 7; i < children.size(); i+=3)
            {
                Label label = (Label)children.get(i);

                // --------------- CAST MEMBER CODE -------------------
                String castQuery = "select concat(P.first_name,' ',P.last_name) " +
                        "from p320_02.person P, p320_02.movie_cast_member C " +
                        "where P.person_id = C.person_id and title = '" +
                        label.getText() + "';";
                rs = ConnectionManager.sendQuery(castQuery);

                StringBuilder members = new StringBuilder();
                while (rs.next())
                {
                    members.append(rs.getString(1) + ", ");
                }

                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setContent(new Label(members.toString()));
                movieGridPane.add(scrollPane, 4, row);
                rs.close();

                // --------------- END CAST MEMBER CODE ----------------------
                // --------------- DIRECTOR CODE -----------------------------

                String directorQuery = "select concat(P.first_name, ' ', P.last_name) " +
                        "from p320_02.person P, p320_02.movie_director D " +
                        "where P.person_id = D.director_id and title = '" +
                        label.getText() + "';";
                rs = ConnectionManager.sendQuery(directorQuery);
                rs.next();

                movieGridPane.add(new Label(rs.getString(1)), 3, row);
                rs.close();

                // --------------- END DIRECTOR CODE -------------------------
                // --------------- BEGIN USR RATING CODE ---------------------
                String usrRatingQuery = "select avg(rating) " +
                        "from p320_02.movie M, p320_02.user_movie U " +
                        "where M.title = '" + label.getText() +
                        "' and U.title = M.title;";
                rs = ConnectionManager.sendQuery(usrRatingQuery);
                rs.next();

                if (rs.getString(1) == null)
                    movieGridPane.add(new Label("N/A"), 5, row);
                else
                    movieGridPane.add(new Label(rs.getString(1)), 5, row);
                rs.close();
                // --------------- END USR RATING CODE -----------------------

                Button addButton = new Button("Add");
                addButton.setOnAction( event -> {
                    String checkMovie = "select title from p320_02.movie_collection where title = '"
                            + label.getText() + "' and collection_id = " + collectionID + ";";
                    if (!checkIfMovieInCollection(checkMovie))
                    {
                        String insertQuery = "insert into p320_02.movie_collection(collection_id, title) " +
                                "values (" + collectionID + ",'" + label.getText() + "')";
                        sendInsert(insertQuery);
                        initialize(url, resource);
                    }
                });
                movieGridPane.add(addButton, 6, row);

                row++;
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    private boolean checkIfMovieInCollection(String query)
    {
        ResultSet rs = null;
        try{
            rs = ConnectionManager.sendQuery(query);

            if (rs.next())
            {
                // movie in collection
                rs.close();
                return true;
            }
            rs.close();
            return false;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        try{
            rs.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return false;
    }

    private void sendInsert(String query){
        ConnectionManager.sendUpdate(query);
    }
}
