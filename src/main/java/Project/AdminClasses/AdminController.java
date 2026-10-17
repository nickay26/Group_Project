package Project.AdminClasses;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminController {

    // View Containers
    @FXML private VBox dashboardView;
    @FXML private HBox manageQuestionsView;
    @FXML private BorderPane managePracticeView;
    @FXML private VBox userAnalyticsView;
    @FXML private VBox settingsView;

    // Sidebar Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnManageQuestions;
    @FXML private Button btnManagePractice;
    @FXML private Button btnUserAnalytics;
    @FXML private Button btnSettings;

    // Header
    @FXML private Label pageTitle;
    @FXML private Label pageSubTitle;

    @FXML
    public void initialize() {
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
    private void showManagePracticeView() {
        hideAllViews();
        if (managePracticeView != null) managePracticeView.setVisible(true);
        if (pageTitle != null) pageTitle.setText("Manage Practice");
        if (pageSubTitle != null) pageSubTitle.setText("Create and manage practice questions");
        setActiveButton(btnManagePractice);
    }

    @FXML
    private void showUserAnalyticsView() {
        hideAllViews();
        if (userAnalyticsView != null) userAnalyticsView.setVisible(true);
        if (pageTitle != null) pageTitle.setText("User Analytics");
        if (pageSubTitle != null) pageSubTitle.setText("View detailed user engagement and activity metrics");
        setActiveButton(btnUserAnalytics);
    }

    // ============================================
    // ✅ ဒါ အဓိက — managePracticeView ထည့်ထား
    // ============================================
    private void hideAllViews() {
        if (dashboardView != null) dashboardView.setVisible(false);
        if (manageQuestionsView != null) manageQuestionsView.setVisible(false);
        if (managePracticeView != null) managePracticeView.setVisible(false);   // ← ဒါ ထည့်
        if (userAnalyticsView != null) userAnalyticsView.setVisible(false);
        if (settingsView != null) settingsView.setVisible(false);
    }

    private void setActiveButton(Button activeButton) {
        if (btnDashboard != null) btnDashboard.getStyleClass().remove("sidebar-btn-active");
        if (btnManageQuestions != null) btnManageQuestions.getStyleClass().remove("sidebar-btn-active");
        if (btnManagePractice != null) btnManagePractice.getStyleClass().remove("sidebar-btn-active");
        if (btnUserAnalytics != null) btnUserAnalytics.getStyleClass().remove("sidebar-btn-active");
        if (btnSettings != null) btnSettings.getStyleClass().remove("sidebar-btn-active");

        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-btn-active")) {
            activeButton.getStyleClass().add("sidebar-btn-active");
        }
    }
}