package Project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class PracticeQuestionController {

    @FXML private Label titleLabel;
    @FXML private Label languageBadge;
    @FXML private Label taskLabel;
    @FXML private Label instructionsLabel;
    @FXML private Label expectedOutputLabel;
    @FXML private Label feedbackLabel;
    @FXML private TextArea codeEditor;
    @FXML private TextArea outputConsole;
    @FXML private Button runBtn;
    @FXML private Button hintBtn;
    @FXML private Button nextBtn;
    @FXML private Button tryAgainBtn;

    @FXML private VBox blankSection;
    @FXML private VBox blankArea;
    @FXML private FlowPane choiceArea;
    @FXML private VBox codeSection;
    @FXML private VBox outputSection;

    private QuestionData currentQuestion;
    private String currentLanguage;
    private List<Label> blankSlots = new ArrayList<>();
    private List<String> userAnswers = new ArrayList<>();
    private List<Button> allChoiceButtons = new ArrayList<>();
    private String currentType = null;

    // ============================================
// ✅ SET OUTPUT STYLE — Border ပါ
// ============================================
    private void setOutputStyle(String textColor) {
        outputConsole.setStyle(
                "-fx-text-fill: " + textColor + "; " +
                        "-fx-control-inner-background: #0f172a; " +
                        "-fx-background-color: #0f172a; " +
                        "-fx-border-color: #38bdf8; " +
                        "-fx-border-width: 1.5; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-font-family: 'Consolas', monospace; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 10;"
        );
    }

    // ============================================
    // SET QUESTION
    // ============================================
    public void setQuestion(QuestionData question) {
        this.currentQuestion = question;
        this.currentLanguage = question.getLanguage();
        this.currentType = question.getQuestionType();

        if (titleLabel != null) titleLabel.setText(question.getTitle());
        if (languageBadge != null) languageBadge.setText(question.getLanguage());
        if (taskLabel != null) taskLabel.setText(question.getTask());
        if (instructionsLabel != null) instructionsLabel.setText(question.getInstructions());
        if (expectedOutputLabel != null) expectedOutputLabel.setText(question.getExpectedOutput());
        if (codeEditor != null) codeEditor.setText(question.getStarterCode());

        if (feedbackLabel != null) feedbackLabel.setText("");
        if (nextBtn != null) {
            nextBtn.setVisible(false);
            nextBtn.setManaged(false);
        }
        if (tryAgainBtn != null) {
            tryAgainBtn.setVisible(false);
            tryAgainBtn.setManaged(false);
        }
        if (outputConsole != null) {
            outputConsole.clear();
            setOutputStyle("#4ade80");
        }

        String type = question.getQuestionType();
        boolean isBlank = "fill-blank".equals(type) || "drag-drop".equals(type);

        if (blankSection != null) {
            blankSection.setVisible(isBlank);
            blankSection.setManaged(isBlank);
        }
        if (codeSection != null) {
            codeSection.setVisible(!isBlank);
            codeSection.setManaged(!isBlank);
        }
        if (outputSection != null) {
            outputSection.setVisible(false);
            outputSection.setManaged(false);
        }

        if (isBlank) {
            setupBlankUI(question);
        }
    }

    // ============================================
    // SETUP BLANK UI
    // ============================================
    private void setupBlankUI(QuestionData q) {
        if (blankArea == null || choiceArea == null) return;

        blankArea.getChildren().clear();
        choiceArea.getChildren().clear();
        blankSlots.clear();
        userAnswers.clear();
        allChoiceButtons.clear();

        String[] lines = q.getStarterCode().split("\n");
        int blankIndex = 0;

        for (String line : lines) {
            if (line.contains("___BLANK___")) {
                HBox lineBox = new HBox(8);
                lineBox.setAlignment(Pos.CENTER_LEFT);

                String[] parts = line.split("___BLANK___", -1);
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        Label codeLabel = new Label(parts[i]);
                        codeLabel.setStyle("-fx-font-family: 'Consolas', monospace; " +
                                "-fx-font-size: 15px; -fx-text-fill: #e2e8f0;");
                        lineBox.getChildren().add(codeLabel);
                    }

                    if (i < parts.length - 1) {
                        Label slot = new Label("");
                        slot.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: #38bdf8; " +
                                "-fx-padding: 6 18; -fx-background-radius: 6; " +
                                "-fx-border-color: #38bdf8; -fx-border-radius: 6; " +
                                "-fx-border-width: 1.5; -fx-min-width: 60; " +
                                "-fx-min-height: 32; -fx-alignment: center; " +
                                "-fx-font-family: 'Consolas', monospace; " +
                                "-fx-font-size: 14px; -fx-cursor: hand;");
                        slot.setUserData(blankIndex);
                        blankSlots.add(slot);
                        final int idx = blankIndex;
                        slot.setOnMouseClicked(e -> handleBlankClick(idx));
                        lineBox.getChildren().add(slot);
                        blankIndex++;
                    }
                }
                blankArea.getChildren().add(lineBox);
            } else {
                Label codeLabel = new Label(line);
                codeLabel.setStyle("-fx-font-family: 'Consolas', monospace; " +
                        "-fx-font-size: 15px; -fx-text-fill: #e2e8f0;");
                blankArea.getChildren().add(codeLabel);
            }
        }

        if (q.getChoices() != null) {
            List<String> choices = new ArrayList<>(q.getChoices());
            Collections.shuffle(choices);

            for (String choice : choices) {
                Button choiceBtn = new Button(choice);
                choiceBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; " +
                        "-fx-padding: 12 24; -fx-background-radius: 10; " +
                        "-fx-font-family: 'Consolas', monospace; -fx-font-size: 14px; " +
                        "-fx-cursor: hand; -fx-border-color: rgba(56,189,248,0.3); " +
                        "-fx-border-radius: 10; -fx-border-width: 1.5;");
                choiceBtn.setOnAction(e -> handleChoiceClick(choiceBtn, choice));
                allChoiceButtons.add(choiceBtn);
                choiceArea.getChildren().add(choiceBtn);
            }
        }
    }

    private void handleBlankClick(int blankIndex) {
        if (blankIndex >= blankSlots.size()) return;
        Label slot = blankSlots.get(blankIndex);
        String oldValue = slot.getText();

        if (oldValue != null && !oldValue.isEmpty()) {
            for (Button btn : allChoiceButtons) {
                if (btn.getText().equals(oldValue)) {
                    btn.setDisable(false);
                    break;
                }
            }
            slot.setText("");
            if (blankIndex < userAnswers.size()) userAnswers.set(blankIndex, null);
        }
    }

    private void handleChoiceClick(Button choiceBtn, String choice) {
        for (int i = 0; i < blankSlots.size(); i++) {
            if (i >= userAnswers.size() || userAnswers.get(i) == null) {
                if (userAnswers.size() <= i) userAnswers.add(choice);
                else userAnswers.set(i, choice);

                Label slot = blankSlots.get(i);
                slot.setText(choice);
                choiceBtn.setDisable(true);
                break;
            }
        }
    }

    // ============================================
    // HINT
    // ============================================
    @FXML
    public void handleHint() {
        if (currentQuestion == null) return;

        String hint = currentQuestion.getHint();
        if (hint == null || hint.isEmpty()) {
            hint = "Try reading the task carefully.";
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hint");
        alert.setHeaderText("💡 Hint");
        alert.setContentText(hint);
        alert.showAndWait();
    }

    // ============================================
    // TRY AGAIN
    // ============================================
    @FXML
    public void handleTryAgain() {
        if (feedbackLabel != null) feedbackLabel.setText("");
        if (nextBtn != null) {
            nextBtn.setVisible(false);
            nextBtn.setManaged(false);
        }
        if (tryAgainBtn != null) {
            tryAgainBtn.setVisible(false);
            tryAgainBtn.setManaged(false);
        }
        if (outputConsole != null) {
            outputConsole.clear();
            setOutputStyle("#4ade80");
        }

        if (currentQuestion != null &&
                ("fill-blank".equals(currentQuestion.getQuestionType()) ||
                        "drag-drop".equals(currentQuestion.getQuestionType()))) {
            for (Label slot : blankSlots) slot.setText("");
            userAnswers.clear();
            for (Button btn : allChoiceButtons) btn.setDisable(false);
        }
    }

    // ============================================
    // NEXT
    // ============================================
    @FXML
    public void handleNext() {
        if (currentQuestion == null) return;

        List<QuestionData> list = JsonUtils.getPracticeListByType(
                currentLanguage, currentType);

        if (list == null || list.isEmpty()) return;

        list.removeIf(q -> q.getTitle().equals(currentQuestion.getTitle()));

        if (list.isEmpty()) {
            list = JsonUtils.getPracticeListByType(currentLanguage, currentType);
        }

        java.util.Random rand = new java.util.Random();
        QuestionData nextQ = list.get(rand.nextInt(list.size()));

        setQuestion(nextQ);
    }

    // ============================================
    // RUN
    // ============================================
    @FXML
    public void handleRun() {
        if (currentQuestion == null) return;

        outputSection.setVisible(true);
        outputSection.setManaged(true);

        if (feedbackLabel != null) feedbackLabel.setText("");
        if (nextBtn != null) {
            nextBtn.setVisible(false);
            nextBtn.setManaged(false);
        }
        if (tryAgainBtn != null) {
            tryAgainBtn.setVisible(false);
            tryAgainBtn.setManaged(false);
        }

        String type = currentQuestion.getQuestionType();
        if ("fill-blank".equals(type) || "drag-drop".equals(type)) {
            checkBlankAnswer();
        } else {
            runCode();
        }
    }

    private void checkBlankAnswer() {
        List<String> correct = currentQuestion.getBlanks();
        boolean allCorrect = true;
        StringBuilder feedback = new StringBuilder();

        for (int i = 0; i < correct.size(); i++) {
            String userAnswer = (i < userAnswers.size()) ? userAnswers.get(i) : null;
            if (!correct.get(i).equals(userAnswer)) {
                allCorrect = false;
                feedback.append("❌ Blank ").append(i + 1).append(": ");
                feedback.append("expected '").append(correct.get(i))
                        .append("' but got '").append(userAnswer).append("'\n");
            }
        }

        outputConsole.clear();
        if (allCorrect) {
            setOutputStyle("#4ade80");
            outputConsole.appendText("✅ Correct! Well done.\n\n");
            outputConsole.appendText(currentQuestion.getExpectedOutput());
            showSuccess();
        } else {
            setOutputStyle("#f87171");
            outputConsole.appendText("❌ Not quite.\n\n");
            outputConsole.appendText(feedback.toString());
            showFailure();
        }
    }

    private void runCode() {
        String code = codeEditor.getText();
        if (code.trim().isEmpty()) {
            setOutputStyle("#fbbf24");
            outputConsole.setText("⚠️ Please write code first.");
            return;
        }

        setOutputStyle("#4ade80");
        outputConsole.setText("▶ Running...\n");
        runBtn.setDisable(true);

        ApiClient.executeCodeAsync(currentLanguage.toLowerCase(), code)
                .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                    String actual = extractField(response, "output").trim();
                    String expected = currentQuestion.getExpectedOutput().trim();

                    outputConsole.clear();

                    if (actual.equals(expected)) {
                        setOutputStyle("#4ade80");
                        outputConsole.appendText("✅ Correct! Well done.\n\n");
                        outputConsole.appendText(actual + "\n");
                        showSuccess();
                    } else {
                        setOutputStyle("#f87171");
                        outputConsole.appendText("❌ Error!\n\n");
                        outputConsole.appendText(actual + "\n");
                        outputConsole.appendText("─────────────────\n");
                        outputConsole.appendText("Expected: " + expected + "\n");
                        showFailure();
                    }
                    runBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> {
                        setOutputStyle("#f87171");
                        outputConsole.setText("❌ Error: " + ex.getMessage());
                        showFailure();
                        runBtn.setDisable(false);
                    });
                    return null;
                });
    }

    // ============================================
    // SUCCESS / FAILURE
    // ============================================
    private void showSuccess() {
        if (feedbackLabel != null) {
            feedbackLabel.setText("✅ Correct!");
            feedbackLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
        }
        if (nextBtn != null) {
            nextBtn.setVisible(true);
            nextBtn.setManaged(true);
        }
        if (tryAgainBtn != null) {
            tryAgainBtn.setVisible(false);
            tryAgainBtn.setManaged(false);
        }
    }

    private void showFailure() {
        if (feedbackLabel != null) {
            feedbackLabel.setText("❌ Try again!");
            feedbackLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
        }
        if (tryAgainBtn != null) {
            tryAgainBtn.setVisible(true);
            tryAgainBtn.setManaged(true);
        }
        if (nextBtn != null) {
            nextBtn.setVisible(false);
            nextBtn.setManaged(false);
        }
    }

    // ============================================
    // BACK
    // ============================================
    @FXML
    public void handleBack() {
        // ✅ MainLayoutController ကို ခေါ်
        MainLayoutController layout = MainLayoutController.getInstance();
        if (layout != null) {
            layout.loadPage("PracticeView.fxml");
        }
    }

    // ============================================
    // HELPERS
    // ============================================
    private String extractField(String json, String field) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + field + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
                java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1)
                    .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t")
                    .replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return "";
    }
}