module com.example.group_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.desktop;
    requires jdk.httpserver;
    requires java.sql;


    opens Project to javafx.fxml;
    exports Project;
}