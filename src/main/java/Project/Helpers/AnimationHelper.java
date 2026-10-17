package Project.Helpers;

import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.Node;

public class AnimationHelper {

    // ဆုလာဘ်ပြမည့် Node ကို Pop-up Animation ဖြင့် ပေါ်လာစေရန်
    public static void playRewardPopUp(Node rewardNode) {
        // ပုံမှန်အရွယ်အစားအထိ ကြီးလာစေရန် ScaleTransition
        ScaleTransition scaleTrans = new ScaleTransition(Duration.millis(400), rewardNode);
        scaleTrans.setFromX(0.1);
        scaleTrans.setFromY(0.1);
        scaleTrans.setToX(1.0);
        scaleTrans.setToY(1.0);

        // တဖြည်းဖြည်း ပေါ်လာစေရန် FadeTransition
        FadeTransition fadeTrans = new FadeTransition(Duration.millis(400), rewardNode);
        fadeTrans.setFromValue(0.0);
        fadeTrans.setToValue(1.0);

        scaleTrans.play();
        fadeTrans.play();
    }
}