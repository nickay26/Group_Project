package Project.Controllers;

import Project.API_Database.DatabaseManager;
import Project.Helpers.DialogUtils;
import Project.Helpers.GoogleAuthService;
import Project.Helpers.SceneManager;
import Project.Getters_Setters.User;
import Project.Getters_Setters.UserSession;
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

    // ⭐ Password ပြသရန် အသစ်ထည့်သွင်းထားသော Controls များ
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordBtn;

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

        // ⭐ visiblePasswordField အတွက်ပါ ထည့်ပါ (Floating Label အတွက်)
        setupFloatingLabel(visiblePasswordField, passwordFloatLabel);

        // ⭐ PasswordField နဲ့ visiblePasswordField ရဲ့ စာသားတွေကို အတူတူဖြစ်နေအောင် ချိတ်ဆက်ခြင်း
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // ⭐ မျက်လုံးကို နှိပ်ထားရင် (Press) Password ပြမယ်
        togglePasswordBtn.setOnMousePressed(event -> {
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);

            // Focus ကို ရွှေ့ပေးပြီး Cursor ကို အဆုံးမှာထားမယ်
            visiblePasswordField.requestFocus();
            visiblePasswordField.positionCaret(visiblePasswordField.getText().length());

            togglePasswordBtn.setText("🙈"); // မျက်လုံးပိတ်ပုံပြောင်းမယ်
        });

        // ⭐ မျက်လုံးကို လွှတ်လိုက်ရင် (Release) ပြန်ဝှက်မယ်
        togglePasswordBtn.setOnMouseReleased(event -> {
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            // Focus ကို ပြန်ရွှေ့ပေးမယ်
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());

            togglePasswordBtn.setText("👁"); // မျက်လုံးပုံ ပြန်ထားမယ်
        });
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
        SceneManager.switchScene("WelcomeView.fxml", "Sodexa Platform - Welcome");
    }

    @FXML
    private void handleAbout() {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        // Main Dark Container
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(16);
        root.setPadding(new javafx.geometry.Insets(24));
        root.setStyle(
                "-fx-background-color: #0b1329;" +
                        "-fx-border-color: #1e3a8a;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-background-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 25, 0, 0, 10);"
        );
        root.setPrefWidth(620);

        // Header Title
        Label titleLabel = new Label("🚀 Sodex Platform - About");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label subTitleLabel = new Label("Gamified Interactive Coding & Multi-Track Learning Platform");
        subTitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        javafx.scene.layout.VBox headerBox = new javafx.scene.layout.VBox(4, titleLabel, subTitleLabel);

        // Feature Cards Container
        javafx.scene.layout.VBox featuresBox = new javafx.scene.layout.VBox(10);

        featuresBox.getChildren().addAll(
                createFeatureCard(
                        "🎓 Courses Track",
                        "Standard login or registration lands directly on the Courses page to let users freely browse and select any available course (Java, C++, Python)."
                ),
                createFeatureCard(
                        "📚 Learn Path",
                        "If a user selects a course from the Welcome/Overview screen before logging in or registering, they are navigated directly to that course's Learn page (e.g., C++ Basic)."
                ),
                createFeatureCard(
                        "💻 Playground",
                        "Create new coding projects for your preferred language (C++, Java, Python), write custom scripts, and run/execute code live."
                ),
                createFeatureCard(
                        "🏆 Leaderboard & Leagues",
                        "Competitions are divided into 3 League Levels based on XP. Top 1-5 performers within a week advance to the next higher level!"
                ),
                createFeatureCard(
                        "🎯 Practice",
                        "Practice targeted topics divided into 3 sub-categories per course (e.g., Variables, Loops, Classes) to solidify your skills."
                ),
                createFeatureCard(
                        "🔄 Review",
                        "Easily revisit and review all your saved notes, code snippets, and previous lesson progress in one organized view."
                )
        );

        // Close Button
        Button closeBtn = new Button("Close");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10 0;" +
                        "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(headerBox, featuresBox, closeBtn);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // Helper method to build custom dark feature cards
    private javafx.scene.layout.HBox createFeatureCard(String title, String desc) {
        javafx.scene.layout.HBox card = new javafx.scene.layout.HBox(12);
        card.setPadding(new javafx.geometry.Insets(10, 14, 10, 14));
        card.setStyle(
                "-fx-background-color: #15203b;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: #24345d;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-border-width: 1px;"
        );

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 13px;");
        titleLbl.setMinWidth(170);

        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        descLbl.setWrapText(true);

        card.getChildren().addAll(titleLbl, descLbl);
        javafx.scene.layout.HBox.setHgrow(descLbl, javafx.scene.layout.Priority.ALWAYS);

        return card;
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
        String password = passwordField.getText().trim(); // PasswordField ကနေ ယူလို့ရပါတယ်

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

            // ⭐ ADMIN CHECK (Admin Email ဖြစ်ရင် Admin Dashboard ကို ပို့မယ်)
            if ("admin@gmail.com".equalsIgnoreCase(user.getEmail())) {
                System.out.println("Admin logged in. Redirecting to Admin Dashboard...");
                SceneManager.switchScene("AdminView.fxml", "Sodexa Platform - Admin Dashboard");
            } else {
                // ကျန်တဲ့ သာမန် User တွေ အတွက် ပုံမှန် Dashboard
                navigateToNextScene();
            }

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

                        // ⭐ PREMIUM CHECK
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

                        // ⭐ ADMIN CHECK (Google Login နဲ့ Admin ဝင်လာရင်လည်း စစ်မယ်)
                        if ("admin@gmail.com".equalsIgnoreCase(dbUser.getEmail())) {
                            System.out.println("Admin logged in via Google. Redirecting to Admin Dashboard...");
                            SceneManager.switchScene("AdminView.fxml", "Sodexa Platform - Admin Dashboard");
                        } else {
                            sendAuthCodeToFlask(authCode);
                            navigateToNextScene();
                        }
                    });
                } else {
                    System.err.println("Failed to sync Google user with local database.");
                }
            });
        });
    }

    /**
     * Dashboard (MainLayoutView) သို့ သွားမည်။
     */
    private void navigateToNextScene() {
        SceneManager.switchScene("MainLayoutView.fxml", "Sodexa Platform");
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
        DialogUtils.showForgotPasswordDialog();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}