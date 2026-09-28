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
        checkLoginAndNavigate("JAVA_BASIC");
    }

    @FXML
    private void handleStartIntermediate() {
        checkLoginAndNavigate("JAVA_INTERMEDIATE");
    }

    private void checkLoginAndNavigate(String courseId) {
        // 1. နှိပ်လိုက်သည့် Course ID ကို Session တွင် သတ်မှတ်မည်
        UserSession.setActiveCourseId(courseId);

        // 2. Overview မှ လာခြင်း ဖြစ်သောကြောင့် Flag ကို true သတ်မှတ်မည်
        UserSession.setRedirectedFromOverview(true);

        // 3. User Login ဝင်ထားခြင်း ရှိ/မရှိ စစ်ဆေးမည်
        if (UserSession.getInstance() != null && UserSession.getUserId() != -1) {
            // Login ဝင်ထားပြီးပါက Main Dashboard သို့ တန်းသွားမည်
            SceneManager.switchScene("MainLayoutView.fxml", "Mimo Platform - Dashboard");
        } else {
            // Account မရှိသေး/Login မဝင်ရသေးပါက Register ပါဝင်သော WelcomeView သို့ ပို့ပေးပါမည်
            SceneManager.switchScene("WelcomeView.fxml", "Mimo Platform - Welcome");
        }
    }
}