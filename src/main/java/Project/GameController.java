package Project;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameController {

    @FXML private Label challengeTitleLabel;
    @FXML private Label progressLabel;
    @FXML private Label questionNumberLabel;
    @FXML private Label timerLabel;
    @FXML private Label scoreLabel;
    @FXML private Label questionTextLabel;
    @FXML private Label feedbackLabel;

    @FXML private TextArea codeSnippetArea;

    @FXML private Button option1Btn;
    @FXML private Button option2Btn;
    @FXML private Button option3Btn;
    @FXML private Button option4Btn;

    // ⭐ Result Overlay Dialog Components
    @FXML private VBox resultOverlay;
    @FXML private Label resultTitleLabel;
    @FXML private Label resultDescLabel;
    @FXML private Label xpLabel;

    private List<GameQuestion> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int userScore = 0;

    private int timeRemaining = 15; // 15 Seconds per Sololearn Question
    private Timeline timer;
    private String selectedLanguage = "Java (Basic)";

    // Multiplayer Session Variables
    private String roomId;
    private String opponentId;
    private String currentUserId;
    private boolean isGameOverSent = false;

    public void setCourseData(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }
        if (challengeTitleLabel != null) {
            challengeTitleLabel.setText(selectedLanguage.toUpperCase() + " 1v1 CODE CHALLENGE");
        }
    }

    public void setGameSession(String roomId, String opponentId) {
        this.roomId = roomId;
        this.opponentId = opponentId;

        if (UserSession.getInstance().getCurrentUser() != null) {
            this.currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());
        } else {
            this.currentUserId = "USER_" + System.currentTimeMillis();
        }

        attachGameWebSocketListener();
    }

    @FXML
    public void initialize() {
        if (feedbackLabel != null) feedbackLabel.setText("");
        if (resultOverlay != null) resultOverlay.setVisible(false);

        // User ID မရှိသေးပါက Auto Set ပြုလုပ်မည်
        if (this.currentUserId == null) {
            if (UserSession.getInstance().getCurrentUser() != null) {
                this.currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());
            } else {
                this.currentUserId = "USER_" + System.currentTimeMillis();
            }
        }

        attachGameWebSocketListener();
    }

    private void attachGameWebSocketListener() {
        WebSocketService.getInstance().setNotificationListener(new WebSocketService.NotificationListener() {
            @Override
            public void onInviteReceived(String fromUser, String language) {}

            @Override
            public void onGameStart(String roomId, String language, String opponentId, String questionsJson) {
                Platform.runLater(() -> {
                    GameController.this.roomId = roomId;
                    GameController.this.opponentId = opponentId;
                    if (language != null && !language.isEmpty()) {
                        GameController.this.selectedLanguage = language;
                    }
                    loadQuestionsFromJson(questionsJson);
                });
            }

            @Override
            public void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson) {
                Platform.runLater(() -> {
                    GameController.this.roomId = roomId;
                    GameController.this.opponentId = opponentId;
                    if (language != null && !language.isEmpty()) {
                        GameController.this.selectedLanguage = language;
                    }
                    loadQuestionsFromJson(questionsJson);
                });
            }

            @Override
            public void onQuickMatchTimeout() {}

            @Override
            public void onOpponentProgress(String userId, int progress) {
                if (!userId.equals(currentUserId)) {
                    Platform.runLater(() -> {
                        if (progressLabel != null) {
                            progressLabel.setText("OPPONENT PROGRESS: " + progress + "%");
                        }
                    });
                }
            }

            // ⭐ Basic/Intermediate အလိုက် XP ခွဲခြားတွက်ချက်မည့် Game Over Handler
            @Override
            public void onGameOver(String winnerId) {
                Platform.runLater(() -> {
                    if (timer != null) timer.stop();
                    disableOptionButtons(true);

                    boolean isWinner = currentUserId.equals(winnerId);

                    // Mode ခွဲခြားခြင်း
                    boolean isIntermediate = selectedLanguage.toLowerCase().contains("intermediate");

                    int earnedXp;
                    if (isWinner) {
                        // နိုင်ပါက: Basic = 50 XP, Intermediate = 80 XP
                        earnedXp = isIntermediate ? 80 : 50;
                    } else {
                        // ရှုံးပါက: ဖြေဆိုထားသော Score အပေါ်မူတည်၍ XP ပေးခြင်း
                        // Maximum score 1000 ဟုယူဆပြီး 0 မှ 5 Bonus ထည့်ပေးမည်
                        int scoreBonus = Math.min(5, Math.max(0, (userScore / 200)));

                        // Basic = 5 မှ 10 XP, Intermediate = 15 မှ 20 XP
                        int baseLoserXp = isIntermediate ? 15 : 5;
                        earnedXp = baseLoserXp + scoreBonus;
                    }

                    showGameResultDialog(isWinner, earnedXp);
                });
            }
        });
    }

    // ⭐ နိုင်သူ/ရှုံးသူ အလိုက် အလယ်ခေါင်တွင် Result Card ပြသပေးမည့် ဖန်ရှင်
    public void showGameResultDialog(boolean isWinner, int earnedXp) {
        Platform.runLater(() -> {
            if (isWinner) {
                // 🏆 နိုင်သည့်သူ ဘက်တွင် ပေါ်မည်
                resultTitleLabel.setText("🏆 VICTORY!");
                resultTitleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
                resultDescLabel.setText("You won the match!");
                xpLabel.setText("Earned: +" + earnedXp + " XP");
            } else {
                // 💔 ရှုံးသည့်သူ ဘက်တွင် ပေါ်မည်
                resultTitleLabel.setText("💔 DEFEAT!");
                resultTitleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
                resultDescLabel.setText("Opponent won the match.");
                xpLabel.setText("Earned: +" + earnedXp + " XP");
            }

            if (resultOverlay != null) {
                resultOverlay.setVisible(true);
            }
        });
    }

    // ⭐ WebSocket မှ မေးခွန်း ၅ ခုပါဝင်သော JSON အား Parse လုပ်ခြင်း
    public void loadQuestionsFromJson(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            handleLoadingFailure();
            return;
        }

        try {
            questionList.clear();
            String cleanJson = jsonResponse.trim();
            JSONArray array = null;

            if (cleanJson.startsWith("[")) {
                array = new JSONArray(cleanJson);
            } else if (cleanJson.startsWith("{")) {
                JSONObject obj = new JSONObject(cleanJson);
                if (obj.has("questions")) {
                    Object qObj = obj.get("questions");
                    if (qObj instanceof String) {
                        array = new JSONArray((String) qObj);
                    } else if (qObj instanceof JSONArray) {
                        array = (JSONArray) qObj;
                    }
                }
            }

            if (array == null || array.length() == 0) {
                handleLoadingFailure();
                return;
            }

            for (int i = 0; i < array.length(); i++) {
                JSONObject q = array.getJSONObject(i);

                List<String> options = new ArrayList<>();
                JSONArray optArr = q.getJSONArray("options");
                for (int j = 0; j < optArr.length(); j++) {
                    options.add(optArr.getString(j));
                }

                GameQuestion question = new GameQuestion(
                        q.optString("type", "mcq"),
                        q.optString("title", "Code Challenge"),
                        q.optString("code_snippet", "// No snippet provided"),
                        q.optString("description", "What will be the output?"),
                        options,
                        q.optInt("correct_answer", 0),
                        q.optInt("time_limit", 15)
                );
                questionList.add(question);
            }

            currentQuestionIndex = 0;
            userScore = 0;
            updateScoreDisplay();
            displayCurrentQuestion();

        } catch (Exception e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            e.printStackTrace();
            handleLoadingFailure();
        }
    }

    // ⭐ မေးခွန်း ၁ ခုစီအား UI ပေါ်တွင် ပြသပေးခြင်း
    private void displayCurrentQuestion() {
        if (currentQuestionIndex >= questionList.size()) {
            finishMatch();
            return;
        }

        GameQuestion q = questionList.get(currentQuestionIndex);

        if (questionNumberLabel != null) questionNumberLabel.setText("QUESTION " + (currentQuestionIndex + 1) + " / " + questionList.size());
        if (questionTextLabel != null) questionTextLabel.setText(q.getDescription());
        if (codeSnippetArea != null) codeSnippetArea.setText(q.getCodeSnippet());
        if (feedbackLabel != null) feedbackLabel.setText("");

        // Reset and Set Options
        List<String> opts = q.getOptions();
        resetOptionButtonStyles();

        if (opts.size() >= 4) {
            option1Btn.setText("A)  " + opts.get(0));
            option2Btn.setText("B)  " + opts.get(1));
            option3Btn.setText("C)  " + opts.get(2));
            option4Btn.setText("D)  " + opts.get(3));
        }

        disableOptionButtons(false);
        startQuestionTimer(q.getTimeLimit() > 0 ? q.getTimeLimit() : 15);
    }

    // ⭐ Option Button နှိပ်လိုက်ချိန် အဖြေစစ်ခြင်း
    @FXML
    private void handleOptionClick(ActionEvent event) {
        if (timer != null) timer.stop();
        disableOptionButtons(true);

        Button selectedBtn = (Button) event.getSource();
        int selectedIndex = 0;

        if (selectedBtn == option1Btn) selectedIndex = 0;
        else if (selectedBtn == option2Btn) selectedIndex = 1;
        else if (selectedBtn == option3Btn) selectedIndex = 2;
        else if (selectedBtn == option4Btn) selectedIndex = 3;

        GameQuestion currentQ = questionList.get(currentQuestionIndex);

        if (selectedIndex == currentQ.getCorrectAnswer()) {
            int speedBonus = timeRemaining * 10;
            int qScore = 100 + speedBonus;
            userScore += qScore;

            selectedBtn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-border-color: #16a34a; -fx-border-radius: 8; -fx-border-width: 2; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-padding: 0 16;");
            if (feedbackLabel != null) {
                feedbackLabel.setStyle("-fx-text-fill: #16a34a;");
                feedbackLabel.setText("✓ Correct! +" + qScore + " pts");
            }
        } else {
            selectedBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-border-color: #dc2626; -fx-border-radius: 8; -fx-border-width: 2; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-padding: 0 16;");
            highlightCorrectOption(currentQ.getCorrectAnswer());

            if (feedbackLabel != null) {
                feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                feedbackLabel.setText("✗ Wrong Choice!");
            }
        }

        updateScoreDisplay();
        sendProgressUpdate();

        PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
        pause.setOnFinished(e -> {
            currentQuestionIndex++;
            displayCurrentQuestion();
        });
        pause.play();
    }

    // ⭐ မေးခွန်း ၁ ခုစီအတွက် တိုင်မာ
    private void startQuestionTimer(int seconds) {
        if (timer != null) timer.stop();
        timeRemaining = seconds;

        if (timerLabel != null) timerLabel.setText("Time: " + timeRemaining + "s");

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            if (timerLabel != null) timerLabel.setText("Time: " + timeRemaining + "s");

            if (timeRemaining <= 0) {
                timer.stop();
                disableOptionButtons(true);

                if (feedbackLabel != null) {
                    feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                    feedbackLabel.setText("⏰ Time Out!");
                }

                GameQuestion currentQ = questionList.get(currentQuestionIndex);
                highlightCorrectOption(currentQ.getCorrectAnswer());

                sendProgressUpdate();

                PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
                pause.setOnFinished(ev -> {
                    currentQuestionIndex++;
                    displayCurrentQuestion();
                });
                pause.play();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void sendProgressUpdate() {
        if (roomId != null && !roomId.isEmpty()) {
            int progressPercent = Math.min(100, (currentQuestionIndex + 1) * 20);
            WebSocketService.getInstance().sendProgressUpdate(roomId, currentUserId, progressPercent);
        }
    }

    private void finishMatch() {
        if (timer != null) timer.stop();
        disableOptionButtons(true);

        if (!isGameOverSent && roomId != null && !roomId.isEmpty()) {
            isGameOverSent = true;
            WebSocketService.getInstance().sendGameOver(roomId, currentUserId);
        }
    }

    private void updateScoreDisplay() {
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + userScore);
        }
    }

    private void highlightCorrectOption(int correctIdx) {
        Button targetBtn = null;
        if (correctIdx == 0) targetBtn = option1Btn;
        else if (correctIdx == 1) targetBtn = option2Btn;
        else if (correctIdx == 2) targetBtn = option3Btn;
        else if (correctIdx == 3) targetBtn = option4Btn;

        if (targetBtn != null) {
            targetBtn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-border-color: #16a34a; -fx-border-radius: 8; -fx-border-width: 2; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-padding: 0 16;");
        }
    }

    private void resetOptionButtonStyles() {
        String defaultStyle = "-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-color: #000000; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-cursor: hand; -fx-alignment: CENTER-LEFT; -fx-padding: 0 16;";
        option1Btn.setStyle(defaultStyle);
        option2Btn.setStyle(defaultStyle);
        option3Btn.setStyle(defaultStyle);
        option4Btn.setStyle(defaultStyle);
    }

    private void disableOptionButtons(boolean disable) {
        if (option1Btn != null) option1Btn.setDisable(disable);
        if (option2Btn != null) option2Btn.setDisable(disable);
        if (option3Btn != null) option3Btn.setDisable(disable);
        if (option4Btn != null) option4Btn.setDisable(disable);
    }

    private void handleLoadingFailure() {
        if (questionTextLabel != null) questionTextLabel.setText("Failed to load questions from server.");
        disableOptionButtons(true);
    }

    @FXML
    public void handleBack(ActionEvent event) {
        if (timer != null) timer.stop();
        SceneManager.switchScene("MainLayoutView.fxml");
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPage("GameModeView.fxml");
        }
    }
}