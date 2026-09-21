package Project;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LessonPlayerController {

    @FXML private Button closeBtn;
    @FXML private ProgressBar progressBar;
    @FXML private StackPane cardContainer;
    @FXML private Button continueBtn;

    private JSONArray slides;
    private int currentSlideIndex = 0;
    private Lesson currentLesson;

    // Answer Checking State
    private String selectedAnswer = null;
    private boolean isAnswerChecked = false;
    private VBox feedbackBox;
    private VBox optionsBox; // Promoted to class level

    @FXML
    public void initialize() {
        // Continue Button Hover Effect
        continueBtn.setOnMouseEntered(e ->
                continueBtn.setStyle("-fx-background-color: #1D4ED8; -fx-text-fill: #FFFFFF; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-cursor: hand;")
        );
        continueBtn.setOnMouseExited(e ->
                continueBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: #FFFFFF; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-cursor: hand;")
        );

        // Close Button Hover Effect
        if (closeBtn != null) {
            closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-cursor: hand;"));
            closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #94A3B8; -fx-cursor: hand;"));
        }
    }

    public void startLesson(Lesson lesson) {
        this.currentLesson = lesson;
        startLessonById(lesson.getLessonId());
    }

    public void startLesson(String lessonId) {
        startLessonById(lessonId);
    }

    private void startLessonById(String lessonId) {
        String resourcePath = "/lessons/" + lessonId + ".json";

        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                renderErrorCard("Lesson file not found: " + resourcePath);
                return;
            }

            String jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject lessonJson = new JSONObject(jsonText);
            this.slides = lessonJson.getJSONArray("slides");
            this.currentSlideIndex = 0;

            renderCurrentSlide();

        } catch (Exception e) {
            e.printStackTrace();
            renderErrorCard("Failed to load lesson content.");
        }
    }

    private void renderCurrentSlide() {
        if (slides == null || currentSlideIndex >= slides.length()) {
            if (currentLesson != null) {
                int userId = UserSession.getUserId();
                String courseId = UserSession.getActiveCourseId();

                DatabaseManager.completeLessonAndAddXP(
                        userId,
                        courseId,
                        currentLesson.getLessonOrder(),
                        currentLesson.getXp()
                );
            }

            if (MainLayoutController.getInstance() != null) {
                MainLayoutController.getInstance().loadPage("LearnPage.fxml");
            }
            return;
        }

        selectedAnswer = null;
        isAnswerChecked = false;

        double progress = (double) (currentSlideIndex + 1) / slides.length();
        progressBar.setProgress(progress);

        cardContainer.getChildren().clear();

        JSONObject slide = slides.getJSONObject(currentSlideIndex);
        String title = slide.optString("title", "");
        String prompt = slide.optString("prompt", "");
        String codeSnippet = slide.optString("codeSnippet", null);
        boolean hasQuestion = slide.has("correctAnswer") && !slide.isNull("correctAnswer");

        continueBtn.setText(hasQuestion ? "CHECK" : "CONTINUE");
        continueBtn.setDisable(hasQuestion);

        // Main Card Box (Dark Mode Container)
        VBox cardBox = new VBox(14);
        cardBox.setAlignment(Pos.TOP_LEFT);
        cardBox.setPadding(new Insets(24));
        cardBox.setMaxWidth(620);
        cardBox.setStyle(
                "-fx-background-color: #1E293B; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-border-color: #334155; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-border-width: 1.5px;"
        );

        // Slide Type Badge
        Label typeBadge = new Label(hasQuestion ? "QUESTION" : "CONCEPT");
        typeBadge.setStyle(
                "-fx-background-color: " + (hasQuestion ? "#7C3AED22;" : "#2563EB22;") +
                        "-fx-text-fill: " + (hasQuestion ? "#C084FC;" : "#60A5FA;") +
                        "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-background-radius: 8px;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        titleLabel.setWrapText(true);

        Label promptLabel = new Label(prompt);
        promptLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #94A3B8; -fx-line-spacing: 3px;");
        promptLabel.setWrapText(true);

        cardBox.getChildren().addAll(typeBadge, titleLabel, promptLabel);

        // Render Code Snippet Box
        if (codeSnippet != null && !codeSnippet.isEmpty()) {
            Label codeLabel = new Label(codeSnippet);
            codeLabel.setWrapText(true);
            codeLabel.setMinHeight(Region.USE_COMPUTED_SIZE);
            codeLabel.getStyleClass().add("code-snippet-box");
            codeLabel.setMaxWidth(Double.MAX_VALUE);
            cardBox.getChildren().add(codeLabel);
        }

        // Render Multiple Choice Options
        if (slide.has("options")) {
            JSONArray options = slide.getJSONArray("options");
            optionsBox = new VBox(10);

            for (int i = 0; i < options.length(); i++) {
                String optionText = options.getString(i);
                Button optionBtn = new Button(optionText);
                optionBtn.setMaxWidth(Double.MAX_VALUE);
                optionBtn.setMinHeight(42);
                optionBtn.setWrapText(true);
                optionBtn.getStyleClass().add("option-button");

                optionBtn.setOnAction(e -> {
                    if (isAnswerChecked) return;

                    selectedAnswer = optionText;
                    continueBtn.setDisable(false);

                    // Reset styling on all option buttons
                    for (Node node : optionsBox.getChildren()) {
                        if (node instanceof Button) {
                            Button btn = (Button) node;
                            btn.getStyleClass().remove("option-button-selected");
                            if (!btn.getStyleClass().contains("option-button")) {
                                btn.getStyleClass().add("option-button");
                            }
                        }
                    }
                    // Apply selected class to the clicked button
                    optionBtn.getStyleClass().add("option-button-selected");
                });

                optionsBox.getChildren().add(optionBtn);
            }
            cardBox.getChildren().add(optionsBox);
        }

        // Container for Answer Feedback
        feedbackBox = new VBox(6);
        feedbackBox.setPadding(new Insets(12));
        feedbackBox.setVisible(false);
        cardBox.getChildren().add(feedbackBox);

        // ScrollPane Container
        ScrollPane scrollPane = new ScrollPane(cardBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMaxWidth(640);
        scrollPane.setStyle(
                "-fx-background: transparent; " +
                        "-fx-background-color: transparent; " +
                        "-fx-padding: 0;"
        );

        cardContainer.getChildren().add(scrollPane);
    }

    @FXML
    private void handleContinue() {
        JSONObject slide = slides.getJSONObject(currentSlideIndex);
        boolean hasQuestion = slide.has("correctAnswer") && !slide.isNull("correctAnswer");

        if (!hasQuestion) {
            currentSlideIndex++;
            renderCurrentSlide();
            return;
        }

        if (!isAnswerChecked) {
            String correctAnswer = slide.getString("correctAnswer");
            String explanation = slide.optString("explanation", "");

            boolean isCorrect = selectedAnswer != null && selectedAnswer.trim().equalsIgnoreCase(correctAnswer.trim());

            // Lock option buttons and highlight correct / incorrect choices
            if (optionsBox != null) {
                for (Node node : optionsBox.getChildren()) {
                    if (node instanceof Button) {
                        Button btn = (Button) node;
                        btn.setDisable(true);

                        String btnText = btn.getText().trim();

                        if (btnText.equalsIgnoreCase(correctAnswer.trim())) {
                            // Correct Answer -> Green
                            btn.setStyle("-fx-background-color: #064E3B; -fx-text-fill: #34D399; -fx-border-color: #059669; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-padding: 10 16 10 16; -fx-opacity: 1.0;");
                        } else if (selectedAnswer != null && btnText.equalsIgnoreCase(selectedAnswer.trim()) && !isCorrect) {
                            // Chosen Incorrect Answer -> Red
                            btn.setStyle("-fx-background-color: #7F1D1D; -fx-text-fill: #FCA5A5; -fx-border-color: #DC2626; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-padding: 10 16 10 16; -fx-opacity: 1.0;");
                        } else {
                            // Other unselected options -> Dimmed
                            btn.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #64748B; -fx-border-color: #1E293B; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-size: 14px; -fx-alignment: CENTER-LEFT; -fx-padding: 10 16 10 16; -fx-opacity: 0.5;");
                        }
                    }
                }
            }

            if (isCorrect) {
                showFeedback(true, "Correct! Great job.", explanation);
            } else {
                showFeedback(false, "Incorrect! Correct answer: " + correctAnswer, explanation);
            }

            isAnswerChecked = true;
            continueBtn.setText("CONTINUE");
            return;
        }

        currentSlideIndex++;
        renderCurrentSlide();
    }

    private void showFeedback(boolean isCorrect, String message, String explanation) {
        feedbackBox.getChildren().clear();
        feedbackBox.setVisible(true);

        String bgColor = isCorrect ? "#064E3B" : "#7F1D1D";
        String borderColor = isCorrect ? "#059669" : "#DC2626";
        String titleColor = isCorrect ? "#34D399" : "#FCA5A5";
        String textColor = isCorrect ? "#A7F3D0" : "#FECACA";

        feedbackBox.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-border-width: 1.5px;",
                bgColor, borderColor
        ));

        Label titleLabel = new Label((isCorrect ? "✓ " : "✕ ") + message);
        titleLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 14px;", titleColor));
        feedbackBox.getChildren().add(titleLabel);

        if (explanation != null && !explanation.isEmpty()) {
            Label expLabel = new Label(explanation);
            expLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 13px;", textColor));
            expLabel.setWrapText(true);
            feedbackBox.getChildren().add(expLabel);
        }
    }

    @FXML
    private void handleClose() {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPage("LearnPage.fxml");
        }
    }

    private void renderErrorCard(String message) {
        cardContainer.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 16px; -fx-font-weight: bold;");
        cardContainer.getChildren().add(errorLabel);
    }
}