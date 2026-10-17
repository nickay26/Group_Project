package Project.AdminClasses;

import Project.Getters_Setters.Lesson;
import Project.Getters_Setters.Slide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminQuestionsController {

    // Dropdown / Selection Controls
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> lessonCombo;

    // Slide Input Fields
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField slideTitleInput;
    @FXML private TextArea promptArea;
    @FXML private TextArea codeSnippetArea;
    @FXML private TextField optA, optB, optC, optD;
    @FXML private TextField correctAnswerInput;

    // CheckBoxes for Premium and Interactive
    @FXML private CheckBox isPremiumCheckBox;
    @FXML private CheckBox isInteractiveCheckBox;

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

    // Select လုပ်ထားသော Slide ကို မှတ်ထားရန် Variable
    private Slide selectedSlideForEdit = null;

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("slideId"));
        if (colType != null) colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (colTitle != null) colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colPrompt != null) colPrompt.setCellValueFactory(new PropertyValueFactory<>("prompt"));
        if (colAnswer != null) colAnswer.setCellValueFactory(new PropertyValueFactory<>("correctAnswer"));

        if (slideTable != null) {
            slideTable.setItems(slideObservableList);
            slideTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedSlideForEdit = newVal; // Edit ပြုလုပ်ရန် Select လုပ်ထားသော Slide ကို မှတ်မည်
                    populateForm(newVal);
                }
            });
        }

        if (categoryCombo != null) {
            categoryCombo.setItems(FXCollections.observableArrayList("CPP", "JAVA", "PYTHON"));
            categoryCombo.setOnAction(e -> {
                loadLessonList();
                loadLessonJson();
            });
        }

        if (lessonCombo != null) {
            lessonCombo.setEditable(true);
            lessonCombo.setOnAction(e -> loadLessonJson());
        }

        if (typeCombo != null) {
            typeCombo.setItems(FXCollections.observableArrayList("CONCEPT", "FILL_IN_BLANK", "MULTIPLE_CHOICE"));
            typeCombo.setOnAction(e -> handleTypeChange());
        }

        loadLessonList();
    }

    // Language အလိုက် Correct File Prefix ယူရန် (PYTHON -> PY_, CPP -> CPP_, JAVA -> JAVA_)
    private String getPrefixForCategory(String category) {
        if (category == null) return "CPP_";
        if (category.equalsIgnoreCase("PYTHON")) return "PY_";
        if (category.equalsIgnoreCase("JAVA")) return "JAVA_";
        return category.toUpperCase() + "_";
    }

    private File getLessonsDirectory() {
        File folder = new File("src/main/resources/Lessons");
        if (!folder.exists() || !folder.isDirectory()) {
            folder = new File(System.getProperty("user.home") + "/Sodexa/Lessons");
        }
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    private void loadLessonList() {
        if (categoryCombo == null || lessonCombo == null) return;

        String selectedCategory = categoryCombo.getValue();
        if (selectedCategory == null) {
            selectedCategory = "CPP";
            categoryCombo.setValue(selectedCategory);
        }

        String prefix = getPrefixForCategory(selectedCategory);
        File folder = getLessonsDirectory();
        List<Integer> lessonNumbers = new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    if (name.startsWith(prefix) && name.endsWith(".json")) {
                        String lessonNoStr = name.replace(prefix, "").replace(".json", "");
                        try {
                            lessonNumbers.add(Integer.parseInt(lessonNoStr));
                        } catch (NumberFormatException e) {
                            // ignore non-integer files
                        }
                    }
                }
            }
        }

        if (!lessonNumbers.isEmpty()) {
            Collections.sort(lessonNumbers);
            List<String> lessonStrings = new ArrayList<>();
            for (Integer num : lessonNumbers) {
                lessonStrings.add(String.valueOf(num));
            }
            lessonCombo.setItems(FXCollections.observableArrayList(lessonStrings));
            lessonCombo.setValue(lessonStrings.get(0));
        } else {
            lessonCombo.setItems(FXCollections.observableArrayList("101", "102", "103", "104", "105"));
            lessonCombo.setValue("101");
        }
    }

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

        String prefix = getPrefixForCategory(category);
        String fileName = prefix + lessonNo.trim() + ".json";
        File folder = getLessonsDirectory();
        File file = new File(folder, fileName);

        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                currentLesson = gson.fromJson(reader, Lesson.class);

                if (currentLesson != null) {
                    if (isPremiumCheckBox != null) {
                        isPremiumCheckBox.setSelected(currentLesson.isPremium());
                    }
                    if (currentLesson.getSlides() != null) {
                        slideObservableList.setAll(currentLesson.getSlides());
                    } else {
                        slideObservableList.clear();
                    }
                } else {
                    slideObservableList.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            slideObservableList.clear();
            currentLesson = null;
            if (isPremiumCheckBox != null) {
                isPremiumCheckBox.setSelected(false);
            }
        }

        clearInputs();
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
            String prefix = getPrefixForCategory(category);
            String type = (typeCombo != null && typeCombo.getValue() != null) ? typeCombo.getValue() : "MULTIPLE_CHOICE";

            String slideId;
            if (selectedSlideForEdit != null) {
                slideId = selectedSlideForEdit.getSlideId();
            } else {
                slideId = prefix + lessonNo + "_S" + (slideObservableList.size() + 1);
            }

            String title = slideTitleInput != null ? slideTitleInput.getText() : "";
            String prompt = promptArea != null ? promptArea.getText() : "";
            String codeSnippet = (codeSnippetArea != null) ? codeSnippetArea.getText() : null;
            boolean isInteractive = (isInteractiveCheckBox != null) && isInteractiveCheckBox.isSelected();

            List<String> options = null;
            String correctAnswer = null;

            if (!type.equals("CONCEPT")) {
                options = new ArrayList<>();
                if (optA != null && !optA.getText().isEmpty()) options.add(optA.getText());
                if (optB != null && !optB.getText().isEmpty()) options.add(optB.getText());
                if (optC != null && !optC.getText().isEmpty()) options.add(optC.getText());
                if (optD != null && !optD.getText().isEmpty()) options.add(optD.getText());

                correctAnswer = correctAnswerInput != null ? correctAnswerInput.getText() : "";
            }

            Slide newSlide = new Slide(slideId, type, title, prompt, codeSnippet, options, correctAnswer);
            newSlide.setInteractive(isInteractive);

            if (selectedSlideForEdit != null) {
                int index = slideObservableList.indexOf(selectedSlideForEdit);
                if (index >= 0) {
                    slideObservableList.set(index, newSlide);
                }
            } else {
                slideObservableList.add(newSlide);
            }

            saveToJsonFile(category, lessonNo);

            loadLessonList();
            lessonCombo.setValue(lessonNo);

            clearInputs();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Slide ကို အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ!");

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
        String prefix = getPrefixForCategory(category);
        String fileName = prefix + lessonNo + ".json";
        File folder = getLessonsDirectory();
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, fileName);

        if (currentLesson == null) {
            currentLesson = new Lesson();
            currentLesson.setLessonId(prefix + lessonNo);
            currentLesson.setTitle(category + " Lesson " + lessonNo);
        }

        if (isPremiumCheckBox != null) {
            currentLesson.setPremium(isPremiumCheckBox.isSelected());
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

        if (isInteractiveCheckBox != null) {
            isInteractiveCheckBox.setSelected(slide.isInteractive());
        }

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

    @FXML
    private void handleNewSlide() {
        clearInputs();
    }

    @FXML
    private void clearInputs() {
        selectedSlideForEdit = null;
        if (slideTable != null) slideTable.getSelectionModel().clearSelection();
        if (slideTitleInput != null) slideTitleInput.clear();
        if (promptArea != null) promptArea.clear();
        if (codeSnippetArea != null) codeSnippetArea.clear();
        if (isInteractiveCheckBox != null) isInteractiveCheckBox.setSelected(false);
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