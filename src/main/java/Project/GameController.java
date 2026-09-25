package Project;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameController {

    @FXML private Label challengeTitleLabel;
    @FXML private Label progressLabel;
    @FXML private Label timerLabel;
    @FXML private Label questionTextLabel;
    @FXML private Button option1Btn, option2Btn, option3Btn, option4Btn;

    private int timeRemaining = 15;
    private Timeline timer;
    private int correctIndex = 0;
    private boolean isAnswered = false;

    private String selectedLanguage = "C++ (Basic)";
    private int currentQuestionIndex = 0;
    private final int TOTAL_QUESTIONS = 5;
    private int score = 0;

    private final List<JSONObject> questionList = new ArrayList<>();

    @FXML
    public void initialize() {
        this.isAnswered = false;
        resetOptionStyles();
        startNewGame();
    }

    public void setCourseData(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }
        if (challengeTitleLabel != null) {
            challengeTitleLabel.setText(selectedLanguage.toUpperCase() + " CODE CHALLENGE");
        }
        startNewGame();
    }

    public void startNewGame() {
        this.currentQuestionIndex = 0;
        this.score = 0;
        this.questionList.clear();

        if (questionTextLabel != null) {
            questionTextLabel.setText("Generating 5 unique questions using AI...");
        }
        disableOptions(true);

        CompletableFuture.supplyAsync(() -> AiQuestionService.fetchAiQuestionsBatch(selectedLanguage, TOTAL_QUESTIONS))
                .thenAccept(json -> Platform.runLater(() -> loadQuestionFromJson(json)))
                .exceptionally(ex -> {
                    Platform.runLater(this::handleLoadingFailure);
                    return null;
                });
    }

    public void loadQuestionFromJson(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            handleLoadingFailure();
            return;
        }

        try {
            parseAndStoreQuestions(jsonResponse);

            if (!questionList.isEmpty()) {
                currentQuestionIndex = 0;
                displayCurrentQuestion();
            } else {
                handleLoadingFailure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleLoadingFailure();
        }
    }
    private void parseAndStoreQuestions(String jsonText) {
        try {
            if (jsonText == null || jsonText.isBlank()) {
                System.err.println("JSON Text is empty!");
                return;
            }

            JSONArray questionsArray;
            String trimmed = jsonText.trim();

            // ရောက်လာသော JSON သည် Object ဖြစ်နေပါက Handle လုပ်မည်
            if (trimmed.startsWith("{")) {
                JSONObject obj = new JSONObject(trimmed);
                if (obj.has("questions")) {
                    questionsArray = obj.getJSONArray("questions");
                } else {
                    int start = trimmed.indexOf('[');
                    int end = trimmed.lastIndexOf(']');
                    if (start != -1 && end > start) {
                        questionsArray = new JSONArray(trimmed.substring(start, end + 1));
                    } else {
                        throw new JSONException("No valid JSON Array found in response");
                    }
                }
            } else {
                questionsArray = new JSONArray(trimmed);
            }

            // JSONArray မှ မေးခွန်းများ ထုတ်ယူသည့် Logic
            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject qObj = questionsArray.getJSONObject(i);
                // မိမိ၏ Question Object ထည့်သွင်းသည့် logic များကို ဒီနေရာတွင် ဆက်လက်ရေးသားပါ
            }

        } catch (Exception e) {
            System.err.println("Error parsing questions JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLoadingFailure() {
        if (questionTextLabel != null) {
            questionTextLabel.setText("Failed to load questions from AI.");
        }
        disableOptions(true);
        if (timer != null) timer.stop();
        if (timerLabel != null) timerLabel.setText("Time: --");
    }

    private void displayCurrentQuestion() {
        if (currentQuestionIndex >= questionList.size() || currentQuestionIndex >= TOTAL_QUESTIONS) {
            showFinalResults();
            return;
        }

        updateProgressUI();
        resetOptionStyles();
        disableOptions(false);

        JSONObject qObject = questionList.get(currentQuestionIndex);

        if (questionTextLabel != null) questionTextLabel.setText(qObject.getString("question"));

        JSONArray options = qObject.getJSONArray("options");
        if (option1Btn != null) option1Btn.setText(options.getString(0));
        if (option2Btn != null) option2Btn.setText(options.getString(1));
        if (option3Btn != null) option3Btn.setText(options.getString(2));
        if (option4Btn != null) option4Btn.setText(options.getString(3));

        this.correctIndex = qObject.getInt("correctIndex");
        this.isAnswered = false;

        startTimer();
    }

    private void updateProgressUI() {
        if (progressLabel != null) {
            progressLabel.setText("QUESTION " + (currentQuestionIndex + 1) + " OF " + TOTAL_QUESTIONS);
        }
    }

    private void startTimer() {
        if (timer != null) timer.stop();
        timeRemaining = 15;
        if (timerLabel != null) timerLabel.setText("Time: " + timeRemaining + "s");

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            if (timerLabel != null) timerLabel.setText("Time: " + timeRemaining + "s");
            if (timeRemaining <= 0) {
                timer.stop();
                this.isAnswered = true;
                highlightCorrectOption();
                scheduleNextQuestion();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void onAnswerClicked(int chosenIndex, Button clickedBtn) {
        if (isAnswered) return;
        isAnswered = true;

        if (timer != null) timer.stop();

        if (chosenIndex == correctIndex) {
            score++;
            if (clickedBtn != null) {
                clickedBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 16 22; -fx-background-radius: 12; -fx-border-color: #15803d; -fx-border-radius: 12; -fx-border-width: 2;");
            }
        } else {
            if (clickedBtn != null) {
                clickedBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 16 22; -fx-background-radius: 12; -fx-border-color: #b91c1c; -fx-border-radius: 12; -fx-border-width: 2;");
            }
            highlightCorrectOption();
        }

        scheduleNextQuestion();
    }

    private void scheduleNextQuestion() {
        PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
        pause.setOnFinished(e -> {
            currentQuestionIndex++;
            displayCurrentQuestion();
        });
        pause.play();
    }

    private void highlightCorrectOption() {
        Button correctBtn = switch (correctIndex) {
            case 0 -> option1Btn;
            case 1 -> option2Btn;
            case 2 -> option3Btn;
            case 3 -> option4Btn;
            default -> null;
        };
        if (correctBtn != null) {
            correctBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 16 22; -fx-background-radius: 12; -fx-border-color: #15803d; -fx-border-radius: 12; -fx-border-width: 2;");
        }
    }

    private void resetOptionStyles() {
        String defaultStyle = "-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 16 22; -fx-background-radius: 12; -fx-border-color: #000000; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-cursor: hand;";

        if (option1Btn != null) option1Btn.setStyle(defaultStyle);
        if (option2Btn != null) option2Btn.setStyle(defaultStyle);
        if (option3Btn != null) option3Btn.setStyle(defaultStyle);
        if (option4Btn != null) option4Btn.setStyle(defaultStyle);
    }

    private void disableOptions(boolean disable) {
        if (option1Btn != null) option1Btn.setDisable(disable);
        if (option2Btn != null) option2Btn.setDisable(disable);
        if (option3Btn != null) option3Btn.setDisable(disable);
        if (option4Btn != null) option4Btn.setDisable(disable);
    }

    private void showFinalResults() {
        if (timer != null) timer.stop();
        disableOptions(true);
        if (questionTextLabel != null) {
            questionTextLabel.setText("Game Finished!\nYour Final Score: " + score + " / " + TOTAL_QUESTIONS);
        }
        if (progressLabel != null) {
            progressLabel.setText("COMPLETED");
        }
    }

    @FXML public void handleOption1(ActionEvent event) { onAnswerClicked(0, option1Btn); }
    @FXML public void handleOption2(ActionEvent event) { onAnswerClicked(1, option2Btn); }
    @FXML public void handleOption3(ActionEvent event) { onAnswerClicked(2, option3Btn); }
    @FXML public void handleOption4(ActionEvent event) { onAnswerClicked(3, option4Btn); }

    @FXML
    public void handleBack(ActionEvent event) {
        if (timer != null) timer.stop();
        SceneManager.switchScene("/Project/GameModeView.fxml", "Select Game Mode");
    }
}