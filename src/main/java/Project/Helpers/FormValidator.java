package Project.Helpers;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class FormValidator {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    // Validate Email format
    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    // Validate Password length (minimum 6 characters)
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Setup live email validation listener
    public static void setupEmailValidation(TextField emailField, Label errorLabel) {
        if (emailField == null || errorLabel == null) return;

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                errorLabel.setText("⚠️ Email is required.");
                errorLabel.setVisible(true);
                emailField.setStyle("-fx-border-color: #EF4444; -fx-background-color: #1E293B; -fx-text-fill: white;");
            } else if (!isValidEmail(newValue)) {
                errorLabel.setText("❌ Invalid email format (e.g., user@gmail.com)");
                errorLabel.setVisible(true);
                emailField.setStyle("-fx-border-color: #EF4444; -fx-background-color: #1E293B; -fx-text-fill: white;");
            } else {
                errorLabel.setVisible(false);
                emailField.setStyle("-fx-border-color: #10B981; -fx-background-color: #1E293B; -fx-text-fill: white;");
            }
        });
    }

    // Setup live password validation listener
    public static void setupPasswordValidation(PasswordField passwordField, Label errorLabel) {
        if (passwordField == null || errorLabel == null) return;

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.length() < 6) {
                errorLabel.setText("⚠️ Password must be at least 6 characters.");
                errorLabel.setVisible(true);
                passwordField.setStyle("-fx-border-color: #EF4444; -fx-background-color: #1E293B; -fx-text-fill: white;");
            } else {
                errorLabel.setVisible(false);
                passwordField.setStyle("-fx-border-color: #10B981; -fx-background-color: #1E293B; -fx-text-fill: white;");
            }
        });
    }
}