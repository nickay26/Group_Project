package Project.Helpers;

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
        try {
            String resourcePath = fxmlFileName.startsWith("/") ? fxmlFileName : "/Project/" + fxmlFileName;
            URL resource = PageLoader.class.getResource(resourcePath);

            if (resource == null) resource = PageLoader.class.getResource("/fxml/" + fxmlFileName);
            if (resource == null) resource = PageLoader.class.getResource("/" + fxmlFileName);

            if (resource == null) throw new IOException("Could not find " + fxmlFileName);

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

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

            // Loading Overlay ကို အရင်ပြမည်
            targetContainer.getChildren().setAll(overlay);

            // 2. 180ms အကြာမှ မူရင်း View ကို ဖော်ပြမည် (UI တင်းမသွားစေရန်)
            PauseTransition delay = new PauseTransition(Duration.millis(180));
            delay.setOnFinished(e -> targetContainer.getChildren().setAll(view));
            delay.play();

            // Loader ကို ချက်ချင်း Return ပြန်ပေးမည် (Controller ယူသုံးမည့် Lesson/Practice များအတွက်)
            return loader;

        } catch (Exception ex) {
            ex.printStackTrace();
            Label errLbl = new Label("❌ Failed to load page: " + fxmlFileName);
            errLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-font-weight: bold;");
            targetContainer.getChildren().setAll(errLbl);
            return null;
        }
    }
}