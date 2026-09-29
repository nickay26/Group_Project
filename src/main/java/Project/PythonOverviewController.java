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
        SceneManager.switchScene("WelcomeView.fxml", "Sodex Platform - Welcome");
    }

    @FXML
    private void handleStartBasic() {
        checkLoginAndNavigate("PYTHON_BASIC");
    }

    @FXML
    private void handleStartIntermediate() {
        checkLoginAndNavigate("PYTHON_INTERMEDIATE");
    }

    private void checkLoginAndNavigate(String courseId) {
        // 1. နှိပ်လိုက်သည့် Course ID ကို Session တွင် သတ်မှတ်မည်
        UserSession.setActiveCourseId(courseId);

        // 2. Overview မှ လာခြင်း ဖြစ်သောကြောင့် Flag ကို အမြဲ true သတ်မှတ်ပေးမည်
        UserSession.setRedirectedFromOverview(true);

        // 3. User Login ဝင်ထားခြင်း ရှိ/မရှိ စစ်ဆေးပြီး လမ်းကြောင်းခွဲမည်
        if (UserSession.getInstance() != null && UserSession.getUserId() != -1) {
            // Login ဝင်ထားပါက Main Layout သို့ တန်းသွားမည် (MainLayout မှတစ်ဆင့် Learn Page သို့ ရောက်မည်)
            SceneManager.switchScene("MainLayoutView.fxml", "Sodex Platform - Dashboard");
        } else {
            // Login မဝင်ရသေးပါက Login View သို့ ပို့ပေးမည်
            SceneManager.switchScene("WelcomeView.fxml", "Sodex Platform - Login");
        }
    }
}