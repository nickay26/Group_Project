package Project.Helpers;

import javafx.scene.media.AudioClip;

public class SoundHelper {

    private static AudioClip correctSound;
    private static AudioClip wrongSound;
    private static AudioClip rewardSound;

    private static boolean muted = false;

    static {
        try {
            correctSound = load("/sounds/correct.wav");
            wrongSound   = load("/sounds/wrong.wav");
            rewardSound  = load("/sounds/reward.wav");

            // လိုအပ်ရင် volume ချိန်နိုင်တယ် (0.0 - 1.0)
            if (correctSound != null) correctSound.setVolume(0.8);
            if (wrongSound   != null) wrongSound.setVolume(0.8);
            if (rewardSound  != null) rewardSound.setVolume(0.9);

        } catch (Exception e) {
            System.err.println("Sound load failed: " + e.getMessage());
        }
    }

    private static AudioClip load(String path) {
        try {
            var url = SoundHelper.class.getResource(path);
            if (url == null) {
                System.err.println("⚠ Sound file not found: " + path);
                return null;
            }
            return new AudioClip(url.toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void playCorrect() { if (!muted && correctSound != null) correctSound.play(); }
    public static void playWrong()   { if (!muted && wrongSound   != null) wrongSound.play(); }
    public static void playReward()  { if (!muted && rewardSound  != null) rewardSound.play(); }

    public static void setMuted(boolean m) { muted = m; }
    public static boolean isMuted() { return muted; }
}