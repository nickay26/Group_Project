package Project;

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
    // ✅ Password Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private TextField currentPasswordVisible;
    @FXML private Button currentShowBtn;

    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordVisible;
    @FXML private Button newShowBtn;
    // Sections
    @FXML private VBox infoSection;
    @FXML private VBox passwordSection;
    @FXML private VBox subscriptionSection;
    @FXML private VBox deleteSection;

    // Info Fields
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextArea bioField;
    @FXML private ComboBox<Country> countryCombo;

    // Password Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

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
    }

    private void loadUserProfileImage(String profilePath, String name) {
        if (profilePath != null && !profilePath.trim().isEmpty() && !"null".equalsIgnoreCase(profilePath.trim())) {
            try {
                String imageUrl;
                // If it's a local disk path, format as file URI; otherwise convert server filename to full URL
                if (profilePath.startsWith("file:") || profilePath.contains(":\\")) {
                    imageUrl = new File(profilePath.replace("file:", "")).toURI().toString();
                } else {
                    imageUrl = ApiClient.getProfileImageUrl(profilePath);
                }

                Image img = new Image(imageUrl, 100, 100, false, true, true);
                avatarPreview.setImage(img);
                avatarPreview.setVisible(true);
                avatarInitialLabel.setVisible(false);
                return;
            } catch (Exception e) {
                System.err.println("Failed to load avatar image: " + e.getMessage());
            }
        }
        showInitialAvatar(name);
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
                new Country("sg", "Singapore"),
                new Country("af", "Afghanistan"),
                new Country("al", "Albania"),
                new Country("dz", "Algeria"),
                new Country("ad", "Andorra"),
                new Country("ar", "Argentina"),
                new Country("au", "Australia"),
                new Country("at", "Austria"),
                new Country("bd", "Bangladesh"),
                new Country("be", "Belgium"),
                new Country("br", "Brazil"),
                new Country("ca", "Canada"),
                new Country("dk", "Denmark"),
                new Country("eg", "Egypt"),
                new Country("fr", "France"),
                new Country("de", "Germany"),
                new Country("hk", "Hong Kong"),
                new Country("id", "Indonesia"),
                new Country("it", "Italy"),
                new Country("my", "Malaysia"),
                new Country("np", "Nepal"),
                new Country("nl", "Netherlands"),
                new Country("nz", "New Zealand"),
                new Country("pk", "Pakistan"),
                new Country("ph", "Philippines"),
                new Country("ru", "Russia"),
                new Country("sa", "Saudi Arabia"),
                new Country("es", "Spain"),
                new Country("lk", "Sri Lanka"),
                new Country("se", "Sweden"),
                new Country("ch", "Switzerland"),
                new Country("tr", "Turkey"),
                new Country("ae", "United Arab Emirates"),
                new Country("vn", "Vietnam")
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

    // ============================================
    // INITIAL AVATAR
    // ============================================
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

    // ============================================
    // CHANGE IMAGE
    // ============================================
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

    // ============================================
    // DELETE IMAGE
    // ============================================
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

    // ============================================
    // SET IMAGE
    // ============================================
    @FXML
    private void handleSetImage() {
        if (!hasNewAvatar || tempAvatarPath == null) return;

        User user = UserSession.getCurrentUser();
        if (user == null) return;

        // Convert file: URI string to a valid Windows File object
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

        // Upload actual image file to PythonAnywhere
        ApiClient.uploadProfileImageAsync(user.getUserId(), imageFile)
                .thenAccept(responseJson -> {
                    javafx.application.Platform.runLater(() -> {
                        if (responseJson != null) {
                            try {
                                org.json.JSONObject json = new org.json.JSONObject(responseJson);
                                String savedFileName = json.optString("profile_pic", null);

                                if (savedFileName != null) {
                                    // Store server filename (e.g. user_3_a1b2c3d4.png), NOT local path
                                    user.setProfile(savedFileName);

                                    setBtn.setVisible(false);
                                    setBtn.setManaged(false);
                                    deleteBtn.setVisible(true);
                                    deleteBtn.setManaged(true);
                                    hasNewAvatar = false;

                                    showAlert(Alert.AlertType.INFORMATION, "Success", "✅ Profile picture uploaded successfully!");
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

    // ============================================
    // SAVE USER INFO
    // ============================================
    @FXML
    private void handleSaveInfo() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        String newName = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username and Email cannot be empty.");
            return;
        }

        boolean success = DatabaseManager.updateUserInfo(user.getUserId(), newName, newEmail);
        if (success) {
            user.setName(newName);
            user.setEmail(newEmail);
            showAlert(Alert.AlertType.INFORMATION, "Success", "✅ Profile information updated successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update information on server.");
        }
    }

    // ============================================
    // CHANGE PASSWORD
    // ============================================
    @FXML
    private void handleChangePassword() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        String currentPw = currentPasswordField != null ? currentPasswordField.getText() : "";
        String newPw = newPasswordField != null ? newPasswordField.getText() : "";
        String confirmPw = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all password fields.");
            return;
        }

        if (!newPw.equals(confirmPw)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New password and confirm password do not match.");
            return;
        }

        if (newPw.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Error", "New password must be at least 6 characters long.");
            return;
        }

        boolean success = DatabaseManager.changePassword(user.getUserId(), currentPw, newPw);
        if (success) {
            if (currentPasswordField != null) currentPasswordField.clear();
            if (newPasswordField != null) newPasswordField.clear();
            if (confirmPasswordField != null) confirmPasswordField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Success", "✅ Password changed successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to change password. Please verify your current password.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This action cannot be undone!");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Account deleted");
            }
        });
    }

    @FXML
    private void handleUpgradePlan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PremiumView.fxml"));
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
    // ============================================
// ✅ SHOW/HIDE — CURRENT PASSWORD
// ============================================
    @FXML
    private void toggleCurrentPassword() {
        if (currentPasswordVisible.isVisible()) {
            // Hide → PasswordField
            currentPasswordVisible.setVisible(false);
            currentPasswordField.setVisible(true);
            currentPasswordField.setText(currentPasswordVisible.getText());
            currentShowBtn.setText("Show");
        } else {
            // Show → TextField
            currentPasswordVisible.setText(currentPasswordField.getText());
            currentPasswordVisible.setVisible(true);
            currentPasswordField.setVisible(false);
            currentShowBtn.setText("Hide");
        }
    }

    // ============================================
// ✅ SHOW/HIDE — NEW PASSWORD
// ============================================
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
}