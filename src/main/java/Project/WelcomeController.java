package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WelcomeController {

    @FXML private Button aboutBtn;
    @FXML private Button helpBtn;
    @FXML private Button loginBtn;
    @FXML private Button signUpButton;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

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

    // Manual Form Registration in MySQL
    @FXML
    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Form Incomplete", "Please fill in all fields to complete registration.");
            return;
        }

        String insertSql = "INSERT INTO User_table (Name, Email, Password, last_login_date) VALUES (?, ?, ?, CURRENT_DATE())";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int newUserId = keys.getInt(1);
                User newUser = new User(newUserId, username, email, null, 0, 0, 0, 5);
                UserSession.initSession(newUser);

                SceneManager.switchScene("MainLayoutView.fxml", "Mimo Platform - Dashboard");
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate email error code
                showAlert(Alert.AlertType.ERROR, "Registration Error", "This email is already registered.");
            } else {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to local database.");
            }
        }
    }

    // Google Authentication Flow using direct MySQL Sync
    @FXML
    private void handleGoogleSignUp() {
        System.out.println("Opening browser for Google Registration...");

        GoogleAuthService.startGoogleLogin(authCode -> {
            System.out.println("Received Google Code: " + authCode);

            GoogleAuthService.fetchUserProfile(authCode, (userId, name, email) -> {
                System.out.println("Authenticated Real User: " + email + " (" + name + ")");

                User user = new User(userId, email, name, "", 0, 0, 0, 0);

                Platform.runLater(() -> {
                    UserSession.initSession(user);
                    SceneManager.switchScene("MainLayoutView.fxml");
                });
            });
        });
    }

    private void navigateToDetail(String title, String subtitle, String details, String codeSample) {
        FXMLLoader loader = SceneManager.switchScene("LanguageDetailView.fxml", "Mimo Platform - " + title + " Overview");

        if (loader != null) {
            LanguageDetailController controller = loader.getController();
            controller.setLanguageData(title, subtitle, details, codeSample);
        }
    }

    @FXML
    private void handleJavaCard() {
        navigateToDetail(
                "Java",
                "Strongly Typed & Object-Oriented",
                "Java powers backend enterprise systems, Android applications, and high-scale infrastructure. Learn core OOP principles, memory management, and class design.",
                "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, Mimo!\");\n    }\n}"
        );
    }

    @FXML
    private void handleCppCard() {
        navigateToDetail(
                "C++",
                "High-Performance System Programming",
                "C++ provides low-level memory access, high-speed execution, and full manual control over system hardware.",
                "#include <iostream>\n\nint main() {\n    std::cout << \"Hello, Mimo!\" << std::endl;\n    return 0;\n}"
        );
    }

    @FXML
    private void handlePythonCard() {
        navigateToDetail(
                "Python",
                "Beginner-Friendly & Powerful",
                "Python features clean syntax optimized for rapid development, data science, machine learning, and automation scripts.",
                "def greet():\n    print(\"Hello, Mimo!\")\n\ngreet()"
        );
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}