package Project.Controllers;

import Project.API_Database.ApiClient;
import Project.Getters_Setters.Project;
import Project.Getters_Setters.QuestionData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONObject;

import java.net.URL;

public class MonacoEditorController {

    @FXML private Label titleLabel;
    @FXML private Label langBadge;
    @FXML private Label promptLabel;
    @FXML private WebView webView;
    @FXML private TextArea consoleOutput;
    @FXML private Button runBtn;
    @FXML private VBox terminalContainer;
    @FXML private VBox instructionContainer; // 🌟 ညွှန်ကြားချက်ကတ်ကို ထိန်းချုပ်ရန်

    private WebEngine webEngine;
    private String currentLanguage = "python";
    private boolean isEditorLoaded = false;
    private String pendingCode = "";
    private QuestionData currentQuestion;
    private Project currentProject;

    @FXML
    public void initialize() {
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        URL htmlUrl = getClass().getResource("/Monaco/monaco_editor.html");
        if (htmlUrl == null) {
            htmlUrl = getClass().getResource("/monaco_editor.html");
        }

        if (htmlUrl != null) {
            webEngine.load(htmlUrl.toExternalForm());
        } else {
            consoleOutput.setText("Error: monaco_editor.html not found in resources!");
        }

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                isEditorLoaded = true;
                if (!pendingCode.isEmpty()) {
                    setCode(pendingCode);
                }
            }
        });
    }

    private void showTerminal() {
        if (terminalContainer != null) {
            terminalContainer.setVisible(true);
            terminalContainer.setManaged(true);
        }
    }

    // ============================================
    // 1️⃣ PRACTICE MODE (JSON မေးခွန်းများအတွက်)
    // ============================================
    public void initQuestion(QuestionData question) {
        this.currentQuestion = question;
        this.currentProject = null;

        // Practice ဖြစ်သောကြောင့် Instructions ကတ်ကို ပေါ်စေရန်
        if (instructionContainer != null) {
            instructionContainer.setVisible(true);
            instructionContainer.setManaged(true);
        }

        if (question != null) {
            this.currentLanguage = question.getLanguage() != null ? question.getLanguage().toLowerCase() : "python";
            if (langBadge != null) langBadge.setText(this.currentLanguage.toUpperCase());
            if (titleLabel != null) titleLabel.setText(question.getTitle());

            if (promptLabel != null) {
                String fullText = "📖 " + question.getInstructions() + "\n\n🎯 Task: " + question.getTask();
                promptLabel.setText(fullText);
            }

            setEditorLanguageAndCode(this.currentLanguage, question.getStarterCode());
        }
    }

    // ============================================
    // 2️⃣ PLAYGROUND / GENERAL MODE (လွတ်လပ်စွာ ကုဒ်ရေးရန်)
    // ============================================
    public void initializeEditor(String language, String topic) {
        this.currentQuestion = null;
        this.currentProject = null;

        // Playground ဖြစ်သောကြောင့် Instructions ကတ်ကို ဖုံးကွယ်ထားရန် (နေရာမယူစေရန်)
        if (instructionContainer != null) {
            instructionContainer.setVisible(false);
            instructionContainer.setManaged(false);
        }

        this.currentLanguage = language != null ? language.toLowerCase() : "python";
        if (langBadge != null) langBadge.setText(this.currentLanguage.toUpperCase());
        if (titleLabel != null) titleLabel.setText(topic);

        String starter = getStarterCode(this.currentLanguage);
        setEditorLanguageAndCode(this.currentLanguage, starter);
    }

    public void initData(Project project) {
        this.currentProject = project;
        this.currentQuestion = null;

        if (instructionContainer != null) {
            instructionContainer.setVisible(false);
            instructionContainer.setManaged(false);
        }

        if (project != null) {
            this.currentLanguage = project.getLanguage() != null ? project.getLanguage().toLowerCase() : "java";
            if (langBadge != null) langBadge.setText(this.currentLanguage.toUpperCase());
            if (titleLabel != null) titleLabel.setText(project.getTitle());

            String codeContent = project.getCodeContent() != null ? project.getCodeContent() : getStarterCode(this.currentLanguage);
            setEditorLanguageAndCode(this.currentLanguage, codeContent);
        }
    }

    private String getStarterCode(String lang) {
        if (lang.contains("java")) {
            return "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, Java!\");\n    }\n}";
        } else if (lang.contains("cpp") || lang.contains("c++")) {
            return "#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << \"Hello, C++!\" << endl;\n    return 0;\n}";
        } else {
            return "print('Hello, Python!')";
        }
    }

    public void setEditorLanguageAndCode(String lang, String code) {
        this.currentLanguage = lang;
        if (isEditorLoaded) {
            webEngine.executeScript("setEditorContent(" + JSONObject.quote(code) + ", '" + mapLangToMonaco(lang) + "');");
        } else {
            pendingCode = code;
        }
    }

    public void setCode(String code) {
        setEditorLanguageAndCode(currentLanguage, code);
    }

    private String mapLangToMonaco(String lang) {
        if (lang.contains("java")) return "java";
        if (lang.contains("cpp") || lang.contains("c++")) return "cpp";
        return "python";
    }

    @FXML
    private void handleRunCode() {
        showTerminal();

        Object codeObj = webEngine.executeScript("getEditorContent();");
        String code = codeObj != null ? codeObj.toString() : "";

        if (code.trim().isEmpty()) {
            consoleOutput.setText("Error: Editor is empty.");
            return;
        }

        runBtn.setDisable(true);
        runBtn.setText("Executing...");
        consoleOutput.setText("Running code on server...");

        ApiClient.executeCodeAsync(currentLanguage, code).thenAccept(responseBody -> {
            Platform.runLater(() -> {
                runBtn.setDisable(false);
                runBtn.setText("▶ Run Code");

                if (responseBody == null || responseBody.isEmpty()) {
                    consoleOutput.setText("Error: Connection failed.");
                    return;
                }

                try {
                    JSONObject json = new JSONObject(responseBody);
                    String output = json.optString("output", json.optString("result", responseBody));
                    consoleOutput.setText(output);
                } catch (Exception e) {
                    consoleOutput.setText(responseBody);
                }
            });
        });
    }

    @FXML
    private void handleBack() {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPage("PracticeView.fxml");
        }
    }

    @FXML
    private void handleHint() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hint");
        alert.setHeaderText("💡 Need help?");
        String hintText = (currentQuestion != null && currentQuestion.getHint() != null)
                ? currentQuestion.getHint()
                : "Review the syntax and core concepts of this topic, then try running your code!";
        alert.setContentText(hintText);
        alert.showAndWait();
    }

    @FXML
    private void handleReviewCode() {
        Object codeObj = webEngine.executeScript("getEditorContent();");
        String code = codeObj != null ? codeObj.toString() : "";

        if (code.trim().isEmpty()) {
            consoleOutput.setText("Error: Editor is empty.");
            return;
        }

        consoleOutput.setText("🤖 Analyzing code with AI...");
        ApiClient.reviewCodeAsync(currentLanguage, code).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response != null && !response.isEmpty()) {
                    String cleaned = ApiClient.cleanAndFormatAiText(response);
                    consoleOutput.setText("=== AI CODE REVIEW ===\n" + cleaned);
                    consoleOutput.setStyle("-fx-control-inner-background: #020617; -fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-text-fill: #38BDF8;");
                } else {
                    consoleOutput.setText("Error: Failed to fetch AI review.");
                }
            });
        });
    }
}