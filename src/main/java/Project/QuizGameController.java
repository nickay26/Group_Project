package Project;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QuizGameController {

    @FXML private Label challengeTitleLabel;
    @FXML private Label questionLabel;
    @FXML private Label progressLabel;
    @FXML private Label timerLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button opt1, opt2, opt3, opt4;

    private String selectedLanguage = "Python (Basic)";
    private int currentQuestionIndex = 0;
    private final int TOTAL_QUESTIONS = 5;
    private int score = 0;
    private int correctIndex = -1;
    private boolean isAnswered = false;

    private Timeline timer;
    private int timeRemaining = 15;

    private final List<JSONObject> questionList = new ArrayList<>();

    @FXML
    public void initialize() {
        startNewGame();
    }

    public void setCourseData(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }
        if (challengeTitleLabel != null) {
            challengeTitleLabel.setText(this.selectedLanguage.toUpperCase() + " CODE CHALLENGE");
        }
    }

    private void startNewGame() {
        currentQuestionIndex = 0;
        score = 0;
        questionList.clear();

        Platform.runLater(() -> {
            if (challengeTitleLabel != null) {
                challengeTitleLabel.setText(selectedLanguage.toUpperCase() + " CODE CHALLENGE");
            }
            if (progressLabel != null) {
                progressLabel.setText("LOADING QUESTIONS...");
            }
            if (progressBar != null) {
                progressBar.setProgress(0);
            }
            resetOptionStyles();
            setButtonsEnabled(false);
            questionLabel.setText("Fetching questions from AI...");
        });

        CompletableFuture.supplyAsync(() -> AiQuestionService.fetchAiQuestionsBatch(selectedLanguage, TOTAL_QUESTIONS))
                .thenAccept(jsonResponse -> {
                    Platform.runLater(() -> {
                        try {
                            if (jsonResponse != null) {
                                parseBatchQuestions(jsonResponse);
                                displayQuestion(currentQuestionIndex);
                            } else {
                                questionLabel.setText("Failed to load questions. Please try again.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            questionLabel.setText("Error parsing AI response.");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        ex.printStackTrace();
                        questionLabel.setText("Network error occurred!");
                    });
                    return null;
                });
    }

    private void parseBatchQuestions(String jsonString) throws Exception {
        JSONObject json = new JSONObject(jsonString);

        JSONArray candidates = json.getJSONArray("candidates");
        String rawText = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        // Gemini Markdown (```json ... ```) များကို ဖယ်ရှားသန့်စင်ခြင်း
        rawText = rawText.replaceAll("```json", "").replaceAll("```", "").trim();

        int firstBracket = rawText.indexOf("[");
        int lastBracket = rawText.lastIndexOf("]");
        if (firstBracket != -1 && lastBracket != -1) {
            rawText = rawText.substring(firstBracket, lastBracket + 1);
        }

        JSONArray array = new JSONArray(rawText);
        questionList.clear();
        for (int i = 0; i < array.length(); i++) {
            questionList.add(array.getJSONObject(i));
        }
    }

    private void displayQuestion(int index) {
        if (index >= questionList.size() || index >= TOTAL_QUESTIONS) {
            showFinalResults();
            return;
        }

        isAnswered = false;
        int displayNum = index + 1;

        if (progressLabel != null) {
            progressLabel.setText("QUESTION " + displayNum + " OF " + TOTAL_QUESTIONS);
        }
        if (progressBar != null) {
            progressBar.setProgress((double) displayNum / TOTAL_QUESTIONS);
        }

        resetOptionStyles();

        JSONObject qObject = questionList.get(index);
        questionLabel.setText(qObject.getString("question"));

        JSONArray options = qObject.getJSONArray("options");
        opt1.setText(options.getString(0));
        opt2.setText(options.getString(1));
        opt3.setText(options.getString(2));
        opt4.setText(options.getString(3));

        this.correctIndex = qObject.getInt("correctIndex");
        setButtonsEnabled(true);

        resetAndStartTimer();
    }

    @FXML private void handleOption1() { checkAnswer(0, opt1); }
    @FXML private void handleOption2() { checkAnswer(1, opt2); }
    @FXML private void handleOption3() { checkAnswer(2, opt3); }
    @FXML private void handleOption4() { checkAnswer(3, opt4); }

    private void checkAnswer(int selectedIndex, Button clickedBtn) {
        if (isAnswered) return;
        isAnswered = true;

        if (timer != null) {
            timer.stop();
        }

        if (selectedIndex == correctIndex) {
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

    private void highlightCorrectOption() {
        Button correctBtn = switch (correctIndex) {
            case 0 -> opt1;
            case 1 -> opt2;
            case 2 -> opt3;
            case 3 -> opt4;
            default -> null;
        };
        if (correctBtn != null) {
            correctBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 16 22; -fx-background-radius: 12; -fx-border-color: #15803d; -fx-border-radius: 12; -fx-border-width: 2;");
        }
    }

    private void scheduleNextQuestion() {
        PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
        pause.setOnFinished(e -> {
            currentQuestionIndex++;
            displayQuestion(currentQuestionIndex);
        });
        pause.play();
    }

    private void resetAndStartTimer() {
        if (timer != null) {
            timer.stop();
        }

        timeRemaining = 15;
        if (timerLabel != null) {
            timerLabel.setText("Time: " + timeRemaining + "s");
        }

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            if (timerLabel != null) {
                timerLabel.setText("Time: " + timeRemaining + "s");
            }

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

    private void resetOptionStyles() {
        String defaultStyle = "-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 16 22; -fx-background-radius: 12; -fx-border-color: #000000; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-cursor: hand;";

        if (opt1 != null) opt1.setStyle(defaultStyle);
        if (opt2 != null) opt2.setStyle(defaultStyle);
        if (opt3 != null) opt3.setStyle(defaultStyle);
        if (opt4 != null) opt4.setStyle(defaultStyle);
    }

    private void setButtonsEnabled(boolean enabled) {
        if (opt1 != null) opt1.setDisable(!enabled);
        if (opt2 != null) opt2.setDisable(!enabled);
        if (opt3 != null) opt3.setDisable(!enabled);
        if (opt4 != null) opt4.setDisable(!enabled);
    }

    private void showFinalResults() {
        if (timer != null) {
            timer.stop();
        }
        questionLabel.setText("Game Finished!\nYour Final Score: " + score + " / " + TOTAL_QUESTIONS);
        if (progressLabel != null) {
            progressLabel.setText("COMPLETED");
        }
        if (progressBar != null) {
            progressBar.setProgress(1.0);
        }
        setButtonsEnabled(false);
    }

    @FXML
    public void handleBack() {
        if (timer != null) timer.stop();
        SceneManager.switchScene("/Project/GameModeView.fxml", "Select Game Mode");
    }
}