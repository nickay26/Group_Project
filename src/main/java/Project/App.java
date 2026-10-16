package Project;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);

        // Launch initial welcome screen
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/SodexaLogo.png"))));
        stage.setMaximized(true);
        SceneManager.switchScene("WelcomeView.fxml", "Sodexa Platform - Welcome");
    }

    public static void main(String[] args) {
        launch(args);
    }
}