package Project;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);

        // Launch initial welcome screen
        stage.setMaximized(true);
        SceneManager.switchScene("MainLayoutView.fxml", "Sodex Platform - Welcome");
    }

    public static void main(String[] args) {
        launch(args);
    }
}