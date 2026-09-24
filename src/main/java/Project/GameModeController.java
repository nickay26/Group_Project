package Project;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class GameModeController {

    @FXML private VBox quickMatchCard;
    @FXML private VBox friendMatchCard;
    @FXML private ComboBox<String> languageComboBox;

    private String selectedMode = "Quick Match";

    @FXML
    public void initialize() {
        languageComboBox.getItems().clear();
        languageComboBox.getItems().addAll(
                "C++ (Basic)",
                "C++ (Intermediate)",
                "Java (Basic)",
                "Java (Intermediate)",
                "Python (Basic)",
                "Python (Intermediate)"
        );
        languageComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleQuickMatch() {
        selectedMode = "Quick Match";
        quickMatchCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-background-radius: 16; -fx-border-color: #000000; -fx-border-radius: 16; -fx-border-width: 2.5; -fx-cursor: hand;");
        friendMatchCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-background-radius: 16; -fx-border-color: #cccccc; -fx-border-radius: 16; -fx-border-width: 1.5; -fx-cursor: hand;");
    }

    @FXML
    private void handleFriendMatch() {
        selectedMode = "Friend Match";
        friendMatchCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-background-radius: 16; -fx-border-color: #000000; -fx-border-radius: 16; -fx-border-width: 2.5; -fx-cursor: hand;");
        quickMatchCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-background-radius: 16; -fx-border-color: #cccccc; -fx-border-radius: 16; -fx-border-width: 1.5; -fx-cursor: hand;");
    }

    @FXML
    private void handleStartGame() {
        String selectedLang = languageComboBox.getValue().toLowerCase();

        if ("Friend Match".equals(selectedMode)) {
            // Friend Match ဆိုပါက Create/Join ရွေးသည့် FriendMatchView သို့ သွားမည်
            SceneManager.switchScene("/Project/FriendMatchView.fxml", "Play with a Friend");
        } else {
            // Quick Match ဆိုပါက GameView အစား QuickMatchLobbyView သို့ အရင်သွားမည်
            FXMLLoader loader = SceneManager.switchScene("/Project/QuickMatchLobbyView.fxml", "Finding Match...");
            if (loader != null) {
                QuickMatchLobbyController lobbyController = loader.getController();
                lobbyController.setSelectedLanguage(selectedLang);
            }
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/Project/LearnPage.fxml", "Mimo Platform - Dashboard");
    }
}