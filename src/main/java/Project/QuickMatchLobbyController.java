package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

public class QuickMatchLobbyController {

    @FXML private Label statusTitleLabel;
    @FXML private Label opponentAvatarLabel;
    @FXML private Label opponentNameLabel;
    @FXML private Label opponentStatusLabel;
    @FXML private Label countdownLabel;

    private String selectedLanguage = "C++";
    private String currentUserId;

    // Render Cloud WebSocket Server URL
    private static final String SERVER_URL = "wss://websocket-server-s9ki.onrender.com";

    public void setSelectedLanguage(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }

        // User ID ကို UserSession မှ ယူသုံးပါမည်
        if (UserSession.getInstance().getCurrentUser() != null) {
            this.currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());
        } else {
            this.currentUserId = "GUEST_" + System.currentTimeMillis();
        }

        startMatchmaking();
    }

    @FXML
    public void initialize() {
    }

    private void startMatchmaking() {
        if (statusTitleLabel != null) statusTitleLabel.setText("Searching for Online Opponent...");
        if (opponentNameLabel != null) opponentNameLabel.setText("Waiting for player...");
        if (opponentStatusLabel != null) opponentStatusLabel.setText("In Queue");

        // Render WebSocket Server သို့ တိုက်ရိုက် ချိတ်ဆက်မည်
        WebSocketService.getInstance().connect(
                SERVER_URL,
                currentUserId,
                new WebSocketService.NotificationListener() {
                    @Override
                    public void onInviteReceived(String fromUser, String language) {}

                    // ⭐ WebSocketService ရှိ Interface Signature နှင့် ကိုက်ညီစေရန် Parameter ၄ ခု ပြင်ဆင်ထားပါသည်
                    @Override
                    public void onGameStart(String roomId, String language, String opponentId, String questionsJson) {}

                    // ⭐ Quick Match စတင်သည့် မက်သဒ်
                    @Override
                    public void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson) {
                        // Real User Match တွေ့သွားပါက နှစ်ဖက်လုံး GameView သို့ ပြိုင်တူ သွားမည်
                        if (statusTitleLabel != null) statusTitleLabel.setText("Match Found!");
                        if (opponentNameLabel != null) opponentNameLabel.setText("Opponent: User " + opponentId);
                        if (opponentStatusLabel != null) opponentStatusLabel.setText("Ready");
                        if (countdownLabel != null) countdownLabel.setText("Starting game...");

                        FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Live Coding Arena");
                        if (loader != null) {
                            GameController gameController = loader.getController();
                            gameController.setCourseData(selectedLanguage);

                            // 1. Session Data (roomId နှင့် opponentId) ကို GameController ထံ လွှဲပေးမည်
                            gameController.setGameSession(roomId, opponentId);

                            // 2. WebSocket မှ ရလာသော မေးခွန်းများကို GameController ထံ ပို့ပေးမည်
                            gameController.loadQuestionFromJson(questionsJson);
                        }
                    }

                    @Override
                    public void onQuickMatchTimeout() {
                        // စက္ကန့် ၃၀ အတွင်း လူမတွေ့ပါက UI တွင် စာတန်းပြမည်
                        if (statusTitleLabel != null) statusTitleLabel.setText("No Player Found");
                        if (opponentNameLabel != null) opponentNameLabel.setText("Queue Timeout");
                        if (opponentStatusLabel != null) opponentStatusLabel.setText("Failed");
                        if (countdownLabel != null) countdownLabel.setText("No active player online. Please try again later!");
                    }
                },
                // Connection ပွင့်သည်နှင့် Queue ထဲဝင်ရန် Server သို့ စာပို့မည်
                () -> WebSocketService.getInstance().joinQuickMatch(currentUserId, selectedLanguage)
        );
    }

    @FXML
    private void handleCancel() {
        // Queue ထဲမှ ထွက်ရန် Server သို့ Cancel Signal ပို့မည်
        WebSocketService.getInstance().cancelQuickMatch(currentUserId);

        SceneManager.switchScene("MainLayoutView.fxml", "Select Game Mode");
        MainLayoutController.getInstance().loadPage("GameModeView.fxml");
    }
}