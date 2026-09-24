package Project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

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
        loadPage("LearnPage.fxml");
    }

    @FXML
    public void handleNavCourses() {
        loadPage("CoursePage.fxml");
    }

    @FXML
    public void handleNavPlayground() {
        loadPage("PlaygroundDashboardView.fxml");
    }

    @FXML
    public void handleLeaderBoard() {
        loadPage("LeaderboardView.fxml");
    }

    @FXML
    public void handleNavPractice() {
        loadPage("PracticeView.fxml");
    }

    @FXML
    public void handleNavGames() {
        loadPage("GamesView.fxml");
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