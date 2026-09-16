package Project;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaygroundPageController {

    @FXML private ComboBox<String> languagePicker;
    @FXML private TextArea codeEditor;
    @FXML private TextArea outputArea;
    @FXML private TextArea aiReviewArea;
    @FXML private Button runBtn;
    @FXML private Button reviewBtn;

    @FXML
    public void initialize() {
        languagePicker.setItems(FXCollections.observableArrayList("C++", "Python", "Java"));
        languagePicker.setValue("C++");

        // Set default boilerplate code on language selection
        setTemplateCode("C++");
        languagePicker.setOnAction(e -> setTemplateCode(languagePicker.getValue()));
    }

    @FXML
    public void handleRunCode() {
        String lang = languagePicker.getValue().toLowerCase();
        String code = codeEditor.getText();

        if (code.trim().isEmpty()) {
            outputArea.setText("Error: Editor is empty.");
            return;
        }

        outputArea.setText("Running code on PythonAnywhere sandbox...");
        runBtn.setDisable(true);

        ApiClient.executeCodeAsync(lang, code)
                .thenAccept(responseJson -> Platform.runLater(() -> {
                    outputArea.setText(extractJsonField(responseJson, "output"));
                    runBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        outputArea.setText("Execution Error: " + ex.getMessage());
                        runBtn.setDisable(false);
                    });
                    return null;
                });
    }

    @FXML
    public void handleReviewCode() {
        String lang = languagePicker.getValue().toLowerCase();
        String code = codeEditor.getText();

        if (code.trim().isEmpty()) {
            aiReviewArea.setText("Error: Editor is empty.");
            return;
        }

        aiReviewArea.setText("Analyzing code with Gemini AI...");
        reviewBtn.setDisable(true);

        ApiClient.reviewCodeAsync(lang, code)
                .thenAccept(responseJson -> Platform.runLater(() -> {
                    aiReviewArea.setText(extractJsonField(responseJson, "review"));
                    reviewBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        aiReviewArea.setText("AI Review Error: " + ex.getMessage());
                        reviewBtn.setDisable(false);
                    });
                    return null;
                });
    }

    private void setTemplateCode(String language) {
        switch (language) {
            case "C++" -> codeEditor.setText("""
                #include <iostream>

                int main() {
                    std::cout << "Hello from Mimo Playground!" << std::endl;
                    return 0;
                }
                """);
            case "Python" -> codeEditor.setText("""
                print("Hello from Mimo Playground!")
                """);
            case "Java" -> codeEditor.setText("""
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello from Mimo Playground!");
                    }
                }
                """);
        }
    }

    // Unescapes JSON string field without needing third-party libraries
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