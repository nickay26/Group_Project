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
    private String currentUserId; // မိမိ User ID (Room Code ကိုပဲ ခေတ္တ ID အဖြစ် သုံးနိုင်ပါသည်)

    @FXML
    public void initialize() {
        String generatedCode = generateRandomCode();
        roomCodeLabel.setText(generatedCode);
        this.currentUserId = generatedCode; // Room Code ကို မိမိ User ID အဖြစ် သတ်မှတ်ခြင်း

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

            @Override
            public void onGameStart(String roomId, String language) {
                // အခြားသူမှ Accept လုပ်လိုက်ပါက တိုက်ရိုက် Game Start မည်
                System.out.println("Friend accepted! Starting Game... Room: " + roomId);
                handleStartMatch();
            }
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
        // ဖိတ်ခေါ်ချင်သော သူငယ်ချင်း၏ User ID / Code ကို တောင်းဆိုခြင်း
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

                startMatchBtn.setDisable(false);
            }
        });
    }

    // Invite ရောက်လာချိန်တွင် လက်ခံ/ငင်းပယ် စစ်ဆေးသည့် Pop-up
    private void showInviteDialog(String fromUser, String language) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Invite");
        alert.setHeaderText(fromUser + " ထံမှ ဖိတ်ခေါ်စာ ရောက်ရှိလာသည်");
        alert.setContentText("Language: " + language + "\nလက်ခံမှာလား?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            WebSocketService.getInstance().respondInvite(fromUser, "ACCEPT");
        } else {
            WebSocketService.getInstance().respondInvite(fromUser, "REJECT");
        }
    }

    // Start Match နှိပ်ပါက AI ထံမှ မေးခွန်းယူပြီး GameView သို့ ပို့ပေးမည်
    @FXML
    private void handleStartMatch() {
        // 1. AI ထံမှ မေးခွန်း လှမ်းတောင်းမည်
        String aiJsonResponse = AiQuestionService.fetchAiQuestion(selectedLanguage);

        // 2. GameView စာမျက်နှာ သို့ ပြောင်းမည်
        FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Code Challenge");

        // 3. AI ထံမှ ရရှိလာသော မေးခွန်းကို GameController သို့ ထည့်ပေးမည်
        if (loader != null) {
            GameController gameController = loader.getController();
            if (gameController != null) {
                gameController.loadQuestionFromJson(aiJsonResponse);
            }
        }
    }

    @FXML
    private void handleLeaveRoom() {
        SceneManager.switchScene("/Project/FriendMatchView.fxml", "Play with a Friend");
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