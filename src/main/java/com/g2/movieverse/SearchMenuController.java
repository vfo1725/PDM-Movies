package com.g2.movieverse;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import java.util.*;
import java.util.logging.Logger;

public class SearchMenuController implements Initializable {
    private static final Logger LOG = Logger.getLogger(SearchMenuController.class.getName());
    @FXML
    private TextField searchField;
    @FXML
    private RadioButton searchByNameButton;
    @FXML
    private RadioButton searchByGenreButton;
    @FXML
    private RadioButton searchByCastMemberButton;
    @FXML
    private RadioButton searchByReleaseDateButton;
    @FXML
    private RadioButton searchByStudioButton;
    @FXML
    private RadioButton searchByDirectorButton;
    @FXML
    private RadioButton movieNameFilter;
    @FXML
    private RadioButton genreFilter;
    @FXML
    private RadioButton studioFilter;
    @FXML
    private RadioButton yearFilter;
    @FXML
    private RadioButton ascendingButton;
    @FXML
    private RadioButton descendingButton;
    @FXML
    private GridPane results;

    private String user;

    private Label titleLabel = new Label("Movie Name"),
            runtimeLabel = new Label("Length"),
            ratingLabel = new Label("Rating"),
            castMemberLabel = new Label("Cast Members"),
            directorLabel = new Label("Director"),
            userRatingLabel = new Label("User Rating");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Border border = new Border(new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(0),
                BorderStroke.THIN));
        titleLabel.setBorder(border);
        runtimeLabel.setBorder(border);
        ratingLabel.setBorder(border);
        castMemberLabel.setBorder(border);
        directorLabel.setBorder(border);
        userRatingLabel.setBorder(border);

        results.getChildren().clear();
        results.getColumnConstraints().clear();
        results.getRowConstraints().clear();
        results.setVgap(5);
        ColumnConstraints movieCol = new ColumnConstraints(200);
        ColumnConstraints lengthCol = new ColumnConstraints(50);
        ColumnConstraints ratingCol = new ColumnConstraints(50);
        ColumnConstraints directorCol = new ColumnConstraints(150);
        ColumnConstraints castMemberCol = new ColumnConstraints(150);
        ColumnConstraints userRatingCol = new ColumnConstraints(75);
        results.getColumnConstraints().addAll(movieCol, lengthCol,
                ratingCol, directorCol, castMemberCol, userRatingCol);
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
    public void onSearchButtonPressed(ActionEvent event)
    {
        try {
            String query = null;
            ResultSet rs = null;

            // See if search is empty
            if (searchField.getText().trim().equals("")) {
                return;
            }

            if (searchByNameButton.isSelected()) {
                query = "select title, length, mpaa_rating, release_date from p320_02.movie M " +
                        "where M.title like '%" + searchField.getText() + "%' ";
            } else if (searchByCastMemberButton.isSelected()) {
                String name = searchField.getText().trim();
                String[] names = name.split(" ");
                if (names.length == 1)
                    query = "select M.title, M.length, M.mpaa_rating, M.release_date " +
                            "from p320_02.movie M, p320_02.movie_cast_member C, p320_02.person P " +
                            "where (P.first_name like '%" + names[0].trim() +
                            "%' or P.last_name like '%" + names[0].trim() +
                            "%') and P.person_id = C.person_id and C.title = M.title ";
                else if (names.length == 2)
                    query = "select M.title, M.length, M.mpaa_rating, M.release_date " +
                            "from p320_02.movie M, p320_02.movie_cast_member C, p320_02.person P " +
                            "where (P.first_name like '%" + names[0] + "%' and P.last_name like '%" +
                            names[1] + "%') and P.person_id = C.person_id and C.title = M.title ";
            } else if (searchByDirectorButton.isSelected()) {
                String name = searchField.getText().trim();
                String[] names = name.split(" ");
                if (names.length == 1)
                    query = "select M.title, M.length, M.mpaa_rating, M.release_date " +
                            "from p320_02.movie M, p320_02.movie_director D, p320_02.person P " +
                            "where (P.first_name like '%" + names[0].trim() +
                            "%' or P.last_name like '%" + names[0].trim() +
                            "%') and P.person_id = D.director_id and D.title = M.title ";
                else if (names.length == 2)
                    query = "select M.title, M.length, M.mpaa_rating, M.release_date " +
                            "from p320_02.movie M, p320_02.movie_director D, p320_02.person P " +
                            "where (P.first_name like '%" + names[0] + "%' and P.last_name like '%" +
                            names[1] + "%') and P.person_id = D.director_id and D.title = M.title ";
            } else if (searchByStudioButton.isSelected()) {
                query = "select M.title, M.length, M.mpaa_rating, M.release_date " +
                        "from p320_02.movie M, p320_02.studio_movie S, p320_02.studio T " +
                        "where (T.name like '%" + searchField.getText() +
                        "%') and T.studio_id = S.studio_id and S.title = m.title ";
            } else if (searchByGenreButton.isSelected()) {
                query = "select M.title, M.length, M.mpaa_rating, M.release_date " +
                        "from p320_02.movie M, p320_02.genre_movie G, p320_02.genre T " +
                        "where (T.name like '%" + searchField.getText()
                        + "%') and T.genre_id = G.genre_id and G.title = m.title ";
            } else if (searchByReleaseDateButton.isSelected()) {
                query = "select title, length, mpaa_rating, release_date " +
                        "from p320_02.movie M " +
                        "where extract(year from M.release_date) = " + searchField.getText() + " ";
            }

            // -------------------- FILTER OPTIONS ----------------------------------
            // Ascending or descending
            if (ascendingButton.isSelected()) {
                if (movieNameFilter.isSelected())
                    query = query + " order by M.title ASC;";
                else if (yearFilter.isSelected())
                    query = query + " order by M.release_date ASC, M.title ASC;";
            }
            else if (descendingButton.isSelected())
                query = query + " order by M.title DESC;";
            else if (yearFilter.isSelected())
                query = query + " order by M.release_date DESC, M.title ASC;";

            // Send the query
            rs = ConnectionManager.sendQuery(query);

            // Load the results
            results.getChildren().clear();
            results.add(titleLabel,0, 0);
            results.add(runtimeLabel, 1, 0);
            results.add(ratingLabel, 2, 0);
            results.add(directorLabel, 3, 0);
            results.add(castMemberLabel, 4, 0);
            results.add(userRatingLabel, 5, 0);
            int i = 1;

            while (rs.next())
            {
                Label title = new Label(rs.getString(1));
                Label length = new Label(rs.getString(2) + " mins");
                Label rating = new Label(rs.getString(3));

                if (rs.getString(1).contains("\'"))
                    continue;

                results.add(title, 0, i);
                results.add(length, 1, i);
                results.add(rating, 2, i);
                i++;
            }

            rs.close();

            // Now the fun part.
            // Getting the cast members, directors, ratings
            int row = 1;
            ObservableList<Node> allChildren = results.getChildren();
            ArrayList<Node> children = new ArrayList<>();

            // workaround where allChildren updates as you enter the children
            for (Node node : allChildren)
                children.add(node);

            for (i = 6; i < children.size(); i+=3)
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
                results.add(scrollPane, 4, row);
                rs.close();

                // --------------- END CAST MEMBER CODE ----------------------
                // --------------- DIRECTOR CODE -----------------------------

                String directorQuery = "select concat(P.first_name, ' ', P.last_name) " +
                        "from p320_02.person P, p320_02.movie_director D " +
                        "where P.person_id = D.director_id and title = '" +
                        label.getText() + "';";
                rs = ConnectionManager.sendQuery(directorQuery);
                rs.next();

                results.add(new Label(rs.getString(1)), 3, row);
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
                    results.add(new Label("N/A"), 5, row);
                else
                    results.add(new Label(rs.getString(1)), 5, row);
                rs.close();
                // --------------- END USR RATING CODE -----------------------
                // --------------- ADDING PLAY BUTTON ------------------------
                /*
                Button playButton = new Button("Play");
                playButton.setOnAction(buttonEvent -> {
                    String getTodayDate = "select last_access from user where username = " +user + ";";
                });
                */

                row++;
            }


            // Handle other types of sort
            if (studioFilter.isSelected())
            {
                // Sorting by the genre
                TreeSet<Node[]> tree = new TreeSet<>((o1, o2) -> {
                    String studio1 = ((Label)o1[6]).getText();
                    String studio2 = ((Label)o2[6]).getText();
                    return studio1.compareTo(studio2);
                });

                ObservableList<Node> unsortedChildren = results.getChildren();

                // get each row

                for (i = 6; i < unsortedChildren.size(); i+=6)
                {
                    String studio = null;
                    try {
                        if (unsortedChildren.get(i) instanceof ScrollPane)
                            break;

                        String title = ((Label)unsortedChildren.get(i)).getText();
                        String tempQuery = "select T.name " +
                                "from p320_02.studio T, p320_02.movie M, p320_02.studio_movie S " +
                                "where (M.title like '%" + title +
                                "%') and T.studio_id = S.studio_id and S.title = m.title ";
                        rs = ConnectionManager.sendQuery(tempQuery);
                        rs.next();

                        studio = rs.getString(1).split(",")[0].trim();

                        rs.close();
                    } catch (SQLException sqle) {
                        sqle.printStackTrace();
                    }

                    Node[] rowToBeSorted = {unsortedChildren.get(i),
                            unsortedChildren.get(i + 1),
                            unsortedChildren.get(i + 2),
                            unsortedChildren.get(((unsortedChildren.size() / 6) * 3) + 4 + (i - 6)),
                            unsortedChildren.get(((unsortedChildren.size() / 6) * 3) + 3 + (i - 6)),
                            unsortedChildren.get(((unsortedChildren.size() / 6) * 3) + 5 + (i - 6)),
                            new Label(studio)};

                    tree.add(rowToBeSorted);
                }

                // Prepare to re-add them to the table!
                // I know. This isn't ideal, but I'm on a time crunch.
                results.getChildren().clear();
                results.add(titleLabel,0, 0);
                results.add(runtimeLabel, 1, 0);
                results.add(ratingLabel, 2, 0);
                results.add(directorLabel, 3, 0);
                results.add(castMemberLabel, 4, 0);
                results.add(userRatingLabel, 5, 0);
                i = 1;

                Iterator<Node[]> it = tree.iterator();
                while(it.hasNext()) {
                    Node[] newRow = it.next();
                    results.add(newRow[0], 0, i);
                    results.add(newRow[1], 1, i);
                    results.add(newRow[2], 2, i);
                    results.add(newRow[3], 3, i);
                    results.add(newRow[4], 4, i);
                    results.add(newRow[5], 5, i);
                    i++;
                }
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println("There was an error parsing the SQL statement");
        }
    }
}
