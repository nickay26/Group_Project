package Project;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    // ============================================
    // TOP NAVIGATION BUTTONS
    // ============================================
    @FXML private Button aboutBtn;
    @FXML private Button helpBtn;
    @FXML private Button registerNavBtn;   // ← FXML မှာ registerNavBtn ဖြစ်လို့

    // ============================================
    // LOGIN FORM CONTROLS
    // ============================================
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberCheck;
    @FXML private Hyperlink forgotLink;
    @FXML private Button loginBtn;
    @FXML private Button googleBtn;
    // Floating Labels
    @FXML private Label emailFloatLabel;
    @FXML private Label passwordFloatLabel;
    // ============================================
    // NAVIGATION HANDLERS
    // ============================================
    @FXML
    public void initialize() {
        setupFloatingLabel(emailField, emailFloatLabel);
        setupFloatingLabel(passwordField, passwordFloatLabel);
    }
    private void setupFloatingLabel(TextField field, Label label) {
        // Focus listener — cursor ချလိုက်ရင် label ပေါ်
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            updateFloatingLabel(field, label, isFocused);
        });
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            updateFloatingLabel(field, label,field.isFocused());
        });
    }
    private void updateFloatingLabel(TextField field, Label label, boolean focused) {
        boolean hasText = !field.getText().isEmpty();
        boolean showLabel = focused || hasText;

        if (showLabel) {
            if (!label.getStyleClass().contains("floating-label-visible")) {
                label.getStyleClass().add("floating-label-visible");
            }
        } else {
            label.getStyleClass().remove("floating-label-visible");
        }

    if (focused || hasText) {
        field.setStyle("-fx-prompt-text-fill: transparent;");
    } else {
        field.setStyle("-fx-prompt-text-fill: rgba(255, 255, 255, 0.55);");
    }
}
    /** Register Button (Top Right) နှိပ်ရင် Welcome Page ကို သွား */
    @FXML
    private void handleRegister() {
        SceneManager.switchScene("WelcomeView.fxml", "Mimo Platform - Welcome");
    }

    /** About Dialog ဖွင့် */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Mimo Platform");
        alert.setHeaderText("Mimo-style Gamified Coding App");
        alert.setContentText("Learn Java, C++, and Python through interactive micro-lessons, " +
                "live execution, and instant AI code reviews!");
        alert.showAndWait();
    }

    /** Help Dialog ဖွင့် */
    @FXML
    private void handleHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help & Support");
        alert.setHeaderText("Login Assistance");
        alert.setContentText("• Enter your registered email/username and password.\n" +
                "• If you don't have an account yet, click 'Register' at the top right.");
        alert.showAndWait();
    }

    // ============================================
    // LOGIN HANDLERS
    // ============================================

    /** Continue Button နှိပ်ရင် Login လုပ် */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Missing Fields");
            warning.setHeaderText(null);
            warning.setContentText("Please enter both your email/username and password.");
            warning.showAndWait();
            return;
        }

        // Email format စစ်
        if (!email.contains("@") && !email.matches("^[a-zA-Z0-9_]+$")) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Invalid Input");
            warning.setHeaderText(null);
            warning.setContentText("Please enter a valid email address or username.");
            warning.showAndWait();
            return;
        }

        // Password အနည်းဆုံး 6 လုံး စစ်
        if (password.length() < 6) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Weak Password");
            warning.setHeaderText(null);
            warning.setContentText("Password must be at least 6 characters long.");
            warning.showAndWait();
            return;
        }

        // Remember me စစ်
        boolean remember = rememberCheck != null && rememberCheck.isSelected();
        System.out.println("Remember me: " + remember);

        // Login လုပ်နေတဲ့ အခြေအနေ
        System.out.println("Attempting login for: " + email);

        // TODO: Future HTTP POST to Flask /api/login endpoint
        // အောင်မြင်ရင် Dashboard ကို သွား
        // SceneManager.switchScene("LearnPage.fxml", "Mimo Platform - Dashboard");
    }

    /** Google Button နှိပ်ရင် Google Login လုပ် */
    @FXML
    private void handleGoogleLogin() {
        System.out.println("Opening browser for Google Authentication...");

        GoogleAuthService.startGoogleLogin(authCode -> {
            System.out.println("Received Google Code: " + authCode);
            sendAuthCodeToFlask(authCode);
        });
    }

    /** Flask Backend ကို Auth Code ပို့ */
    private void sendAuthCodeToFlask(String code) {
        // TODO: HTTP POST request to your backend: /api/auth/google
        // Verified ဖြစ်ရင် Dashboard ကို သွား
        SceneManager.switchScene("LearnPage.fxml", "Mimo Platform - Dashboard");
    }

    // ============================================
    // OPTIONAL: Forgot Password Handler
    // ============================================

    /** Forgot Password Link နှိပ်ရင် */
    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reset Password");
        alert.setHeaderText("Password Reset");
        alert.setContentText("Please contact admin or check your email for reset instructions.");
        alert.showAndWait();
    }
}