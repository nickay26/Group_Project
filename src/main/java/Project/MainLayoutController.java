package Project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;

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
            handleNavCourses(); // ဒါကြောင့် Login ဝင်လာရင် Courses Page နဲ့ Active Highlight အော်တို ပေါ်ပါမည်
        }
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    public void setCourseTitle(String title) {
        if (currentCourseLabel != null) {
            currentCourseLabel.setText(title != null ? title : "No Active Course");
        }
    }

    public void updateHeaderStats() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            topStreakLabel.setText(" " + currentUser.getStreak());
            topBrainLabel.setText(" " + currentUser.getBrainCount());
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
    public void handlePractice() {
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

            MenuItem editProfileItem = new MenuItem("Edit profile");
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

    private void handleLogout() {
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