package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ProfileViewController {

    @FXML private ImageView avatarImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label streakLabel;
    @FXML private Label xpLabel;
    @FXML private Label coinLabel;
    @FXML private Label brainLabel;
    @FXML private Button editProfileBtn;

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        User currentUser = UserSession.getCurrentUser();

        if (currentUser != null) {
            userNameLabel.setText(currentUser.getName());
            userEmailLabel.setText(currentUser.getEmail());
            streakLabel.setText(String.valueOf(currentUser.getStreak()));
            xpLabel.setText(String.valueOf(currentUser.getXp()));
            coinLabel.setText(String.valueOf(currentUser.getCoin()));
            brainLabel.setText(String.valueOf(currentUser.getBrainCount()));

            // Profile Picture ရှိလျှင် Load လုပ်ခြင်း
            if (currentUser.getProfile() != null && !currentUser.getProfile().isEmpty()) {
                try {
                    avatarImageView.setImage(new Image(currentUser.getProfile(), true));
                } catch (Exception e) {
                    System.out.println("Could not load user avatar: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleEditProfile() {
        System.out.println("Navigating to Edit Profile...");
        // MainLayoutController မှတစ်ဆင့် Edit Profile Page သို့ တွန်းပေးရန်
    }
}