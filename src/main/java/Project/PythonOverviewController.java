package Project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ResourceBundle;

public class PythonOverviewController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label detailsLabel;

    @FXML private Label basicTitleLabel;
    @FXML private Label basicDescLabel;

    @FXML private Label interTitleLabel;
    @FXML private Label interDescLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupOverviewData();
    }

    private void setupOverviewData() {
        if (titleLabel != null) titleLabel.setText("Python");
        if (subtitleLabel != null) subtitleLabel.setText("Language Overview");
        if (detailsLabel != null) {
            detailsLabel.setText(
                    "📌 INTRODUCTION\n" +
                            "Python is a beginner-friendly, interpreted programming language known for its clean syntax, rapid development capabilities, and powerful libraries.\n\n" +
                            "🎯 USE FOR WHAT\n" +
                            "• Artificial Intelligence & Machine Learning\n" +
                            "• Data Science, Analytics & Visualization\n" +
                            "• Web Backend Services & Automation Scripts"
            );
        }

        if (basicTitleLabel != null) basicTitleLabel.setText("Python Basic");
        if (basicDescLabel != null) basicDescLabel.setText("Learn Python basics, variables, user input, and string formatting.");

        if (interTitleLabel != null) interTitleLabel.setText("Python Intermediate");
        if (interDescLabel != null) interDescLabel.setText("Work with Data Structures (Lists, Dicts, Sets), Control Flow, and Functions.");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("WelcomeView.fxml", "Sodex Platform - Welcome");
    }

    @FXML
    private void handleStartBasic() {
        checkLoginAndNavigate("PYTHON_BASIC");
    }

    @FXML
    private void handleStartIntermediate() {
        checkLoginAndNavigate("PYTHON_INTERMEDIATE");
    }

    private void checkLoginAndNavigate(String courseId) {
        // 1. နှိပ်လိုက်သည့် Course ID ကို Session တွင် သတ်မှတ်မည်
        UserSession.setActiveCourseId(courseId);

        // 2. Overview မှ လာခြင်း ဖြစ်သောကြောင့် Flag ကို true သတ်မှတ်မည်
        UserSession.setRedirectedFromOverview(true);

        // 3. User Login ဝင်ထားခြင်း ရှိ/မရှိ စစ်ဆေးမည်
        if (UserSession.getInstance() != null && UserSession.getUserId() != -1) {
            // Login ဝင်ထားပြီးပါက Main Layout သို့ တန်းသွားမည်
            SceneManager.switchScene("MainLayoutView.fxml", "Sodex Platform - Dashboard");
        } else {
            // Login မဝင်ရသေးပါက Register Alert Box ကို ပြသမည်
            showRegisterAlert();
        }
    }

    // ⭐ Register ပြုလုပ်ရန် အသိပေးသည့် Custom Modal Dialog Box
    private void showRegisterAlert() {
        // 1. Alert UI Elements
        Label iconLabel = new Label("🔒");
        iconLabel.setStyle("-fx-font-size: 36px;");

        Label titleText = new Label("Registration Required");
        titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");

        Label messageLabel = new Label("Please register first to start learning this course.");
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-text-alignment: center;");
        messageLabel.setWrapText(true);

        // 2. Custom OK Button Design
        Button okBtn = new Button("OK, Register Now");
        okBtn.setStyle(
                "-fx-background-color: #38BDF8; " +
                        "-fx-text-fill: #0F172A; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 8 20 8 20; " +
                        "-fx-cursor: hand;"
        );

        // 3. Container Layout Design
        VBox dialogBox = new VBox(14, iconLabel, titleText, messageLabel, okBtn);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setPadding(new Insets(24));
        dialogBox.setPrefWidth(320);
        dialogBox.setStyle(
                "-fx-background-color: #1E293B; " +
                        "-fx-background-radius: 16px; " +
                        "-fx-border-color: #334155; " +
                        "-fx-border-radius: 16px; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 8);"
        );

        // 4. Modal Stage & Scene ပြင်ဆင်ခြင်း
        Stage alertStage = new Stage();
        alertStage.initStyle(StageStyle.TRANSPARENT);
        alertStage.initModality(Modality.APPLICATION_MODAL);

        if (titleLabel != null && titleLabel.getScene() != null) {
            alertStage.initOwner(titleLabel.getScene().getWindow());
        }

        javafx.scene.Scene scene = new javafx.scene.Scene(dialogBox);
        scene.setFill(Color.TRANSPARENT);
        alertStage.setScene(scene);

        // 5. OK Button Click မှသာ Stage ပိတ်ပြီး Welcome/Register Page သို့ သွားမည်
        okBtn.setOnAction(e -> {
            alertStage.close();
            SceneManager.switchScene("WelcomeView.fxml", "Sodex Platform - Welcome");
        });

        alertStage.show();
    }
}