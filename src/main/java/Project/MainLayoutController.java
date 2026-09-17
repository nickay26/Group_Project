package Project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainLayoutController {

    @FXML private StackPane contentArea;

    @FXML private Button learnBtn;
    @FXML private Button coursesBtn;
    @FXML private Button playgroundBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button practiceBtn;
    @FXML private Button gamesBtn;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        navButtons = Arrays.asList(learnBtn, coursesBtn, playgroundBtn, leaderboardBtn, practiceBtn, gamesBtn);

        // Load default page on startup
        handleNavLearn();
    }

    @FXML
    public void handleNavLearn() {
        setActiveButton(learnBtn);
        loadView("LearnPage.fxml");
    }

    @FXML
    public void handleNavCourses() {
        setActiveButton(coursesBtn);
        loadView("CoursePage.fxml");
    }

    @FXML
    public void handleNavPlayground() {
        setActiveButton(playgroundBtn);
        loadView("PlaygroundDashboardView.fxml");
    }

    @FXML
    public void handleLeaderBoard(){
        setActiveButton(leaderboardBtn);
        loadView("LeaderboardView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        for (Button btn : navButtons) {
            if (btn == activeButton) {
                btn.setStyle("-fx-background-color: #000000; -fx-border-color: #000000; -fx-border-radius: 6px; -fx-text-fill: #FFFFFF; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #000000; -fx-border-radius: 6px; -fx-text-fill: #000000; -fx-cursor: hand;");
            }
        }
    }
}