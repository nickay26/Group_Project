package Project;

import javafx.application.Platform;
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
    @FXML private TextArea codeEditor;
    @FXML private TextArea outputConsole;
    @FXML private TextArea aiReviewArea;
    @FXML private Button runBtn;
    @FXML private Button checkBtn;

    @FXML private VBox blankSection;
    @FXML private VBox blankArea;
    @FXML private VBox choiceSection;
    @FXML private FlowPane choiceArea;
    @FXML private VBox codeSection;
    @FXML private VBox outputSection;

    private QuestionData currentQuestion;
    private String currentLanguage;
    private List<Label> blankSlots = new ArrayList<>();
    private List<String> userAnswers = new ArrayList<>();
    private List<Button> allChoiceButtons = new ArrayList<>();

    // ============================================
    // LOAD QUESTION
    // ============================================
    public void loadQuestion(String language, String topic) {
        this.currentLanguage = language;

        taskLabel.setText("🤖 Loading from Gemini AI...");

        ApiClient.generateQuestionAsync(language, topic, 1)
                .thenAccept(json -> Platform.runLater(() -> {
                    try {
                        QuestionData q = parseQuestionJson(json, language);
                        this.currentQuestion = q;
                        populateUI(q);
                    } catch (Exception e) {
                        taskLabel.setText("❌ Failed: " + e.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> taskLabel.setText("❌ " + ex.getMessage()));
                    return null;
                });
    }

    private void populateUI(QuestionData q) {
        titleLabel.setText(q.getTitle());
        languageBadge.setText(q.getLanguage());
        taskLabel.setText(q.getTask());

        String type = q.getQuestionType();
        boolean isBlank = "fill-blank".equals(type) || "drag-drop".equals(type);

        // Show/hide sections
        blankSection.setVisible(isBlank);
        blankSection.setManaged(isBlank);
        choiceSection.setVisible(isBlank);
        choiceSection.setManaged(isBlank);
        codeSection.setVisible(!isBlank);
        codeSection.setManaged(!isBlank);
        outputSection.setVisible(true);
        outputSection.setManaged(true);

        if (isBlank) {
            setupBlankUI(q);
        } else {
            codeEditor.setText(q.getStarterCode());
        }
    }

    // ============================================
    // SETUP BLANK UI (Mimo Style)
    // ============================================
    private void setupBlankUI(QuestionData q) {
        blankArea.getChildren().clear();
        choiceArea.getChildren().clear();
        blankSlots.clear();
        userAnswers.clear();
        allChoiceButtons.clear();

        String[] lines = q.getStarterCode().split("\n");
        int blankIndex = 0;

        for (String line : lines) {
            // ===== Blank ပါတဲ့ line =====
            if (line.contains("___BLANK___")) {
                HBox lineBox = new HBox(8);
                lineBox.setAlignment(Pos.CENTER_LEFT);

                String[] parts = line.split("___BLANK___", -1);

                for (int i = 0; i < parts.length; i++) {
                    // Text part
                    if (!parts[i].isEmpty()) {
                        Label codeLabel = new Label(parts[i]);
                        codeLabel.setStyle(
                                "-fx-font-family: 'Consolas', monospace; " +
                                        "-fx-font-size: 15px; " +
                                        "-fx-text-fill: #e2e8f0;"
                        );
                        lineBox.getChildren().add(codeLabel);
                    }

                    // Blank slot
                    if (i < parts.length - 1) {
                        Label slot = new Label("");
                        slot.setStyle(
                                "-fx-background-color: #1e3a5f; " +
                                        "-fx-text-fill: #38bdf8; " +
                                        "-fx-padding: 6 18; " +
                                        "-fx-background-radius: 6; " +
                                        "-fx-border-color: #38bdf8; " +
                                        "-fx-border-radius: 6; " +
                                        "-fx-border-width: 1.5; " +
                                        "-fx-min-width: 60; " +
                                        "-fx-min-height: 32; " +
                                        "-fx-alignment: center; " +
                                        "-fx-font-family: 'Consolas', monospace; " +
                                        "-fx-font-size: 14px; " +
                                        "-fx-cursor: hand;"
                        );
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
                // ===== ရိုးရိုး line =====
                Label codeLabel = new Label(line);
                codeLabel.setStyle(
                        "-fx-font-family: 'Consolas', monospace; " +
                                "-fx-font-size: 15px; " +
                                "-fx-text-fill: #e2e8f0;"
                );
                blankArea.getChildren().add(codeLabel);
            }
        }

        // ===== Choices =====
        List<String> choices = new ArrayList<>(q.getChoices());
        Collections.shuffle(choices);

        for (String choice : choices) {
            Button choiceBtn = new Button(choice);
            choiceBtn.setStyle(
                    "-fx-background-color: #334155; " +
                            "-fx-text-fill: #e2e8f0; " +
                            "-fx-padding: 12 24; " +
                            "-fx-background-radius: 10; " +
                            "-fx-font-family: 'Consolas', monospace; " +
                            "-fx-font-size: 14px; " +
                            "-fx-cursor: hand; " +
                            "-fx-border-color: rgba(56,189,248,0.3); " +
                            "-fx-border-radius: 10; " +
                            "-fx-border-width: 1.5;"
            );
            choiceBtn.setOnAction(e -> handleChoiceClick(choiceBtn, choice));
            allChoiceButtons.add(choiceBtn);
            choiceArea.getChildren().add(choiceBtn);
        }
    }

    private void handleBlankClick(int blankIndex) {
        if (blankIndex >= blankSlots.size()) return;

        Label slot = blankSlots.get(blankIndex);
        String oldValue = slot.getText();

        if (oldValue != null && !oldValue.isEmpty()) {
            // Choice ကို ပြန် enable
            for (Button btn : allChoiceButtons) {
                if (btn.getText().equals(oldValue)) {
                    btn.setDisable(false);
                    break;
                }
            }
            // Blank ကို ဖျက်
            slot.setText("");
            slot.setStyle(
                    "-fx-background-color: #1e3a5f; -fx-text-fill: #38bdf8; " +
                            "-fx-padding: 6 18; -fx-background-radius: 6; " +
                            "-fx-border-color: #38bdf8; -fx-border-radius: 6; " +
                            "-fx-border-width: 1.5; -fx-min-width: 60; -fx-min-height: 32; " +
                            "-fx-alignment: center; -fx-font-family: 'Consolas'; " +
                            "-fx-font-size: 14px; -fx-cursor: hand;"
            );
            if (blankIndex < userAnswers.size()) {
                userAnswers.set(blankIndex, null);
            }
        }
    }

    private void handleChoiceClick(Button choiceBtn, String choice) {
        // ပထမ ဗလာ Blank ကို ရှာ
        for (int i = 0; i < blankSlots.size(); i++) {
            if (i >= userAnswers.size() || userAnswers.get(i) == null) {
                if (userAnswers.size() <= i) userAnswers.add(choice);
                else userAnswers.set(i, choice);

                Label slot = blankSlots.get(i);
                slot.setText(choice);
                slot.setStyle(
                        "-fx-background-color: #2563eb; -fx-text-fill: #ffffff; " +
                                "-fx-padding: 6 18; -fx-background-radius: 6; " +
                                "-fx-border-color: #38bdf8; -fx-border-radius: 6; " +
                                "-fx-border-width: 1.5; -fx-min-width: 60; -fx-min-height: 32; " +
                                "-fx-alignment: center; -fx-font-family: 'Consolas'; " +
                                "-fx-font-size: 14px; -fx-cursor: hand;"
                );
                choiceBtn.setDisable(true);
                break;
            }
        }
    }

    // ============================================
    // CHECK
    // ============================================
    @FXML
    public void handleCheck() {
        if (currentQuestion == null) return;

        String type = currentQuestion.getQuestionType();
        if ("fill-blank".equals(type) || "drag-drop".equals(type)) {
            checkBlankAnswer();
        } else {
            checkCodeAnswer();
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
            outputConsole.appendText("✅ Correct! Well done.\n\n");
            outputConsole.appendText("Output:\n" + currentQuestion.getExpectedOutput());
        } else {
            outputConsole.appendText("❌ Not quite.\n\n");
            outputConsole.appendText(feedback.toString());
            outputConsole.appendText("\n💡 Hint: " + currentQuestion.getHint());
        }
    }

    private void checkCodeAnswer() {
        String code = codeEditor.getText();
        if (code.trim().isEmpty()) {
            outputConsole.setText("⚠️ Please write code first.");
            return;
        }
        outputConsole.setText("▶ Running...\n");

        ApiClient.executeCodeAsync(currentLanguage.toLowerCase(), code)
                .thenAccept(response -> Platform.runLater(() -> {
                    String actual = extractField(response, "output").trim();
                    String expected = currentQuestion.getExpectedOutput().trim();
                    outputConsole.clear();
                    outputConsole.appendText(actual + "\n─────────────────\n");
                    if (actual.equals(expected)) {
                        outputConsole.appendText("✅ Correct! Well done.\n");
                    } else {
                        outputConsole.appendText("❌ Not quite.\n");
                        outputConsole.appendText("Expected: " + expected + "\n");
                        outputConsole.appendText("Got: " + actual + "\n");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> outputConsole.setText("❌ " + ex.getMessage()));
                    return null;
                });
    }

    // ============================================
    // RUN — Preview Browser
    // ============================================
    @FXML
    public void handleRun() {
        if (currentQuestion == null) return;
        checkBlankAnswer();

        if ("browser".equals(currentQuestion.getPreviewType())) {
            try {
                java.io.File tempFile = java.io.File.createTempFile("mimo_preview_", ".html");
                tempFile.deleteOnExit();
                try (java.io.FileWriter writer = new java.io.FileWriter(tempFile)) {
                    writer.write(currentQuestion.getSolutionCode());
                }
                java.awt.Desktop.getDesktop().browse(tempFile.toURI());
            } catch (Exception e) {
                outputConsole.setText("Preview error: " + e.getMessage());
            }
        }
    }

    // ============================================
    // HINT / SOLUTION / BACK
    // ============================================
    @FXML
    public void handleHint() {
        if (currentQuestion == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hint");
        alert.setHeaderText("💡 Need help?");
        alert.setContentText(currentQuestion.getHint());
        alert.showAndWait();
    }

    @FXML
    public void handleSolution() {
        if (currentQuestion == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Show Solution");
        alert.setHeaderText("👁 Are you sure?");
        alert.setContentText(currentQuestion.getSolutionCode());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (currentQuestion.getQuestionType().equals("write-code")) {
                    codeEditor.setText(currentQuestion.getSolutionCode());
                } else {
                    // Blank တွေ အကုန် ဖြည့်
                    List<String> correct = currentQuestion.getBlanks();
                    for (int i = 0; i < blankSlots.size() && i < correct.size(); i++) {
                        if (i >= userAnswers.size()) userAnswers.add(correct.get(i));
                        else userAnswers.set(i, correct.get(i));
                        blankSlots.get(i).setText(correct.get(i));
                    }
                }
            }
        });
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Project/PracticeView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mimo Platform - Practice");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================
    // HELPERS
    // ============================================
    private QuestionData parseQuestionJson(String json, String language) {
        return new QuestionData(
                language,
                extractField(json, "title"),
                extractField(json, "instructions"),
                extractField(json, "task"),
                extractField(json, "starterCode"),
                extractField(json, "expectedOutput"),
                extractField(json, "hint"),
                extractField(json, "solutionCode"),
                extractList(json, "blanks"),
                extractList(json, "choices"),
                extractField(json, "questionType"),
                extractField(json, "previewType")
        );
    }

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

    private List<String> extractList(String json, String field) {
        List<String> result = new ArrayList<>();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + field + "\"\\s*:\\s*\\[(.*?)\\]",
                java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            String listContent = m.group(1);
            java.util.regex.Pattern itemP = java.util.regex.Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");
            java.util.regex.Matcher itemM = itemP.matcher(listContent);
            while (itemM.find()) {
                result.add(itemM.group(1).replace("\\\"", "\"").replace("\\\\", "\\"));
            }
        }
        return result;
    }
}