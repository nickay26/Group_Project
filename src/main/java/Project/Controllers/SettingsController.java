package Project.Controllers;

import Project.API_Database.ApiClient;
import Project.API_Database.DatabaseManager;
import Project.Helpers.AvatarCropDialog;
import Project.Helpers.DialogUtils;
import Project.Getters_Setters.User;
import Project.Getters_Setters.UserSession;
import Project.Helpers.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SettingsController {

    // Tabs
    @FXML private Button infoTabBtn;
    @FXML private Button passwordTabBtn;
    @FXML private Button subscriptionTabBtn;
    @FXML private Button deleteTabBtn;

    // Sections
    @FXML private VBox infoSection;
    @FXML private VBox passwordSection;
    @FXML private VBox subscriptionSection;
    @FXML private VBox deleteSection;

    // Subscription Section Controls
    @FXML private Label currentPlanNameLabel; // Make sure fx:id="currentPlanNameLabel" matches your FXML
    @FXML private Label planFeaturesLabel;    // Make sure fx:id="planFeaturesLabel" matches your FXML
    @FXML private Button upgradePlanBtn;      // Make sure fx:id="upgradePlanBtn" matches your FXML

    // Password Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private TextField currentPasswordVisible;
    @FXML private Button currentShowBtn;

    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordVisible;
    @FXML private Button newShowBtn;
    @FXML private PasswordField confirmPasswordField;

    // Info Fields
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextArea bioField;
    @FXML private ComboBox<Country> countryCombo;

    // Avatar Components
    @FXML private StackPane avatarContainer;
    @FXML private ImageView avatarPreview;
    @FXML private Label avatarInitialLabel;
    @FXML private Button changeBtn;
    @FXML private Button deleteBtn;
    @FXML private Button setBtn;

    // Center Container
    @FXML private BorderPane centerContainer;
    @FXML private ScrollPane settingsScrollPane;

    // Temp
    private String tempAvatarPath = null;
    private boolean hasNewAvatar = false;

    // ============================================
    // INITIALIZE
    // ============================================
    @FXML
    public void initialize() {
        showInfoSection();

        // Avatar Container Clip (Circle)
        Circle containerClip = new Circle(50, 50, 50);
        avatarContainer.setClip(containerClip);

        // Country Setup
        setupCountryCombo();

        // User Data
        User user = UserSession.getCurrentUser();
        if (user != null) {
            usernameField.setText(user.getName());
            emailField.setText(user.getEmail());

            loadUserProfileImage(user.getProfile(), user.getName());
        }

        // Default button states
        deleteBtn.setVisible(true);
        deleteBtn.setManaged(true);
        setBtn.setVisible(false);
        setBtn.setManaged(false);

        // Synchronize Subscription UI
        updateSubscriptionUI();
    }

    private void loadUserProfileImage(String profilePath, String name) {
        if (profilePath != null && !profilePath.trim().isEmpty() && !"null".equalsIgnoreCase(profilePath.trim())) {
            try {
                String imageUrl;
                if (profilePath.startsWith("file:") || profilePath.contains(":\\")) {
                    imageUrl = new File(profilePath.replace("file:", "")).toURI().toString();
                } else {
                    imageUrl = ApiClient.getProfileImageUrl(profilePath);
                }

                if (imageUrl != null) {
                    // Background loading = true (prevents UI freeze while downloading image)
                    Image img = new Image(imageUrl, 100, 100, false, true, true);

                    // Set listener for image loading error
                    img.errorProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            javafx.application.Platform.runLater(() -> showInitialAvatar(name));
                        }
                    });

                    avatarPreview.setImage(img);
                    avatarPreview.setVisible(true);
                    avatarInitialLabel.setVisible(false);

                    // Set proper clip on avatarPreview
                    Circle imgClip = new Circle(50, 50, 50);
                    avatarPreview.setClip(imgClip);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Failed to load avatar image: " + e.getMessage());
            }
        }
        showInitialAvatar(name);
    }

    // ============================================
    // DYNAMIC SUBSCRIPTION UI REFRESH
    // ============================================
    private void updateSubscriptionUI() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        if (user.isPremium()) {
            String plan = user.getPlanType() != null ? user.getPlanType().toUpperCase() : "";

            String displayName = plan.contains("YEARLY") ? "Sodex Premium (Yearly)" : "Sodex Premium (Monthly)";

            if (currentPlanNameLabel != null) {
                currentPlanNameLabel.setText(displayName);
            }
            if (planFeaturesLabel != null) {
                planFeaturesLabel.setText("✓ Unlimited Brains (Infinite Energy)\n✓ Interactive Coding Space Access\n✓ All Premium Courses Unlocked");
            }
            if (upgradePlanBtn != null) {
                upgradePlanBtn.setText("Active Plan");
                upgradePlanBtn.setDisable(true);
            }
        } else {
            if (currentPlanNameLabel != null) {
                currentPlanNameLabel.setText("Sodex Free");
            }
            if (planFeaturesLabel != null) {
                planFeaturesLabel.setText("✓ Standard lessons");
            }
            if (upgradePlanBtn != null) {
                upgradePlanBtn.setText("Upgrade Plan");
                upgradePlanBtn.setDisable(false);
            }
        }
    }

    // ============================================
    // COUNTRY COMBO
    // ============================================
    private void setupCountryCombo() {
        ObservableList<Country> countries = FXCollections.observableArrayList(
                new Country("mm", "Myanmar"),
                new Country("us", "United States"),
                new Country("gb", "United Kingdom"),
                new Country("jp", "Japan"),
                new Country("kr", "South Korea"),
                new Country("cn", "China"),
                new Country("in", "India"),
                new Country("th", "Thailand"),
                new Country("sg", "Singapore")
        );

        countryCombo.setItems(countries);
        countryCombo.setValue(countries.get(0));

        countryCombo.setCellFactory(lv -> new CountryCell());
        countryCombo.setButtonCell(new CountryCell());
        countryCombo.setVisibleRowCount(8);

        countryCombo.setPrefWidth(400);
        countryCombo.setMaxWidth(Double.MAX_VALUE);
    }

    public static class Country {
        private final String code;
        private final String name;

        public Country(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() { return code; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class CountryCell extends ListCell<Country> {
        private final HBox content;
        private final ImageView flagView;
        private final Label nameLabel;

        public CountryCell() {
            super();

            flagView = new ImageView();
            flagView.setFitWidth(24);
            flagView.setFitHeight(18);
            flagView.setPreserveRatio(true);
            flagView.setSmooth(true);
            flagView.setCache(true);
            flagView.setCacheHint(CacheHint.QUALITY);

            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1E293B;");

            content = new HBox(10);
            content.setAlignment(Pos.CENTER_LEFT);
            content.getChildren().addAll(flagView, nameLabel);
        }

        @Override
        protected void updateItem(Country country, boolean empty) {
            super.updateItem(country, empty);

            if (empty || country == null) {
                setGraphic(null);
                setText(null);
            } else {
                String path = "/flags/" + country.getCode() + ".png";
                try {
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is != null) {
                        Image img = new Image(is, 48, 36, true, true);
                        flagView.setImage(img);
                    } else {
                        flagView.setImage(null);
                    }
                } catch (Exception e) {
                    flagView.setImage(null);
                }

                nameLabel.setText(country.getName());
                setGraphic(content);
                setText(null);
            }
        }
    }

    private void showInitialAvatar(String name) {
        avatarPreview.setVisible(false);
        avatarInitialLabel.setVisible(true);

        String initial = "?";
        if (name != null && !name.trim().isEmpty()) {
            initial = String.valueOf(name.trim().charAt(0)).toUpperCase();
        }
        avatarInitialLabel.setText(initial);
        avatarInitialLabel.setStyle(
                "-fx-font-size: 40px; -fx-font-weight: bold; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-color: #EC4899; " +
                        "-fx-background-radius: 50px; " +
                        "-fx-alignment: center; " +
                        "-fx-pref-width: 100px; -fx-pref-height: 100px;"
        );
    }

    @FXML
    private void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(changeBtn.getScene().getWindow());

        if (selectedFile != null) {
            String croppedPath = AvatarCropDialog.showCropDialog(selectedFile);
            if (croppedPath != null) {
                tempAvatarPath = croppedPath;
                hasNewAvatar = true;

                String fileUri = new File(croppedPath).toURI().toString();
                Image img = new Image(fileUri, 100, 100, false, true, true);
                avatarPreview.setImage(img);
                avatarPreview.setVisible(true);
                avatarInitialLabel.setVisible(false);

                Circle imgClip = new Circle(50, 50, 50);
                avatarPreview.setClip(imgClip);

                deleteBtn.setVisible(false);
                deleteBtn.setManaged(false);
                setBtn.setVisible(true);
                setBtn.setManaged(true);
            }
        }
    }

    @FXML
    private void handleDeleteImage() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        showInitialAvatar(user.getName());
        avatarPreview.setImage(null);
        tempAvatarPath = null;
        hasNewAvatar = false;

        deleteBtn.setVisible(true);
        deleteBtn.setManaged(true);
        setBtn.setVisible(false);
        setBtn.setManaged(false);

        user.setProfile(null);
        DatabaseManager.updateUserProfile(user.getUserId(), null);
    }

    @FXML
    private void handleSetImage() {
        if (!hasNewAvatar || tempAvatarPath == null) return;

        User user = UserSession.getCurrentUser();
        if (user == null) return;

        File imageFile;
        try {
            if (tempAvatarPath.startsWith("file:")) {
                imageFile = new File(new java.net.URI(tempAvatarPath));
            } else {
                imageFile = new File(tempAvatarPath);
            }
        } catch (Exception e) {
            imageFile = new File(tempAvatarPath.replace("file:/", "").replace("file:", ""));
        }

        if (!imageFile.exists()) {
            System.err.println("File not found on disk: " + imageFile.getAbsolutePath());
            return;
        }

        ApiClient.uploadProfileImageAsync(user.getUserId(), imageFile)
                .thenAccept(responseJson -> {
                    javafx.application.Platform.runLater(() -> {
                        if (responseJson != null) {
                            try {
                                org.json.JSONObject json = new org.json.JSONObject(responseJson);
                                String savedFileName = json.optString("profile_pic", null);

                                if (savedFileName != null) {
                                    user.setProfile(savedFileName);

                                    setBtn.setVisible(false);
                                    setBtn.setManaged(false);
                                    deleteBtn.setVisible(true);
                                    deleteBtn.setManaged(true);
                                    hasNewAvatar = false;

                                    // Reload avatar locally immediately
                                    loadUserProfileImage(savedFileName, user.getName());

                                    // Update main layout top bar avatar
                                    if (MainLayoutController.getInstance() != null) {
                                        MainLayoutController.getInstance().loadUserProfileImage();
                                    }
                                    // ⭐ Alert အစား DialogUtils ကို ခေါ်သုံးခြင်း
                                    DialogUtils.showSuccessDialog("Profile picture uploaded successfully!");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                });
    }

    // ============================================
    // TABS
    // ============================================
    @FXML
    private void showInfoSection() {
        centerContainer.setCenter(settingsScrollPane);
        hideAll();
        infoSection.setVisible(true);
        infoSection.setManaged(true);
        setActiveTab(infoTabBtn);
    }

    @FXML
    private void showPasswordSection() {
        centerContainer.setCenter(settingsScrollPane);
        hideAll();
        passwordSection.setVisible(true);
        passwordSection.setManaged(true);
        setActiveTab(passwordTabBtn);
    }

    @FXML
    private void showSubscriptionSection() {
        centerContainer.setCenter(settingsScrollPane);
        hideAll();
        subscriptionSection.setVisible(true);
        subscriptionSection.setManaged(true);
        setActiveTab(subscriptionTabBtn);

        // Refresh subscription section dynamic labels
        updateSubscriptionUI();
    }

    @FXML
    private void showDeleteSection() {
        centerContainer.setCenter(settingsScrollPane);
        hideAll();
        deleteSection.setVisible(true);
        deleteSection.setManaged(true);
        setActiveTab(deleteTabBtn);
    }

    private void hideAll() {
        infoSection.setVisible(false);
        infoSection.setManaged(false);
        passwordSection.setVisible(false);
        passwordSection.setManaged(false);
        subscriptionSection.setVisible(false);
        subscriptionSection.setManaged(false);
        deleteSection.setVisible(false);
        deleteSection.setManaged(false);
    }

    private void setActiveTab(Button active) {
        infoTabBtn.getStyleClass().remove("settings-tab-active");
        passwordTabBtn.getStyleClass().remove("settings-tab-active");
        subscriptionTabBtn.getStyleClass().remove("settings-tab-active");
        deleteTabBtn.getStyleClass().remove("settings-tab-active");

        if (active != null && !active.getStyleClass().contains("settings-tab-active")) {
            active.getStyleClass().add("settings-tab-active");
        }
    }

    @FXML
    private void handleSaveInfo() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        String newName = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            // ⭐ Alert အစား DialogUtils ကို ခေါ်သုံးခြင်း
            DialogUtils.showErrorDialog("Username and Email cannot be empty.");
            return;
        }

        boolean success = DatabaseManager.updateUserInfo(user.getUserId(), newName, newEmail);
        if (success) {
            user.setName(newName);
            user.setEmail(newEmail);
            // ⭐ Alert အစား DialogUtils ကို ခေါ်သုံးခြင်း
            DialogUtils.showSuccessDialog("Profile information updated successfully!");
        } else {
            // ⭐ Alert အစား DialogUtils ကို ခေါ်သုံးခြင်း
            DialogUtils.showErrorDialog("Failed to update information on server.");
        }
    }

    @FXML
    private void handleChangePassword() {
        String currentPass = currentPasswordField.isVisible() ? currentPasswordField.getText().trim() : currentPasswordVisible.getText().trim();
        String newPass = newPasswordField.isVisible() ? newPasswordField.getText().trim() : newPasswordVisible.getText().trim();

        if (currentPass.isEmpty() || newPass.isEmpty()) {
            DialogUtils.showErrorDialog("Please fill in all password fields.");
            return;
        }

        if (newPass.length() < 6) {
            DialogUtils.showErrorDialog("New password must be at least 6 characters long.");
            return;
        }

        if (UserSession.getCurrentUser() == null) {
            DialogUtils.showErrorDialog("No active user session found. Please log in again.");
            return;
        }

        int userId = UserSession.getCurrentUser().getUserId();

        if (changeBtn != null) {
            changeBtn.setDisable(true);
            changeBtn.setText("Updating...");
        }

        new Thread(() -> {
            boolean success = DatabaseManager.updatePassword(userId, currentPass, newPass);
            Platform.runLater(() -> {
                if (changeBtn != null) {
                    changeBtn.setDisable(false);
                    changeBtn.setText("Change");
                }
                if (success) {
                    currentPasswordField.clear();
                    if (currentPasswordVisible != null) currentPasswordVisible.clear();
                    newPasswordField.clear();
                    if (newPasswordVisible != null) newPasswordVisible.clear();

                    DialogUtils.showSuccessDialog("Your password has been changed successfully!");
                } else {
                    DialogUtils.showErrorDialog("Incorrect current password or server error. Please try again.");
                }
            });
        }).start();
    }

    // ⭐ မူရင်း showAlert method ကို ဖျက်ပစ်လိုက်ပါပြီ (အသုံးမလိုတော့ပါ)

    // Delete Button ကို နှိပ်လိုက်ရင်
    @FXML
    private void handleDeleteAccount() {
        // ⭐ Dark Theme Confirmation Dialog ခေါ်ခြင်း (OK/Cancel ပါမယ်)
        boolean isConfirmed = DialogUtils.showConfirmationDialog(
                "Delete Account",
                "Are you sure you want to delete your account?\nThis action cannot be undone!"
        );

        if (isConfirmed) {
            int userId = UserSession.getUserId();
            boolean isDeleted = DatabaseManager.deleteUser(userId);

            if (isDeleted) {
                // အောင်မြင်ရင် Success Dialog ပြ
                DialogUtils.showSuccessDialog("Your account has been deleted successfully.");
                UserSession.clearSession();
                SceneManager.switchScene("WelcomeView.fxml", "Sodexa Platform - Welcome");
            } else {
                // မအောင်မြင်ရင် Error Dialog ပြ
                DialogUtils.showErrorDialog("Failed to delete account. Please try again.");
            }
        }
    }

    @FXML
    private void handleUpgradePlan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Project/PremiumView.fxml"));
            Parent premiumView = loader.load();

            Button backBtn = new Button("← Back");
            backBtn.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #1E293B; " +
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 15;"
            );

            backBtn.setOnAction(e -> {
                centerContainer.setCenter(settingsScrollPane);
                showSubscriptionSection();
            });

            HBox backBar = new HBox(backBtn);
            backBar.setAlignment(Pos.CENTER_LEFT);
            backBar.setStyle("-fx-padding: 10 20 0 20;");

            Label titleLabel = (Label) premiumView.lookup(".main-title");
            if (titleLabel != null) {
                titleLabel.setStyle(
                        "-fx-text-fill: #1E293B; " +
                                "-fx-font-size: 42px; " +
                                "-fx-font-weight: bold;"
                );
            }
            Label subTitle = (Label) premiumView.lookup(".sub-title");
            if (subTitle != null) {
                subTitle.setStyle(
                        "-fx-text-fill: #64748B; " +
                                "-fx-font-size: 16px;"
                );
            }

            VBox wrapper = new VBox(backBar, premiumView);
            VBox.setVgrow(premiumView, Priority.ALWAYS);

            ScrollPane premiumScroll = new ScrollPane(wrapper);
            premiumScroll.setFitToWidth(true);
            premiumScroll.setFitToHeight(true);
            premiumScroll.setStyle("-fx-background-color: transparent;");

            centerContainer.setCenter(premiumScroll);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleCurrentPassword() {
        if (currentPasswordVisible.isVisible()) {
            currentPasswordVisible.setVisible(false);
            currentPasswordField.setVisible(true);
            currentPasswordField.setText(currentPasswordVisible.getText());
            currentShowBtn.setText("Show");
        } else {
            currentPasswordVisible.setText(currentPasswordField.getText());
            currentPasswordVisible.setVisible(true);
            currentPasswordField.setVisible(false);
            currentShowBtn.setText("Hide");
        }
    }

    @FXML
    private void toggleNewPassword() {
        if (newPasswordVisible.isVisible()) {
            newPasswordVisible.setVisible(false);
            newPasswordField.setVisible(true);
            newPasswordField.setText(newPasswordVisible.getText());
            newShowBtn.setText("Show");
        } else {
            newPasswordVisible.setText(newPasswordField.getText());
            newPasswordVisible.setVisible(true);
            newPasswordField.setVisible(false);
            newShowBtn.setText("Hide");
        }
    }
    @FXML
    private void handleClose() {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.handleClosePage();
        }
    }
}