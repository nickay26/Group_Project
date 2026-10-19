package Project.GameClasses;

import Project.Helpers.SceneManager;
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
        // Quick Match Card ကို Select လုပ်ခြင်း
        quickMatchCard.getStyleClass().add("selected-card");
        friendMatchCard.getStyleClass().remove("selected-card");
        String cleanLanguage = getSelectedLanguageClean();
        if (cleanLanguage == null) return;
        launchGameView("QUICK", cleanLanguage);
    }

    @FXML
    private void handleFriendMatch() {
        // Friend Match Card ကို Select လုပ်ခြင်း
        friendMatchCard.getStyleClass().add("selected-card");
        quickMatchCard.getStyleClass().remove("selected-card");
        String cleanLanguage = getSelectedLanguageClean();
        if (cleanLanguage == null) return;
        launchGameView("FRIEND", cleanLanguage);
    }

    private String getSelectedLanguageClean() {
        String rawLang = languageComboBox.getValue();
        if (rawLang == null || rawLang.trim().isEmpty()) {
            return "C++"; // Default fallback
        }

        if (rawLang.contains("(")) {
            return rawLang.substring(0, rawLang.indexOf("(")).trim();
        }
        return rawLang.trim();
    }

    private void launchGameView(String mode, String language) {
        try {
            if ("QUICK".equals(mode)) {

                FXMLLoader loader = SceneManager.switchScene("QUICK.fxml");
                if (loader != null) {
                    QuickMatchLobbyController controller = loader.getController();
                    if (controller != null) {
                        controller.setSelectedLanguage(language);
                    }
                }
            } else if ("FRIEND".equals(mode)) {
                // ⭐ FRIEND.fxml သို့ ကူးပြောင်းစဉ် ရွေးချယ်ထားသော language ကို လွှဲပေးခြင်း
                FXMLLoader loader = SceneManager.switchScene("/Project/FRIEND.fxml", "Sodexa Platform");
                if (loader != null) {
                    FriendMatchController controller = loader.getController();
                    if (controller != null) {
                        controller.setSelectedLanguage(language);
                    }
                }
            }
            System.out.println("Starting " + mode + " Match with language: " + language);
        } catch (Exception e) {
            System.err.println("Scene Switch Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}