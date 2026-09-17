package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private Button aboutBtn;
    @FXML private Button helpBtn;
    @FXML private Button registerBtn;
    @FXML private Button loginBtn;
    @FXML private Hyperlink registerLink;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleRegister() {
        SceneManager.switchScene("WelcomeView.fxml", "Mimo Platform - Welcome");
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
        alert.setHeaderText("Login Assistance");
        alert.setContentText("• Enter your registered email/username and password.\n" +
                "• If you don't have an account yet, click 'Register' at the top right or bottom link.");
        alert.showAndWait();
    }

    // Direct MySQL Authentication
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please enter both your email/username and password.");
            return;
        }

        String query = "SELECT * FROM User_table WHERE Email = ? AND Password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("User_ID"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("Profile"),
                        rs.getInt("XP"),
                        rs.getInt("Streak"),
                        rs.getInt("coin"),
                        rs.getInt("brain_count")
                );

                UserSession.initSession(user);
                SceneManager.switchScene("MainLayoutView.fxml", "Mimo Platform - Dashboard");
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to reach database.");
        }
    }

    // Direct Google Auth Flow using local MySQL
    @FXML
    private void handleGoogleLogin() {
        GoogleAuthService.startGoogleLogin(authCode -> {
            GoogleAuthService.fetchUserProfile(authCode, (userId, name, email) -> {
                User user = new User(userId, email, name, "", 0, 0, 0, 0);
                Platform.runLater(() -> {
                    UserSession.initSession(user);
                    SceneManager.switchScene("MainLayoutView.fxml");
                });
            });
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}