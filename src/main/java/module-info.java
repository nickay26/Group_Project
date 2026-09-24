module com.example.group_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires jdk.httpserver;
    requires java.desktop;

    requires com.google.gson;
    requires java.sql;
    requires org.controlsfx.controls;
    requires org.json;

    // Gson နှင့် JavaFX FXML နှစ်ခုစလုံးကို Open လုပ်ပေးရန် ပြင်ဆင်ထားပါသည်
    opens Project to javafx.fxml, com.google.gson;

    exports Project;
}