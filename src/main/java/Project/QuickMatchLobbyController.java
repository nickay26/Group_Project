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

public class QuickMatchLobbyController {

    @FXML private Label statusTitleLabel;
    @FXML private Label opponentAvatarLabel;
    @FXML private Label opponentNameLabel;
    @FXML private Label opponentStatusLabel;
    @FXML private Label countdownLabel;

    private Timeline searchTimeline;
    private PauseTransition transitionDelay;
    private String selectedLanguage = "C++ (Basic)"; // Default Value
    private String currentUserId = "Player_" + (int)(Math.random() * 1000); // 📌 Unique Player ID

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
        if (statusTitleLabel != null) statusTitleLabel.setText("Searching for Online Opponent...");

        // 📌 Python Flask WebSocket URL (Local Host သို့မဟုတ် Render Server)
        String serverWsUrl = "ws://localhost:5000/ws";

        // 1. WebSocket Server သို့ ချိတ်ဆက်၍ Event စောင့်ဆိုင်းခြင်း
        WebSocketService.getInstance().connect(serverWsUrl, currentUserId, new WebSocketService.NotificationListener() {
            @Override
            public void onInviteReceived(String fromUser, String language) {}

            @Override
            public void onGameStart(String roomId, String language) {}

            @Override
            public void onQuickMatchStart(String roomId, String language, String questionsJson) {
                Platform.runLater(() -> {
                    Random rnd = new Random();
                    if (statusTitleLabel != null) statusTitleLabel.setText("Match Found!");
                    if (opponentAvatarLabel != null) opponentAvatarLabel.setText(randomAvatars[rnd.nextInt(randomAvatars.length)]);
                    if (opponentNameLabel != null) opponentNameLabel.setText(randomNames[rnd.nextInt(randomNames.length)]);
                    if (opponentStatusLabel != null) opponentStatusLabel.setText("Level " + (rnd.nextInt(10) + 5) + " • Ready");
                    if (countdownLabel != null) countdownLabel.setText("Starting game...");

                    // GameView.fxml သို့ ပြောင်းလဲပြီး မေးခွန်း JSON ပေးပို့မည်
                    FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Live Coding Arena");
                    if (loader != null) {
                        GameController gameController = loader.getController();
                        gameController.setCourseData(selectedLanguage);
                        gameController.loadQuestionFromJson(questionsJson);
                    }
                });
            }
        });

        // 2. Queue ထဲ ဝင်ရောက်ရန် Server ထံ Signal ပို့မည်
        WebSocketService.getInstance().joinQuickMatch(currentUserId, selectedLanguage);
    }

    @FXML
    private void handleCancel() {
        if (searchTimeline != null) searchTimeline.stop();
        if (transitionDelay != null) transitionDelay.stop();

        // 📌 Match Cancel လုပ်သည့် Signal ပို့မည်
        WebSocketService.getInstance().cancelQuickMatch(currentUserId);

        SceneManager.switchScene("MainLayoutView.fxml", "Select Game Mode");
        MainLayoutController.getInstance().loadPage("GameModeView.fxml");
    }
}