package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.concurrent.CompletableFuture;

public class FriendMatchController {

    @FXML private TextField joinCodeInput;
    @FXML private Label statusLabel;

    // Render Cloud WebSocket Server URL
    private static final String SERVER_URL = "wss://websocket-server-s9ki.onrender.com";

    @FXML
    public void initialize() {

        // ====================================================================
        // [1] Production Mode (အကောင့်အစစ်ဖြင့် သုံးရန်)
        String currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());

        // [2] Testing Mode (စက်တစ်လုံးတည်း App ၂ ခုပြိုင်ဖွင့် စမ်းလိုပါက အောက်ပါလိုင်းကို Uncomment လုပ်ပါ)
        // String currentUserId = "TEST_USER_" + (int)(Math.random() * 1000);
        // ====================================================================

        // WebSocket Server ဆီ ချိတ်ဆက်ခြင်း
        WebSocketService.getInstance().connect(SERVER_URL, currentUserId, new WebSocketService.NotificationListener() {

            @Override
            public void onInviteReceived(String fromUser, String language) {
                // တခြား Player ဆီမှ Invite Noti ရောက်လာပါက Popup ပြပေးမည်
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Game Challenge");
                alert.setHeaderText(fromUser + " offers a " + language.toUpperCase() + " match!");
                alert.setContentText("Do you want to accept?");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        WebSocketService.getInstance().respondInvite(fromUser, "ACCEPT");
                    } else {
                        WebSocketService.getInstance().respondInvite(fromUser, "REJECT");
                    }
                });
            }

            @Override
            public void onGameStart(String roomId, String language) {
                // နှစ်ဘက်လုံး Accept လုပ်ပြီးပါက Game View သို့ ကူးမည်
                loadGameViewAndStart(language);
            }

            @Override
            public void onQuickMatchStart(String roomId, String language, String questionsJson) {

            }
        });
    }

    // 1. Friend List မှတစ်ဆင့် Invite ပို့ခြင်း
    public void handleInvitePlayer(String targetUserId) {
        String currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());

        WebSocketService.getInstance().sendInvite(currentUserId, targetUserId, "java");

        if (statusLabel != null) {
            statusLabel.setText("Invite sent to " + targetUserId + "...");
        }
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

    // 3. AI Question Load လုပ်ပြီး GamePanel (Gameview.fxml) သို့ သွားမည်
    private void loadGameViewAndStart(String language) {
        if (statusLabel != null) {
            statusLabel.setText("Loading question from AI...");
        }

        CompletableFuture.supplyAsync(() -> AiQuestionService.fetchAiQuestion(language))
                .thenAccept(aiJsonResponse -> Platform.runLater(() -> {
                    // FXML File Path Case Sensitivity စစ်ဆေးပါ (GameView.fxml သို့မဟုတ် Gameview.fxml)
                    FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Code Challenge");

                    if (loader != null) {
                        GameController gameController = loader.getController();
                        gameController.loadQuestionFromJson(aiJsonResponse);
                    }
                }));
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