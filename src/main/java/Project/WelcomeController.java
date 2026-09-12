package Project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class WelcomeController extends BorderPane {

    public WelcomeController() {
        this.setPadding(new Insets(20));

        // 1. TOP NAVIGATION BAR
        HBox navBar = new HBox(25);
        navBar.setAlignment(Pos.TOP_RIGHT);
        navBar.setPadding(new Insets(10, 40, 20, 10));

        Label aboutBtn = new Label("About");
        Label helpBtn = new Label("Help");
        Label loginBtn = new Label("Login");

        navBar.getChildren().addAll(aboutBtn, helpBtn, loginBtn);
        this.setTop(navBar);

        // 2. CENTER CONTENT CONTAINER
        VBox centerContainer = new VBox(40);
        centerContainer.setAlignment(Pos.CENTER);

        // Register Form Card (Structural Outline)
        VBox registerCard = createRegisterForm();

        // Language Cards Row (Structural Outlines)
        HBox languageCardsRow = new HBox(35);
        languageCardsRow.setAlignment(Pos.CENTER);

        VBox javaCard = createLanguageCard("Java", () -> onLanguageSelected("Java"));
        VBox cppCard = createLanguageCard("C++", () -> onLanguageSelected("C++"));
        VBox pythonCard = createLanguageCard("Python", () -> onLanguageSelected("Python"));

        languageCardsRow.getChildren().addAll(javaCard, cppCard, pythonCard);

        centerContainer.getChildren().addAll(registerCard, languageCardsRow);
        this.setCenter(centerContainer);
    }

    private VBox createRegisterForm() {
        VBox formBox = new VBox(12);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPrefSize(420, 260);
        formBox.setMaxSize(420, 260);
        formBox.setPadding(new Insets(20));

        // Outline border only (No background colors)
        formBox.setStyle("-fx-border-color: #000000; -fx-border-width: 1px; -fx-border-radius: 8px;");

        Label title = new Label("Register form");

        TextField nameField = new TextField();
        nameField.setPromptText("Username");
        nameField.setMaxWidth(300);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        Button submitBtn = new Button("Sign Up");

        formBox.getChildren().addAll(title, nameField, emailField, passwordField, submitBtn);
        return formBox;
    }

    private VBox createLanguageCard(String languageName, Runnable onClick) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(200, 200);

        // Outline border only (No background colors)
        card.setStyle("-fx-border-color: #000000; -fx-border-width: 1px; -fx-border-radius: 12px; -fx-cursor: hand;");

        Label label = new Label(languageName);
        card.getChildren().add(label);

        card.setOnMouseClicked(e -> onClick.run());

        return card;
    }

    private void onLanguageSelected(String language) {
        System.out.println("Language selected: " + language);
    }
}