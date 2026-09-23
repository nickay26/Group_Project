package Project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class CppOverviewController implements Initializable {

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
        if (titleLabel != null) titleLabel.setText("C++");
        if (subtitleLabel != null) subtitleLabel.setText("Language Overview");
        if (detailsLabel != null) {
            detailsLabel.setText(
                    "📌 INTRODUCTION\n" +
                            "C++ is a high-performance general-purpose programming language that provides low-level memory control, extreme speed, and direct hardware access.\n\n" +
                            "🎯 USE FOR WHAT\n" +
                            "• Game Engines & 3D Real-time Graphics\n" +
                            "• Operating Systems & Low-level System Drivers\n" +
                            "• High-Frequency Financial Trading & Embedded Systems"
            );
        }

        if (basicTitleLabel != null) basicTitleLabel.setText("C++ Basic");
        if (basicDescLabel != null) basicDescLabel.setText("Understand C++ syntax, basic data types, condition statements, and loops.");

        if (interTitleLabel != null) interTitleLabel.setText("C++ Intermediate");
        if (interDescLabel != null) interDescLabel.setText("Master Pointers, Memory Management (Stack/Heap), References, and OOP concepts.");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("WelcomeView.fxml", "Mimo Platform - Welcome");
    }

    @FXML
    private void handleStartBasic() {
        // C++ Basic Lesson သို့ သွားမည့် Logic
        SceneManager.switchScene("LessonView.fxml", "Mimo Platform - C++ Basic");
    }

    @FXML
    private void handleStartIntermediate() {
        // C++ Intermediate Lesson သို့ သွားမည့် Logic
        SceneManager.switchScene("LessonView.fxml", "Mimo Platform - C++ Intermediate");
    }
}