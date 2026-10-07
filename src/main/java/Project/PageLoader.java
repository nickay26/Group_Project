package Project;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class PageLoader {

    public static FXMLLoader loadPageWithOverlay(StackPane targetContainer, String fxmlFileName, String pageTitle, String emoji) {
        // 1. Create Loading Card UI Overlay
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: #38BDF8;");
        spinner.setMaxSize(40, 40);

        Label titleLabel = new Label(emoji + " Loading " + pageTitle + "...");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");

        Label subText = new Label("Preparing your workspace...");
        subText.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        VBox loadingCard = new VBox(12, spinner, titleLabel, subText);
        loadingCard.setAlignment(Pos.CENTER);
        loadingCard.setPadding(new Insets(24, 32, 24, 32));
        loadingCard.setStyle(
                "-fx-background-color: #1E293B; " +
                        "-fx-background-radius: 16px; " +
                        "-fx-border-color: #334155; " +
                        "-fx-border-radius: 16px; " +
                        "-fx-border-width: 1px;"
        );

        StackPane overlay = new StackPane(loadingCard);
        overlay.setStyle("-fx-background-color: #0F172A;");
        targetContainer.getChildren().setAll(overlay);

        final FXMLLoader[] loaderHolder = new FXMLLoader[1];

        // 2. Short UI delay without blocking FX Application Thread
        PauseTransition delay = new PauseTransition(Duration.millis(200));
        delay.setOnFinished(e -> {
            try {
                String resourcePath = fxmlFileName.startsWith("/") ? fxmlFileName : "/fxml/" + fxmlFileName;
                URL resource = PageLoader.class.getResource(resourcePath);

                if (resource == null) resource = PageLoader.class.getResource("/" + fxmlFileName);
                if (resource == null) resource = PageLoader.class.getResource(fxmlFileName);

                if (resource == null) throw new IOException("Could not find " + fxmlFileName);

                FXMLLoader loader = new FXMLLoader(resource);
                Parent view = loader.load();
                loaderHolder[0] = loader;

                targetContainer.getChildren().setAll(view);
            } catch (Exception ex) {
                ex.printStackTrace();
                Label errLbl = new Label("❌ Failed to load page");
                errLbl.setStyle("-fx-text-fill: #EF4444;");
                targetContainer.getChildren().setAll(errLbl);
            }
        });
        delay.play();

        return loaderHolder[0];
    }
}