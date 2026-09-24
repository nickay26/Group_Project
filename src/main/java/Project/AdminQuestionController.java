package Project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AdminQuestionController {

    @FXML private ComboBox<String> languageCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField titleField;
    @FXML private TextArea instructionsArea;
    @FXML private TextArea taskArea;
    @FXML private TextArea starterCodeArea;
    @FXML private TextArea expectedOutputArea;
    @FXML private TextArea hintArea;
    @FXML private TextArea solutionArea;
    @FXML private TextField blanksField;
    @FXML private TextField choicesField;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        languageCombo.getItems().addAll("Python", "Java", "C++", "HTML");
        languageCombo.setValue("Python");

        typeCombo.getItems().addAll("write-code", "fill-blank", "drag-drop");
        typeCombo.setValue("write-code");
    }

    // ============================================
    // SAVE QUESTION
    // ============================================
    @FXML
    public void handleSave() {
        String language = languageCombo.getValue();
        String type = typeCombo.getValue();
        String title = titleField.getText().trim();

        // Validation
        if (title.isEmpty()) {
            statusLabel.setText("❌ Title is required");
            statusLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
            return;
        }

        // Blanks + Choices
        List<String> blanks = parseList(blanksField.getText());
        List<String> choices = parseList(choicesField.getText());

        // Preview type
        String previewType = language.equals("HTML") ? "browser" : "console";

        // QuestionData ဖန်တီး
        QuestionData q = new QuestionData(
                language,
                title,
                instructionsArea.getText(),
                taskArea.getText(),
                starterCodeArea.getText(),
                expectedOutputArea.getText(),
                hintArea.getText(),
                solutionArea.getText(),
                blanks,
                choices,
                type,
                previewType
        );

        // JSON မှာ သိမ်း
        boolean success = JsonUtils.savePracticeQuestion(q);

        if (success) {
            statusLabel.setText("✅ Saved to questions.json");
            statusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            clearForm();
        } else {
            statusLabel.setText("❌ Failed to save");
            statusLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
        }
    }

    private List<String> parseList(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return result;

        String[] parts = text.split(",");
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    @FXML
    public void handleClear() {
        clearForm();
        statusLabel.setText("");
    }

    private void clearForm() {
        titleField.clear();
        instructionsArea.clear();
        taskArea.clear();
        starterCodeArea.clear();
        expectedOutputArea.clear();
        hintArea.clear();
        solutionArea.clear();
        blanksField.clear();
        choicesField.clear();
    }

    // ============================================
    // VIEW ALL — Question List
    // ============================================
    @FXML
    public void handleViewAll() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Project/AdminListView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin - All Questions");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================
    // BACK
    // ============================================
    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Project/PracticeView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mimo Platform - Practice");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}