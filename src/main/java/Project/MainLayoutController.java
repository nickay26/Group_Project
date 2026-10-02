package Project;

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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

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
    private ContextMenu profileMenu;
    @FXML private Button premiumBtn;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        instance = this;
        navButtons = Arrays.asList(learnBtn, coursesBtn, playgroundBtn, leaderboardBtn, practiceBtn, gamesBtn);

        // Automatically fetch and display active course title
        updateHeaderStats();
        updateCourseTitle();

        // ⭐ Overview မှ လာခြင်း မဟုတ်ပါက Course Page ကို Default အဖြစ် Load လုပ်မည်
        if (UserSession.isRedirectedFromOverview()) {
            handleNavLearn();
        } else {
            handleNavCourses(); // Login ဝင်လာပါက Courses Page ကို Default Load လုပ်မည်
        }
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    public void updateBrainDisplay(int count) {
        if (topBrainLabel != null) {
            topBrainLabel.setText(String.valueOf(count));
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
        // Session clear ပြုလုပ်ခြင်း
        UserSession.clearSession();
        SceneManager.switchScene("WelcomeView.fxml");
    }

    /**
     * ⭐ Page ပြောင်းလဲသည့်အခါ Emoji + Modern Card UI Loading ပြသပေးမည်
     */
    public FXMLLoader loadPage(String fxmlFileName) {
        // FXML Name အလိုက် Sidebar Highlight အာမခံချက်ရှိအောင် အရင်ပြောင်းမည်
        updateSidebarHighlight(fxmlFileName);

        // Page အမျိုးအစားပေါ်မူတည်ပြီး Dynamic Emoji နှင့် Text သတ်မှတ်ခြင်း
        String dynamicEmoji = "🚀";
        String pageName = "Page";

        if (fxmlFileName.contains("LearnPage")) {
            dynamicEmoji = "📚";
            pageName = "Learn Path";
        } else if (fxmlFileName.contains("Course")) {
            dynamicEmoji = "🎓";
            pageName = "Courses";
        } else if (fxmlFileName.contains("Playground")) {
            dynamicEmoji = "💻";
            pageName = "Playground";
        } else if (fxmlFileName.contains("Leaderboard")) {
            dynamicEmoji = "🏆";
            pageName = "Leaderboard";
        } else if (fxmlFileName.contains("Practice")) {
            dynamicEmoji = "🎯";
            pageName = "Practice";
        } else if (fxmlFileName.contains("Game")) {
            dynamicEmoji = "🎮";
            pageName = "Games";
        } else if (fxmlFileName.contains("Profile")) {
            dynamicEmoji = "👤";
            pageName = "Profile";
        } else if (fxmlFileName.contains("Settings")) {
            dynamicEmoji = "⚙️";
            pageName = "Settings";
        } else if (fxmlFileName.contains("Help")) {
            dynamicEmoji = "❓";
            pageName = "Help & Support";
        } else if (fxmlFileName.contains("Premium")) {
            dynamicEmoji = "👑";
            pageName = "Premium";
        }

        // 1. Loading UI Elements ပြင်ဆင်ခြင်း
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: #38BDF8;");
        spinner.setMaxSize(42, 42);

        Label titleLabel = new Label("🤖 " + dynamicEmoji + " Loading " + pageName + "...");
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");

        Label subText = new Label("Preparing your workspace...");
        subText.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8; -fx-font-weight: 500;");

        // UI Card Container
        VBox loadingCard = new VBox(12, spinner, titleLabel, subText);
        loadingCard.setAlignment(Pos.CENTER);
        loadingCard.setPadding(new javafx.geometry.Insets(28, 36, 28, 36));
        loadingCard.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-background-radius: 18px;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 18px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 18, 0, 0, 6);"
        );

        // Centered Wrapper Panel
        StackPane overlayPane = new StackPane(loadingCard);
        overlayPane.setAlignment(Pos.CENTER);
        overlayPane.setStyle("-fx-background-color: #0F172A;"); // Dashboard Dark BG

        // Content Area ထဲတွင် Loading Card ကို အရင်ပြသမည်
        contentArea.getChildren().setAll(overlayPane);

        final FXMLLoader[] loadedLoader = new FXMLLoader[1];

        // 2. Background Thread ဖြင့် FXML ကို Load ပြုလုပ်မည်
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                String resourcePath = fxmlFileName.startsWith("/") ? fxmlFileName : "/fxml/" + fxmlFileName;
                URL resource = getClass().getResource(resourcePath);

                if (resource == null) {
                    resource = getClass().getResource("/" + fxmlFileName);
                }
                if (resource == null) {
                    resource = getClass().getResource(fxmlFileName);
                }

                if (resource == null) {
                    throw new IOException("Could not find FXML file: " + fxmlFileName);
                }

                FXMLLoader loader = new FXMLLoader(resource);
                Parent view = loader.load();
                loadedLoader[0] = loader;

                // Loading Card ကို မြင်သာအောင် 300ms ခဏစောင့်မည်
                Thread.sleep(300);

                return view;
            }
        };

        // 3. Load ပြီးပါက Main UI Thread ပေါ်တွင် View အသစ်ကို လဲလှယ်မည်
        loadTask.setOnSucceeded(e -> {
            Parent newView = loadTask.getValue();
            contentArea.getChildren().setAll(newView);
        });

        loadTask.setOnFailed(e -> {
            System.err.println("Error loading FXML: " + fxmlFileName);
            if (loadTask.getException() != null) {
                loadTask.getException().printStackTrace();
            }
            Label errorLabel = new Label("❌ Failed to load page: " + fxmlFileName);
            errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-font-weight: bold;");
            contentArea.getChildren().setAll(errorLabel);
        });

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();

        return loadedLoader[0];
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
}