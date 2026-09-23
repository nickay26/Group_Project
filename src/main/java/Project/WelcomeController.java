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
import java.util.ResourceBundle;

public class WelcomeController implements Initializable {

    @FXML private Button aboutBtn;
    @FXML private Button helpBtn;
    @FXML private Button loginBtn;
    @FXML private Button signUpButton;

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

    // Manual Form Registration via PythonAnywhere API
    @FXML
    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Form Incomplete", "Please fill in all fields to complete registration.");
            return;
        }

        // DatabaseManager မှတစ်ဆင့် Cloud REST API သို့ Register လုပ်ခြင်း
        User newUser = DatabaseManager.registerUser(username, email, password);

        if (newUser != null) {
            UserSession.initSession(newUser);
            SceneManager.switchScene("MainLayoutView.fxml", "Mimo Platform - Dashboard");
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Email is already registered or backend connection failed.");
        }
    }

    // Google Authentication Flow via Cloud API Sync
    @FXML
    private void handleGoogleSignUp() {
        System.out.println("Opening browser for Google Registration...");

        GoogleAuthService.startGoogleLogin(authCode -> {
            System.out.println("Received Google Code: " + authCode);

            GoogleAuthService.fetchUserProfile(authCode, (googleUserId, name, email) -> {
                System.out.println("Authenticated Real User: " + email + " (" + name + ")");

                // Cloud Database ထဲတွင် Google User ကို တိုက်ရိုက် Sync/Save လုပ်ခြင်း
                User user = DatabaseManager.findOrCreateGoogleUser(email, name, "");

                if (user != null) {
                    Platform.runLater(() -> {
                        UserSession.initSession(user);
                        SceneManager.switchScene("MainLayoutView.fxml", "Mimo Platform - Dashboard");
                    });
                } else {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Authentication Error", "Failed to sync Google user with cloud backend."));
                }
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
        String title = "Java";
        String subtitle = "Language Overview";
        String details = "🚀 INTRODUCTION\n" +
                "Java is a high-level, object-oriented programming language designed to build robust, secure, and scalable applications with minimal implementation dependencies.\n\n" +
                "🎯 USE FOR WHAT\n" +
                "• Enterprise Software & Backend Systems\n" +
                "• Android Mobile Application Development\n" +
                "• High-Scale Web Services & Cloud Architecture";

        String[] titles = {"Java Basic", "Java Intermediate"};
        String[] descriptions = {
                "Learn basic syntax, variables, primitive types, and control flow.",
                "Master Object-Oriented Programming (OOP), Classes, Inheritance, and Exceptions."
        };

        navigateToDetail(title, subtitle, details, titles, descriptions);
    }

    @FXML
    private void handleCppCard() {
        String title = "C++";
        String subtitle = "Language Overview";
        String details = "🚀 INTRODUCTION\n" +
                "C++ is a high-performance general-purpose programming language that provides low-level memory control, extreme speed, and direct hardware access.\n\n" +
                "🎯 USE FOR WHAT\n" +
                "• Game Engines & 3D Real-time Graphics\n" +
                "• Operating Systems & Low-level System Drivers\n" +
                "• High-Frequency Financial Trading & Embedded Systems";

        String[] titles = {"C++ Basic", "C++ Intermediate"};
        String[] descriptions = {
                "Understand C++ syntax, basic data types, condition statements, and loops.",
                "Master Pointers, Memory Management (Stack/Heap), References, and OOP concepts."
        };

        navigateToDetail(title, subtitle, details, titles, descriptions);
    }

    @FXML
    private void handlePythonCard() {
        String title = "Python";
        String subtitle = "Language Overview";
        String details = "🚀 INTRODUCTION\n" +
                "Python is a beginner-friendly, interpreted programming language known for its clean syntax, rapid development capabilities, and powerful libraries.\n\n" +
                "🎯 USE FOR WHAT\n" +
                "• Artificial Intelligence & Machine Learning\n" +
                "• Data Science, Analytics & Visualization\n" +
                "• Web Backend Services & Automation Scripts";

        String[] titles = {"Python Basic", "Python Intermediate"};
        String[] descriptions = {
                "Learn Python basics, variables, user input, and string formatting.",
                "Work with Data Structures (Lists, Dicts, Sets), Control Flow, and Functions."
        };

        navigateToDetail(title, subtitle, details, titles, descriptions);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}