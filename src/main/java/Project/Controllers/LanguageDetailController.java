package Project.Controllers;

import Project.Helpers.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LanguageDetailController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label descriptionLabel;
    @FXML private VBox basicLessonsContainer;
    @FXML private VBox intermediateLessonsContainer;

    // Language Detail Page ကို Data များ ထည့်သွင်းပေးသည့် Method
    public void setLanguageData(String title, String subtitle, String details, String[] lessonTitles, String[] lessonDescriptions) {

        // Header Data များ ပြသခြင်း
        if (titleLabel != null) titleLabel.setText(title);
        if (subtitleLabel != null) subtitleLabel.setText(subtitle);
        if (descriptionLabel != null) descriptionLabel.setText(details);

        // Container များကို ရှင်းထုတ်ခြင်း
        if (basicLessonsContainer != null) basicLessonsContainer.getChildren().clear();
        if (intermediateLessonsContainer != null) intermediateLessonsContainer.getChildren().clear();

        // Cards များကို Dynamic ဆွဲထုတ်ပြသခြင်း
        if (lessonTitles != null && lessonDescriptions != null) {
            for (int i = 0; i < lessonTitles.length; i++) {
                String lTitle = lessonTitles[i];
                String lDesc = (i < lessonDescriptions.length) ? lessonDescriptions[i] : "";

                HBox card = createLessonCard(lTitle, lDesc);

                // Title စာသားပေါ် မူတည်၍ သက်ဆိုင်ရာ Container ထဲသို့ ခွဲခြားထည့်ခြင်း
                if (lTitle.toLowerCase().contains("basic")) {
                    if (basicLessonsContainer != null) {
                        basicLessonsContainer.getChildren().add(card);
                    }
                } else if (lTitle.toLowerCase().contains("intermediate")) {
                    if (intermediateLessonsContainer != null) {
                        intermediateLessonsContainer.getChildren().add(card);
                    }
                } else {
                    // အခြား မပါပါက Default အနေဖြင့် Basic ထဲသို့ ထည့်မည်
                    if (basicLessonsContainer != null) {
                        basicLessonsContainer.getChildren().add(card);
                    }
                }
            }
        }
    }

    // Mimo Style Card တည်ဆောက်သည့် Helper Method (UI/UX Enhanced)
    private HBox createLessonCard(String titleText, String descText) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(20.0);
        card.setPadding(new Insets(20, 24, 20, 24));

        // Modern Card Style - Glassmorphic / Dark Elevate Design
        String baseStyle =
                "-fx-background-color: linear-gradient(to right, #1e293b, #0f172a); " +
                        "-fx-background-radius: 16px; " +
                        "-fx-border-color: #334155; " +
                        "-fx-border-radius: 16px; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 10, 0, 0, 4);";

        String hoverStyle =
                "-fx-background-color: linear-gradient(to right, #334155, #1e293b); " +
                        "-fx-background-radius: 16px; " +
                        "-fx-border-color: #38bdf8; " +
                        "-fx-border-radius: 16px; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(56, 189, 248, 0.25), 15, 0, 0, 6);";

        card.setStyle(baseStyle);

        // Hover Effect - Mouse တင်လိုက်လျှင် Highlight လင်းလာစေရန်
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        // စာသားများပြသရန် VBox
        VBox textContainer = new VBox(6);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label desc = new Label(descText);
        desc.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #94a3b8; -fx-line-spacing: 2px;");
        desc.setWrapText(true);

        textContainer.getChildren().addAll(title, desc);

        // Modern Rounded Button ("Start for Free")
        Button startBtn = new Button("Start for Free");
        startBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #38bdf8, #0284c7); " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 10 20 10 20; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(56, 189, 248, 0.3), 8, 0, 0, 2);"
        );

        // Button Hover Effect
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #0284c7, #0369a1); " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 10 20 10 20; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(56, 189, 248, 0.5), 12, 0, 0, 4);"
        ));

        startBtn.setOnMouseExited(e -> startBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #38bdf8, #0284c7); " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 10 20 10 20; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(56, 189, 248, 0.3), 8, 0, 0, 2);"
        ));

        // Login Page သို့ ပို့ပေးခြင်း
        startBtn.setOnAction(e -> handleLogin());

        card.getChildren().addAll(textContainer, startBtn);
        return card;
    }

    @FXML
    private void handleLogin() {
        SceneManager.switchScene("LoginView.fxml", "Sodexa Platform - Login");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("WelcomeView.fxml", "Sodexa  Platform - Welcome");
    }
}