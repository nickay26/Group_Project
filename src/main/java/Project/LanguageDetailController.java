package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class LanguageDetailController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label detailsLabel;
    @FXML private TextArea codeSampleArea;

    public void setLanguageData(String title, String subtitle, String details, String codeSample) {
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
        detailsLabel.setText(details);
        codeSampleArea.setText(codeSample);
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("WelcomeView.fxml", "Mimo Platform - Welcome");
    }
}