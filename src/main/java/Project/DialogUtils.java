package Project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
}