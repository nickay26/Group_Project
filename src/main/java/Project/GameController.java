package Project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import org.json.JSONObject;

public class GameController {

    @FXML private Label challengeTitleLabel;
    @FXML private Label progressLabel;
    @FXML private Label timerLabel;
    @FXML private Label questionTextLabel;

    @FXML private TextArea codeEditorArea;
    @FXML private TextArea consoleOutputArea;
    @FXML private Button runBtn;
    @FXML private Button submitBtn;

    private int timeRemaining = 300; // 5 Minutes
    private Timeline timer;
    private String selectedLanguage = "Python";
    private String currentProblemTask = "";

    // Multiplayer Session Variables
    private String roomId;
    private String opponentId;
    private String currentUserId;

    public void setCourseData(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }
        if (challengeTitleLabel != null) {
            challengeTitleLabel.setText(selectedLanguage.toUpperCase() + " LIVE CODING ARENA");
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
        if (consoleOutputArea != null) {
            consoleOutputArea.setText("Ready. Click 'Run Code' to execute output.");
        }

        if (codeEditorArea != null) {
            codeEditorArea.textProperty().addListener((obs, oldText, newText) -> {
                if (roomId != null && !roomId.isEmpty()) {
                    int progress = Math.min(100, newText.trim().length() / 2);
                    WebSocketService.getInstance().sendProgressUpdate(roomId, currentUserId, progress);
                }
            });
        }
    }

    private void attachGameWebSocketListener() {
        WebSocketService.getInstance().setNotificationListener(new WebSocketService.NotificationListener() {
            @Override
            public void onInviteReceived(String fromUser, String language) {}

            @Override
            public void onGameStart(String roomId, String language, String opponentId, String questionsJson) {}

            @Override
            public void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson) {}

            @Override
            public void onQuickMatchTimeout() {}

            @Override
            public void onOpponentProgress(String userId, int progress) {
                if (!userId.equals(currentUserId)) {
                    Platform.runLater(() -> {
                        if (progressLabel != null) {
                            progressLabel.setText("Opponent (" + userId + ") Progress: " + progress + "%");
                        }
                    });
                }
            }

            // ⭐ P2P/Server ဘက်မှ GameOver ဖြစ်ကြောင်း လာပါက နိုင်/ရှုံး အလိုက် XP တွက်ချက်ပြသမည်
            @Override
            public void onGameOver(String winnerId) {
                Platform.runLater(() -> {
                    if (timer != null) timer.stop();
                    disableControls(true);

                    boolean isWinner = winnerId.equals(currentUserId);
                    int earnedXp = isWinner ? 50 : 10;

                    if (isWinner) {
                        if (consoleOutputArea != null) {
                            consoleOutputArea.setText("🏆 VICTORY!\n\nYou submitted first and won the match!\nEarned: +" + earnedXp + " XP");
                        }
                    } else {
                        if (consoleOutputArea != null) {
                            consoleOutputArea.setText("❌ DEFEAT!\n\nPlayer " + winnerId + " submitted first!\nEarned: +" + earnedXp + " XP");
                        }
                    }
                });
            }
        });
    }

    public void loadQuestionFromJson(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            System.err.println("Error: Empty response received from server.");
            handleLoadingFailure();
            return;
        }

        try {
            String cleanJson = jsonResponse.trim();
            JSONObject obj = null;

            if (cleanJson.startsWith("[")) {
                org.json.JSONArray array = new org.json.JSONArray(cleanJson);
                if (array.length() > 0) {
                    obj = array.getJSONObject(0);
                }
            } else if (cleanJson.startsWith("{")) {
                obj = new JSONObject(cleanJson);
                if (obj.has("questions")) {
                    org.json.JSONArray questionsArr = obj.getJSONArray("questions");
                    if (questionsArr.length() > 0) {
                        obj = questionsArr.getJSONObject(0);
                    }
                }
            }

            if (obj == null) {
                handleLoadingFailure();
                return;
            }

            String title = obj.optString("title", selectedLanguage + " Challenge");
            String description = obj.optString("description", "Solve the challenge using correct logic.");
            String starterCode = obj.optString("starter_code", "// Write your code here");

            this.currentProblemTask = description;

            if (challengeTitleLabel != null) challengeTitleLabel.setText(title.toUpperCase());
            if (questionTextLabel != null) questionTextLabel.setText(description);
            if (codeEditorArea != null) codeEditorArea.setText(starterCode);
            if (progressLabel != null) progressLabel.setText("MATCH IN PROGRESS - OPPONENT: " + (opponentId != null ? opponentId : "WAITING..."));

            startTimer();

        } catch (Exception e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            e.printStackTrace();
            handleLoadingFailure();
        }
    }

    @FXML
    private void handleRunCode() {
        if (codeEditorArea == null || consoleOutputArea == null) return;

        String code = codeEditorArea.getText();
        String cleanLanguage = selectedLanguage;
        if (cleanLanguage != null && cleanLanguage.contains("(")) {
            cleanLanguage = cleanLanguage.substring(0, cleanLanguage.indexOf("(")).trim();
        }

        consoleOutputArea.setText("Executing code...");

        ApiClient.executeCodeAsync(cleanLanguage, code).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response != null && !response.isEmpty()) {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        if (json.has("output")) {
                            consoleOutputArea.setText(json.getString("output"));
                        } else {
                            consoleOutputArea.setText(response);
                        }
                    } catch (Exception e) {
                        consoleOutputArea.setText(response);
                    }
                } else {
                    consoleOutputArea.setText("Error: Could not reach execution server.");
                }
            });
        });
    }

    @FXML
    public void handleSubmitCode(ActionEvent event) {
        String code = codeEditorArea.getText();
        if (consoleOutputArea != null) consoleOutputArea.setText("Submitting code for AI Evaluation...");

        disableControls(true);

        // Submit ပြုလုပ်လိုက်ပါက Server သို့ Match ပြီးဆုံးကြောင်း Signal လှမ်းပို့မည်
        if (roomId != null && !roomId.isEmpty()) {
            WebSocketService.getInstance().sendGameOver(roomId, currentUserId);
        }

        ApiClient.reviewCodeAsync(selectedLanguage, code).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response != null && !response.isEmpty()) {
                    String formattedReview = response;

                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        if (json.has("review")) {
                            formattedReview = json.getString("review");
                        }
                    } catch (Exception e) {
                    }

                    consoleOutputArea.setText("=== MATCH RESULT & AI REVIEW ===\n\n" + formattedReview);
                } else {
                    consoleOutputArea.setText("=== MATCH RESULT & AI REVIEW ===\n\nFailed to fetch AI Review.");
                }
            });
        });
    }

    private void startTimer() {
        if (timer != null) timer.stop();
        timeRemaining = 300;

        // ⭐ Fix: 30-seconds မဖြစ်စေဘဲ တစက္ကန့်လျှင် 1 ကြိမ် (Duration.seconds(1)) အလုပ်လုပ်စေရန် ပြင်ထားသည်
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            int mins = timeRemaining / 60;
            int secs = timeRemaining % 60;

            if (timerLabel != null) {
                timerLabel.setText(String.format("Time Left: %02d:%02d", mins, secs));
            }

            if (timeRemaining <= 0) {
                timer.stop();
                handleSubmitCode(null);
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void handleLoadingFailure() {
        if (questionTextLabel != null) questionTextLabel.setText("Failed to load coding challenge.");
        disableControls(true);
    }

    private void disableControls(boolean disable) {
        if (codeEditorArea != null) codeEditorArea.setDisable(disable);
        if (runBtn != null) runBtn.setDisable(disable);
        if (submitBtn != null) submitBtn.setDisable(disable);
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