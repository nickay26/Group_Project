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

            // 1. ရွေးချယ်ထားသော selectedLanguage ဖြင့် AI မေးခွန်းကို တောင်းယူပါမည်
            CompletableFuture<String> aiQuestionFuture = CompletableFuture.supplyAsync(() ->
                    AiQuestionService.fetchAiQuestion(selectedLanguage)
            );

            // 2. CountDown ပြီးမှ GameView သို့ ပြောင်းပါမည်
            transitionDelay = new PauseTransition(Duration.seconds(1.5));
            transitionDelay.setOnFinished(evt -> {
                aiQuestionFuture.thenAccept(aiJsonResponse ->
                        Platform.runLater(() -> {
                            FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Code Challenge");
                            if (loader != null) {
                                GameController gameController = loader.getController();

                                // (က) GameController ထံသို့ ရွေးချယ်ထားသော Language ပို့ပေးခြင်း (Header Title ပြောင်းရန်)
                                gameController.setCourseData(selectedLanguage);

                                // (ခ) ပထမမေးခွန်း JSON ကို ပို့ပေးခြင်း
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

        SceneManager.switchScene("/Project/GameModeView.fxml", "Select Game Mode");
    }
}