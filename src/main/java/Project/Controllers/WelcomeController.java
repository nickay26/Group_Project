package Project.Controllers;

import Project.API_Database.DatabaseManager;
import Project.Helpers.*;
import Project.Getters_Setters.User;
import Project.Getters_Setters.UserSession;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeController implements Initializable {

    @FXML private Button aboutBtn;
    @FXML private Button helpBtn;
    @FXML private Button loginBtn;
    @FXML private Button signUpButton;
    @FXML private Button adminBtn;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // Floating Label များအတွက် UI Variable များ
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label passwordLabel;

    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Form field တိုင်းကို Floating Animation ချိတ်ဆက်ခြင်း
        setupFloatingLabel(usernameField, usernameLabel, "Username", "Enter Username");
        setupFloatingLabel(emailField, emailLabel, "Email", "Enter Email");
        setupFloatingLabel(passwordField, passwordLabel, "Password", "Enter Password");

        FormValidator.setupEmailValidation(emailField, emailErrorLabel);
        FormValidator.setupPasswordValidation(passwordField, passwordErrorLabel);
    }

    // Pointer ထောက်လိုက်လျှင် စာသား အပေါ်သို့ ရောက်သွားစေမည့် Helper Method
    private void setupFloatingLabel(javafx.scene.control.TextInputControl field, Label label, String floatTitle, String placeholderText) {
        if (field == null || label == null) return;

        label.setMouseTransparent(true);
        label.setText(placeholderText);
        label.setTranslateY(0);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-background-color: transparent;");

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused || !field.getText().isEmpty()) {
                TranslateTransition transition = new TranslateTransition(Duration.millis(200), label);
                transition.setToY(-26);
                transition.play();

                label.setText(floatTitle);
                label.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8; -fx-background-color: #0b132b; -fx-padding: 0 4px;");
            } else {
                TranslateTransition transition = new TranslateTransition(Duration.millis(200), label);
                transition.setToY(0);
                transition.play();

                label.setText(placeholderText);
                label.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-background-color: transparent;");
            }
        });
    }

    @FXML
    private void handleLogin() {
        SceneManager.switchScene("LoginView.fxml", "Sodexa Platform - Login");
    }

    @FXML
    private void handleAbout() {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        // Main Dark Container
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(16);
        root.setPadding(new javafx.geometry.Insets(24));
        root.setStyle(
                "-fx-background-color: #0b1329;" +
                        "-fx-border-color: #1e3a8a;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-background-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 25, 0, 0, 10);"
        );
        root.setPrefWidth(620);

        // Header Title
        Label titleLabel = new Label("🚀 Sodex Platform - About");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label subTitleLabel = new Label("Gamified Interactive Coding & Multi-Track Learning Platform");
        subTitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        javafx.scene.layout.VBox headerBox = new javafx.scene.layout.VBox(4, titleLabel, subTitleLabel);

        // Feature Cards Container
        javafx.scene.layout.VBox featuresBox = new javafx.scene.layout.VBox(10);

        featuresBox.getChildren().addAll(
                createFeatureCard(
                        "🎓 Courses Track",
                        "Standard login or registration lands directly on the Courses page to let users freely browse and select any available course (Java, C++, Python)."
                ),
                createFeatureCard(
                        "📚 Learn Path",
                        "If a user selects a course from the Welcome/Overview screen before logging in or registering, they are navigated directly to that course's Learn page (e.g., C++ Basic)."
                ),
                createFeatureCard(
                        "💻 Playground",
                        "Create new coding projects for your preferred language (C++, Java, Python), write custom scripts, and run/execute code live."
                ),
                createFeatureCard(
                        "🏆 Leaderboard & Leagues",
                        "Competitions are divided into 3 League Levels based on XP. Top 1-5 performers within a week advance to the next higher level!"
                ),
                createFeatureCard(
                        "🎯 Practice",
                        "Practice targeted topics divided into 3 sub-categories per course (e.g., Variables, Loops, Classes) to solidify your skills."
                ),
                createFeatureCard(
                        "🔄 Review",
                        "Easily revisit and review all your saved notes, code snippets, and previous lesson progress in one organized view."
                )
        );

        // Close Button
        Button closeBtn = new Button("Close");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10 0;" +
                        "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(headerBox, featuresBox, closeBtn);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // Helper method to build custom dark feature cards
    private javafx.scene.layout.HBox createFeatureCard(String title, String desc) {
        javafx.scene.layout.HBox card = new javafx.scene.layout.HBox(12);
        card.setPadding(new javafx.geometry.Insets(10, 14, 10, 14));
        card.setStyle(
                "-fx-background-color: #15203b;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: #24345d;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-border-width: 1px;"
        );

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 13px;");
        titleLbl.setMinWidth(170);

        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        descLbl.setWrapText(true);

        card.getChildren().addAll(titleLbl, descLbl);
        javafx.scene.layout.HBox.setHgrow(descLbl, javafx.scene.layout.Priority.ALWAYS);

        return card;
    }

    @FXML
    private void handleHelp() {
        String message = "Getting Started\n\n" +
                "• Register an account to track your learning progress.\n" +
                "• Click on Java, C++, or Python to preview language courses.\n" +
                "• Already have an account? Click Login at the top right.";

        // Assuming DialogUtils is imported
        DialogUtils.showMessageDialog("Help & Support", message, false);
    }

    @FXML
    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Form Incomplete", "Please fill in all fields to complete registration.");
            return;
        }

        if (!ValidationUtils.isValidUsername(username)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Username", "Username must be at least 3 characters long.");
            usernameField.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email address (e.g., user@example.com).");
            emailField.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            showAlert(Alert.AlertType.WARNING, "Weak Password", "Password must be at least 6 characters long.");
            passwordField.requestFocus();
            return;
        }

        User registeredUser = DatabaseManager.registerUser(username, email, password);

        if (registeredUser != null) {
            UserSession.initSession(registeredUser);
            SceneManager.switchScene("MainLayoutView.fxml", "Sodexa Platform");
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Failed to register. Email might already exist or network error.");
        }
    }

    @FXML
    private void handleGoogleSignUp() {
        System.out.println("Opening browser for Google Registration...");

        GoogleAuthService.startGoogleLogin(authCode -> {
            System.out.println("Received Google Code: " + authCode);

            GoogleAuthService.fetchUserProfile(authCode, (googleId, name, email) -> {
                System.out.println("Authenticated Real User: " + email + " (" + name + ")");

                Platform.runLater(() -> {
                    // DatabaseManager ရှိ Google API Endpoint ကို တိုက်ရိုက် ခေါ်ယူခြင်း
                    User googleUser = DatabaseManager.findOrCreateGoogleUser(name, email, null);

                    if (googleUser != null) {
                        UserSession.initSession(googleUser);

                        // Google Sign-Up အောင်မြင်ပြီးနောက် Overview မှ လာလျှင် MainLayout (Learn Screen) သို့ ပို့ပေးခြင်း
                        if (UserSession.isRedirectedFromOverview()) {
                            SceneManager.switchScene("MainLayoutView.fxml", "Sodexa Platform");
                        } else {
                            SceneManager.switchScene("MainLayoutView.fxml", "Sodexa Platform");
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Sync Error", "Error syncing Google login with server.");
                    }
                });
            });
        });
    }

    private void navigateToDetail(String title, String subtitle, String details, String[] lessonTitles, String[] lessonDescriptions) {
        FXMLLoader loader = SceneManager.switchScene("LanguageDetailView.fxml", "Sodexa Platform - " + title + " Overview");

        if (loader != null) {
            LanguageDetailController controller = loader.getController();
            controller.setLanguageData(title, subtitle, details, lessonTitles, lessonDescriptions);
        }
    }

    @FXML
    private void handleJavaCard() {
        SceneManager.switchScene("JavaOverviewView.fxml", "Sodexa Platform - Java Overview");
    }

    @FXML
    private void handleCppCard() {
        SceneManager.switchScene("CppOverviewView.fxml", "Sodexa Platform - C++ Overview");
    }

    @FXML
    private void handlePythonCard() {
        SceneManager.switchScene("PythonOverviewView.fxml", "Sodexa Platform - Python Overview");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}