package Project.AdminClasses;

import Project.Helpers.JsonUtils;
import Project.Getters_Setters.QuestionData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
        // Languages
        languageCombo.getItems().addAll("Python", "Java", "C++");
        languageCombo.setValue("Python");

        // ✅ Language ရွေးရင် Type update + Table refresh
        languageCombo.setOnAction(e -> {
            updateTypeCombo();
            refreshTable();

        });
        typeCombo.setOnAction(e -> refreshTable());
        updateTypeCombo();
        refreshTable();
        // Table columns
        colId.setCellValueFactory(data -> {
            int idx = tableData.indexOf(data.getValue()) + 1;
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(idx));
        });
        colLanguage.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getLanguage()));
        colTitle.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        colType.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getQuestionType()));
        colTask.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTask()));

        questionTable.setItems(tableData);

        // ပထမဆုံး Type update
        updateTypeCombo();

        // Load
        refreshTable();
    }

    // ============================================
    // TYPE COMBO — Language အလိုက် ပြောင်း
    // ============================================
    private void updateTypeCombo() {
        String lang = languageCombo.getValue();
        typeCombo.getItems().clear();

        if (lang == null) return;

        switch (lang) {
            case "Python":
                typeCombo.getItems().addAll(
                        "Lists & Dicts",
                        "Functions",
                        "OOP Basics"
                );
                break;
            case "Java":
                typeCombo.getItems().addAll(
                        "Variables & Types",
                        "Loops",
                        "Classes & Objects"
                );
                break;
            case "C++":
                typeCombo.getItems().addAll(
                        "Pointers",
                        "Arrays",
                        "Inheritance"
                );
                break;
        }

        if (!typeCombo.getItems().isEmpty()) {
            typeCombo.setValue(typeCombo.getItems().get(0));
        }
    }

    // ============================================
    // REFRESH — Language Filter
    // ============================================
    private void refreshTable() {
        String selectedLang = languageCombo.getValue();
        String selectedType = typeCombo.getValue();

        // Language အလိုက် load
        List<QuestionData> all = JsonUtils.loadPracticeQuestionsByLanguage(selectedLang);

        // Type အလိုက် filter
        List<QuestionData> filtered = new ArrayList<>();
        if (selectedType == null || selectedType.isEmpty()) {
            filtered = all;
        } else {
            for (QuestionData q : all) {
                if (q.getQuestionType().equalsIgnoreCase(selectedType)) {
                    filtered.add(q);
                }
            }
        }

        tableData.setAll(filtered);

        if (statusLabel != null) {
            statusLabel.setText("📋 " + filtered.size() + " questions ("
                    + selectedLang + " - " + selectedType + ")");
            statusLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        }
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

        if (type == null || type.isEmpty()) {
            statusLabel.setText("❌ Type is required");
            return;
        }

        List<String> blanks = new ArrayList<>();
        List<String> choices = new ArrayList<>();
        String previewType = "console";

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
            success = JsonUtils.updatePracticeQuestion(
                    editingQuestion.getLanguage(),
                    editingQuestion.getTitle(),
                    q
            );
        } else {
            success = JsonUtils.savePracticeQuestion(q);
        }

        if (success) {
            statusLabel.setText("✅ Saved! (" + language + " - " + type + ")");
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
    // CLEAR / DELETE
    // ============================================
    @FXML
    public void handleClear() {
        QuestionData q = questionTable.getSelectionModel().getSelectedItem();
        if (q != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete");
            alert.setHeaderText("Delete question?");
            alert.setContentText(q.getTitle());
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    JsonUtils.deletePracticeQuestion(q.getLanguage(), q.getTitle());
                    statusLabel.setText("✅ Deleted: " + q.getTitle());
                    refreshTable();
                }
            });
            return;
        }

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
    }

    // ============================================
    // LOAD SELECTED → Form
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

        // Type update
        updateTypeCombo();

        // Type ကို select
        if (q.getQuestionType() != null) {
            typeCombo.setValue(q.getQuestionType());
        }

        titleField.setText(q.getTitle());
        instructionsArea.setText(q.getInstructions());
        taskArea.setText(q.getTask());
        starterCodeArea.setText(q.getStarterCode());
        expectedOutputArea.setText(q.getExpectedOutput());
        hintArea.setText(q.getHint());
        solutionArea.setText(q.getSolutionCode());

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
                JsonUtils.deletePracticeQuestion(q.getLanguage(), q.getTitle());
                statusLabel.setText("✅ Deleted: " + q.getTitle());
                refreshTable();
            }
        });
    }
}