package Project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.Optional;
import java.util.Random;

public class CreateRoomLobbyController {

    @FXML private Label roomCodeLabel;
    @FXML private Button startMatchBtn;

    private String selectedLanguage = "java"; // Default language

    // Cloud WebSocket URL နှင့် User ID သတ်မှတ်ခြင်း
    private final String SERVER_URL = "wss://websocket-server-s9ki.onrender.com";
    private String currentUserId;

    @FXML
    public void initialize() {
        String generatedCode = generateRandomCode();
        roomCodeLabel.setText(generatedCode);

        // ⭐ ROOM JOIN ပြဿနာဖြေရှင်းရန် -
        // UI ပေါ်တွင် ပြထားသော Room Code ကိုပဲ WebSocket ID အဖြစ် အမြဲသုံးပါမည်။
        // သို့မှသာ အခြား Player မှ Room Code ရိုက်ထည့်ပါက Server မှ တိုက်ရိုက် ရှာတွေ့မည် ဖြစ်ပါသည်။
        this.currentUserId = generatedCode;

        // ၁။ Cloud WebSocket Server သို့ စတင် ချိတ်ဆက်ခြင်း
        initWebSocket();
    }

    private void initWebSocket() {
        WebSocketService.getInstance().connect(SERVER_URL, currentUserId, new WebSocketService.NotificationListener() {
            @Override
            public void onInviteReceived(String fromUser, String language) {
                // အခြားသူမှ မိမိထံ Invite ပို့လာပါက Pop-up ပြပေးမည်
                showInviteDialog(fromUser, language);
            }

            // ⭐ Server မှ မေးခွန်း နှင့် ပြိုင်ဘက် အချက်အလက်များပါသော START_GAME Signal ရောက်လာပါက GameView သို့ ကူးမည်
            @Override
            public void onGameStart(String roomId, String language, String opponentId, String questionsJson) {
                System.out.println("Match Starting... Room: " + roomId + " Opponent: " + opponentId);

                FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Code Challenge");

                if (loader != null) {
                    GameController gameController = loader.getController();
                    if (gameController != null) {
                        gameController.setCourseData(language);
                        gameController.setGameSession(roomId, opponentId);
                        gameController.loadQuestionFromJson(questionsJson);
                    }
                }
            }

            @Override
            public void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson) {}

            @Override
            public void onQuickMatchTimeout() {}
        });
    }

    // Language ကို အပြင်မှ သတ်မှတ်ချင်ပါက ခေါ်သုံးရန် မက်သဒ်
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

    // ၂။ Friend ကို Real-time Invite ပို့မည့် Method
    @FXML
    private void handleInviteFriend(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Invite Friend");
        dialog.setHeaderText("သူငယ်ချင်းကို ဖိတ်ခေါ်ရန်");
        dialog.setContentText("သူငယ်ချင်း၏ ID / Room Code ကို ရိုက်ထည့်ပါ:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(targetUserId -> {
            if (!targetUserId.trim().isEmpty()) {
                // WebSocket မှတစ်ဆင့် Real-time Invite ပို့လိုက်ခြင်း
                WebSocketService.getInstance().sendInvite(currentUserId, targetUserId.trim(), selectedLanguage);

                Button btn = (Button) event.getSource();
                btn.setText("Invited!");
                btn.setDisable(true);
                btn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6;");

                if (startMatchBtn != null) {
                    startMatchBtn.setDisable(false);
                }
            }
        });
    }

    // Invite ရောက်လာချိန်တွင် လက်ခံ/ငင်းပယ် စစ်ဆေးသည့် Pop-up
    private void showInviteDialog(String fromUser, String language) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Invite");
        alert.setHeaderText(fromUser + " ထံမှ ဖိတ်ခေါ်စာ ရောက်ရှိလာသည်");
        alert.setContentText("Language: " + language.toUpperCase() + "\nလက်ခံမှာလား?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            WebSocketService.getInstance().respondInvite(fromUser, "ACCEPT", language);
        } else {
            WebSocketService.getInstance().respondInvite(fromUser, "REJECT", language);
        }
    }

    @FXML
    private void handleStartMatch() {
        // Invite လက်ခံပြီးပါက Server ဘက်မှ onGameStart Event ဖြင့် တိုက်ရိုက် GameView သို့ ပို့ပေးမည် ဖြစ်ပါသည်။
        System.out.println("Waiting for server to dispatch game start session...");
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