package Project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AdminPracticeController {

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

    @FXML private TableView<QuestionData> questionTable;
    @FXML private TableColumn<QuestionData, String> colId;
    @FXML private TableColumn<QuestionData, String> colLanguage;
    @FXML private TableColumn<QuestionData, String> colTitle;
    @FXML private TableColumn<QuestionData, String> colType;
    @FXML private TableColumn<QuestionData, String> colTask;

    private final ObservableList<QuestionData> tableData = FXCollections.observableArrayList();
    private QuestionData editingQuestion = null;

    @FXML
    public void initialize() {
        languageCombo.getItems().addAll("Python", "Java", "C++", "HTML");
        languageCombo.setValue("Python");

        typeCombo.getItems().addAll("write-code", "fill-blank", "drag-drop");
        typeCombo.setValue("write-code");

        // Table columns
        colLanguage.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getLanguage()));
        colTitle.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        colType.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getQuestionType()));
        colTask.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTask()));

        questionTable.setItems(tableData);

        // Load data
        refreshTable();
    }

    // ============================================
    // REFRESH TABLE
    // ============================================
    @FXML
    public void handleRefresh() {
        refreshTable();
        statusLabel.setText("🔄 Refreshed");
    }

    private void refreshTable() {
        List<QuestionData> list = JsonUtils.loadPracticeQuestions();
        tableData.setAll(list);
    }

    // ============================================
    // SAVE
    // ============================================
    @FXML
    public void handleSave() {
        String language = languageCombo.getValue();
        String type = typeCombo.getValue();
        String title = titleField.getText().trim();

        if (title.isEmpty()) {
            statusLabel.setText("❌ Title is required");
            statusLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
            return;
        }

        List<String> blanks = parseList(blanksField.getText());
        List<String> choices = parseList(choicesField.getText());

        String previewType = language.equals("HTML") ? "browser" : "console";

        QuestionData q = new QuestionData(
                language, title,
                instructionsArea.getText(),
                taskArea.getText(),
                starterCodeArea.getText(),
                expectedOutputArea.getText(),
                hintArea.getText(),
                solutionArea.getText(),
                blanks, choices,
                type, previewType
        );

        boolean success;
        if (editingQuestion != null) {
            // UPDATE
            success = JsonUtils.updatePracticeQuestion(
                    editingQuestion.getLanguage(),
                    editingQuestion.getTitle(),
                    q
            );
        } else {
            // CREATE
            success = JsonUtils.savePracticeQuestion(q);
        }

        if (success) {
            statusLabel.setText("✅ Saved to questions.json");
            statusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            clearForm();
            editingQuestion = null;
            refreshTable();
        } else {
            statusLabel.setText("❌ Save failed");
            statusLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
        }
    }

    // ============================================
    // LOAD SELECTED FOR EDIT
    // ============================================
    @FXML
    public void handleLoadSelected() {
        QuestionData q = questionTable.getSelectionModel().getSelectedItem();
        if (q == null) {
            statusLabel.setText("⚠️ Select a question first");
            return;
        }

        editingQuestion = q;
        languageCombo.setValue(q.getLanguage());
        typeCombo.setValue(q.getQuestionType());
        titleField.setText(q.getTitle());
        instructionsArea.setText(q.getInstructions());
        taskArea.setText(q.getTask());
        starterCodeArea.setText(q.getStarterCode());
        expectedOutputArea.setText(q.getExpectedOutput());
        hintArea.setText(q.getHint());
        solutionArea.setText(q.getSolutionCode());

        if (q.getBlanks() != null) blanksField.setText(String.join(", ", q.getBlanks()));
        if (q.getChoices() != null) choicesField.setText(String.join(", ", q.getChoices()));

        statusLabel.setText("✏️ Editing: " + q.getTitle());
        statusLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
    }

    // ============================================
    // DELETE SELECTED
    // ============================================
    @FXML
    public void handleDeleteSelected() {
        QuestionData q = questionTable.getSelectionModel().getSelectedItem();
        if (q == null) {
            statusLabel.setText("⚠️ Select a question first");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete");
        alert.setHeaderText("Delete question?");
        alert.setContentText(q.getTitle());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = JsonUtils.deletePracticeQuestion(
                        q.getLanguage(), q.getTitle());
                if (success) {
                    statusLabel.setText("✅ Deleted");
                    refreshTable();
                }
            }
        });
    }

    // ============================================
    // CLEAR FORM
    // ============================================
    @FXML
    public void handleClear() {
        clearForm();
        editingQuestion = null;
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

    private List<String> parseList(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return result;
        for (String p : text.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) result.add(t);
        }
        return result;
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