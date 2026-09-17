module com.example.group_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires jdk.httpserver;
    requires java.desktop;

    // MySQL နှင့် ControlsFX အတွက် ထပ်ဖြည့်ရန်
    requires java.sql;
    requires org.controlsfx.controls;

    opens Project to javafx.fxml;
    exports Project;
}