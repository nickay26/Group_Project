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
    @FXML private Label questionTextLabel; // Challenge Problem Description

    @FXML private TextArea codeEditorArea;     // Code ရေးသားရမည့်နေရာ
    @FXML private TextArea consoleOutputArea;  // Run ရလဒ် ထွက်မည့်နေရာ
    @FXML private Button runBtn;
    @FXML private Button submitBtn;

    private int timeRemaining = 300; // မိနစ် ၅ မိနစ် (စက္ကန့် ၃၀၀)
    private Timeline timer;
    private String selectedLanguage = "Python";
    private String currentProblemTask = "";

    public void setCourseData(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }
        if (challengeTitleLabel != null) {
            challengeTitleLabel.setText(selectedLanguage.toUpperCase() + " LIVE CODING ARENA");
        }
    }

    @FXML
    public void initialize() {
        if (consoleOutputArea != null) {
            consoleOutputArea.setText("Ready. Click 'Run Code' to execute output.");
        }
    }

    // Lobby မှတစ်ဆင့် ပုစ္ဆာ JSON ရောက်ရှိလာပါက Loading လုပ်ပေးမည်
    public void loadQuestionFromJson(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            System.err.println("Error: Empty response received from server.");
            handleLoadingFailure();
            return;
        }

        // Backend မှ ပြန်လာသော Raw Response ကို Console တွင် စစ်ဆေးရန် Log ထုတ်ကြည့်ခြင်း
        System.out.println("DEBUG - Received Response: " + jsonResponse);

        try {
            String cleanJson = jsonResponse.trim();
            JSONObject obj = null;

            // Response သည် Array format ([...]) ဖြစ်နေပါက
            if (cleanJson.startsWith("[")) {
                org.json.JSONArray array = new org.json.JSONArray(cleanJson);
                if (array.length() > 0) {
                    obj = array.getJSONObject(0);
                }
            }
            // Response သည် Object format ({...}) ဖြစ်နေပါက
            else if (cleanJson.startsWith("{")) {
                obj = new JSONObject(cleanJson);
                if (obj.has("questions")) {
                    org.json.JSONArray questionsArr = obj.getJSONArray("questions");
                    if (questionsArr.length() > 0) {
                        obj = questionsArr.getJSONObject(0);
                    }
                }
            }
            // JSON မဟုတ်သော Error Text သို့မဟုတ် HTML ပြန်လာပါက
            else {
                System.err.println("Error: Server did not return a valid JSON format -> " + cleanJson);
                handleLoadingFailure();
                return;
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
            if (progressLabel != null) progressLabel.setText("LIVE MATCH IN PROGRESS");

            startTimer();

        } catch (Exception e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            e.printStackTrace();
            handleLoadingFailure();
        }
    }

    // 1. Code ကို Execution Server တွင် Run ကြည့်ခြင်း[cite: 12]
    @FXML
    private void handleRunCode() {
        if (codeEditorArea == null || consoleOutputArea == null) return;

        String code = codeEditorArea.getText();

        // Playground ကဲ့သို့ "Python" / "C++" သန့်သန့်ဖြစ်စေရန် (Basic) စာသားကို ဖယ်ထုတ်ခြင်း
        String cleanLanguage = selectedLanguage;
        if (cleanLanguage != null && cleanLanguage.contains("(")) {
            cleanLanguage = cleanLanguage.substring(0, cleanLanguage.indexOf("(")).trim();
        }

        consoleOutputArea.setText("Executing code...");

        ApiClient.executeCodeAsync(cleanLanguage, code).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response != null && !response.isEmpty()) {
                    try {
                        // Response JSON ထဲမှ "output" စာသားကို သီးသန့် ထုတ်ပြခြင်း
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        if (json.has("output")) {
                            consoleOutputArea.setText(json.getString("output"));
                        } else {
                            consoleOutputArea.setText(response);
                        }
                    } catch (Exception e) {
                        // Raw string ဖြစ်နေပါက တိုက်ရိုက်ပြမည်
                        consoleOutputArea.setText(response);
                    }
                } else {
                    consoleOutputArea.setText("Error: Could not reach execution server.");
                }
            });
        });
    }

    // 2. ရေးထားသော Code ကို AI စစ်ဆေးရေးထံ ပို့ပြီး Match အမှတ်ဆုံးဖြတ်ခြင်း[cite: 12]
    @FXML
    public void handleSubmitCode(ActionEvent event) {
        String code = codeEditorArea.getText();
        if (consoleOutputArea != null) consoleOutputArea.setText("Submitting code for AI Evaluation...");

        disableControls(true);

        // Submit Solution သို့မဟုတ် Match Result Processing လုပ်သည့် Method ထဲတွင်
        ApiClient.reviewCodeAsync(selectedLanguage, code).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response != null && !response.isEmpty()) {
                    String formattedReview = response;

                    try {
                        // Backend မှလာသော JSON ကို Parse လုပ်ပြီး "review" field ကိုသာ သီးခြားထုတ်ယူခြင်း
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        if (json.has("review")) {
                            formattedReview = json.getString("review");
                        }
                    } catch (Exception e) {
                        // JSON parsing မရပါက raw response အတိုင်း ထားမည်
                    }

                    // Console Output တွင် သန့်ရှင်းသော AI Review စာသားကိုသာ ပြသမည်
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

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            int mins = timeRemaining / 60;
            int secs = timeRemaining % 60;

            if (timerLabel != null) {
                timerLabel.setText(String.format("Time Left: %02d:%02d", mins, secs));
            }

            if (timeRemaining <= 0) {
                timer.stop();
                handleSubmitCode(null); // အချိန်ပြည့်ပါက အော်တို Submit လုပ်မည်
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
        MainLayoutController.getInstance().loadPage("GameModeView.fxml");
    }
}