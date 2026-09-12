package Project;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static Stage primaryStage;
    private static Scene mainScene;

    // Initialize with the primary stage inside App.java start()
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    // Switch view by FXML path and optional window title
    public static FXMLLoader switchScene(String fxmlFileName, String windowTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFileName));
            Parent root = loader.load();

            if (mainScene == null) {
                mainScene = new Scene(root, 900, 700);
                primaryStage.setScene(mainScene);
            } else {
                mainScene.setRoot(root);
            }

            if (windowTitle != null) {
                primaryStage.setTitle(windowTitle);
            }

            primaryStage.show();
            return loader; // Returns FXMloader in case you need to pass data to the target controller
        } catch (IOException e) {
            System.err.println("Error loading FXML view: " + fxmlFileName);
            e.printStackTrace();
            return null;
        }
    }

    public static FXMLLoader switchScene(String fxmlFileName) {
        return switchScene(fxmlFileName, null);
    }
}