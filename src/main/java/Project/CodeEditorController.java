package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditorController {

    @FXML private Label projectTitleLabel;
    @FXML private Label languageBadge;
    @FXML private Label instructionsLabel;
    @FXML private Label taskLabel;
    @FXML private Label expectedOutputLabel;
    @FXML private TextArea codeEditor;
    @FXML private TextArea outputConsole;
    @FXML private TextArea aiReviewArea;
    @FXML private Button runBtn;
    @FXML private Button reviewBtn;
    @FXML private Button checkBtn;

    private Project currentProject;
    private QuestionData currentQuestion;

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
        if (codeEditor != null) codeEditor.setText(question.getStarterCode());
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
        if (codeEditor != null) codeEditor.setText(
                project.getCodeContent() != null ? project.getCodeContent() : "");
    }

    // ============================================
    // RUN CODE
    // ============================================
    @FXML
    public void handleRunCode() {
        String lang = getLanguage();
        String code = codeEditor.getText();

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
    // CHECK — Mimo Style (Expected Output နဲ့ ယှဉ်)
    // ============================================
    @FXML
    public void handleCheckCode() {
        if (currentQuestion == null) {
            outputConsole.setText("Check is only available in Practice mode.");
            return;
        }

        String lang = getLanguage();
        String code = codeEditor.getText();

        if (code.trim().isEmpty()) {
            outputConsole.setText("Error: Editor is empty.");
            return;
        }

        outputConsole.setText("▶ Running and checking...\n");
        checkBtn.setDisable(true);

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
                    checkBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        outputConsole.setText("Error: " + ex.getMessage());
                        checkBtn.setDisable(false);
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
        String code = codeEditor.getText();
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
                codeEditor.setText(currentQuestion.getSolutionCode());
            }
        });
    }

    // ============================================
    // SAVE
    // ============================================
    @FXML
    public void handleSaveCode() {
        if (currentProject == null || codeEditor == null) return;

        String updatedCode = codeEditor.getText();
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
        return null;
    }

    private String extractJsonField(String json, String fieldName) {
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