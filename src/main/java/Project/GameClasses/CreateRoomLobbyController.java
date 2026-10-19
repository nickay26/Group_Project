package Project.GameClasses;

import java.util.Optional;
import java.util.Random;

import Project.Helpers.SceneManager;
import Project.Getters_Setters.User;
import Project.Getters_Setters.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

public class CreateRoomLobbyController {

    @FXML private Label roomCodeLabel;
    @FXML private Button startMatchBtn;
    @FXML private Label hostNameLabel;
    @FXML private Label opponentNameLabel;

    // FXML ထဲပါရှိသော Avatar Labels နှင့် ImageViews များကို ချိတ်ဆက်ခြင်း
    @FXML private Label hostAvatarLabel;
    @FXML private Label opponentAvatarLabel;
    @FXML private ImageView hostAvatarImageView;
    @FXML private ImageView opponentAvatarImageView;

    private String selectedLanguage = "java";
    private final String SERVER_URL = "wss://websocket-server-s9ki.onrender.com";
    private String currentUserId;
    private String currentUserName;
    private String currentUserAvatarUrl;
    private String roomId;
    private boolean isHost = true;
    private String pendingOpponentId;

    @FXML
    public void initialize() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        this.currentUserId = (currentUser != null) ? String.valueOf(currentUser.getUserId()) : "USER_" + System.currentTimeMillis();
        this.currentUserName = (currentUser != null) ? currentUser.getUsername() : "Player";

        // Quick Match ကဲ့သို့ User object ထဲမှ Profile URL ကို တိုက်ရိုက်ယူသုံးခြင်း
        if (currentUser != null && currentUser.getProfile() != null && !currentUser.getProfile().isEmpty()) {
            this.currentUserAvatarUrl = currentUser.getProfile();
        } else {
            this.currentUserAvatarUrl = "https://NickayJohn26.pythonanywhere.com/uploads/profiles/user_" + currentUserId + "_d7d30c72.png";
        }
    }

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

        if (isHost) {
            // Host ဘက်တွင် မိမိ၏ Profile ပုံနှင့် နာမည်ကို ပြသမည်
            if (hostNameLabel != null) hostNameLabel.setText(currentUserName + " (Host)");
            loadAvatar(hostAvatarImageView, hostAvatarLabel, currentUserAvatarUrl);

            if (opponentNameLabel != null) opponentNameLabel.setText("Waiting for Opponent...");
            if (opponentAvatarLabel != null) {
                opponentAvatarLabel.setText("❓");
                opponentAvatarLabel.setVisible(true);
            }
            if (opponentAvatarImageView != null) opponentAvatarImageView.setImage(null);

            if (startMatchBtn != null) {
                startMatchBtn.setText("Start Match ➔");
                startMatchBtn.setDisable(true);
            }
        } else {
            // Opponent ဘက်တွင် မိမိ၏ Profile ပုံနှင့် နာမည်ကို ပြသမည်
            if (hostNameLabel != null) hostNameLabel.setText("Host (Connecting...)");
            if (hostAvatarLabel != null) {
                hostAvatarLabel.setText("👤");
                hostAvatarLabel.setVisible(true);
            }

            if (opponentNameLabel != null) opponentNameLabel.setText(currentUserName + " (You)");
            loadAvatar(opponentAvatarImageView, opponentAvatarLabel, currentUserAvatarUrl);

            if (startMatchBtn != null) {
                startMatchBtn.setText("Ready");
                startMatchBtn.setDisable(false);
            }
        }

        initWebSocket();
    }

    // ImageView တွင် Profile ပုံ Load လုပ်ပေးပြီး Emoji Label ကို ဖျောက်ပေးသည့် Method
    private void loadAvatar(ImageView imageView, Label avatarLabel, String imageUrl) {
        if (imageView != null && imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image image = new Image(imageUrl, true); // Background loading
                imageView.setImage(image);
                if (avatarLabel != null) {
                    avatarLabel.setVisible(false); // ပုံပေါ်လာပါက အရန် Emoji လေကို ဖျောက်မည်
                }
            } catch (Exception e) {
                System.err.println("Avatar Load Error: " + e.getMessage());
            }
        }
    }

    private void initWebSocket() {
        String wsRegisterId = isHost ? roomId : currentUserId;

        WebSocketService.getInstance().connect(SERVER_URL, wsRegisterId, new WebSocketService.NotificationListener() {
            @Override
            public void onInviteReceived(String fromUser, String payload) {
                Platform.runLater(() -> {
                    String senderName = fromUser;
                    String senderAvatar = "https://NickayJohn26.pythonanywhere.com/uploads/profiles/user_" + fromUser + "_d7d30c72.png"; // Fallback URL

                    if (payload != null) {
                        try {
                            if (payload.contains("NAME:")) {
                                senderName = payload.split("NAME:")[1].split("\\|")[0];
                            }
                            if (payload.contains("AVATAR:")) {
                                senderAvatar = payload.split("AVATAR:")[1].split("\\|")[0];
                            }
                        } catch (Exception e) {
                            senderName = fromUser;
                        }
                    }

                    if (isHost) {
                        pendingOpponentId = fromUser;
                        if (opponentNameLabel != null) {
                            opponentNameLabel.setText(senderName + " (Ready ✅)");
                        }

                        // ⭐ Host ဘက်တွင် Opponent ၏ Profile ပုံကို ချက်ချင်း ဖော်ပြမည်
                        loadAvatar(opponentAvatarImageView, opponentAvatarLabel, senderAvatar);

                        if (startMatchBtn != null) {
                            startMatchBtn.setDisable(false);
                        }

                        // Host ဘက်မှ နာမည်နှင့် Profile ပုံကို Opponent ထံ ပြန်ပို့မည်
                        String hostData = "NAME:" + currentUserName + "|AVATAR:" + currentUserAvatarUrl + "|LANG:" + selectedLanguage;
                        WebSocketService.getInstance().sendInvite(currentUserId, fromUser, hostData);

                    } else {
                        // ⭐ Opponent ဘက်တွင် Host ၏ နာမည်နှင့် Profile ပုံကို ချက်ချင်း ဖော်ပြမည်
                        if (hostNameLabel != null) {
                            hostNameLabel.setText(senderName + " (Host)");
                        }
                        loadAvatar(hostAvatarImageView, hostAvatarLabel, senderAvatar);
                    }
                });
            }

            @Override
            public void onGameStart(String roomId, String language, String opponentId, String questionsJson) {
                Platform.runLater(() -> {
                    FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Sodexa Platform");

                    if (loader != null) {
                        GameController gameController = loader.getController();
                        if (gameController != null) {
                            gameController.setCourseData(language);
                            gameController.setGameSession(roomId, opponentId);
                            gameController.loadQuestionsFromJson(questionsJson);
                        }
                    }
                });
            }

            @Override
            public void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson) {}

            @Override
            public void onQuickMatchTimeout() {}
        });

        if (!isHost) {
            Platform.runLater(() -> {
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {}

                String opponentData = "NAME:" + currentUserName + "|AVATAR:" + currentUserAvatarUrl + "|LANG:" + selectedLanguage;
                WebSocketService.getInstance().sendInvite(currentUserId, roomId, opponentData);
            });
        }
    }
    @FXML
    private void handleStartMatch() {
        if (isHost) {
            if (pendingOpponentId != null) {
                WebSocketService.getInstance().respondInvite(pendingOpponentId, "ACCEPT", selectedLanguage);
            }
        } else {
            if (startMatchBtn != null) {
                startMatchBtn.setText("Ready! ⏳");
                startMatchBtn.setDisable(true);
            }
            if (opponentNameLabel != null) {
                opponentNameLabel.setText(currentUserName + " (Ready ✅)");
            }

            String opponentData = "NAME:" + currentUserName + "|AVATAR:" + currentUserAvatarUrl + "|LANG:" + selectedLanguage;
            WebSocketService.getInstance().sendInvite(currentUserId, roomId, opponentData);
        }
    }

    public void setSelectedLanguage(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
            System.out.println("✅ Lobby Language Set to: " + this.selectedLanguage);
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
                String inviteData = "NAME:" + currentUserName + "|AVATAR:" + currentUserAvatarUrl + "|LANG:" + selectedLanguage;
                WebSocketService.getInstance().sendInvite(currentUserId, targetUserId.trim(), inviteData);

                Button btn = (Button) event.getSource();
                btn.setText("Invited!");
                btn.setDisable(true);
            }
        });
    }

    @FXML
    private void handleLeaveRoom() {
        SceneManager.switchScene("/Project/FRIEND.fxml", "Sodexa Platform");
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