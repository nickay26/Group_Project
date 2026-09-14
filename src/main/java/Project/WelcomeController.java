package Project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class WelcomeController {

    @FXML private Button aboutBtn;
    @FXML private Button helpBtn;
    @FXML private Button loginBtn;
    @FXML private Button signUpButton;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // Switch to Login View
    @FXML
    private void handleLogin() {
        SceneManager.switchScene("LoginView.fxml", "Mimo Platform - Login");
    }

    // Open About Information Dialog
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Mimo Platform");
        alert.setHeaderText("Mimo-style Gamified Coding App");
        alert.setContentText("Learn Java, C++, and Python through interactive micro-lessons, live execution, and instant AI code reviews!");
        alert.showAndWait();
    }

    // Open Help & Guidance Dialog
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

    // Handle Form Submission
    @FXML
    private void handleSignUp() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Form Incomplete");
            warning.setHeaderText(null);
            warning.setContentText("Please fill in all fields to complete registration.");
            warning.showAndWait();
            return;
        }

        System.out.println("Registering: " + username + " (" + email + ")");
        // Future HTTP POST to /api/register
    }

    @FXML
    private void handleGoogleSignUp() {
        System.out.println("Opening browser for Google Registration...");

        GoogleAuthService.startGoogleLogin(authCode -> {
            System.out.println("Received Google Code (Register): " + authCode);

            // POST code to Flask backend to verify and create account in platform.db
            sendAuthCodeToFlask(authCode);
        });
    }

    private void sendAuthCodeToFlask(String authCode) {
        System.out.println("Testing Flask authentication with code: " + authCode);

        // Send code to backend
        boolean success = ApiClient.sendGoogleAuthCode(authCode);

        if (success) {
            UserSession session = UserSession.getInstance();

            // Success dialog verifying data from platform.db
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Google Auth Test Passed!");
            alert.setHeaderText("Successfully Logged In / Registered!");
            alert.setContentText(String.format(
                    "User Data Saved in Session:\n\n" +
                            "• User ID: %d\n" +
                            "• Name: %s\n" +
                            "• Email: %s",
                    session.getUserId(), session.getName(), session.getEmail()
            ));
            alert.showAndWait();

        } else {
            // Failure dialog if backend/Google verification fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Auth Test Failed");
            alert.setHeaderText("Google Authentication Failed");
            alert.setContentText("Could not exchange authorization code with Flask API. Check server logs.");
            alert.showAndWait();
        }
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
    }