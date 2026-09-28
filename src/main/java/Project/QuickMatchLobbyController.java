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
    private String selectedLanguage = "C++ (Basic)";
    private String currentUserId = "Player_" + (int)(Math.random() * 1000);

    private final String[] randomNames = {"CodeNinja_99", "DevMaster_X", "JavaGuru", "ByteCoder", "AlgoWizard"};
    private final String[] randomAvatars = {"🤖", "🦊", "🦁", "👾", "🐯"};

    public void setSelectedLanguage(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }
        startMatchmaking();
    }

    @FXML
    public void initialize() {
    }

    private void startMatchmaking() {
        if (statusTitleLabel != null) statusTitleLabel.setText("Searching for Online Opponent...");

        // Python Flask WebSocket URL
        String serverWsUrl = "ws://localhost:5000/ws";

        // 📌 Connection အောင်မြင်မှသာ joinQuickMatch(...) ကို ပို့မည်ဖြစ်၍ Error ကျမည်မဟုတ်ပါ
        WebSocketService.getInstance().connect(
                serverWsUrl,
                currentUserId,
                new WebSocketService.NotificationListener() {
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

                            FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Live Coding Arena");
                            if (loader != null) {
                                GameController gameController = loader.getController();
                                gameController.setCourseData(selectedLanguage);
                                gameController.loadQuestionFromJson(questionsJson);
                            }
                        });
                    }
                },
                // 📌 Callback Parameter: Connection အဆင်သင့်ဖြစ်မှ Queue ထဲဝင်ရန် Signal ပို့ပေးခြင်း
                () -> WebSocketService.getInstance().joinQuickMatch(currentUserId, selectedLanguage)
        );
    }

    @FXML
    private void handleCancel() {
        if (searchTimeline != null) searchTimeline.stop();
        if (transitionDelay != null) transitionDelay.stop();

        WebSocketService.getInstance().cancelQuickMatch(currentUserId);

        SceneManager.switchScene("MainLayoutView.fxml", "Select Game Mode");
        MainLayoutController.getInstance().loadPage("GameModeView.fxml");
    }
}