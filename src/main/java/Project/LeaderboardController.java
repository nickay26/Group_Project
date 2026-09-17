package Project;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

public class LeaderboardController {

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private void handleStartLearning() {
        // နည်းလမ်း ၁: ချက်ချင်း အောက်ဆုံးသို့ Scroll ဆွဲရန်
        // mainScrollPane.setVvalue(1.0);

        // နည်းလမ်း ၂: ဖြည်းဖြည်းချင်း Smooth ဖြစ်အောင် Scroll ဆွဲရန် (Smooth Animation)
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(mainScrollPane.vvalueProperty(), 1.0);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(500), keyValue); // 0.5 စက္ကန့်
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }
}