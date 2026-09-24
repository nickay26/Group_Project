package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class LeaderboardController {

    @FXML private Label leagueTitleLabel;

    @FXML private StackPane nebulaBox;
    @FXML private ImageView nebulaImg;
    @FXML private Label nebulaLabel;
    @FXML private Label nebulaLockIcon;

    @FXML private StackPane protostarBox;
    @FXML private ImageView protostarImg;
    @FXML private Label protostarLabel;
    @FXML private Label protostarLockIcon;

    @FXML private StackPane supernovaBox;
    @FXML private ImageView supernovaImg;
    @FXML private Label supernovaLabel;
    @FXML private Label supernovaLockIcon;

    @FXML
    public void initialize() {
        // Image များကို resource folder ထဲမှ dynamic သို့ တိုက်ရိုက် load လုပ်ခြင်း
        loadImages();

        // စမ်းသပ်ရန် Level ပြောင်းကြည့်ပါ (0, 1, 5, 10)
        int currentLevel = 0;
        updateUserProgress(currentLevel);
    }

    private void loadImages() {
        try {
            nebulaImg.setImage(new Image(getClass().getResourceAsStream("/Nebula.png")));
            protostarImg.setImage(new Image(getClass().getResourceAsStream("/Protostar.png")));
            supernovaImg.setImage(new Image(getClass().getResourceAsStream("/Supernova.png")));
        } catch (Exception e) {
            System.err.println("Images could not be loaded: " + e.getMessage());
        }
    }

    public void updateUserProgress(int userLevel) {
        int NEBULA_LEVEL = 1;
        int PROTOSTAR_LEVEL = 5;
        int SUPERNOVA_LEVEL = 10;

        // Dynamic Title Update
        if (userLevel >= SUPERNOVA_LEVEL) {
            leagueTitleLabel.setText("Supernova league");
        } else if (userLevel >= PROTOSTAR_LEVEL) {
            leagueTitleLabel.setText("Protostar league");
        } else if (userLevel >= NEBULA_LEVEL) {
            leagueTitleLabel.setText("Nebula league");
        } else {
            leagueTitleLabel.setText("Venus league");
        }

        // Dynamic Lock / Unlock States
        setCardState(nebulaBox, nebulaImg, nebulaLabel, nebulaLockIcon, userLevel >= NEBULA_LEVEL, "#EBF8FF");
        setCardState(protostarBox, protostarImg, protostarLabel, protostarLockIcon, userLevel >= PROTOSTAR_LEVEL, "#FAF5FF");
        setCardState(supernovaBox, supernovaImg, supernovaLabel, supernovaLockIcon, userLevel >= SUPERNOVA_LEVEL, "#FEFCBF");
    }

    private void setCardState(StackPane cardBox, ImageView img, Label titleLabel, Label lockIcon, boolean isUnlocked, String activeBgColor) {
        if (isUnlocked) {
            img.setVisible(true);
            titleLabel.setVisible(true);
            lockIcon.setVisible(false);
            cardBox.setStyle("-fx-background-color: " + activeBgColor + "; -fx-background-radius: 12px;");
        } else {
            img.setVisible(false);
            titleLabel.setVisible(false);
            lockIcon.setVisible(true);
            cardBox.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 12px;");
        }
    }

    @FXML
    private void handleStartLearning() {
        // Navigation Code
    }
}