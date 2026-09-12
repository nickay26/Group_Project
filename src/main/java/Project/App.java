package Project;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);

        // Launch initial welcome screen
        SceneManager.switchScene("WelcomeView.fxml", "Mimo Platform - Welcome");
    }

    public static void main(String[] args) {
        launch(args);
    }
}