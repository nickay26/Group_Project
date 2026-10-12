package Project;

import java.util.Optional;
import java.util.Random;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

public class CreateRoomLobbyController {

    @FXML private Label roomCodeLabel;
    @FXML private Button startMatchBtn;
    @FXML private Label hostNameLabel;
    @FXML private Label opponentNameLabel;
    @FXML private VBox friendsContainer;

    private String selectedLanguage = "java";
    private final String SERVER_URL = "wss://websocket-server-s9ki.onrender.com";
    private String currentUserId;
    private String roomId;
    private boolean isHost = true;
    private String pendingOpponentId;

    @FXML
    public void initialize() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        this.currentUserId = (currentUser != null) ? String.valueOf(currentUser.getUserId()) : "USER_" + System.currentTimeMillis();
    }

    // Role (Host / Opponent) အလိုက် Lobby UI ပြင်ဆင်ပေးသည့် Method
    public void setupRoom(String code, boolean isHostRole) {
        this.isHost = isHostRole;

        if (isHost) {
            this.roomId = (code != null && !code.isEmpty()) ? code : generateRandomCode();
        } else {
            this.roomId = code;
        }

        if (roomCodeLabel != null) {
            roomCodeLabel.setText(this.roomId);
        }

        User currentUser = UserSession.getInstance().getCurrentUser();
        String myName = (currentUser != null) ? currentUser.getUsername() : "Player";

        if (isHost) {
            if (hostNameLabel != null) hostNameLabel.setText(myName + " (Host)");
            if (opponentNameLabel != null) opponentNameLabel.setText("Waiting for Opponent...");
            if (startMatchBtn != null) {
                startMatchBtn.setText("Start Match ➔");
                startMatchBtn.setDisable(true); // Opponent Ready ဖြစ်မှ ပွင့်မည်
            }
        } else {
            if (hostNameLabel != null) hostNameLabel.setText("Host (Room: " + roomId + ")");
            if (opponentNameLabel != null) opponentNameLabel.setText(myName + " (You)");
            if (startMatchBtn != null) {
                startMatchBtn.setText("Ready"); // ⭐ Opponent ဘက်တွင် Ready Button ပွင့်နေမည်
                startMatchBtn.setDisable(false);
            }
        }

        initWebSocket();
    }

    private void initWebSocket() {
        // ⭐ Host ဖြစ်ပါက Room Code (roomId) ဖြင့် WebSocket ချိတ်ပါမည်
        String wsRegisterId = isHost ? roomId : currentUserId;

        WebSocketService.getInstance().connect(SERVER_URL, wsRegisterId, new WebSocketService.NotificationListener() {
            @Override
            public void onInviteReceived(String fromUser, String language) {
                // Opponent က "Ready" နှိပ်လိုက်ချိန်တွင် Host ထံ Signal ရောက်လာမည်
                Platform.runLater(() -> {
                    if (isHost) {
                        pendingOpponentId = fromUser;
                        if (opponentNameLabel != null) {
                            opponentNameLabel.setText("Player " + fromUser + " (Ready ✅)");
                        }
                        if (startMatchBtn != null) {
                            startMatchBtn.setDisable(false); // Host ဘက်ရှိ Start Match Button ပွင့်သွားမည်
                        }
                    }
                });
            }

            @Override
            public void onGameStart(String roomId, String language, String opponentId, String questionsJson) {
                // Host က Start Match နှိပ်လိုက်ပါက နှစ်ဦးလုံး GameView သို့ ကူးပြောင်းမည်
                Platform.runLater(() -> {
                    FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Code Challenge");

                    if (loader != null) {
                        GameController gameController = loader.getController();
                        if (gameController != null) {
                            gameController.setCourseData(language);
                            gameController.setGameSession(roomId, opponentId);
                           // gameController.loadQuestionsFromJson(questionsJson);
                        }
                    }
                });
            }

            @Override
            public void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson) {}

            @Override
            public void onQuickMatchTimeout() {}
        });
    }

    @FXML
    private void handleStartMatch() {
        if (isHost) {
            // Host က "Start Match ➔" နှိပ်သည့်အခါ
            if (pendingOpponentId != null) {
                WebSocketService.getInstance().respondInvite(pendingOpponentId, "ACCEPT", selectedLanguage);
            }
        } else {
            // Opponent က "Ready" နှိပ်သည့်အခါ
            if (startMatchBtn != null) {
                startMatchBtn.setText("Ready! ⏳");
                startMatchBtn.setDisable(true);
            }
            // Host ထံ မိမိ Ready ဖြစ်ကြောင်း Room Code ဖြင့် Signal လှမ်းပို့သည်
            WebSocketService.getInstance().sendInvite(currentUserId, roomId, selectedLanguage);
        }
    }

    public void setSelectedLanguage(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }
    }

    @FXML
    private void handleCopyCode() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(roomCodeLabel.getText());
        clipboard.setContent(content);
    }

    @FXML
    private void handleInviteFriend(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Invite Friend");
        dialog.setHeaderText("သူငယ်ချင်းကို ဖိတ်ခေါ်ရန်");
        dialog.setContentText("သူငယ်ချင်း၏ ID / Room Code ကို ရိုက်ထည့်ပါ:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(targetUserId -> {
            if (!targetUserId.trim().isEmpty()) {
                WebSocketService.getInstance().sendInvite(currentUserId, targetUserId.trim(), selectedLanguage);

                Button btn = (Button) event.getSource();
                btn.setText("Invited!");
                btn.setDisable(true);
            }
        });
    }

    @FXML
    private void handleLeaveRoom() {
        SceneManager.switchScene("/Project/FRIEND.fxml", "Play with a Friend");
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }
}