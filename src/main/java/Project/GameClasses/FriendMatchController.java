package Project.GameClasses;

import Project.Helpers.SceneManager;
import Project.Controllers.MainLayoutController;
import Project.Getters_Setters.User;
import Project.Getters_Setters.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class FriendMatchController {

    @FXML private TextField joinCodeInput;
    @FXML private Label statusLabel;

    private static final String SERVER_URL = "wss://websocket-server-s9ki.onrender.com";
    private String selectedLanguage = "Java (Basic)";

    // ⭐ Language လက်ခံမည့် Method ထည့်ပါ
    public void setSelectedLanguage(String language) {
        if (language != null && !language.isEmpty()) {
            this.selectedLanguage = language;
        }
    }

    @FXML
    public void initialize() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? String.valueOf(currentUser.getUserId()) : "USER_" + System.currentTimeMillis();

        WebSocketService.getInstance().connect(SERVER_URL, currentUserId, new WebSocketService.NotificationListener() {
            @Override
            public void onInviteReceived(String fromUser, String language) {}

            @Override
            public void onGameStart(String roomId, String language, String opponentId, String questionsJson) {}

            @Override
            public void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson) {}

            @Override
            public void onQuickMatchTimeout() {}
        });
    }

    public void handleInvitePlayer(String targetUserId, String language) {
        User currentUser = UserSession.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? String.valueOf(currentUser.getUserId()) : "GUEST";

        WebSocketService.getInstance().sendInvite(currentUserId, targetUserId, language != null ? language : "java");

        if (statusLabel != null) {
            statusLabel.setText("Invite sent to " + targetUserId + "...");
        }
    }

    public void handleInvitePlayer(String targetUserId) {
        handleInvitePlayer(targetUserId, "java");
    }

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
            statusLabel.setText("Connecting to room " + enteredCode + "...");
        }

        FXMLLoader loader = SceneManager.switchScene("/Project/CreateRoomLobbyView.fxml", "Room Lobby");
        if (loader != null) {
            CreateRoomLobbyController lobbyController = loader.getController();
            if (lobbyController != null) {
                lobbyController.setupRoom(enteredCode, false);
            }
        }
    }

    @FXML
    private void handleCreateRoom() {
        FXMLLoader loader = SceneManager.switchScene("/Project/CreateRoomLobbyView.fxml", "Create Room Lobby");
        if (loader != null) {
            CreateRoomLobbyController lobbyController = loader.getController();
            if (lobbyController != null) {
                lobbyController.setSelectedLanguage(selectedLanguage); // ⭐ Lobby ဆီသို့ Language ပို့ပေးခြင်း
                lobbyController.setupRoom(null, true);
            }
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("MainLayoutView.fxml", "Sodexa Platform");
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPage("GameModeView.fxml");
        }
    }
}