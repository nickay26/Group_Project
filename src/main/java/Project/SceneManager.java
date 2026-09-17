package Project;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static Stage primaryStage;
    private static Scene mainScene;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static FXMLLoader switchScene(String fxmlFileName, String windowTitle) {
        try {
            // Path ကို အဆင်ပြေအောင် ရှာဖွေခြင်း
            URL resource = SceneManager.class.getResource(fxmlFileName);
            if (resource == null) {
                resource = SceneManager.class.getResource("/Project/" + fxmlFileName);
            }
            if (resource == null) {
                resource = SceneManager.class.getResource("/" + fxmlFileName);
            }

            if (resource == null) {
                throw new IOException("Cannot find FXML file: " + fxmlFileName);
            }

            FXMLLoader loader = new FXMLLoader(resource);
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
            return loader;

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