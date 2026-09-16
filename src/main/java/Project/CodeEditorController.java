package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditorController {

    @FXML private Label projectTitleLabel;
    @FXML private Label languageBadge;
    @FXML private TextArea codeEditor;
    @FXML private TextArea outputConsole;
    @FXML private TextArea aiReviewArea;
    @FXML private Button runBtn;
    @FXML private Button reviewBtn;

    private Project currentProject;

    /**
     * Initializes the controller with data from the selected project.
     */
    public void initData(Project project) {
        this.currentProject = project;

        if (projectTitleLabel != null) projectTitleLabel.setText(project.getTitle());
        if (languageBadge != null) languageBadge.setText(project.getLanguage());
        if (codeEditor != null) {
            codeEditor.setText(project.getCodeContent() != null ? project.getCodeContent() : "");
        }
    }

    /**
     * Runs the code remotely on the PythonAnywhere sandbox.
     */
    @FXML
    public void handleRunCode() {
        if (currentProject == null) return;

        String lang = currentProject.getLanguage().toLowerCase();
        String code = codeEditor.getText();

        if (code.trim().isEmpty()) {
            if (outputConsole != null) outputConsole.setText("Error: Editor is empty.");
            return;
        }

        if (outputConsole != null) outputConsole.setText("Running code on PythonAnywhere sandbox...");
        if (runBtn != null) runBtn.setDisable(true);

        // Auto-save code before execution
        handleSaveCode();

        ApiClient.executeCodeAsync(lang, code)
                .thenAccept(responseJson -> Platform.runLater(() -> {
                    if (outputConsole != null) {
                        outputConsole.setText(extractJsonField(responseJson, "output"));
                    }
                    if (runBtn != null) runBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        if (outputConsole != null) {
                            outputConsole.setText("Execution Error: " + ex.getMessage());
                        }
                        if (runBtn != null) runBtn.setDisable(false);
                    });
                    return null;
                });
    }

    /**
     * Sends the code to Gemini AI for analysis and optimization feedback.
     */
    @FXML
    public void handleReviewCode() {
        if (currentProject == null) return;

        String lang = currentProject.getLanguage().toLowerCase();
        String code = codeEditor.getText();

        if (code.trim().isEmpty()) {
            if (aiReviewArea != null) aiReviewArea.setText("Error: Editor is empty.");
            return;
        }

        if (aiReviewArea != null) aiReviewArea.setText("Analyzing code with Gemini AI...");
        if (reviewBtn != null) reviewBtn.setDisable(true);

        ApiClient.reviewCodeAsync(lang, code)
                .thenAccept(responseJson -> Platform.runLater(() -> {
                    if (aiReviewArea != null) {
                        aiReviewArea.setText(extractJsonField(responseJson, "review"));
                    }
                    if (reviewBtn != null) reviewBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        if (aiReviewArea != null) {
                            aiReviewArea.setText("AI Review Error: " + ex.getMessage());
                        }
                        if (reviewBtn != null) reviewBtn.setDisable(false);
                    });
                    return null;
                });
    }

    /**
     * Saves code updates back to the SQLite database on PythonAnywhere.
     */
    @FXML
    public void handleSaveCode() {
        if (currentProject == null || codeEditor == null) return;

        String updatedCode = codeEditor.getText();
        currentProject.setCodeContent(updatedCode);

        ApiClient.saveProjectCodeAsync(currentProject.getProjectId(), updatedCode)
                .thenAccept(res -> Platform.runLater(() -> {
                    if (outputConsole != null && !outputConsole.getText().startsWith("Running")) {
                        outputConsole.setText("Project saved to PythonAnywhere SQLite!");
                    }
                }));
    }

    /**
     * Auto-saves the current file and navigates back to the Dashboard page.
     */
    @FXML
    public void handleBackToDashboard() {
        handleSaveCode();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PlaygroundDashboardView.fxml"));
            Parent dashboardView = loader.load();

            StackPane contentArea = (StackPane) codeEditor.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(dashboardView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to unescape and extract values from JSON API responses.
     */
    private String extractJsonField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\":\\s*\"(.*?)\"\\s*\\}", Pattern.DOTALL);
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