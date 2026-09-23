package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminController {

    // View Containers (Main Content Area)
    @FXML private VBox dashboardView;
    @FXML private HBox manageQuestionsView;
    @FXML private VBox userAnalyticsView; // Option အတွက် ထည့်သွင်းထားသည်
    @FXML private VBox settingsView;      // Option အတွက် ထည့်သွင်းထားသည်

    // Sidebar Navigation Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnManageQuestions;
    @FXML private Button btnUserAnalytics;
    @FXML private Button btnSettings;

    // Header Labels
    @FXML private Label pageTitle;
    @FXML private Label pageSubTitle;

    @FXML
    public void initialize() {
        // စတင်ချိန်တွင် Dashboard View ကို Default ပြပေးမည်
        showDashboardView();
    }

    @FXML
    private void showDashboardView() {
        hideAllViews();
        if (dashboardView != null) dashboardView.setVisible(true);

        if (pageTitle != null) pageTitle.setText("Dashboard Overview");
        if (pageSubTitle != null) pageSubTitle.setText("Monitor real-time user statistics and system performance");

        setActiveButton(btnDashboard);
    }

    @FXML
    private void showManageQuestionsView() {
        hideAllViews();
        if (manageQuestionsView != null) manageQuestionsView.setVisible(true);

        if (pageTitle != null) pageTitle.setText("Manage Questions");
        if (pageSubTitle != null) pageSubTitle.setText("Create, update, and manage quiz slides and JSON data");

        setActiveButton(btnManageQuestions);
    }

    @FXML
    private void showUserAnalyticsView() {
        hideAllViews();
        if (userAnalyticsView != null) userAnalyticsView.setVisible(true);

        if (pageTitle != null) pageTitle.setText("User Analytics");
        if (pageSubTitle != null) pageSubTitle.setText("View detailed user engagement and activity metrics");

        setActiveButton(btnUserAnalytics);
    }

    @FXML
    private void showSettingsView() {
        hideAllViews();
        if (settingsView != null) settingsView.setVisible(true);

        if (pageTitle != null) pageTitle.setText("Settings");
        if (pageSubTitle != null) pageSubTitle.setText("Configure application preferences and system settings");

        setActiveButton(btnSettings);
    }

    private void hideAllViews() {
        if (dashboardView != null) dashboardView.setVisible(false);
        if (manageQuestionsView != null) manageQuestionsView.setVisible(false);
        if (userAnalyticsView != null) userAnalyticsView.setVisible(false);
        if (settingsView != null) settingsView.setVisible(false);
    }

    private void setActiveButton(Button activeButton) {
        // Active Button Class များကို Reset လုပ်ခြင်း
        if (btnDashboard != null) btnDashboard.getStyleClass().remove("sidebar-btn-active");
        if (btnManageQuestions != null) btnManageQuestions.getStyleClass().remove("sidebar-btn-active");
        if (btnUserAnalytics != null) btnUserAnalytics.getStyleClass().remove("sidebar-btn-active");
        if (btnSettings != null) btnSettings.getStyleClass().remove("sidebar-btn-active");

        // နှိပ်လိုက်သော Button တစ်ခုတည်းကို Active Class ပေးခြင်း
        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-btn-active")) {
            activeButton.getStyleClass().add("sidebar-btn-active");
        }
    }
}