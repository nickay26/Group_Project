package Project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardController {

    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> lessonCombo;

    // Slide Input Fields
    @FXML private ComboBox<String> typeCombo; // CONCEPT, FILL_IN_BLANK, MULTIPLE_CHOICE
    @FXML private TextField slideTitleInput;
    @FXML private TextArea promptArea;
    @FXML private TextArea codeSnippetArea;
    @FXML private TextField optA, optB, optC, optD;
    @FXML private TextField correctAnswerInput;

    // TableView Components
    @FXML private TableView<Slide> slideTable;
    @FXML private TableColumn<Slide, String> colId;
    @FXML private TableColumn<Slide, String> colType;
    @FXML private TableColumn<Slide, String> colTitle;
    @FXML private TableColumn<Slide, String> colPrompt;
    @FXML private TableColumn<Slide, String> colAnswer;

    private final ObservableList<Slide> slideObservableList = FXCollections.observableArrayList();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Lesson currentLesson;

    @FXML
    public void initialize() {
        // TableView Column များ သတ်မှတ်ခြင်း
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("slideId"));
        if (colType != null) colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (colTitle != null) colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colPrompt != null) colPrompt.setCellValueFactory(new PropertyValueFactory<>("prompt"));
        if (colAnswer != null) colAnswer.setCellValueFactory(new PropertyValueFactory<>("correctAnswer"));

        if (slideTable != null) {
            slideTable.setItems(slideObservableList);

            // Table Row ကို Select လုပ်ပါက Input Form များထဲသို့ Data ဖြည့်ပေးမည်
            slideTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    populateForm(newVal);
                }
            });
        }

        // Category ComboBox Initial Data
        if (categoryCombo != null) {
            categoryCombo.setItems(FXCollections.observableArrayList("CPP", "JAVA", "PYTHON", "HTML", "CSS"));
            categoryCombo.setOnAction(e -> {
                loadLessonList(); // Category ပြောင်းသွားပါက သက်ဆိုင်ရာ Lesson ဖိုင်များကို Auto ပြန်ရှာမည်
                loadLessonJson();
            });
        }

        // Lesson ComboBox - စိတ်ကြိုက် စာရိုက်ထည့်နိုင်ရန် Editable ပြုလုပ်ထားပါသည်
        if (lessonCombo != null) {
            lessonCombo.setEditable(true);
            lessonCombo.setOnAction(e -> loadLessonJson());
        }

        if (typeCombo != null) {
            typeCombo.setItems(FXCollections.observableArrayList("CONCEPT", "FILL_IN_BLANK", "MULTIPLE_CHOICE"));
            typeCombo.setOnAction(e -> handleTypeChange());
        }

        // စတင်ချိန်တွင် Lessons folder ထဲရှိ ဖိုင်များကို Scan ဖတ်၍ Lesson List တင်ပေးမည်
        loadLessonList();
    }

    /**
     * Lessons folder ထဲရှိ JSON ဖိုင်များကို Scan ဖတ်ပြီး ရွေးချယ်ထားသော Category အတွက် Lesson List ကို Auto တင်ပေးမည်
     */
    private void loadLessonList() {
        if (categoryCombo == null || lessonCombo == null) return;

        String selectedCategory = categoryCombo.getValue();
        if (selectedCategory == null) {
            selectedCategory = "CPP"; // Default Category
            categoryCombo.setValue(selectedCategory);
        }

        File folder = new File("src/main/resources/Lessons");
        List<String> lessonNumbers = new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                String prefix = selectedCategory.toUpperCase() + "_";
                for (File file : files) {
                    if (file.getName().startsWith(prefix) && file.getName().endsWith(".json")) {
                        // ဥပမာ - CPP_101.json မှ "101" ကို ခွဲထုတ်ယူမည်
                        String lessonNo = file.getName().replace(prefix, "").replace(".json", "");
                        lessonNumbers.add(lessonNo);
                    }
                }
            }
        }

        if (!lessonNumbers.isEmpty()) {
            lessonCombo.setItems(FXCollections.observableArrayList(lessonNumbers));
        } else {
            // ဖိုင်များ မရှိသေးပါက Default ရွေးချယ်နိုင်သော စာရင်း ပေးထားမည်
            lessonCombo.setItems(FXCollections.observableArrayList("101", "102", "103", "104", "105"));
        }
    }

    /**
     * Slide Type အလိုက် မလိုအပ်သော Input Field များကို Auto Disable / Enable လုပ်ပေးသည်
     */
    @FXML
    private void handleTypeChange() {
        String type = typeCombo.getValue();
        if (type == null) return;

        switch (type) {
            case "CONCEPT":
                if (codeSnippetArea != null) codeSnippetArea.setDisable(false);
                setOptionFieldsDisable(true);
                clearOptionInputs();
                break;

            case "MULTIPLE_CHOICE":
                if (codeSnippetArea != null) {
                    codeSnippetArea.setDisable(true);
                    codeSnippetArea.clear();
                }
                setOptionFieldsDisable(false);
                break;

            case "FILL_IN_BLANK":
                if (codeSnippetArea != null) codeSnippetArea.setDisable(false);
                setOptionFieldsDisable(false);
                break;
        }
    }

    private void setOptionFieldsDisable(boolean disable) {
        if (optA != null) optA.setDisable(disable);
        if (optB != null) optB.setDisable(disable);
        if (optC != null) optC.setDisable(disable);
        if (optD != null) optD.setDisable(disable);
        if (correctAnswerInput != null) correctAnswerInput.setDisable(disable);
    }

    private void loadLessonJson() {
        if (categoryCombo == null || lessonCombo == null) return;

        String category = categoryCombo.getValue();
        String lessonNo = lessonCombo.getValue();

        if (category == null || lessonNo == null || lessonNo.trim().isEmpty()) return;

        String fileName = category.toUpperCase() + "_" + lessonNo.trim() + ".json";
        File file = new File("src/main/resources/Lessons/" + fileName);

        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                currentLesson = gson.fromJson(reader, Lesson.class);

                if (currentLesson != null && currentLesson.getSlides() != null) {
                    slideObservableList.setAll(currentLesson.getSlides());
                } else {
                    slideObservableList.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // ဖိုင်မရှိသေးပါက (Lesson သစ်) Table ကို ရှင်းပေးမည်
            slideObservableList.clear();
            currentLesson = null;
        }
    }

    @FXML
    private void handleSaveQuestion() {
        try {
            if (categoryCombo == null || lessonCombo == null) return;

            String category = categoryCombo.getValue();
            String lessonNo = lessonCombo.getValue();

            if (category == null || lessonNo == null || lessonNo.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Category နှင့် Lesson Number ကို ရွေးချယ်ပါ သို့မဟုတ် ရိုက်ထည့်ပါ!");
                return;
            }

            lessonNo = lessonNo.trim();
            String type = (typeCombo != null && typeCombo.getValue() != null) ? typeCombo.getValue() : "MULTIPLE_CHOICE";
            String slideId = category.toUpperCase() + "_" + lessonNo + "_S" + (slideObservableList.size() + 1);
            String title = slideTitleInput != null ? slideTitleInput.getText() : "";
            String prompt = promptArea != null ? promptArea.getText() : "";

            // Type အလိုက် Code Snippet သတ်မှတ်ခြင်း
            String codeSnippet = (type.equals("MULTIPLE_CHOICE") || codeSnippetArea == null) ? null : codeSnippetArea.getText();

            List<String> options = null;
            String correctAnswer = null;

            // CONCEPT မှလွဲ၍ ကျန် Type များအတွက် Options နှင့် Answer ယူမည်
            if (!type.equals("CONCEPT")) {
                options = new ArrayList<>();
                if (optA != null && !optA.getText().isEmpty()) options.add(optA.getText());
                if (optB != null && !optB.getText().isEmpty()) options.add(optB.getText());
                if (optC != null && !optC.getText().isEmpty()) options.add(optC.getText());
                if (optD != null && !optD.getText().isEmpty()) options.add(optD.getText());

                correctAnswer = correctAnswerInput != null ? correctAnswerInput.getText() : "";
            }

            Slide newSlide = new Slide(slideId, type, title, prompt, codeSnippet, options, correctAnswer);

            // List ထဲသို့ ပေါင်းထည့်ပြီး JSON သိမ်းဆည်းမည်
            slideObservableList.add(newSlide);
            saveToJsonFile(category, lessonNo);

            // Save ပြီးပါက Lesson List ကို Update ပြုလုပ်ပေးမည် (ဖိုင်သစ်အတွက် ComboBox ထဲ ရောက်သွားစေရန်)
            loadLessonList();
            lessonCombo.setValue(lessonNo);

            clearInputs();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Slide အသစ်ကို အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Save လုပ်ရာတွင် အမှားအယွင်း ဖြစ်ပေါ်ခဲ့သည်: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteQuestion() {
        if (slideTable == null) return;

        Slide selectedSlide = slideTable.getSelectionModel().getSelectedItem();

        if (selectedSlide == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "ကျေးဇူးပြု၍ ဖျက်လိုသော Slide/Question ကို Table ထဲမှ ရွေးချယ်ပါ!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "ဒီ Slide ကို ဖျက်ရန် သေချာပါသလား?", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                slideObservableList.remove(selectedSlide);

                if (categoryCombo != null && lessonCombo != null) {
                    String category = categoryCombo.getValue();
                    String lessonNo = lessonCombo.getValue();
                    if (category != null && lessonNo != null) {
                        saveToJsonFile(category, lessonNo.trim());
                    }
                }

                clearInputs();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Slide ကို အောင်မြင်စွာ ဖျက်ပြီးပါပြီ!");
            }
        });
    }

    private void saveToJsonFile(String category, String lessonNo) {
        String fileName = category.toUpperCase() + "_" + lessonNo + ".json";
        File folder = new File("src/main/resources/Lessons");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, fileName);

        if (currentLesson == null) {
            currentLesson = new Lesson();
            currentLesson.setLessonId(category.toUpperCase() + "_" + lessonNo);
            currentLesson.setTitle(category + " Lesson " + lessonNo);
        }

        currentLesson.setSlides(new ArrayList<>(slideObservableList));

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(currentLesson, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateForm(Slide slide) {
        if (typeCombo != null) {
            typeCombo.setValue(slide.getType());
            handleTypeChange();
        }
        if (slideTitleInput != null) slideTitleInput.setText(slide.getTitle());
        if (promptArea != null) promptArea.setText(slide.getPrompt());
        if (codeSnippetArea != null) codeSnippetArea.setText(slide.getCodeSnippet() != null ? slide.getCodeSnippet() : "");
        if (correctAnswerInput != null) correctAnswerInput.setText(slide.getCorrectAnswer() != null ? slide.getCorrectAnswer() : "");

        List<String> opts = slide.getOptions();
        if (opts != null) {
            if (optA != null) optA.setText(opts.size() > 0 ? opts.get(0) : "");
            if (optB != null) optB.setText(opts.size() > 1 ? opts.get(1) : "");
            if (optC != null) optC.setText(opts.size() > 2 ? opts.get(2) : "");
            if (optD != null) optD.setText(opts.size() > 3 ? opts.get(3) : "");
        } else {
            clearOptionInputs();
        }
    }

    private void clearInputs() {
        if (slideTitleInput != null) slideTitleInput.clear();
        if (promptArea != null) promptArea.clear();
        if (codeSnippetArea != null) codeSnippetArea.clear();
        clearOptionInputs();
    }

    private void clearOptionInputs() {
        if (optA != null) optA.clear();
        if (optB != null) optB.clear();
        if (optC != null) optC.clear();
        if (optD != null) optD.clear();
        if (correctAnswerInput != null) correctAnswerInput.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}