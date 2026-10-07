package Project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.json.JSONObject;

public class GameController {

    @FXML private Label challengeTitleLabel;
    @FXML private Label progressLabel;
    @FXML private Label timerLabel;
    @FXML private Label questionTextLabel;

    @FXML private WebView codeWebView; // ⭐ Replaced TextArea with Monaco WebView
    @FXML private TextArea consoleOutputArea;
    @FXML private Button runBtn;
    @FXML private Button submitBtn;

    private WebEngine webEngine;
    private boolean isEditorReady = false;
    private String pendingCode = "";
    private String pendingLanguage = "python";

    private int timeRemaining = 300;
    private Timeline timer;
    private String selectedLanguage = "Python";
    private String currentProblemTask = "";

    // ⭐ Multiplayer Session Variables
    private String roomId;
    private String opponentId;
    private String currentUserId;

    public void setCourseData(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
            this.pendingLanguage = lang;
        }
        if (challengeTitleLabel != null) {
            challengeTitleLabel.setText(selectedLanguage.toUpperCase() + " LIVE CODING ARENA");
        }
    }

    // ⭐ Room ID နှင့် Opponent ID သတ်မှတ်ပေးသည့် Method
    public void setGameSession(String roomId, String opponentId) {
        this.roomId = roomId;
        this.opponentId = opponentId;

        if (UserSession.getInstance().getCurrentUser() != null) {
            this.currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());
        } else {
            this.currentUserId = "USER_" + System.currentTimeMillis();
        }

        // Live WebSocket events များ ရယူရန် Listener ချိတ်ဆက်မည်
        attachGameWebSocketListener();
    }

    @FXML
    public void initialize() {
        if (consoleOutputArea != null) {
            consoleOutputArea.setText("Ready. Click 'Run Code' to execute output.");
        }

        if (codeWebView != null) {
            webEngine = codeWebView.getEngine();
            setupMonacoEditor();
        }
    }

    // ============================================
    // MONACO EDITOR SETUP (Playground Style)
    // ============================================
    private void setupMonacoEditor() {
        String monacoHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    html, body, #container { 
                        width: 100%; 
                        height: 100%; 
                        margin: 0; 
                        padding: 0; 
                        overflow: hidden; 
                        background-color: #0d1322; 
                    }
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
                                'editor.background': '#0d1322',
                                'editor.foreground': '#e2e8f0',
                                'editor.lineHighlightBackground': '#1e293b',
                                'editorLineNumber.foreground': '#475569',
                                'editorLineNumber.activeForeground': '#38bdf8',
                                'editorCursor.foreground': '#38bdf8'
                            }
                        });

                        editor = monaco.editor.create(document.getElementById('container'), {
                            value: '',
                            language: 'python',
                            theme: 'navyTheme',
                            automaticLayout: true,
                            fontSize: 14,
                            minimap: { enabled: false },
                            scrollBeyondLastLine: false
                        });

                        // ⭐ Real-time WebSocket Progress Listener on typing
                        editor.onDidChangeModelContent(function() {
                            if (window.javaBridge) {
                                window.javaBridge.onCodeChange(editor.getValue());
                            }
                        });
                    });

                    function getCode() {
                        return editor ? editor.getValue() : '';
                    }

                    function setCode(code, language) {
                        if (editor) {
                            editor.setValue(code);
                            if (language) {
                                monaco.editor.setModelLanguage(editor.getModel(), language);
                            }
                        }
                    }

                    function setReadOnly(readOnly) {
                        if (editor) {
                            editor.updateOptions({ readOnly: readOnly });
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

                // Bind JavaBridge to receive JavaScript content change events
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", new JavaBridge());
                } catch (Exception e) {
                    System.err.println("Failed to bind JavaBridge: " + e.getMessage());
                }

                if (!pendingCode.isEmpty()) {
                    applyCodeToEditor(pendingCode, pendingLanguage);
                }
            }
        });
    }

    // ⭐ Java Bridge Class for WebEngine -> WebSocket updates
    public class JavaBridge {
        public void onCodeChange(String newText) {
            Platform.runLater(() -> {
                if (roomId != null && !roomId.isEmpty()) {
                    int progress = Math.min(100, newText.trim().length() / 2);
                    WebSocketService.getInstance().sendProgressUpdate(roomId, currentUserId, progress);
                }
            });
        }
    }

    private void applyCodeToEditor(String code, String language) {
        String monacoLang = mapToMonacoLanguage(language);
        String escapedCode = JSONObject.quote(code != null ? code : "");
        String script = String.format("setCode(%s, '%s');", escapedCode, monacoLang);
        webEngine.executeScript(script);
    }

    private String getCode() {
        if (!isEditorReady) return pendingCode;
        Object result = webEngine.executeScript("getCode()");
        return result != null ? result.toString() : "";
    }

    private void setCode(String code, String language) {
        this.pendingCode = code != null ? code : "";
        this.pendingLanguage = language;

        if (isEditorReady) {
            applyCodeToEditor(pendingCode, pendingLanguage);
        }
    }

    private String mapToMonacoLanguage(String lang) {
        if (lang == null) return "plaintext";
        return switch (lang.trim().toLowerCase()) {
            case "c++", "cpp" -> "cpp";
            case "python" -> "python";
            case "java" -> "java";
            default -> "plaintext";
        };
    }

    // ============================================
    // WEBSOCKET & GAME LISTENERS
    // ============================================
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

            // ⭐ ပြိုင်ဘက် စာရိုက်နေသည့် Progress ရောက်လာပါက UI တွင် ဖော်ပြပေးမည်
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

            // ⭐ အခြားသူ သို့မဟုတ် မိမိ အနိုင်ရသွားပါက အသိပေးမည်
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

        System.out.println("DEBUG - Received Response: " + jsonResponse);

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
            } else {
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

            // Set code in Monaco
            setCode(starterCode, selectedLanguage);

            if (progressLabel != null) progressLabel.setText("MATCH IN PROGRESS - OPPONENT: " + (opponentId != null ? opponentId : "WAITING..."));

            startTimer();

        } catch (Exception e) {
            e.printStackTrace();
            handleLoadingFailure();
        }
    }

    @FXML
    private void handleRunCode() {
        if (consoleOutputArea == null) return;

        String code = getCode();
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
        String code = getCode();
        if (consoleOutputArea != null) consoleOutputArea.setText("Submitting code for AI Evaluation...");

        disableControls(true);

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
        if (codeWebView != null) {
            codeWebView.setDisable(disable);
            if (isEditorReady) {
                webEngine.executeScript(String.format("setReadOnly(%b);", disable));
            }
        }
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