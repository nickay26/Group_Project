package Project.GameClasses;

import Project.Helpers.SceneManager;
import Project.Controllers.MainLayoutController;
import Project.Getters_Setters.UserSession;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import com.google.gson.stream.JsonReader;
import org.json.JSONObject;

import java.io.StringReader;

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

    @FXML private WebView codeWebView; // ⭐ Monaco Editor WebView integration[cite: 10, 12]
    private WebEngine webEngine;
    private boolean isEditorReady = false;
    private String pendingSnippet = "";
    private String pendingLanguage = "python";

    @FXML private Button option1Btn;
    @FXML private Button option2Btn;
    @FXML private Button option3Btn;
    @FXML private Button option4Btn;

    // ⭐ Result Overlay Dialog Components[cite: 11]
    @FXML private VBox resultOverlay;
    @FXML private Label resultTitleLabel;
    @FXML private Label resultDescLabel;
    @FXML private Label xpLabel;

    private List<GameQuestion> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int userScore = 0;

    private int timeRemaining = 15;
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
            this.pendingLanguage = lang;
        }
        if (challengeTitleLabel != null) {
            challengeTitleLabel.setText(selectedLanguage.toUpperCase() + " 1v1 CODE ARENA");
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

        if (this.currentUserId == null) {
            if (UserSession.getInstance().getCurrentUser() != null) {
                this.currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());
            } else {
                this.currentUserId = "USER_" + System.currentTimeMillis();
            }
        }

        if (codeWebView != null) {
            webEngine = codeWebView.getEngine();
            setupMonacoEditor();
        }

        attachGameWebSocketListener();
    }

    // ============================================
    // MONACO EDITOR SETUP
    // ============================================
    private void setupMonacoEditor() {
        String monacoHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    html, body, #container { width: 100%; height: 100%; margin: 0; padding: 0; overflow: hidden; background-color: #1e293b; }
                </style>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.38.0/min/vs/loader.min.js"></script>
            </head>
            <body>
                <div id="container"></div>
                <script>
                    var editor;
                    require.config({ paths: { 'vs': 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.38.0/min/vs' }});
                    require(['vs/editor/editor.main'], function() {
                        monaco.editor.defineTheme('navyTheme', {
                            base: 'vs-dark',
                            inherit: true,
                            rules: [
                                { token: 'comment', foreground: '64748b', fontStyle: 'italic' },
                                { token: 'keyword', foreground: '38bdf8', fontStyle: 'bold' },
                                { token: 'string', foreground: 'f43f5e' },
                                { token: 'number', foreground: 'fbbf24' }
                            ],
                            colors: {
                                'editor.background': '#1e293b',
                                'editor.foreground': '#e2e8f0',
                                'editor.lineHighlightBackground': '#334155',
                                'editorLineNumber.foreground': '#475569',
                                'editorLineNumber.activeForeground': '#38bdf8',
                                'editorCursor.foreground': '#38bdf8'
                            }
                        });

                        editor = monaco.editor.create(document.getElementById('container'), {
                            value: '',
                            language: 'java',
                            theme: 'navyTheme',
                            automaticLayout: true,
                            fontSize: 13,
                            readOnly: true,
                            minimap: { enabled: false },
                            scrollBeyondLastLine: false
                        });
                    });

                    function setCode(code, language) {
                        if (editor) {
                            editor.setValue(code || '');
                            if (language) {
                                monaco.editor.setModelLanguage(editor.getModel(), language);
                            }
                        }
                    }
                </script>
            </body>
            </html>
            """;

        webEngine.loadContent(monacoHtml);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                isEditorReady = true;
                if (!pendingSnippet.isEmpty()) {
                    applyCodeToEditor(pendingSnippet, pendingLanguage);
                }
            }
        });
    }

    private void applyCodeToEditor(String code, String language) {
        String monacoLang = mapToMonacoLanguage(language);
        String escapedCode = JSONObject.quote(code != null ? code : "");
        String script = String.format("setCode(%s, '%s');", escapedCode, monacoLang);
        webEngine.executeScript(script);
    }

    private String mapToMonacoLanguage(String lang) {
        if (lang == null) return "plaintext";
        String l = lang.trim().toLowerCase();
        if (l.contains("cpp") || l.contains("c++")) return "cpp";
        if (l.contains("python")) return "python";
        if (l.contains("java")) return "java";
        return "plaintext";
    }

    private void updateMonacoSnippet(String code, String language) {
        this.pendingSnippet = code != null ? code : "";
        this.pendingLanguage = language;
        if (isEditorReady) {
            applyCodeToEditor(pendingSnippet, pendingLanguage);
        }
    }

    // ============================================
    // WEBSOCKET & GAME LISTENERS[cite: 11]
    // ============================================
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

            @Override
            public void onGameOver(String winnerId) {
                Platform.runLater(() -> {
                    if (timer != null) timer.stop();
                    disableOptionButtons(true);

                    boolean isWinner = currentUserId.equals(winnerId);
                    boolean isIntermediate = selectedLanguage.toLowerCase().contains("intermediate");

                    int earnedXp = isWinner ? (isIntermediate ? 80 : 50) : (isIntermediate ? 15 : 5);
                    showGameResultDialog(isWinner, earnedXp);
                });
            }
        });
    }

    public void showGameResultDialog(boolean isWinner, int earnedXp) {
        Platform.runLater(() -> {
            if (isWinner) {
                resultTitleLabel.setText("🏆 VICTORY!");
                resultTitleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
                resultDescLabel.setText("You won the match!");
                xpLabel.setText("Earned: +" + earnedXp + " XP");
            } else {
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

    public void loadQuestionsFromJson(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            handleLoadingFailure();
            return;
        }

        try {
            questionList.clear();
            String raw = jsonResponse.trim().replace('\u00a0', ' ');
            System.out.println("📥 Raw JSON received length: " + raw.length());

            JsonArray qArray = null;
            JsonElement rootElement = JsonParser.parseString(raw);

            if (rootElement.isJsonObject()) {
                JsonObject rootObj = rootElement.getAsJsonObject();
                if (rootObj.has("questions")) {
                    JsonElement qEl = rootObj.get("questions");
                    if (qEl.isJsonArray()) {
                        qArray = qEl.getAsJsonArray();
                    } else if (qEl.isJsonPrimitive()) {
                        String innerStr = qEl.getAsString();
                        System.out.println("🔍 Parsing inner string questions with lenient reader...");
                        try {
                            JsonReader reader = new JsonReader(new StringReader(innerStr));
                            reader.setLenient(true);
                            JsonElement innerEl = JsonParser.parseReader(reader);
                            if (innerEl.isJsonArray()) {
                                qArray = innerEl.getAsJsonArray();
                            }
                        } catch (Exception e2) {
                            System.err.println("⚠️ Lenient parse failed, trying fallback: " + e2.getMessage());
                            String fixedStr = innerStr.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                            JsonReader reader2 = new JsonReader(new StringReader(fixedStr));
                            reader2.setLenient(true);
                            qArray = JsonParser.parseReader(reader2).getAsJsonArray();
                        }
                    }
                }
            } else if (rootElement.isJsonArray()) {
                qArray = rootElement.getAsJsonArray();
            }

            if (qArray == null || qArray.size() == 0) {
                System.err.println("❌ Parsed JsonArray is null or empty!");
                handleLoadingFailure();
                return;
            }

            System.out.println("✅ Successfully parsed " + qArray.size() + " questions via Gson Lenient Parser!");

            for (JsonElement el : qArray) {
                JsonObject qObj = el.getAsJsonObject();

                List<String> options = new ArrayList<>();
                JsonElement optEl = null;
                if (qObj.has("options")) optEl = qObj.get("options");
                else if (qObj.has("choices")) optEl = qObj.get("choices");
                else if (qObj.has("answers")) optEl = qObj.get("answers");

                if (optEl != null) {
                    if (optEl.isJsonArray()) {
                        for (JsonElement o : optEl.getAsJsonArray()) {
                            options.add(o.getAsString());
                        }
                    } else if (optEl.isJsonPrimitive()) {
                        try {
                            String optStr = optEl.getAsString();
                            JsonReader optReader = new JsonReader(new StringReader(optStr));
                            optReader.setLenient(true);
                            JsonArray parsedOptArr = JsonParser.parseReader(optReader).getAsJsonArray();
                            for (JsonElement o : parsedOptArr) {
                                options.add(o.getAsString());
                            }
                        } catch (Exception ignored) {}
                    }
                }

                if (options.isEmpty()) {
                    options.add("Option 1");
                    options.add("Option 2");
                    options.add("Option 3");
                    options.add("Option 4");
                }

                String snippet = "";
                if (qObj.has("code_snippet")) snippet = qObj.get("code_snippet").getAsString();
                else if (qObj.has("codeSnippet")) snippet = qObj.get("codeSnippet").getAsString();
                else if (qObj.has("snippet")) snippet = qObj.get("snippet").getAsString();
                else if (qObj.has("code")) snippet = qObj.get("code").getAsString();

                if (snippet == null || snippet.isEmpty()) {
                    snippet = "// No code snippet provided";
                }

                String desc = "";
                if (qObj.has("description")) desc = qObj.get("description").getAsString();
                else if (qObj.has("question")) desc = qObj.get("question").getAsString();

                if (desc == null || desc.isEmpty()) {
                    desc = "What will be the output of the following code snippet?";
                }

                int correctAns = 0;
                if (qObj.has("correct_answer")) correctAns = qObj.get("correct_answer").getAsInt();
                else if (qObj.has("correctAnswer")) correctAns = qObj.get("correctAnswer").getAsInt();
                else if (qObj.has("answer")) correctAns = qObj.get("answer").getAsInt();

                int timeLimit = 15;
                if (qObj.has("time_limit")) timeLimit = qObj.get("time_limit").getAsInt();
                else if (qObj.has("timeLimit")) timeLimit = qObj.get("timeLimit").getAsInt();

                GameQuestion question = new GameQuestion(
                        qObj.has("type") ? qObj.get("type").getAsString() : "mcq",
                        qObj.has("title") ? qObj.get("title").getAsString() : "Code Challenge",
                        snippet,
                        desc,
                        options,
                        correctAns,
                        timeLimit
                );
                questionList.add(question);
            }

            System.out.println("✅ Total questions loaded into game list: " + questionList.size());
            currentQuestionIndex = 0;
            userScore = 0;

            Platform.runLater(() -> {
                updateScoreDisplay();
                displayCurrentQuestion();
            });

        } catch (Exception e) {
            e.printStackTrace();
            handleLoadingFailure();
        }
    }

    private void displayCurrentQuestion() {
        if (questionList == null || questionList.isEmpty() || currentQuestionIndex >= questionList.size()) {
            finishMatch();
            return;
        }

        GameQuestion q = questionList.get(currentQuestionIndex);

        if (questionNumberLabel != null) {
            questionNumberLabel.setText("QUESTION " + (currentQuestionIndex + 1) + " / " + questionList.size());
        }
        if (questionTextLabel != null) {
            questionTextLabel.setText(q.getDescription());
        }

        // Update Monaco Editor Snippet
        updateMonacoSnippet(q.getCodeSnippet(), selectedLanguage);

        if (feedbackLabel != null) feedbackLabel.setText("");

        List<String> opts = q.getOptions();
        resetOptionButtonStyles();

        if (opts != null && !opts.isEmpty()) {
            if (option1Btn != null) option1Btn.setText("A)  " + (opts.size() > 0 ? opts.get(0) : "Option 1"));
            if (option2Btn != null) option2Btn.setText("B)  " + (opts.size() > 1 ? opts.get(1) : "Option 2"));
            if (option3Btn != null) option3Btn.setText("C)  " + (opts.size() > 2 ? opts.get(2) : "Option 3"));
            if (option4Btn != null) option4Btn.setText("D)  " + (opts.size() > 3 ? opts.get(3) : "Option 4"));
        }

        disableOptionButtons(false);
        startQuestionTimer(q.getTimeLimit() > 0 ? q.getTimeLimit() : 15);
    }

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

                if (questionList != null && !questionList.isEmpty() && currentQuestionIndex < questionList.size()) {
                    GameQuestion currentQ = questionList.get(currentQuestionIndex);
                    highlightCorrectOption(currentQ.getCorrectAnswer());
                }

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
        if (option1Btn != null) option1Btn.setStyle(defaultStyle);
        if (option2Btn != null) option2Btn.setStyle(defaultStyle);
        if (option3Btn != null) option3Btn.setStyle(defaultStyle);
        if (option4Btn != null) option4Btn.setStyle(defaultStyle);
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
        if (timer != null) {
            timer.stop();
        }
        SceneManager.switchScene("MainLayoutView.fxml");
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPage("GameModeView.fxml");
        }
    }
}