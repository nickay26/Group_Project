package Project;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class DialogUtils {

    /**
     * General reusable method to show any key-value details in a sleek dark-themed dialog.
     */
    public static void showDetailsDialog(String title, String headerText, Map<String, String> details) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Dark theme background matching the app aesthetic
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #1E293B; " +
                        "-fx-text-fill: white;"
        );

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 25, 20, 25));

        int row = 0;
        for (Map.Entry<String, String> entry : details.entrySet()) {
            Label keyLabel = new Label(entry.getKey() + ":");
            keyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-weight: bold; -fx-font-size: 13px;");

            Label valLabel = new Label(entry.getValue() != null && !entry.getValue().equals("null") ? entry.getValue() : "N/A");
            valLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 13px; -fx-font-weight: bold;");

            grid.add(keyLabel, 0, row);
            grid.add(valLabel, 1, row);
            row++;
        }

        dialog.getDialogPane().setContent(grid);

        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        // Style the close button
        javafx.scene.Node closeButton = dialog.getDialogPane().lookupButton(closeButtonType);
        if (closeButton != null) {
            closeButton.setStyle(
                    "-fx-background-color: #3B82F6; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-background-radius: 6;"
            );
        }

        dialog.showAndWait();
    }

    public static void showSuccessDialog(String message) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
                "-fx-background-color: #0b1329;" +
                        "-fx-border-color: #10b981;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-background-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 25, 0, 0, 10);"
        );
        root.setPrefWidth(360);
        root.setAlignment(javafx.geometry.Pos.CENTER);

        Label iconLabel = new Label("✅");
        iconLabel.setStyle("-fx-font-size: 36px;");

        Label titleLabel = new Label("Success");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34d399;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        msgLabel.setWrapText(true);
        msgLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button okBtn = new Button("OK");
        okBtn.setMaxWidth(Double.MAX_VALUE);
        okBtn.setStyle(
                "-fx-background-color: #10b981;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 8 0;" +
                        "-fx-cursor: hand;"
        );
        okBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(iconLabel, titleLabel, msgLabel, okBtn);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void showErrorDialog(String message) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
                "-fx-background-color: #0b1329;" +
                        "-fx-border-color: #ef4444;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-background-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 25, 0, 0, 10);"
        );
        root.setPrefWidth(360);
        root.setAlignment(javafx.geometry.Pos.CENTER);

        Label iconLabel = new Label("❌");
        iconLabel.setStyle("-fx-font-size: 36px;");

        Label titleLabel = new Label("Error");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f87171;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        msgLabel.setWrapText(true);
        msgLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button okBtn = new Button("OK");
        okBtn.setMaxWidth(Double.MAX_VALUE);
        okBtn.setStyle(
                "-fx-background-color: #ef4444;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 8 0;" +
                        "-fx-cursor: hand;"
        );
        okBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(iconLabel, titleLabel, msgLabel, okBtn);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Specific convenience method to show User Details when a row is clicked.
     */
    public static void showUserDialog(JSONObject userJson) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("User ID", String.valueOf(userJson.opt("User_ID")));
        details.put("Name", userJson.optString("Name", "N/A"));
        details.put("Email", userJson.optString("Email", "N/A"));
        details.put("XP", String.valueOf(userJson.opt("XP")));
        details.put("Streak", String.valueOf(userJson.opt("Streak")));
        details.put("Coins", String.valueOf(userJson.opt("coin")));
        details.put("Brain Count", String.valueOf(userJson.opt("brain_count")));
        details.put("Plan Type", userJson.optString("plan_type", "FREE"));
        details.put("Role", userJson.optString("role", "USER"));
        details.put("Last Active", userJson.optString("last_login_date", "N/A"));

        showDetailsDialog("User Profile Details", "Account Overview", details);
    }

    public static void showCertificateDialog(String courseName, String userName, String issuedDate) {
        Stage certStage = new Stage();
        certStage.initModality(Modality.APPLICATION_MODAL);
        certStage.setTitle("Certificate of Completion");

        Label titleLabel = new Label("🏆 CERTIFICATE OF COMPLETION 🏆");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F59E0B;");

        Label descLabel = new Label("This is proudly presented to");
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");

        Label nameLabel = new Label(userName);
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label courseText = new Label("for successfully mastering the course\n" + courseName);
        courseText.setStyle("-fx-font-size: 14px; -fx-text-fill: #38BDF8; -fx-alignment: center;");
        courseText.setWrapText(true);

        Label dateLabel = new Label("Issued Date: " + issuedDate);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        Button closeBtn = new Button("Awesome!");
        closeBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8 20;");
        closeBtn.setOnAction(e -> certStage.close());

        VBox layout = new VBox(15, titleLabel, descLabel, nameLabel, courseText, dateLabel, closeBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #0F172A; -fx-padding: 30px; -fx-border-color: #3B82F6; -fx-border-width: 2px; -fx-background-radius: 12px;");

        Scene scene = new Scene(layout, 450, 320);
        certStage.setScene(scene);
        certStage.showAndWait();
    }

    /**
     * Reusable Dark-themed Info/Error Message Dialog
     */
    public static void showMessageDialog(String title, String message, boolean isError) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.getDialogPane().setStyle(
                "-fx-background-color: #1E293B; " +
                        "-fx-text-fill: white;"
        );

        VBox content = new VBox(12);
        content.setPadding(new Insets(20, 25, 20, 25));
        content.setMaxWidth(400);

        Label msgLabel = new Label(message);
        msgLabel.setStyle(
                "-fx-text-fill: " + (isError ? "#FCA5A5" : "#F8FAFC") + "; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;"
        );
        msgLabel.setWrapText(true);
        content.getChildren().add(msgLabel);

        dialog.getDialogPane().setContent(content);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButtonType);

        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        if (okButton != null) {
            okButton.setStyle(
                    "-fx-background-color: " + (isError ? "#DC2626" : "#2563EB") + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-background-radius: 6;"
            );
        }

        dialog.showAndWait();
    }

    /**
     * Reusable Dark-themed Confirmation Dialog
     */
    public static boolean showConfirmationDialog(String title, String message) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.getDialogPane().setStyle(
                "-fx-background-color: #1E293B; " +
                        "-fx-text-fill: white;"
        );

        VBox content = new VBox(12);
        content.setPadding(new Insets(20, 25, 20, 25));
        content.setMaxWidth(400);

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 14px; -fx-font-weight: bold;");
        msgLabel.setWrapText(true);
        content.getChildren().add(msgLabel);

        dialog.getDialogPane().setContent(content);

        ButtonType confirmButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(confirmButtonType, cancelButtonType);

        javafx.scene.Node confirmButton = dialog.getDialogPane().lookupButton(confirmButtonType);
        if (confirmButton != null) {
            confirmButton.setStyle(
                    "-fx-background-color: #DC2626; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-background-radius: 6;"
            );
        }

        javafx.scene.Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
        if (cancelButton != null) {
            cancelButton.setStyle(
                    "-fx-background-color: #475569; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-background-radius: 6;"
            );
        }

        return dialog.showAndWait().filter(response -> response == confirmButtonType).isPresent();
    }

    public static void showStreakDialog() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox dialogContent = new VBox(16);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setStyle(
                "-fx-background-color: #0F172A; " +
                        "-fx-border-color: #334155; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 20, 0, 0, 10);"
        );

        Label titleLabel = new Label("🔥 " + user.getStreak() + " Day Streak!");
        titleLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label descLabel = new Label("Complete at least one lesson every day to keep your streak burning bright!");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-alignment: center;");

        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #1E293B; -fx-padding: 14; -fx-background-radius: 12px;");

        Label statusLbl = new Label(user.getStreak() > 0 ? "You're doing amazing! Keep the momentum going." : "Complete your first lesson today to start your streak!");
        statusLbl.setWrapText(true);
        statusLbl.setStyle("-fx-text-fill: #38BDF8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: center;");
        infoBox.getChildren().add(statusLbl);

        Button closeBtn = new Button("Got it!");
        closeBtn.setStyle(
                "-fx-background-color: #2563EB; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 24 10 24; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> dialog.close());

        dialogContent.getChildren().addAll(titleLabel, descLabel, infoBox, closeBtn);

        StackPane root = new StackPane(dialogContent);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65);");

        Scene scene = new Scene(root, 400, 280);
        scene.setFill(null);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void showForgotPasswordDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
                "-fx-background-color: #0b1329;" +
                        "-fx-border-color: #1e3a8a;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-background-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 25, 0, 0, 10);"
        );
        root.setPrefWidth(420);

        Label titleLabel = new Label("🔒 Reset Password");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label descLabel = new Label("Enter your registered email to receive a verification code.");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        descLabel.setWrapText(true);

        TextField emailFieldInput = new TextField();
        emailFieldInput.setPromptText("Email Address");
        emailFieldInput.setStyle("-fx-background-color: #15203b; -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-padding: 10; -fx-background-radius: 8px;");

        // Hidden initially until code is sent
        TextField codeField = new TextField();
        codeField.setPromptText("6-Digit Verification Code");
        codeField.setStyle("-fx-background-color: #15203b; -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-padding: 10; -fx-background-radius: 8px;");
        codeField.setVisible(false);
        codeField.setManaged(false);

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("New Password (min 6 chars)");
        newPassField.setStyle("-fx-background-color: #15203b; -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-padding: 10; -fx-background-radius: 8px;");
        newPassField.setVisible(false);
        newPassField.setManaged(false);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");

        Button actionBtn = new Button("Send Verification Code");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10 0;" +
                        "-fx-cursor: hand;"
        );

        final boolean[] isVerifying = {false};

        actionBtn.setOnAction(e -> {
            String email = emailFieldInput.getText().trim();

            if (email.isEmpty()) {
                statusLabel.setText("❌ Please enter your email address.");
                statusLabel.setStyle("-fx-text-fill: #f87171;");
                return;
            }

            if (!isVerifying[0]) {
                actionBtn.setText("Sending...");
                actionBtn.setDisable(true);

                new Thread(() -> {
                    JSONObject response = DatabaseManager.requestPasswordReset(email);
                    Platform.runLater(() -> {
                        actionBtn.setDisable(false);
                        if (response != null && response.optBoolean("success", false)) {
                            isVerifying[0] = true;
                            emailFieldInput.setDisable(true);

                            codeField.setVisible(true);
                            codeField.setManaged(true);
                            newPassField.setVisible(true);
                            newPassField.setManaged(true);
                            dialog.sizeToScene();

                            descLabel.setText("Enter the 6-digit code sent to your email and your new password.");
                            actionBtn.setText("Verify & Update Password");

                            String resetCode = response.optString("reset_code", response.optString("dev_code", ""));

                            EmailService.sendPasswordResetCodeAsync(email, resetCode, new EmailService.EmailCallback() {
                                @Override
                                public void onSuccess() {
                                    System.out.println("✅ Reset email sent successfully!");
                                }
                                @Override
                                public void onFailure(Exception ex) {
                                    System.err.println("❌ Failed to send email: " + ex.getMessage());
                                }
                            });

                            statusLabel.setText("✅ Verification code sent to your email!");
                            statusLabel.setStyle("-fx-text-fill: #34d399;");
                        } else {
                            actionBtn.setText("Send Verification Code");
                            String err = response != null ? response.optString("error", "Email not found") : "Server error";
                            statusLabel.setText("❌ " + err);
                            statusLabel.setStyle("-fx-text-fill: #f87171;");
                        }
                    });
                }).start();

            } else {
                String code = codeField.getText().trim();
                String newPass = newPassField.getText().trim();

                if (code.isEmpty() || newPass.isEmpty()) {
                    statusLabel.setText("❌ Please enter the code and new password.");
                    statusLabel.setStyle("-fx-text-fill: #f87171;");
                    return;
                }

                if (newPass.length() < 6) {
                    statusLabel.setText("❌ Password must be at least 6 characters.");
                    statusLabel.setStyle("-fx-text-fill: #f87171;");
                    return;
                }

                actionBtn.setText("Updating...");
                actionBtn.setDisable(true);

                new Thread(() -> {
                    boolean success = DatabaseManager.confirmPasswordReset(email, code, newPass);
                    Platform.runLater(() -> {
                        actionBtn.setDisable(false);
                        if (success) {
                            statusLabel.setText("✅ Password reset successfully!");
                            statusLabel.setStyle("-fx-text-fill: #34d399;");
                            PauseTransition delay = new PauseTransition(Duration.seconds(1.2));
                            delay.setOnFinished(ev -> dialog.close());
                            delay.play();
                        } else {
                            actionBtn.setText("Verify & Update Password");
                            statusLabel.setText("❌ Invalid or expired verification code.");
                            statusLabel.setStyle("-fx-text-fill: #f87171;");
                        }
                    });
                }).start();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #94a3b8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(titleLabel, descLabel, emailFieldInput, codeField, newPassField, statusLabel, actionBtn, cancelBtn);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void showChangePasswordDialog(String userEmail) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
                "-fx-background-color: #0b1329;" +
                        "-fx-border-color: #1e3a8a;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-background-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 25, 0, 0, 10);"
        );
        root.setPrefWidth(420);

        Label titleLabel = new Label("🔒 Change Password");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label descLabel = new Label("A verification code will be sent to your email: " + userEmail);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        descLabel.setWrapText(true);

        TextField codeField = new TextField();
        codeField.setPromptText("6-Digit Verification Code");
        codeField.setStyle("-fx-background-color: #15203b; -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-padding: 10; -fx-background-radius: 8px;");
        codeField.setVisible(false);
        codeField.setManaged(false);

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("New Password (min 6 chars)");
        newPassField.setStyle("-fx-background-color: #15203b; -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-padding: 10; -fx-background-radius: 8px;");
        newPassField.setVisible(false);
        newPassField.setManaged(false);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");

        Button actionBtn = new Button("Send Verification Code");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10 0;" +
                        "-fx-cursor: hand;"
        );

        final boolean[] isVerifying = {false};

        actionBtn.setOnAction(e -> {
            if (!isVerifying[0]) {
                // STEP 1: Request code automatically for the logged-in user's email
                actionBtn.setText("Sending Code...");
                actionBtn.setDisable(true);

                new Thread(() -> {
                    JSONObject response = DatabaseManager.requestPasswordReset(userEmail);
                    Platform.runLater(() -> {
                        actionBtn.setDisable(false);
                        if (response != null && response.optBoolean("success", false)) {
                            isVerifying[0] = true;

                            codeField.setVisible(true);
                            codeField.setManaged(true);
                            newPassField.setVisible(true);
                            newPassField.setManaged(true);
                            dialog.sizeToScene();

                            descLabel.setText("Enter the 6-digit code sent to " + userEmail + " and your new password.");
                            actionBtn.setText("Verify & Change Password");

                            String resetCode = response.optString("reset_code", response.optString("dev_code", ""));

                            // Send real email via EmailService
                            EmailService.sendPasswordResetCodeAsync(userEmail, resetCode, new EmailService.EmailCallback() {
                                @Override
                                public void onSuccess() {
                                    System.out.println("✅ Change password email sent!");
                                }
                                @Override
                                public void onFailure(Exception ex) {
                                    System.err.println("❌ Failed to send email: " + ex.getMessage());
                                }
                            });

                            statusLabel.setText("✅ Code sent to your email!");
                            statusLabel.setStyle("-fx-text-fill: #34d399;");
                        } else {
                            actionBtn.setText("Send Verification Code");
                            statusLabel.setText("❌ Failed to generate code. Please try again.");
                            statusLabel.setStyle("-fx-text-fill: #f87171;");
                        }
                    });
                }).start();

            } else {
                // STEP 2: Verify code and update password
                String code = codeField.getText().trim();
                String newPass = newPassField.getText().trim();

                if (code.isEmpty() || newPass.isEmpty()) {
                    statusLabel.setText("❌ Please enter the code and new password.");
                    statusLabel.setStyle("-fx-text-fill: #f87171;");
                    return;
                }

                if (newPass.length() < 6) {
                    statusLabel.setText("❌ Password must be at least 6 characters.");
                    statusLabel.setStyle("-fx-text-fill: #f87171;");
                    return;
                }

                actionBtn.setText("Updating...");
                actionBtn.setDisable(true);

                new Thread(() -> {
                    boolean success = DatabaseManager.confirmPasswordReset(userEmail, code, newPass);
                    Platform.runLater(() -> {
                        actionBtn.setDisable(false);
                        if (success) {
                            statusLabel.setText("✅ Password changed successfully!");
                            statusLabel.setStyle("-fx-text-fill: #34d399;");
                            PauseTransition delay = new PauseTransition(Duration.seconds(1.2));
                            delay.setOnFinished(ev -> dialog.close());
                            delay.play();
                        } else {
                            actionBtn.setText("Verify & Change Password");
                            statusLabel.setText("❌ Invalid or expired verification code.");
                            statusLabel.setStyle("-fx-text-fill: #f87171;");
                        }
                    });
                }).start();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #94a3b8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(titleLabel, descLabel, codeField, newPassField, statusLabel, actionBtn, cancelBtn);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void showUpgradePromptDialog(String message) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(16);
        root.setPadding(new javafx.geometry.Insets(24));
        root.setStyle(
                "-fx-background-color: #0b1329;" +
                        "-fx-border-color: #3b82f6;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-background-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 25, 0, 0, 10);"
        );
        root.setPrefWidth(380);
        root.setAlignment(javafx.geometry.Pos.CENTER);

        javafx.scene.control.Label iconLabel = new javafx.scene.control.Label("⭐");
        iconLabel.setStyle("-fx-font-size: 36px;");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Unlock Premium");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        javafx.scene.control.Label msgLabel = new javafx.scene.control.Label(message);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        msgLabel.setWrapText(true);
        msgLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        javafx.scene.control.Button upgradeBtn = new javafx.scene.control.Button("Upgrade Plan");
        upgradeBtn.setMaxWidth(Double.MAX_VALUE);
        upgradeBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10 0;" +
                        "-fx-cursor: hand;"
        );
        upgradeBtn.setOnAction(e -> dialog.close());

        javafx.scene.control.Button cancelBtn = new javafx.scene.control.Button("Maybe Later");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #94a3b8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(iconLabel, titleLabel, msgLabel, upgradeBtn, cancelBtn);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}