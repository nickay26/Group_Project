package Project;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class QuickMatchLobbyController {

    @FXML private Label statusTitleLabel;
    @FXML private Label opponentAvatarLabel;
    @FXML private Label opponentNameLabel;
    @FXML private Label opponentStatusLabel;
    @FXML private Label countdownLabel;

    private Timeline searchTimeline;
    private PauseTransition transitionDelay;
    private String selectedLanguage = "C++ (Basic)"; // Default Value

    private final String[] randomNames = {"CodeNinja_99", "DevMaster_X", "JavaGuru", "ByteCoder", "AlgoWizard"};
    private final String[] randomAvatars = {"🤖", "🦊", "🦁", "👾", "🐯"};

    // Language ရောက်ရှိလာမှ Matchmaking စတင်ပါမည်
    public void setSelectedLanguage(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }
        // Language Data ရရှိပြီးမှ Matchmaking ကို စတင်ခေါ်ယူပါမည်
        startMatchmaking();
    }

    @FXML
    public void initialize() {
        // initialize() တွင် တိုက်ရိုက်မခေါ်ဘဲ setSelectedLanguage ရောက်လာမှ ခေါ်ပါမည်
    }

    private void startMatchmaking() {
        // ထပ်ခါထပ်ခါ Run ခြင်းမှ ကာကွယ်ရန် Timeline ဟောင်းကို ရပ်ပါမည်
        if (searchTimeline != null) {
            searchTimeline.stop();
        }

        searchTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            Random rnd = new Random();
            String foundName = randomNames[rnd.nextInt(randomNames.length)];
            String foundAvatar = randomAvatars[rnd.nextInt(randomAvatars.length)];

            statusTitleLabel.setText("Match Found!");
            opponentAvatarLabel.setText(foundAvatar);
            opponentNameLabel.setText(foundName);
            opponentStatusLabel.setText("Level " + (rnd.nextInt(10) + 5) + " • Ready");
            countdownLabel.setText("Starting game in 2 seconds...");

            // "C++ (Basic)" စာသားမှ Language နှင့် Level ကို ခွဲထုတ်ခြင်း
            String langName = selectedLanguage;
            String levelName = "Basic";

            if (selectedLanguage.contains("(")) {
                langName = selectedLanguage.substring(0, selectedLanguage.indexOf("(")).trim();
                levelName = selectedLanguage.substring(selectedLanguage.indexOf("(") + 1, selectedLanguage.indexOf(")")).trim();
            }

            // 1. အသစ်ထည့်သွင်းထားသော ApiClient.generateCodeChallengeAsync ကို ခေါ်ယူခြင်း
            CompletableFuture<String> aiQuestionFuture = ApiClient.generateCodeChallengeAsync(langName, levelName);

            // 2. CountDown ပြီးမှ GameView သို့ ပြောင်းပါမည်
            transitionDelay = new PauseTransition(Duration.seconds(1.5));

            transitionDelay.setOnFinished(evt -> {
                aiQuestionFuture.thenAccept(aiJsonResponse ->
                        Platform.runLater(() -> {
                            FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Live Coding Arena");
                            if (loader != null) {
                                GameController gameController = loader.getController();

                                // Language နှင့် Coding Challenge တိုက်ရိုက် ထည့်သွင်းခြင်း
                                gameController.setCourseData(selectedLanguage);
                                gameController.loadQuestionFromJson(aiJsonResponse);
                            }
                        })
                );
            });
            transitionDelay.play();
        }));

        searchTimeline.setCycleCount(1);
        searchTimeline.play();
    }

    @FXML
    private void handleCancel() {
        if (searchTimeline != null) searchTimeline.stop();
        if (transitionDelay != null) transitionDelay.stop();

        SceneManager.switchScene("MainLayoutView.fxml", "Select Game Mode");
        MainLayoutController.getInstance().loadPage("GameModeView.fxml");
    }
}