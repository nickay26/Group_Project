package Project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainLayoutController {

    private static MainLayoutController instance;

    @FXML private StackPane contentArea;
    @FXML private Label currentCourseLabel;

    @FXML private Label topStreakLabel;
    @FXML private Label topBrainLabel;
    @FXML private Label topCoinLabel;

    @FXML private Button learnBtn;
    @FXML private Button coursesBtn;
    @FXML private Button playgroundBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button practiceBtn;
    @FXML private Button gamesBtn;
    @FXML private Button profileBtn;
    @FXML private Label brainLabel;
    private ContextMenu profileMenu;
    @FXML private Button premiumBtn;


    private List<Button> navButtons;
    private Timeline brainAutoRefreshTimer; // ⭐ Auto Refresh Timer

    @FXML
    public void initialize() {
        instance = this;
        navButtons = Arrays.asList(learnBtn, coursesBtn, playgroundBtn, leaderboardBtn, practiceBtn, gamesBtn);

        // Fetch User Activity (Streak & XP) and Course Title
        updateHeaderStats();
        fetchAndUpdateUserActivity(); // <--- ⭐ ဒီနေရာတွင် ခေါ်ပေးပါ
        updateCourseTitle();

        startBrainAutoRefresh();

        if (UserSession.isRedirectedFromOverview()) {
            handleNavLearn();
        } else {
            handleNavCourses();
        }
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    public void updateBrainDisplay(int count) {
        if (topBrainLabel != null) {
            topBrainLabel.setText(" " + count);
        }
    }

    public void setCourseTitle(String title) {
        if (currentCourseLabel != null) {
            currentCourseLabel.setText(title != null ? title : "No Active Course");
        }
    }

    public void updateHeaderStats() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            // ===== 🔍 DEBUG =====
            System.out.println("=== HEADER DEBUG ===");
            System.out.println("User ID: " + currentUser.getUserId());
            System.out.println("isPremium (local): " + currentUser.isPremium());
            System.out.println("subscriptionType: " + currentUser.getSubscriptionType());
            System.out.println("brainCount: " + currentUser.getBrainCount());
            System.out.println("brainDisplay: " + PremiumService.getBrainDisplay(currentUser));
            System.out.println("====================");
            // ====================

            topStreakLabel.setText(" " + currentUser.getStreak());

            String brainDisplay = PremiumService.getBrainDisplay(currentUser);
            topBrainLabel.setText(" " + brainDisplay);

            if (PremiumService.isPremium(currentUser)) {
                topBrainLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
            } else {
                topBrainLabel.setStyle("-fx-text-fill: white;");
            }

            topCoinLabel.setText(" " + currentUser.getCoin());
        }
    }

    public void updateCourseTitle() {
        String activeCourseId = UserSession.getActiveCourseId();
        int userId = UserSession.getUserId();

        if (activeCourseId != null) {
            List<Course> courses = DatabaseManager.getCoursesForUser(userId);
            if (courses != null) {
                for (Course c : courses) {
                    if (c.getCourseId().equalsIgnoreCase(activeCourseId)) {
                        setCourseTitle(c.getTitle());
                        return;
                    }
                }
            }
        }
        setCourseTitle("No Active Course");
    }

    // Backend မှ Streak နှင့် XP ကို Sync လုပ်ပြီး UI ပေါ်တွင် Update လုပ်ပေးမည့် Method
    public void fetchAndUpdateUserActivity() {
        int userId = UserSession.getUserId();
        if (userId <= 0) return;

        ApiClient.getUserActivityAsync(userId).thenAccept(response -> {
            if (response != null && !response.isEmpty()) {
                try {
                    org.json.JSONObject json = new org.json.JSONObject(response);
                    if (json.has("streak")) {
                        int streak = json.getInt("streak");
                        int xp = json.optInt("xp", 0);

                        // UserSession ထဲသို့ Sync ထည့်ခြင်း
                        if (UserSession.getCurrentUser() != null) {
                            UserSession.getCurrentUser().setStreak(streak);
                            UserSession.getCurrentUser().setXp(xp);
                        }

                        // UI Thread ပေါ်တွင် Streak Counter ပြင်ပေးခြင်း
                        Platform.runLater(() -> {
                            if (topStreakLabel != null) {
                                topStreakLabel.setText(" " + streak);
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing activity response: " + e.getMessage());
                }
            }
        });
    }

    // ============================================
    // ⭐ BRAIN AUTO-REFRESH SYSTEM
    // ============================================
    private void startBrainAutoRefresh() {
        // App စတင်သည်နှင့် အရင်ဆုံး တစ်ကြိမ် Fetch လုပ်မည်
        fetchAndUpdateBrainStatus();

        // ၁ မိနစ်လျှင် တစ်ကြိမ် နောက်ကွယ်မှ Auto Refresh ပြုလုပ်မည်
        brainAutoRefreshTimer = new Timeline(
                new KeyFrame(Duration.minutes(1), e -> fetchAndUpdateBrainStatus())
        );
        brainAutoRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        brainAutoRefreshTimer.play();
    }

    public void fetchAndUpdateBrainStatus() {
        int userId = UserSession.getUserId();
        if (userId <= 0) return;

        // ApiClient မှတဆင့် Async ခေါ်ဆိုခြင်း
        ApiClient.getUserBrainsAsync(userId).thenAccept(response -> {
            if (response != null && !response.isEmpty()) {
                try {
                    org.json.JSONObject json = new org.json.JSONObject(response);
                    if (json.has("brain_count")) {
                        int updatedBrainCount = json.getInt("brain_count");

                        // UserSession ထဲတွင် update ပြုလုပ်ခြင်း
                        if (UserSession.getCurrentUser() != null) {
                            UserSession.getCurrentUser().setBrainCount(updatedBrainCount);
                        }

                        // UI Thread ပေါ်တွင် Display ပြင်ပေးခြင်း
                        Platform.runLater(() -> updateBrainDisplay(updatedBrainCount));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing brain response: " + e.getMessage());
                }
            }
        });
    }

    // ============================================
    // LOAD PRACTICE QUESTION (Sidebar ထဲ)
    // ============================================
    public void loadPracticeQuestion(String language, String type) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Project/PracticeQuestionView.fxml"));
            Parent view = loader.load();

            // Controller ကို ယူ
            PracticeQuestionController controller = loader.getController();

            // Question ဆွဲ
            List<QuestionData> list = JsonUtils.getPracticeListByType(language, type);
            QuestionData question;

            if (list.isEmpty()) {
                question = new QuestionData(
                        language, type,
                        "Complete the task below.",
                        "Write code to solve this problem.",
                        getStarterCode(language),
                        "", "Think step by step.", "",
                        null, null, "write-code", "console"
                );
            } else {
                java.util.Random rand = new java.util.Random();
                question = list.get(rand.nextInt(list.size()));
            }

            controller.setQuestion(question);

            // ✅ contentArea ထဲ ထည့် (Sidebar မပျောက်)
            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Starter Code Helper
    private String getStarterCode(String language) {
        switch (language) {
            case "Java":
                return "public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Write your code here\n" +
                        "    }\n" +
                        "}";
            case "C++":
                return "#include <iostream>\nusing namespace std;\n\n" +
                        "int main() {\n" +
                        "    // Write your code here\n" +
                        "    return 0;\n" +
                        "}";
            case "Python":
                return "# Write your Python code here\n";
            default:
                return "";
        }
    }

    @FXML
    public void handleNavLearn() {
        setActiveButton(learnBtn);
        loadPage("LearnPage.fxml");
    }

    @FXML
    public void handleNavCourses() {
        setActiveButton(coursesBtn);
        loadPage("CoursePage.fxml");
    }

    @FXML
    public void handleNavPlayground() {
        setActiveButton(playgroundBtn);
        loadPage("PlaygroundDashboardView.fxml");
    }

    @FXML
    public void handleLeaderBoard() {
        setActiveButton(leaderboardBtn);
        loadPage("LeaderboardView.fxml");
    }

    @FXML
    public void handleNavPractice() {
        setActiveButton(practiceBtn);
        loadPage("PracticeView.fxml");
    }

    @FXML
    private void handleGame() {
        setActiveButton(gamesBtn);
        loadPage("GameModeView.fxml");
    }

    @FXML
    private void handleProfileClick() {
        if (profileMenu == null) {
            profileMenu = new ContextMenu();

            MenuItem editProfileItem = new MenuItem("Go to profile");
            MenuItem settingsItem = new MenuItem("Settings");
            MenuItem helpItem = new MenuItem("Help");
            MenuItem logoutItem = new MenuItem("Log out");

            // Event Handlers
            editProfileItem.setOnAction(e -> handleEditProfile());
            settingsItem.setOnAction(e -> handleSettings());
            helpItem.setOnAction(e -> handleHelp());
            logoutItem.setOnAction(e -> handleLogout());

            profileMenu.getItems().addAll(editProfileItem, settingsItem, helpItem, logoutItem);
        }

        // Dropdown Menu ဖွင့်/ပိတ် ပြုလုပ်ခြင်း
        if (profileMenu.isShowing()) {
            profileMenu.hide();
        } else {
            profileMenu.show(profileBtn, Side.BOTTOM, -30, 8);
        }
    }

    private void handleEditProfile() {
        loadPage("ProfileView.fxml");
    }

    private void handleSettings() {
        loadPage("SettingsView.fxml");
    }

    private void handleHelp() {
        loadPage("HelpView.fxml");
    }

    @FXML
    public void handlePremium() {
        // Premium Page ကို content area ထဲမှာ Load လုပ်
        loadPage("PremiumView.fxml");
    }

    private void handleLogout() {
        // Timer ကို ရပ်ဆိုင်းခြင်း
        if (brainAutoRefreshTimer != null) {
            brainAutoRefreshTimer.stop();
        }
        // Session clear ပြုလုပ်ခြင်း
        UserSession.clearSession();
        SceneManager.switchScene("WelcomeView.fxml");
    }

    public FXMLLoader loadPage(String fxmlFileName) {
        try {
            String resourcePath = fxmlFileName.startsWith("/") ? fxmlFileName : "/fxml/" + fxmlFileName;
            URL resource = getClass().getResource(resourcePath);

            if (resource == null) {
                resource = getClass().getResource("/" + fxmlFileName);
            }
            if (resource == null) {
                resource = getClass().getResource(fxmlFileName);
            }

            if (resource == null) {
                System.err.println("Could not find FXML file: " + fxmlFileName);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            contentArea.getChildren().setAll(view);

            // FXML Name အလိုက် Sidebar Highlight ပြောင်းမည်
            updateSidebarHighlight(fxmlFileName);

            return loader;
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlFileName);
            e.printStackTrace();
            return null;
        }
    }

    private void updateSidebarHighlight(String fxmlFileName) {
        if (fxmlFileName.contains("LearnPage")) {
            setActiveButton(learnBtn);
        } else if (fxmlFileName.contains("Course")) {
            setActiveButton(coursesBtn);
        } else if (fxmlFileName.contains("Playground")) {
            setActiveButton(playgroundBtn);
        } else if (fxmlFileName.contains("Leaderboard")) {
            setActiveButton(leaderboardBtn);
        } else if (fxmlFileName.contains("Practice")) {
            setActiveButton(practiceBtn);
        } else if (fxmlFileName.contains("Game")) {
            setActiveButton(gamesBtn);
        }
    }
    public void updateUserStats() {
        int userId = UserSession.getUserId();
        if (userId <= 0) return;

        User user = DatabaseManager.getUserById(userId);
        if (user == null) return;

        // Brain Count
        if (brainLabel != null) {
            int brainCount = user.getBrainCount();
            if (brainCount >= 999999) {
                brainLabel.setText(" ∞");
            } else {
                brainLabel.setText(" " + brainCount);
            }
        }
    }

    private void setActiveButton(Button activeButton) {
        if (navButtons == null || activeButton == null) return;

        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-button-active");
            if (!btn.getStyleClass().contains("nav-button")) {
                btn.getStyleClass().add("nav-button");
            }
        }

        activeButton.getStyleClass().remove("nav-button");
        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }

    }

    // ============================================
// ⭐ STATS CLICK ACTIONS (Mimo-Style)
// ============================================

    @FXML
    private void handleStreakClick() {
        User currentUser = UserSession.getCurrentUser();
        int streak = currentUser != null ? currentUser.getStreak() : 0;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Streak Info");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-padding: 20; -fx-pref-width: 300px;");

        Label iconLabel = new Label("🔥");
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label(streak + " Day Streak!");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label descLabel = new Label("Complete at least one lesson every day to keep your streak alive!");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-alignment: center; -fx-text-fill: #666;");

        content.getChildren().addAll(iconLabel, titleLabel, descLabel);
        dialogPane.setContent(content);
        dialog.showAndWait();
    }

    @FXML
    private void handleBrainClick() {
        User currentUser = UserSession.getCurrentUser();
        int currentBrains = currentUser != null ? currentUser.getBrainCount() : 0;
        int coins = currentUser != null ? currentUser.getCoin() : 0;
        int refillCost = 50;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Brain Refill");

        DialogPane dialogPane = dialog.getDialogPane();
        ButtonType refillBtnType = new ButtonType("Refill (" + refillCost + " 🪙)", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(refillBtnType, ButtonType.CANCEL);

        // Disable refill button if user doesn't have enough coins or brains are already full
        Button refillButton = (Button) dialogPane.lookupButton(refillBtnType);
        if (coins < refillCost || currentBrains >= 5) {
            refillButton.setDisable(true);
        }

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-padding: 20; -fx-pref-width: 320px;");

        Label iconLabel = new Label("🧠");
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label("Brains: " + currentBrains + " / 5");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Brains refill automatically every 30 minutes, or you can restore them instantly using coins.");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-text-alignment: center; -fx-text-fill: #555;");

        content.getChildren().addAll(iconLabel, titleLabel, infoLabel);
        dialogPane.setContent(content);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == refillBtnType) {
                int userId = UserSession.getUserId();

                // Call API to refill brains
                ApiClient.refillBrainsAsync(userId, refillCost).thenAccept(response -> {
                    Platform.runLater(() -> {
                        // Update session and header stats
                        if (currentUser != null) {
                            currentUser.setCoin(currentUser.getCoin() - refillCost);
                            currentUser.setBrainCount(5);
                        }
                        updateHeaderStats();
                        fetchAndUpdateBrainStatus();
                    });
                });
            }
        });
    }

    @FXML
    private void handleCoinClick() {
        User currentUser = UserSession.getCurrentUser();
        int coins = currentUser != null ? currentUser.getCoin() : 0;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coin Balance");
        alert.setHeaderText("🪙 You have " + coins + " Coins");
        alert.setContentText("Earn coins by completing practice modules, maintaining streaks, and winning challenges! Use coins to refill brains or unlock premium features.");

        // Add button to navigate directly to the shop/premium view
        ButtonType shopButton = new ButtonType("Go to Shop", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(shopButton, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == shopButton) {
                handlePremium(); // Navigates to PremiumView.fxml
            }
        });
    }
}