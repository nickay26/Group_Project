package Project;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Form field တိုင်းကို Floating Animation ချိတ်ဆက်ခြင်း
        setupFloatingLabel(usernameField, usernameLabel, "Username", "Enter Username");
        setupFloatingLabel(emailField, emailLabel, "Email", "Enter Email");
        setupFloatingLabel(passwordField, passwordLabel, "Password", "Enter Password");
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
        SceneManager.switchScene("LoginView.fxml", "Mimo Platform - Login");
    }

    @FXML
    private void handleAdmin() {
        SceneManager.switchScene("AdminLoginView.fxml", "Mimo Platform - Admin Login");
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Mimo Platform");
        alert.setHeaderText("Mimo-style Gamified Coding App");
        alert.setContentText("Learn Java, C++, and Python through interactive micro-lessons, live execution, and instant AI code reviews!");
        alert.showAndWait();
    }

    @FXML
    private void handleHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help & Support");
        alert.setHeaderText("Getting Started");
        alert.setContentText("• Register an account to track your learning progress.\n" +
                "• Click on Java, C++, or Python to preview language courses.\n" +
                "• Already have an account? Click Login at the top right.");
        alert.showAndWait();
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

        // DatabaseManager ရှိ API registerUser ကို တိုက်ရိုက် ခေါ်သုံးခြင်း
        User registeredUser = DatabaseManager.registerUser(username, email, password);

        if (registeredUser != null) {
            UserSession.initSession(registeredUser);
            SceneManager.switchScene("MainLayoutView.fxml", "Mimo Platform - Dashboard");
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
                    User googleUser = DatabaseManager.findOrCreateGoogleUser(email, name, null);

                    if (googleUser != null) {
                        UserSession.initSession(googleUser);
                        SceneManager.switchScene("MainLayoutView.fxml", "Mimo Platform - Dashboard");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Sync Error", "Error syncing Google login with server.");
                    }
                });
            });
        });
    }

    private void navigateToDetail(String title, String subtitle, String details, String[] lessonTitles, String[] lessonDescriptions) {
        FXMLLoader loader = SceneManager.switchScene("LanguageDetailView.fxml", "Mimo Platform - " + title + " Overview");

        if (loader != null) {
            LanguageDetailController controller = loader.getController();
            controller.setLanguageData(title, subtitle, details, lessonTitles, lessonDescriptions);
        }
    }

    @FXML
    private void handleJavaCard() {
        SceneManager.switchScene("JavaOverviewView.fxml", "Mimo Platform - Java Overview");
    }

    @FXML
    private void handleCppCard() {
        SceneManager.switchScene("CppOverviewView.fxml", "Mimo Platform - C++ Overview");
    }

    @FXML
    private void handlePythonCard() {
        SceneManager.switchScene("PythonOverviewView.fxml", "Mimo Platform - Python Overview");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}