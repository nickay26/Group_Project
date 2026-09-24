package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

public class FriendMatchController {

    @FXML private TextField joinCodeInput;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {

        String currentUserId = String.valueOf(UserSession.getInstance().getCurrentUser().getUserId());
        WebSocketService.getInstance().connect("ws://localhost:8080/ws", currentUserId, new WebSocketService.NotificationListener() {

            @Override
            public void onInviteReceived(String fromUser, String language) {
                // တခြား Player ဆီမှ Invite Noti ရောက်လာပါက Popup ပြပေးမည်
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Game Challenge");
                alert.setHeaderText(fromUser + " offers a " + language.toUpperCase() + " match!");
                alert.setContentText("Do you want to accept?");

                alert.showAndWait().ifPresent(response -> {
                    JSONObject res = new JSONObject();
                    res.put("type", "INVITE_RESPONSE");
                    res.put("fromUser", currentUserId);
                    res.put("toUser", fromUser);

                    if (response == ButtonType.OK) {
                        res.put("action", "ACCEPT");
                    } else {
                        res.put("action", "DECLINE");
                    }
                    WebSocketService.getInstance().sendText(res.toString());
                });
            }

            @Override
            public void onGameStart(String roomId, String language) {
                // နှစ်ဘက်လုံး Accept လုပ်ပြီးပါက Server မှ START_GAME ရောက်လာပြီး Game Panel ပွင့်မည်
                loadGameViewAndStart(language);
            }
        });
    }

    // 1. Invite Following စာရင်းထဲမှ Friend ကို Invite နှိပ်ချိန် ခေါ်ရမည့် မက်သဒ်
    public void handleInvitePlayer(String targetUserId) {
        JSONObject inviteJson = new JSONObject();
        inviteJson.put("type", "INVITE_SEND");
        inviteJson.put("fromUser", UserSession.getInstance().getCurrentUser().getUserId());
        inviteJson.put("toUser", targetUserId);
        inviteJson.put("language", "java");

        WebSocketService.getInstance().sendText(inviteJson.toString());

        if (statusLabel != null) {
            statusLabel.setText("Invite sent to " + targetUserId + "...");
        }
    }

    // 2. Code ရိုက်ပြီး Join မည့် မက်သဒ်
    @FXML
    private void handleJoinMatch() {
        String enteredCode = joinCodeInput.getText().trim();

        if (enteredCode.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setText("Please enter a valid code!");
            }
            return;
        }

        if (statusLabel != null) {
            statusLabel.setText("Joining room " + enteredCode + "...");
        }

        // Server သို့ Code ဖြင့် Join ကြောင်း သတင်းပို့မည်
        JSONObject joinJson = new JSONObject();
        joinJson.put("type", "JOIN_ROOM");
        joinJson.put("userId", UserSession.getInstance().getCurrentUser().getUserId());
        joinJson.put("roomCode", enteredCode);
        WebSocketService.getInstance().sendText(joinJson.toString());
    }

    // 3. AI Question Load လုပ်ပြီး GamePanel (Gameview.fxml) သို့ ကူးပြောင်းပေးမည့် Helper Method
    private void loadGameViewAndStart(String language) {
        if (statusLabel != null) {
            statusLabel.setText("Loading question from AI...");
        }

        CompletableFuture.supplyAsync(() -> AiQuestionService.fetchAiQuestion(language))
                .thenAccept(aiJsonResponse -> Platform.runLater(() -> {
                    FXMLLoader loader = SceneManager.switchScene("/Project/Gameview.fxml", "Mimo Platform - Code Challenge");

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
        SceneManager.switchScene("/Project/GameModeView.fxml", "Select Game Mode");
    }
}