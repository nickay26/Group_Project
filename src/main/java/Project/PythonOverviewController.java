package Project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class PythonOverviewController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label detailsLabel;

    @FXML private Label basicTitleLabel;
    @FXML private Label basicDescLabel;

    @FXML private Label interTitleLabel;
    @FXML private Label interDescLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupOverviewData();
    }

    private void setupOverviewData() {
        if (titleLabel != null) titleLabel.setText("Python");
        if (subtitleLabel != null) subtitleLabel.setText("Language Overview");
        if (detailsLabel != null) {
            detailsLabel.setText(
                    "📌 INTRODUCTION\n" +
                            "Python is a beginner-friendly, interpreted programming language known for its clean syntax, rapid development capabilities, and powerful libraries.\n\n" +
                            "🎯 USE FOR WHAT\n" +
                            "• Artificial Intelligence & Machine Learning\n" +
                            "• Data Science, Analytics & Visualization\n" +
                            "• Web Backend Services & Automation Scripts"
            );
        }

        if (basicTitleLabel != null) basicTitleLabel.setText("Python Basic");
        if (basicDescLabel != null) basicDescLabel.setText("Learn Python basics, variables, user input, and string formatting.");

        if (interTitleLabel != null) interTitleLabel.setText("Python Intermediate");
        if (interDescLabel != null) interDescLabel.setText("Work with Data Structures (Lists, Dicts, Sets), Control Flow, and Functions.");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("WelcomeView.fxml", "Mimo Platform - Welcome");
    }

    @FXML
    private void handleStartBasic() {
        // Python Basic Lesson သို့ သွားမည့် Logic
        SceneManager.switchScene("LessonView.fxml", "Mimo Platform - Python Basic");
    }

    @FXML
    private void handleStartIntermediate() {
        // Python Intermediate Lesson သို့ သွားမည့် Logic
        SceneManager.switchScene("LessonView.fxml", "Mimo Platform - Python Intermediate");
    }
}