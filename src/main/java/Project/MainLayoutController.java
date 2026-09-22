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

        // Automatically fetch and display active course title
        updateCourseTitle();

        // Load default page on startup
        handleNavLearn();
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
            updateSidebarHighlight(fxmlFileName);

            return loader;
        } catch (IOException e) {
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
        }
    }

    private void setActiveButton(Button activeButton) {
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