package Project;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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

    @FXML private Button learnBtn;
    @FXML private Button coursesBtn;
    @FXML private Button playgroundBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button practiceBtn;
    @FXML private Button gamesBtn;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        instance = this;
        navButtons = Arrays.asList(learnBtn, coursesBtn, playgroundBtn, leaderboardBtn, practiceBtn, gamesBtn);

        // Active Course Title ကို Update လုပ်မည်
        updateCourseTitle();

        // ⭐ Overview မှ လာခြင်း မဟုတ်ပါက Course Page ကို Default အဖြစ် Load လုပ်မည်
        if (UserSession.isRedirectedFromOverview()) {
            handleNavLearn();
        } else {
            handleNavCourses();
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

    @FXML
    public void handleNavLearn() {
        loadPageWithLoading("LearnPage.fxml");
    }

    @FXML
    public void handleNavCourses() {
        loadPageWithLoading("CoursePage.fxml");
    }

    @FXML
    public void handleNavPlayground() {
        loadPageWithLoading("PlaygroundDashboardView.fxml");
    }

    @FXML
    public void handleLeaderBoard() {
        loadPageWithLoading("LeaderboardView.fxml");
    }

    @FXML
    public void handleNavPractice() {
        loadPageWithLoading("PracticeView.fxml");
    }

    @FXML
    public void handleNavGames() {
        loadPageWithLoading("GamesView.fxml");
    }

    // ⭐ Emoji နဲ့ စာသားပါဝင်သော Center Loading Spinner Method
    public void loadPageWithLoading(String fxmlFileName) {
        // 1. Spinner ပြုလုပ်ခြင်း
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: #38BDF8;");
        spinner.setMaxSize(45, 45);

        // 2. FXML နာမည်အလိုက် Emoji နှင့် စာသား ရွေးချယ်ခြင်း
        String emoji = "🚀";
        String pageTitle = "Loading...";

        if (fxmlFileName.contains("LearnPage")) {
            emoji = "\uD83E\uDD16";
            pageTitle = "Loading Lessons...";
        } else if (fxmlFileName.contains("Course")) {
            emoji = "📚";
            pageTitle = "Loading Available Courses...";
        } else if (fxmlFileName.contains("Playground")) {
            emoji = "💻";
            pageTitle = "Preparing Playground...";
        } else if (fxmlFileName.contains("Leaderboard")) {
            emoji = "🏆";
            pageTitle = "Loading Leaderboard...";
        } else if (fxmlFileName.contains("Practice")) {
            emoji = "⚡";
            pageTitle = "Preparing Practice Mode...";
        } else if (fxmlFileName.contains("Game")) {
            emoji = "🎮";
            pageTitle = "Loading Games...";
        }

        // Label စာသား ပြင်ဆင်ခြင်း
        Label loadingText = new Label(emoji + " " + pageTitle);
        loadingText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #94A3B8;");

        // 3. Spinner နဲ့ Text ကို VBox ထဲ စုစည်းခြင်း
        VBox loadingBox = new VBox(12, spinner, loadingText);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle("-fx-background-color: #0F172A;"); // Dark Theme Background

        // 4. Loading Box ကို Content Area ထဲသို့ ခဏ အစားထိုးခြင်း
        contentArea.getChildren().setAll(loadingBox);

        // Sidebar Highlight ကို ချက်ချင်း အရင်ပြောင်းထားမည်
        updateSidebarHighlight(fxmlFileName);

        // 5. Background Thread ဖြင့် FXML Load လုပ်ခြင်း
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                // Animation လှလှလေး မြင်ရအောင် အနည်းဆုံး 300ms စောင့်မည်
                Thread.sleep(300);

                String resourcePath = fxmlFileName.startsWith("/") ? fxmlFileName : "/fxml/" + fxmlFileName;
                URL resource = getClass().getResource(resourcePath);

                if (resource == null) resource = getClass().getResource("/" + fxmlFileName);
                if (resource == null) resource = getClass().getResource(fxmlFileName);

                if (resource == null) {
                    throw new IOException("Could not find FXML file: " + fxmlFileName);
                }

                FXMLLoader loader = new FXMLLoader(resource);
                return loader.load();
            }
        };

        loadTask.setOnSucceeded(e -> {
            Parent newPage = loadTask.getValue();
            if (newPage != null) {
                // Load ပြီးသွားပါက စာမျက်နှာသစ်သို့ လဲလှယ်မည်
                contentArea.getChildren().setAll(newPage);
            }
        });

        loadTask.setOnFailed(e -> {
            System.err.println("Error loading FXML: " + fxmlFileName);
            if (loadTask.getException() != null) {
                loadTask.getException().printStackTrace();
            }
        });

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    // တိုက်ရိုက် load လုပ်ရန် လိုအပ်ပါက သုံးနိုင်သော မူလ loadPage မက်သဒ်
    public FXMLLoader loadPage(String fxmlFileName) {
        loadPageWithLoading(fxmlFileName);
        return null;
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