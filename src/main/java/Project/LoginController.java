package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private Button aboutBtn;
    @FXML private Button helpBtn;
    @FXML private Button registerBtn;
    @FXML private Button loginBtn;
    @FXML private Hyperlink registerLink;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // Switch back to Welcome / Register View
    @FXML
    private void handleRegister() {
        SceneManager.switchScene("WelcomeView.fxml", "Mimo Platform - Welcome");
    }

    // Open About Dialog
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Mimo Platform");
        alert.setHeaderText("Mimo-style Gamified Coding App");
        alert.setContentText("Learn Java, C++, and Python through interactive micro-lessons, live execution, and instant AI code reviews!");
        alert.showAndWait();
    }

    // Open Help Dialog
    @FXML
    private void handleHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help & Support");
        alert.setHeaderText("Login Assistance");
        alert.setContentText("• Enter your registered email/username and password.\n" +
                "• If you don't have an account yet, click 'Register' at the top right or bottom link.");
        alert.showAndWait();
    }

    // Process User Authentication
    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Missing Fields");
            warning.setHeaderText(null);
            warning.setContentText("Please enter both your email/username and password.");
            warning.showAndWait();
            return;
        }

        System.out.println("Attempting login for: " + email);
        // Future HTTP POST to Flask /api/login endpoint
    }
}