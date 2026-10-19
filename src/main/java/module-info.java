module com.example.group_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.net.http;
    requires jdk.httpserver;
    requires java.desktop;

    requires com.google.gson;
    requires java.sql;
    requires org.controlsfx.controls;
    requires org.json;
    requires javafx.swing;
    requires javafx.web;
    requires jdk.jsobject;
    requires mysql.connector.j;
    requires java.mail;

    // Gson နှင့် JavaFX FXML နှစ်ခုစလုံးကို Open လုပ်ပေးရန် ပြင်ဆင်ထားပါသည်
    opens Project to javafx.fxml, com.google.gson;

    exports Project;
    exports Project.API_Database;
    opens Project.API_Database to com.google.gson, javafx.fxml;
    exports Project.AdminClasses;
    opens Project.AdminClasses to com.google.gson, javafx.fxml;
    exports Project.Helpers;
    opens Project.Helpers to com.google.gson, javafx.fxml;
    exports Project.GameClasses;
    opens Project.GameClasses to com.google.gson, javafx.fxml;
    exports Project.Controllers;
    opens Project.Controllers to com.google.gson, javafx.fxml;
    exports Project.Getters_Setters;
    opens Project.Getters_Setters to com.google.gson, javafx.fxml;
}