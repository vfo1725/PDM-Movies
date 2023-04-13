module com.g2.movieverse {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.sql;
    requires jsch;


    opens com.g2.movieverse to javafx.fxml;
    exports com.g2.movieverse;
}