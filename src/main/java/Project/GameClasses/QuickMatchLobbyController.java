package Project.GameClasses;

import Project.API_Database.ApiClient;
import Project.Helpers.SceneManager;
import Project.Controllers.MainLayoutController;
import Project.Getters_Setters.User;
import Project.Getters_Setters.UserSession;
import org.json.JSONObject;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class QuickMatchLobbyController {

    // Status & Countdown Labels
    @FXML private Label statusTitleLabel;
    @FXML private Label countdownLabel;

    // Current User Profile (ဘယ်ဘက် Card)
    @FXML private ImageView userAvatarImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userSubTitleLabel;

    // Opponent Profile (ညာဘက် Card)
    @FXML private ImageView opponentAvatarImageView;
    @FXML private Label opponentAvatarLabel;
    @FXML private Label opponentNameLabel;
    @FXML private Label opponentStatusLabel;

    private String selectedLanguage = "C++";
    private String currentUserId;

    // Cloud WebSocket Server & PythonAnywhere REST API Base URLs
    private static final String SERVER_URL = "wss://websocket-server-s9ki.onrender.com";
    private static final String PYTHONANYWHERE_BASE_URL = "https://nickayjohn26.pythonanywhere.com";

    public void setSelectedLanguage(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.selectedLanguage = lang;
        }

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.currentUserId = String.valueOf(currentUser.getUserId());
        } else {
            this.currentUserId = "GUEST_" + System.currentTimeMillis();
        }

        startMatchmaking();
    }

    @FXML
    public void initialize() {
        loadCurrentUserProfile();
    }

    private void loadCurrentUserProfile() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getUsername());
            }

            if (userSubTitleLabel != null) {
                userSubTitleLabel.setText(currentUser.getEmail());
            }

            if (userAvatarImageView != null && currentUser.getProfilePicture() != null && !currentUser.getProfilePicture().isEmpty()) {
                try {
                    Image profileImg = new Image(currentUser.getProfilePicture(), true);
                    userAvatarImageView.setImage(profileImg);

                    Circle clip = new Circle(40, 40, 40);
                    userAvatarImageView.setClip(clip);
                } catch (Exception e) {
                    System.out.println("Profile image load error: " + e.getMessage());
                }
            }
        } else {
            if (userNameLabel != null) userNameLabel.setText("You (Guest)");
            if (userSubTitleLabel != null) userSubTitleLabel.setText("Ready to Play");
        }
    }

    private void startMatchmaking() {
        if (statusTitleLabel != null) statusTitleLabel.setText("Searching for Online Opponent...");
        if (opponentNameLabel != null) opponentNameLabel.setText("Waiting for player...");
        if (opponentStatusLabel != null) opponentStatusLabel.setText("In Queue");

        if (opponentAvatarLabel != null) opponentAvatarLabel.setText("🔍");

        WebSocketService.getInstance().connect(
                SERVER_URL,
                currentUserId,
                new WebSocketService.NotificationListener() {
                    @Override
                    public void onInviteReceived(String fromUser, String language) {}

                    @Override
                    public void onGameStart(String roomId, String language, String opponentId, String questionsJson) {}

                    @Override
                    public void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson) {
                        Platform.runLater(() -> {
                            if (statusTitleLabel != null) statusTitleLabel.setText("Match Found!");
                            if (countdownLabel != null) countdownLabel.setText("Game starting in 4 seconds...");

                            // PythonAnywhere API မှ Opponent အချက်အလက်များ လှမ်းယူပြသခြင်း
                            fetchOpponentProfile(opponentId);

                            // ၄ စက္ကန့် စောင့်ဆိုင်းပေးသည့် Delay Transition
                            PauseTransition delay = new PauseTransition(Duration.seconds(4));

                            delay.setOnFinished(event -> {
                                FXMLLoader loader = SceneManager.switchScene("/Project/GameView.fxml", "Mimo Platform - Live Coding Arena");
                                if (loader != null) {
                                    GameController gameController = loader.getController();
                                    gameController.setCourseData(selectedLanguage);
                                    gameController.setGameSession(roomId, opponentId);
                                    gameController.loadQuestionsFromJson(questionsJson);
                                }
                            });

                            delay.play();
                        });
                    }

                    @Override
                    public void onQuickMatchTimeout() {
                        Platform.runLater(() -> {
                            if (statusTitleLabel != null) statusTitleLabel.setText("No Player Found");
                            if (opponentNameLabel != null) opponentNameLabel.setText("Queue Timeout");
                            if (opponentStatusLabel != null) opponentStatusLabel.setText("Failed");
                            if (countdownLabel != null) countdownLabel.setText("No active player online. Please try again later!");
                        });
                    }
                },
                () -> WebSocketService.getInstance().joinQuickMatch(currentUserId, selectedLanguage)
        );
    }

    private void fetchOpponentProfile(String opponentId) {
        System.out.println("🔍 [DEBUG] Requesting Opponent Profile for ID: " + opponentId);

        ApiClient.getUserProfileAsync(opponentId)
                .thenAccept(responseBody -> {
                    System.out.println("📄 [DEBUG] PythonAnywhere Response Body: " + responseBody);

                    if (responseBody != null && !responseBody.contains("404 Not Found")) {
                        try {
                            JSONObject json = new JSONObject(responseBody);

                            String name = json.optString("username", json.optString("Name", "User " + opponentId));

                            // PythonAnywhere DB Key ဖြစ်သော profile_pic ကို ဦးစားပေးဖတ်ရှုရန် ပြင်ထားသည်
                            String profilePic = json.optString("profile_pic", json.optString("profile_picture", json.optString("Profile", "")));

                            int xp = json.optInt("xp", json.optInt("XP", 0));
                            String statusInfo = "XP: " + xp + " • Ready";

                            Platform.runLater(() -> {
                                if (opponentNameLabel != null) opponentNameLabel.setText(name);
                                if (opponentStatusLabel != null) opponentStatusLabel.setText(statusInfo);
                                updateOpponentAvatar(profilePic);
                            });
                            return;
                        } catch (Exception e) {
                            System.out.println("❌ [DEBUG] JSON Parsing Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println("⚠ [DEBUG] Profile request returned 404 or null!");
                    }

                    Platform.runLater(() -> setDefaultOpponentDisplay(opponentId));
                });
    }

    private void updateOpponentAvatar(String profileUrl) {
        if (profileUrl != null && !profileUrl.isEmpty() && opponentAvatarImageView != null) {
            try {
                // Background loading = true ပေးထားခြင်းဖြင့် UI မတောင့်ဘဲ Load လုပ်နိုင်မည်
                Image img = new Image(profileUrl, true);
                opponentAvatarImageView.setImage(img);

                Circle clip = new Circle(40, 40, 40);
                opponentAvatarImageView.setClip(clip);

                if (opponentAvatarLabel != null) opponentAvatarLabel.setVisible(false);
                opponentAvatarImageView.setVisible(true);
                return;
            } catch (Exception ignored) {}
        }

        setDefaultOpponentAvatar();
    }

    private void setDefaultOpponentAvatar() {
        if (opponentAvatarLabel != null) {
            opponentAvatarLabel.setText("👤");
            opponentAvatarLabel.setVisible(true);
        }
        if (opponentAvatarImageView != null) {
            opponentAvatarImageView.setVisible(false);
        }
    }

    private void setDefaultOpponentDisplay(String opponentId) {
        if (opponentNameLabel != null) opponentNameLabel.setText("User " + opponentId);
        if (opponentStatusLabel != null) opponentStatusLabel.setText("Ready");
        setDefaultOpponentAvatar();
    }

    @FXML
    private void handleCancel() {
        WebSocketService.getInstance().cancelQuickMatch(currentUserId);

        SceneManager.switchScene("MainLayoutView.fxml", "Select Game Mode");
        MainLayoutController.getInstance().loadPage("GameModeView.fxml");
    }
}