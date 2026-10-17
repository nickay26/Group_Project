package Project.GameClasses;

import Project.Helpers.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MatchResultController {

    @FXML private Label resultHeaderLabel; // "VICTORY!" သို့မဟုတ် "DEFEAT"
    @FXML private Label xpEarnedLabel;     // "+50 XP"

    public void setResultData(boolean isWinner, int xpGained) {
        if (isWinner) {
            resultHeaderLabel.setText("🎉 VICTORY!");
            resultHeaderLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
        } else {
            resultHeaderLabel.setText("💔 DEFEAT");
            resultHeaderLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        }

        xpEarnedLabel.setText("+" + xpGained + " XP Earned");
    }

    @FXML
    private void handlePlayAgain() {
        SceneManager.switchScene("/Project/FRIEND.fxml", "Play with a Friend");
    }
}