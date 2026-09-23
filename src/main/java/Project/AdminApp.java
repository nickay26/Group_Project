package Project;

import javafx.application.Application;
import javafx.stage.Stage;

public class AdminApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        SceneManager.setPrimaryStage(primaryStage);

        // Launch initial welcome screen
        primaryStage.setMaximized(true);
        SceneManager.switchScene("AdminDashboard.fxml", "Mimo Platform - Welcome");
    }
}
