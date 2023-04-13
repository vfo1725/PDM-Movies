package com.g2.movieverse;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CollectionMenuController implements Initializable {

    private final int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 500;

    @FXML
    private ScrollPane scrollpane;
    @FXML
    private GridPane results;

    private URL url;
    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.url = url;
        this.resourceBundle = resourceBundle;

        try{
            onLoadUp();
            scrollpane.setContent(results);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @FXML
    public void onBackButtonPressed(ActionEvent event) throws IOException {
        Button searchButton = (Button)event.getSource();

        FXMLLoader fxmlLoader = new FXMLLoader(MovieVerseApplication.class.getResource("mainmenu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.setScene(scene);
    }

    public void onNewCollectionPressed(ActionEvent event) throws IOException {
        Button searchButton = (Button)event.getSource();

        FXMLLoader fxmlLoader = new FXMLLoader(CreateCollection.class.getResource("createcollection.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);

        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.setScene(scene);
    }

    public void onLoadUp() throws IOException{

        String values[] = (String[]) Stage.getWindows().get(0).getUserData();
        String user = values[0];

        String query_cols = "select collection_ID, name from p320_02.collection where username = '" + user + "' order by name asc;";

        results = new GridPane();

        Label header1 = new Label("Collection Name\t\t");
        Label header2 = new Label("Total Length\t\t");
        Label header3 = new Label("Number of Movies\t\t");

        results.add(header1, 0, 0);
        results.add(header2, 1, 0);
        results.add(header3, 2, 0);
        int i = 1;

        try {
            ResultSet rs = ConnectionManager.sendQuery(query_cols);
            while(rs.next())
            {
                String collection_name = rs.getString("name");
                int coll_id = rs.getInt("collection_ID");

                String info_query = "Select count(title) as num_movies, sum(length) as total_length " +
                        "from p320_02.movie where title in " +
                        "(select title from p320_02.movie_collection where collection_id = " + coll_id + ");";
                ResultSet is = ConnectionManager.sendQuery(info_query);

                while(is.next()) {
                    int count_movie = is.getInt("num_movies");
                    int total_length = is.getInt("total_length");

                    int hours = total_length / 60;
                    int minutes = total_length % 60;

                    // below, %02d says to java that I want my integer to be formatted as a 2 digit representation
                    String temp_min = String.format("%02d", minutes);

                    String coll_length = hours + ":" + temp_min + "m";
                    String num_of_movies = "" + count_movie;

                    Label label_length = new Label(coll_length);
                    Label label_movie_count = new Label(num_of_movies);
                    Label label_name = new Label(collection_name);

                    // Play the entire collection

                    Button buttonP = new Button("Play");
                    String finalUser = user;
                    buttonP.setOnAction(buttonEvent -> {
                        String getTitles = "select title from p320_02.movie_collection where collection_id = " + coll_id + ";";
                        ResultSet ts = ConnectionManager.sendQuery(getTitles);

                        while (true) {
                            try {
                                if (!ts.next()) break;

                                String title = null;
                                title = ts.getString("title");

                                String getTodayDate = "select last_access from p320_02.user where username = '" + finalUser + "';";
                                ResultSet tds = ConnectionManager.sendQuery(getTodayDate);
                                tds.next();

                                String todayDate = null;
                                todayDate = tds.getString(1);

                                String getMovieDate = "select last_watched from p320_02.user_movie where username = '" + finalUser + "' AND title = '" + title + "';";
                                ResultSet mds = ConnectionManager.sendQuery(getMovieDate);
                                String query = null;
                                if (mds.next()) {
                                    String movieDate = mds.getString(1);
                                    if (!(todayDate.equals(movieDate))) {
                                        query = "UPDATE p320_02.user_movie" + " SET last_watched = '" + todayDate + "' WHERE username = '" + finalUser + "' AND title = '" + title + "';";
                                    }
                                } else {
                                    query = "insert into p320_02.user_movie(username, title, last_watched, rating) values " +
                                            "('" + finalUser + "', '" + title + "', '"+ todayDate +"', null);";
                                }

                                tds.close();
                                mds.close();
                                if (query != null) {
                                    ConnectionManager.sendUpdate(query);
                                }

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            ts.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    });

                    // Delete a collection

                    Button buttonD = new Button("Delete Collection");
                    String finalUser2 = user;
                    buttonD.setOnAction(buttonEvent -> {
                        String deleteQuery = "delete from p320_02.collection where username = '" + finalUser2 + "' and name = '" + collection_name + "';";
                        ConnectionManager.sendUpdate(deleteQuery);
                        initialize(url, resourceBundle);
                    });

                    // Edit the collection

                    Button buttonE = new Button("View & Edit");

                    String finalUser3 = user;
                    buttonE.setOnAction(buttonEvent -> {
                        String[] info = new String[2];
                        info[0] = values[0];
                        info[1] = collection_name;

                        Stage.getWindows().get(0).setUserData(info);

                        // Load the new scene
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(MovieVerseApplication.class.getResource("singlecollection.fxml"));
                            Scene scene = new Scene(fxmlLoader.load(), 800, 500);

                            // Get current window
                            Stage stage = (Stage) Stage.getWindows().get(0);
                            stage.setScene(scene);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    });

                    results.add(label_name, 0, i);
                    results.add(label_length, 1, i);
                    results.add(label_movie_count, 2, i);
                    results.add(buttonP, 3, i);
                    results.add(buttonE, 4, i);
                    results.add(buttonD, 5, i);
                    i++;

                }
                is.close();
            }

            rs.close();

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println("There was an error parsing the SQL statement");
        }
    }
}
