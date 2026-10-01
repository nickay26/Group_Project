package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    // ============================================
    // TOP NAVIGATION BUTTONS
    // ============================================
    @FXML private Button aboutBtn;
    @FXML private Button helpBtn;
    @FXML private Button registerNavBtn;

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
    // INITIALIZE & FLOATING LABELS
    // ============================================
    @FXML
    public void initialize() {
        setupFloatingLabel(emailField, emailFloatLabel);
        setupFloatingLabel(passwordField, passwordFloatLabel);
    }

    private void setupFloatingLabel(TextField field, Label label) {
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            updateFloatingLabel(field, label, isFocused);
        });
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            updateFloatingLabel(field, label, field.isFocused());
        });
    }

    private void updateFloatingLabel(TextField field, Label label, boolean focused) {
        boolean hasText = field.getText() != null && !field.getText().isEmpty();
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

    // ============================================
    // NAVIGATION HANDLERS
    // ============================================

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

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                    "Please enter both your email/username and password.");
            return;
        }

        if (!email.contains("@") && !email.matches("^[a-zA-Z0-9_]+$")) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input",
                    "Please enter a valid email address or username.");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Weak Password",
                    "Password must be at least 6 characters long.");
            return;
        }

        boolean remember = rememberCheck != null && rememberCheck.isSelected();
        System.out.println("Remember me: " + remember);
        System.out.println("Attempting login for: " + email);

        // Database မှ Authenticate လုပ်ခြင်း
        User user = DatabaseManager.authenticateUser(email, password);

        if (user != null) {
            UserSession.initSession(user);
            System.out.println("Login successful for User ID: " + user.getUserId());

            // ⭐ PREMIUM CHECK
            boolean serverPremium = DatabaseManager.isPremium(user.getUserId());
            System.out.println("Server premium check: " + serverPremium);
            if (serverPremium) {
                user.setPremium(true);
                user.setSubscriptionType("monthly");
                UserSession.updateUser(user);
                System.out.println("✅ Local user updated to PREMIUM");
            } else {
                System.out.println("❌ Server says NOT premium");
            }

            navigateToNextScene();
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed",
                    "Invalid email/username or password. Please try again.");
        }
    }

    /** Google Button နှိပ်ရင် Google Login လုပ် */
    @FXML
    private void handleGoogleLogin() {
        System.out.println("Opening browser for Google Authentication...");

        GoogleAuthService.startGoogleLogin(authCode -> {
            GoogleAuthService.fetchUserProfile(authCode, (userIdFromAuth, email, name) -> {

                User dbUser = DatabaseManager.findOrCreateGoogleUser(name, email, null);

                if (dbUser != null) {
                    Platform.runLater(() -> {
                        UserSession.initSession(dbUser);
                        System.out.println("Session started for Google User ID: " + dbUser.getUserId());

                        // ⭐ PREMIUM CHECK (ဒီနေရာမှာ ထည့်)
                        boolean serverPremium = DatabaseManager.isPremium(dbUser.getUserId());
                        System.out.println("Server premium check: " + serverPremium);
                        if (serverPremium) {
                            dbUser.setPremium(true);
                            dbUser.setSubscriptionType("monthly");
                            UserSession.updateUser(dbUser);
                            System.out.println("✅ Local user updated to PREMIUM");
                        } else {
                            System.out.println("❌ Server says NOT premium");
                        }

                        sendAuthCodeToFlask(authCode);
                        navigateToNextScene();
                    });
                } else {
                    System.err.println("Failed to sync Google user with local database.");
                }
            });
        });
    }

    /**
     * Dashboard (MainLayoutView) သို့ သွားမည်။
     * MainLayoutController က redirectedFromOverview Flag ပေါ်မူတည်ပြီး Learn သို့မဟုတ် Courses Page ကို အလိုအလျောက် ရွေးဖွင့်ပါမည်။
     */
    private void navigateToNextScene() {
        SceneManager.switchScene("MainLayoutView.fxml", "Mimo Platform - Dashboard");
    }

    /** Flask Backend ကို Auth Code ပို့ */
    private void sendAuthCodeToFlask(String code) {
        // TODO: HTTP POST request to your backend: /api/auth/google
    }

    // ============================================
    // OPTIONAL: Forgot Password Handler & Helper
    // ============================================

    @FXML
    private void handleForgotPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Reset Password", "Please contact admin or check your email for reset instructions.");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}