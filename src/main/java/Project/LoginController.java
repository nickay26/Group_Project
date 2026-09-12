package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginBtn;

    @FXML
    private Hyperlink registerLink;

    @FXML
    public void initialize() {
        loginBtn.setOnAction(event -> handleLogin());
        registerLink.setOnAction(event -> handleNavigateToRegister());
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill in all fields.");
            return;
        }

        System.out.println("Attempting login for: " + email);
        // Endpoint call to Flask /api/login goes here
    }

    private void handleNavigateToRegister() {
        System.out.println("Switching to Register view...");
    }
}