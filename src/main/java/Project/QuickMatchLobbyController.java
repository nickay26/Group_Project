package Project;

import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

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

    // 📌 Random ID မသုံးဘဲ Google Login အကောင့်၏ User ID အမှန်ကို ယူသုံးပေးပါ
    private String currentUserId;

    private static final String SERVER_URL = "wss://websocket-server-s9ki.onrender.com";

    private final String[] randomNames = {"CodeNinja_99", "DevMaster_X", "JavaGuru", "ByteCoder", "AlgoWizard"};
    private final String[] randomAvatars = {"🤖", "🦊", "🦁", "👾", "🐯"};

    public void setSelectedLanguage(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }

        // 📌 User ID ကို UserSession မှ ယူပေးပါ
        if (UserSession.getInstance().getCurrentUser() != null) {
            this.currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());
        } else {
            this.currentUserId = "GUEST_USER";
        }

        startMatchmaking();
    }

    @FXML
    public void initialize() {
    }

    private void startMatchmaking() {
        if (statusTitleLabel != null) statusTitleLabel.setText("Searching for Online Opponent...");

        WebSocketService.getInstance().connect(
                SERVER_URL,
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
                // WebSocket Connection တကယ် ပွင့်သွားမှသာ Join ခေါ်ပေးပါမည်
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