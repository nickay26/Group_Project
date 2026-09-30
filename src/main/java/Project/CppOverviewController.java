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
        SceneManager.switchScene("WelcomeView.fxml", "Sodex Platform - Welcome");
    }

    @FXML
    private void handleStartBasic() {
        checkLoginAndNavigate("CPP_BASIC");
    }

    @FXML
    private void handleStartIntermediate() {
        checkLoginAndNavigate("CPP_INTERMEDIATE");
    }

    private void checkLoginAndNavigate(String courseId) {
        // 1. နှိပ်လိုက်သည့် Course ID ကို Session တွင် အရင်သတ်မှတ်မည်
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