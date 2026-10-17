package Project.Controllers;

import Project.API_Database.ApiClient;
import Project.Getters_Setters.QuestionData;
import Project.Helpers.SceneManager;
import Project.Getters_Setters.Project;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditorController {

    @FXML private Label projectTitleLabel;
    @FXML private Label languageBadge;
    @FXML private Label instructionsLabel;
    @FXML private Label taskLabel;
    @FXML private Label expectedOutputLabel;

    @FXML private WebView codeWebView;
    @FXML private TextArea outputConsole;
    @FXML private TextArea aiReviewArea;
    @FXML private Button runBtn;
    @FXML private Button reviewBtn;
    @FXML private Button checkBtn;

    private WebEngine webEngine;
    private boolean isEditorReady = false;
    private String pendingCode = "";
    private String pendingLanguage = "java";

    private Project currentProject;
    private QuestionData currentQuestion;

    @FXML
    public void initialize() {
        if (codeWebView != null) {
            webEngine = codeWebView.getEngine();
            setupMonacoEditor();
        }
    }

    // Monaco Editor HTML & JS Setup with Custom Navy Theme
    // Monaco Editor HTML & JS Setup loading from shared resource file
    private void setupMonacoEditor() {
        if (webEngine == null) return;

        webEngine.setJavaScriptEnabled(true);

        java.net.URL htmlUrl = getClass().getResource("/Monaco/monaco_editor.html");
        if (htmlUrl == null) {
            htmlUrl = getClass().getResource("/monaco_editor.html");
        }

        if (htmlUrl != null) {
            webEngine.load(htmlUrl.toExternalForm());
        } else {
            if (outputConsole != null) {
                outputConsole.setText("Error: monaco_editor.html not found in resources!");
            }
        }

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                isEditorReady = true;
                if (!pendingCode.isEmpty()) {
                    applyCodeToEditor(pendingCode, pendingLanguage);
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

    private String getCode() {
        if (!isEditorReady) {
            return pendingCode;
        }
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
//            case "javascript", "js" -> "javascript";
//            case "sql" -> "sql";
//            case "html" -> "html";
//            case "css" -> "css";
            default -> "plaintext";
        };
    }

    // ============================================
    // INIT — Question (Mimo Style)
    // ============================================
    public void initQuestion(QuestionData question) {
        this.currentQuestion = question;
        this.currentProject = null;

        if (projectTitleLabel != null) projectTitleLabel.setText(question.getTitle());
        if (languageBadge != null) languageBadge.setText(question.getLanguage());
        if (instructionsLabel != null) instructionsLabel.setText(question.getInstructions());
        if (taskLabel != null) taskLabel.setText(question.getTask());
        if (expectedOutputLabel != null) expectedOutputLabel.setText(question.getExpectedOutput());

        setCode(question.getStarterCode(), question.getLanguage());

        if (outputConsole != null) outputConsole.clear();
        if (aiReviewArea != null) aiReviewArea.clear();
    }

    // ============================================
    // INIT — Project (Playground)
    // ============================================
    public void initData(Project project) {
        this.currentProject = project;
        this.currentQuestion = null;

        if (projectTitleLabel != null) projectTitleLabel.setText(project.getTitle());
        if (languageBadge != null) languageBadge.setText(project.getLanguage());
        if (instructionsLabel != null) instructionsLabel.setText("Free coding playground. Write any code.");
        if (taskLabel != null) taskLabel.setText("No task — just code freely.");

        setCode(project.getCodeContent() != null ? project.getCodeContent() : "", project.getLanguage());
    }

    // ============================================
    // RUN CODE
    // ============================================
    @FXML
    public void handleRunCode() {
        String lang = getLanguage();
        String code = getCode();

        if (lang == null || code.trim().isEmpty()) {
            outputConsole.setText("Error: Editor is empty.");
            return;
        }

        outputConsole.setText("▶ Running...\n");
        runBtn.setDisable(true);

        if (currentProject != null) handleSaveCode();

        ApiClient.executeCodeAsync(lang, code)
                .thenAccept(responseJson -> Platform.runLater(() -> {
                    outputConsole.setText(extractJsonField(responseJson, "output"));
                    runBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        outputConsole.setText("Error: " + ex.getMessage());
                        runBtn.setDisable(false);
                    });
                    return null;
                });
    }

    // ============================================
    // CHECK — Mimo Style
    // ============================================
    @FXML
    public void handleCheckCode() {
        if (currentQuestion == null) {
            outputConsole.setText("Check is only available in Practice mode.");
            return;
        }

        String lang = getLanguage();
        String code = getCode();

        if (code.trim().isEmpty()) {
            outputConsole.setText("Error: Editor is empty.");
            return;
        }

        outputConsole.setText("▶ Running and checking...\n");
        if (checkBtn != null) checkBtn.setDisable(true);

        ApiClient.executeCodeAsync(lang, code)
                .thenAccept(responseJson -> Platform.runLater(() -> {
                    String actualOutput = extractJsonField(responseJson, "output").trim();
                    String expected = currentQuestion.getExpectedOutput().trim();

                    outputConsole.appendText(actualOutput + "\n");
                    outputConsole.appendText("─────────────────\n");

                    if (actualOutput.equals(expected)) {
                        outputConsole.appendText("✅ Correct! Well done.\n");
                    } else {
                        outputConsole.appendText("❌ Not quite.\n");
                        outputConsole.appendText("Expected: " + expected + "\n");
                        outputConsole.appendText("Your output: " + actualOutput + "\n");
                    }
                    if (checkBtn != null) checkBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        outputConsole.setText("Error: " + ex.getMessage());
                        if (checkBtn != null) checkBtn.setDisable(false);
                    });
                    return null;
                });
    }

    // ============================================
    // AI REVIEW
    // ============================================
    @FXML
    public void handleReviewCode() {
        String lang = getLanguage();
        String code = getCode();
        String task = currentQuestion != null ? currentQuestion.getTask() : "Free coding";

        if (lang == null || code.trim().isEmpty()) {
            aiReviewArea.setText("Error: Editor is empty.");
            return;
        }

        aiReviewArea.setText("🤖 Analyzing with Gemini AI...");
        reviewBtn.setDisable(true);

        CompletableFuture<String> future;
        if (currentQuestion != null) {
            future = ApiClient.reviewPracticeCodeAsync(lang, code, task);
        } else {
            future = ApiClient.reviewCodeAsync(lang, code);
        }

        future.thenAccept(responseJson -> Platform.runLater(() -> {
                    aiReviewArea.setText(extractJsonField(responseJson, "review"));
                    reviewBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        aiReviewArea.setText("Error: " + ex.getMessage());
                        reviewBtn.setDisable(false);
                    });
                    return null;
                });
    }

    // ============================================
    // HINT
    // ============================================
    @FXML
    public void handleHint() {
        if (currentQuestion == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hint");
            alert.setHeaderText("💡 Playground Mode");
            alert.setContentText("Write any code you like. Click Run to execute.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hint");
        alert.setHeaderText("💡 Need help?");
        alert.setContentText(currentQuestion.getHint());
        alert.showAndWait();
    }

    // ============================================
    // SHOW SOLUTION
    // ============================================
    @FXML
    public void handleShowSolution() {
        if (currentQuestion == null || currentQuestion.getSolutionCode() == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Show Solution");
        alert.setHeaderText("👁 Solution");
        alert.setContentText("Are you sure? Try solving it first!\n\nSolution:\n"
                + currentQuestion.getSolutionCode());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                setCode(currentQuestion.getSolutionCode(), getLanguage());
            }
        });
    }

    // ============================================
    // SAVE
    // ============================================
    @FXML
    public void handleSaveCode() {
        if (currentProject == null) return;

        String updatedCode = getCode();
        currentProject.setCodeContent(updatedCode);

        ApiClient.saveProjectCodeAsync(currentProject.getProjectId(), updatedCode)
                .thenAccept(res -> Platform.runLater(() -> {
                    if (outputConsole != null && outputConsole.getText().isEmpty()) {
                        outputConsole.setText("💾 Project saved!");
                    }
                }));
    }

    // ============================================
    // BACK
    // ============================================
    @FXML
    public void handleBackToDashboard() {
        if (currentProject != null) handleSaveCode();
        SceneManager.switchScene("MainLayoutView.fxml");
        MainLayoutController.getInstance().loadPage("PlaygroundDashboardView.fxml");
    }

    // ============================================
    // HELPERS
    // ============================================
    private String getLanguage() {
        if (currentQuestion != null) return currentQuestion.getLanguage().toLowerCase();
        if (currentProject != null) return currentProject.getLanguage().toLowerCase();
        return "java";
    }

    private String extractJsonField(String json, String fieldName) {
        if (json == null) return "";
        Pattern pattern = Pattern.compile(
                "\"" + fieldName + "\":\\s*\"(.*?)\"\\s*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return json;
    }
}