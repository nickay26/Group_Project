package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class FriendMatchController {

    @FXML private TextField joinCodeInput;
    @FXML private Label statusLabel;

    // Render Cloud WebSocket Server URL
    private static final String SERVER_URL = "wss://websocket-server-s9ki.onrender.com";

    @FXML
    public void initialize() {

        // User ID ကို UserSession မှ ယူသုံးပါမည်
        String currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());

        // WebSocket Server ဆီ ချိတ်ဆက်ခြင်း
        WebSocketService.getInstance().connect(SERVER_URL, currentUserId, new WebSocketService.NotificationListener() {

            @Override
            public void onInviteReceived(String fromUser, String language) {
                // တခြား Player ဆီမှ Invite Noti ရောက်လာပါက Popup ပြပေးမည် (Platform.runLater ကို WebSocketService မှ လုပ်ပြီးဖြစ်သည်)
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Game Challenge");
                alert.setHeaderText(fromUser + " offers a " + language.toUpperCase() + " match!");
                alert.setContentText("Do you want to accept?");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        // ⭐ Server ထံ ရွေးချယ်ထားသော language ပါ ရောက်ရှိစေရန် ပို့ပေးမည်
                        WebSocketService.getInstance().respondInvite(fromUser, "ACCEPT", language);
                    } else {
                        WebSocketService.getInstance().respondInvite(fromUser, "REJECT", language);
                    }
                });
            }

            // ⭐ Server ဘက်မှ AI မေးခွန်းနှင့် Session အချက်အလက်များ ရောက်လာပါက GameView သို့ ကူးမည်
            @Override
            public void onGameStart(String roomId, String language, String opponentId, String questionsJson) {
                if (statusLabel != null) {
                    statusLabel.setText("Starting game...");
                }

                FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Code Challenge");

                if (loader != null) {
                    GameController gameController = loader.getController();
                    gameController.setCourseData(language);

                    // 1. Game Session (roomId, opponentId) ကို GameController သို့ ပို့ပေးမည်
                    gameController.setGameSession(roomId, opponentId);

                    // 2. Server မှ ရလာသော AI မေးခွန်းကို တိုက်ရိုက် ထည့်သွင်းမည် (နှစ်ဦးလုံး မေးခွန်း အတူတူ ရပါမည်)
                    gameController.loadQuestionFromJson(questionsJson);
                }
            }

            @Override
            public void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson) {}

            @Override
            public void onQuickMatchTimeout() {}
        });
    }

    // 1. Friend List မှတစ်ဆင့် Invite ပို့ခြင်း
    public void handleInvitePlayer(String targetUserId, String language) {
        String currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());

        WebSocketService.getInstance().sendInvite(currentUserId, targetUserId, language != null ? language : "java");

        if (statusLabel != null) {
            statusLabel.setText("Invite sent to " + targetUserId + "...");
        }
    }

    public void handleInvitePlayer(String targetUserId) {
        handleInvitePlayer(targetUserId, "java");
    }

    // 2. Room Code ရိုက်ထည့်၍ Join/Invite ပို့ခြင်း
    @FXML
    private void handleJoinMatch() {
        String enteredCode = joinCodeInput.getText().trim();
        String currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());

        if (enteredCode.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setText("Please enter a valid code!");
            }
            return;
        }

        if (statusLabel != null) {
            statusLabel.setText("Connecting to room " + enteredCode + "...");
        }

        // Host ၏ Room Code/User ID ထံ Invite Request ပို့မည်
        WebSocketService.getInstance().sendInvite(currentUserId, enteredCode, "java");
    }

    @FXML
    private void handleCreateRoom() {
        SceneManager.switchScene("/Project/CreateRoomLobbyView.fxml", "Create Room Lobby");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("MainLayoutView.fxml", "Select Game Mode");
        MainLayoutController.getInstance().loadPage("GameModeView.fxml");
    }
}