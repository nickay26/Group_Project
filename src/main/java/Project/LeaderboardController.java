package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class LeaderboardController {

    @FXML private Label leagueTitleLabel;

    @FXML private VBox nebulaContainer;
    @FXML private ImageView nebulaImg;
    @FXML private Label nebulaLabel;

    @FXML private VBox protostarContainer;
    @FXML private ImageView protostarImg;
    @FXML private Label protostarLabel;

    @FXML private VBox supernovaContainer;
    @FXML private ImageView supernovaImg;
    @FXML private Label supernovaLabel;

    @FXML
    public void initialize() {
        loadImages();

        int currentLevel = 0; // Testing Level
        updateUserProgress(currentLevel);
    }

    private void loadImages() {
        try {
            if (nebulaImg != null && nebulaImg.getImage() == null) {
                nebulaImg.setImage(new Image(getClass().getResourceAsStream("/Images/nebula.png")));
            }
            if (protostarImg != null && protostarImg.getImage() == null) {
                protostarImg.setImage(new Image(getClass().getResourceAsStream("/Images/Protostar.png")));
            }
            if (supernovaImg != null && supernovaImg.getImage() == null) {
                supernovaImg.setImage(new Image(getClass().getResourceAsStream("/Images/Supernova.png")));
            }
        } catch (Exception e) {
            System.err.println("Images load karne me dikkat aayi: " + e.getMessage());
        }
    }

    public void updateUserProgress(int userLevel) {
        int NEBULA_LEVEL = 1;
        int PROTOSTAR_LEVEL = 5;
        int SUPERNOVA_LEVEL = 10;

        if (leagueTitleLabel != null) {
            if (userLevel >= SUPERNOVA_LEVEL) {
                leagueTitleLabel.setText("Supernova league");
            } else if (userLevel >= PROTOSTAR_LEVEL) {
                leagueTitleLabel.setText("Protostar league");
            } else if (userLevel >= NEBULA_LEVEL) {
                leagueTitleLabel.setText("Nebula league");
            } else {
                leagueTitleLabel.setText("Venus league");
            }
        }

        setCardState(nebulaImg, userLevel >= NEBULA_LEVEL);
        setCardState(protostarImg, userLevel >= PROTOSTAR_LEVEL);
        setCardState(supernovaImg, userLevel >= SUPERNOVA_LEVEL);
    }

    private void setCardState(ImageView img, boolean isUnlocked) {
        if (img != null) {
            img.setOpacity(isUnlocked ? 1.0 : 0.0);
        }
    }

    @FXML
    private void handleStartLearning() {
        // Navigation Logic
    }
}