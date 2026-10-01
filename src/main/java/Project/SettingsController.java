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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

    // Info Fields
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextArea bioField;
    @FXML private ComboBox<Country> countryCombo;   // ✅ Country Type

    // Avatar Components
    @FXML private StackPane avatarContainer;
    @FXML private ImageView avatarPreview;
    @FXML private Label avatarInitialLabel;
    @FXML private Button changeBtn;
    @FXML private Button deleteBtn;
    @FXML private Button setBtn;

    // Temp
    private String tempAvatarPath = null;
    private boolean hasNewAvatar = false;

    // ============================================
    // INITIALIZE
    // ============================================
    @FXML
    public void initialize() {
        showInfoSection();

        // ✅ Avatar Container Clip (အဝိုင်း)
        Circle containerClip = new Circle(50, 50, 50);
        avatarContainer.setClip(containerClip);

        // ✅ Country Setup
        setupCountryCombo();

        // User Data
        User user = UserSession.getCurrentUser();
        if (user != null) {
            usernameField.setText(user.getName());
            emailField.setText(user.getEmail());

            if (user.getProfile() != null && !user.getProfile().isEmpty()) {
                try {
                    Image img = new Image(user.getProfile(), 100, 100, false, true, true);
                    avatarPreview.setImage(img);
                    avatarPreview.setVisible(true);
                    avatarInitialLabel.setVisible(false);
                } catch (Exception e) {
                    showInitialAvatar(user.getName());
                }
            } else {
                showInitialAvatar(user.getName());
            }
        }

        // ✅ Default: Change + Delete ပြ၊ Set ဖျောက်
        deleteBtn.setVisible(true);
        deleteBtn.setManaged(true);
        setBtn.setVisible(false);
        setBtn.setManaged(false);
    }

    // ============================================
    // ✅ COUNTRY COMBO — PNG Flag + Name
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

        // ✅ Custom CellFactory
        countryCombo.setCellFactory(lv -> new CountryCell());
        countryCombo.setButtonCell(new CountryCell());
        countryCombo.setVisibleRowCount(8);

        countryCombo.setPrefWidth(400);
        countryCombo.setMaxWidth(Double.MAX_VALUE);
    }

    // ============================================
    // COUNTRY MODEL
    // ============================================
    public static class Country {
        private final String code;   // "mm", "us"
        private final String name;   // "Myanmar"

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

    // ============================================
    // COUNTRY CELL — PNG ImageView
    // ============================================
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
                // ✅ Local PNG load: /flags/mm.png
                String path = "/flags/" + country.getCode() + ".png";
                try {
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is != null) {
                        Image img = new Image(is, 48, 36, true, true);
                        flagView.setImage(img);
                    } else {
                        flagView.setImage(null);
                        System.out.println("⚠️ Flag not found: " + path);
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
    // ✅ CHANGE
    // ============================================
    @FXML
    private void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files",
                        "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(
                changeBtn.getScene().getWindow());

        if (selectedFile != null) {
            String croppedPath = AvatarCropDialog.showCropDialog(selectedFile);
            if (croppedPath != null) {
                tempAvatarPath = croppedPath;
                hasNewAvatar = true;

                Image img = new Image(croppedPath, 100, 100, false, true, true);
                avatarPreview.setImage(img);
                avatarPreview.setVisible(true);
                avatarInitialLabel.setVisible(false);

                // ✅ Image ပြောင်းတိုင်း Clip ပြန်
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
    // ✅ DELETE
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
        DatabaseManager.updateUserProfile(user.getUserId(), user.getName(), null);
    }

    // ============================================
    // ✅ SET
    // ============================================
    @FXML
    private void handleSetImage() {
        if (!hasNewAvatar || tempAvatarPath == null) return;

        User user = UserSession.getCurrentUser();
        if (user == null) return;

        user.setProfile(tempAvatarPath);
        DatabaseManager.updateUserProfile(user.getUserId(), user.getName(), tempAvatarPath);

        deleteBtn.setVisible(true);
        deleteBtn.setManaged(true);
        setBtn.setVisible(false);
        setBtn.setManaged(false);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("✅ Profile picture updated!");
        alert.showAndWait();

        hasNewAvatar = false;
    }

    // ============================================
    // TABS
    // ============================================
    @FXML
    private void showInfoSection() {
        hideAll();
        infoSection.setVisible(true);
        infoSection.setManaged(true);
        setActiveTab(infoTabBtn);
    }

    @FXML
    private void showPasswordSection() {
        hideAll();
        passwordSection.setVisible(true);
        passwordSection.setManaged(true);
        setActiveTab(passwordTabBtn);
    }

    @FXML
    private void showSubscriptionSection() {
        hideAll();
        subscriptionSection.setVisible(true);
        subscriptionSection.setManaged(true);
        setActiveTab(subscriptionTabBtn);
    }

    @FXML
    private void showDeleteSection() {
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
        if (user != null) {
            String newName = usernameField.getText().trim();
            if (!newName.isEmpty()) {
                user.setName(newName);
                DatabaseManager.updateUserProfile(user.getUserId(), newName, user.getProfile());
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("✅ Information saved!");
        alert.showAndWait();
    }

    @FXML
    private void handleChangePassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password");
        alert.setHeaderText(null);
        alert.setContentText("✅ Password changed!");
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
    @FXML private StackPane centerContainer;
    @FXML private ScrollPane settingsScrollPane;

    @FXML
    private void handleUpgradePlan() {
        try {
            // Settings content ကို ဖျောက်
            settingsScrollPane.setVisible(false);
            settingsScrollPane.setManaged(false);

            // PremiumView ကို load
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PremiumView.fxml"));
            Parent premiumView = loader.load();
            StackPane.setAlignment(premiumView, javafx.geometry.Pos.TOP_LEFT);
            // Center ထဲ ထည့်
            centerContainer.getChildren().add(premiumView);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleClose() {
        infoSection.getScene().getWindow().hide();
    }
}