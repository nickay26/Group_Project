module com.example.group_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;


    opens Project to javafx.fxml;
    exports Project;
}