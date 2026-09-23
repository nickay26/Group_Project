package Project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class JavaOverviewController implements Initializable {

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
        if (titleLabel != null) titleLabel.setText("Java");
        if (subtitleLabel != null) subtitleLabel.setText("Language Overview");
        if (detailsLabel != null) {
            detailsLabel.setText(
                    "📌 INTRODUCTION\n" +
                            "Java is a high-level, object-oriented programming language designed to build robust, secure, and scalable applications with minimal implementation dependencies.\n\n" +
                            "🎯 USE FOR WHAT\n" +
                            "• Enterprise Software & Backend Systems\n" +
                            "• Android Mobile Application Development\n" +
                            "• High-Scale Web Services & Cloud Architecture"
            );
        }

        if (basicTitleLabel != null) basicTitleLabel.setText("Java Basic");
        if (basicDescLabel != null) basicDescLabel.setText("Learn basic syntax, variables, primitive types, and control flow.");

        if (interTitleLabel != null) interTitleLabel.setText("Java Intermediate");
        if (interDescLabel != null) interDescLabel.setText("Master Object-Oriented Programming (OOP), Classes, Inheritance, and Exceptions.");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("WelcomeView.fxml", "Mimo Platform - Welcome");
    }

    @FXML
    private void handleStartBasic() {
        // Java Basic Lesson သို့ သွားမည့် Logic
        SceneManager.switchScene("LessonView.fxml", "Mimo Platform - Java Basic");
    }

    @FXML
    private void handleStartIntermediate() {
        // Java Intermediate Lesson သို့ သွားမည့် Logic
        SceneManager.switchScene("LessonView.fxml", "Mimo Platform - Java Intermediate");
    }
}