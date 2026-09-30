package Project;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;

public class ProfileViewController {

    @FXML private ImageView avatarImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label streakLabel;
    @FXML private Label xpLabel;
    @FXML private Label coinLabel;
    @FXML private Label brainLabel;
    @FXML private Button editProfileBtn;

    // ============================================
    // INITIALIZE
    // ============================================
    @FXML
    public void initialize() {
        // Profile Avatar — အဝိုင်း
        Circle clip = new Circle(40, 40, 40);
        avatarImageView.setClip(clip);

        loadUserData();
    }

    // ============================================
    // LOAD USER DATA
    // ============================================
    private void loadUserData() {
        User currentUser = UserSession.getCurrentUser();

        if (currentUser != null) {
            userNameLabel.setText(currentUser.getName());
            userEmailLabel.setText(currentUser.getEmail());
            streakLabel.setText(String.valueOf(currentUser.getStreak()));
            xpLabel.setText(String.valueOf(currentUser.getXp()));
            coinLabel.setText(String.valueOf(currentUser.getCoin()));
            brainLabel.setText(String.valueOf(currentUser.getBrainCount()));

            if (currentUser.getProfile() != null && !currentUser.getProfile().isEmpty()) {
                try {
                    // ✅ Source Size အတိုင်း Load — မချုံ့
                    Image img = new Image(currentUser.getProfile(),
                            0, 0, true, true, true);
                    avatarImageView.setImage(img);
                } catch (Exception e) {
                    System.out.println("Could not load avatar: " + e.getMessage());
                }
            }

            // ✅ အဝိုင်း 120×120
            Circle clip = new Circle(60, 60, 60);
            avatarImageView.setClip(clip);
        }
    }

    // ============================================
    // EDIT PROFILE
    // ============================================
    @FXML
    private void handleEditProfile() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your profile information");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Name Field
        TextField nameField = new TextField(currentUser.getName());
        nameField.setPrefWidth(250);

        // Avatar Preview — အဝိုင်း
        ImageView previewAvatar = new ImageView();
        previewAvatar.setFitWidth(100);
        previewAvatar.setFitHeight(100);
        previewAvatar.setPreserveRatio(false);

        Circle previewClip = new Circle(50, 50, 50);
        previewAvatar.setClip(previewClip);

        if (currentUser.getProfile() != null && !currentUser.getProfile().isEmpty()) {
            try {
                Image img = new Image(currentUser.getProfile(), 100, 100, false, true);
                previewAvatar.setImage(img);
            } catch (Exception e) {
                System.out.println("Could not load avatar: " + e.getMessage());
            }
        }

        final String[] selectedImagePath = {currentUser.getProfile()};

        // Choose Image Button
        Button chooseImageBtn = new Button("📷 Choose Image");
        chooseImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Profile Picture");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files",
                            "*.png", "*.jpg", "*.jpeg", "*.gif"));

            File selectedFile = fileChooser.showOpenDialog(
                    editProfileBtn.getScene().getWindow());

            if (selectedFile != null) {
                String croppedPath = showCropDialog(selectedFile);
                if (croppedPath != null) {
                    selectedImagePath[0] = croppedPath;
                    previewAvatar.setImage(new Image(croppedPath, 100, 100, false, true));
                    previewAvatar.setClip(previewClip);
                }
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Avatar:"), 0, 1);
        grid.add(previewAvatar, 1, 1);
        grid.add(chooseImageBtn, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                String newName = nameField.getText().trim();
                if (!newName.isEmpty()) {
                    currentUser.setName(newName);
                }
                if (selectedImagePath[0] != null) {
                    currentUser.setProfile(selectedImagePath[0]);
                }

                loadUserData();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("✅ Profile updated!");
                alert.showAndWait();
            }
        });
    }

    // ============================================
    // CROP DIALOG — အဝိုင်း (Circle)
    // ============================================
    private String showCropDialog(File originalFile) {
        try {
            Image originalImage = new Image(originalFile.toURI().toString());

            Dialog<ButtonType> cropDialog = new Dialog<>();
            cropDialog.setTitle("Crop Image");
            cropDialog.setHeaderText("Drag to move • Scroll to resize");

            ButtonType cropButtonType = new ButtonType("Crop", ButtonBar.ButtonData.OK_DONE);
            cropDialog.getDialogPane().getButtonTypes().addAll(cropButtonType, ButtonType.CANCEL);

            // ✅ Image Size — တကယ့် အရွယ်
            double maxSize = 400;
            double origW = originalImage.getWidth();
            double origH = originalImage.getHeight();

            double scale = Math.min(maxSize / origW, maxSize / origH);
            double imgW = origW * scale;
            double imgH = origH * scale;

            ImageView cropPreview = new ImageView(originalImage);
            cropPreview.setPreserveRatio(true);
            cropPreview.setFitWidth(imgW);
            cropPreview.setFitHeight(imgH);

            // ✅ Circle Crop (အဝိုင်း)
            Circle cropCircle = new Circle(Math.min(imgW, imgH) / 3);
            cropCircle.setCenterX(imgW / 2);
            cropCircle.setCenterY(imgH / 2);
            cropCircle.setFill(Color.rgb(56, 189, 248, 0.15));
            cropCircle.setStroke(Color.web("#38bdf8"));
            cropCircle.setStrokeWidth(2);
            cropCircle.setCursor(Cursor.MOVE);

            // ✅ Pane — Image Size အတိုင်း
            Pane cropPane = new Pane();
            cropPane.setPrefSize(imgW, imgH);
            cropPane.setMinSize(imgW, imgH);
            cropPane.setMaxSize(imgW, imgH);
            cropPane.getChildren().addAll(cropPreview, cropCircle);
            cropPane.setStyle("-fx-background-color: black;");

            cropCircle.toFront();

            // ✅ Circle ရွှေ့
            final double[] dragDelta = new double[2];
            cropCircle.setOnMousePressed(e -> {
                dragDelta[0] = cropCircle.getCenterX() - e.getSceneX();
                dragDelta[1] = cropCircle.getCenterY() - e.getSceneY();
            });
            cropCircle.setOnMouseDragged(e -> {
                double newX = e.getSceneX() + dragDelta[0];
                double newY = e.getSceneY() + dragDelta[1];

                newX = Math.max(cropCircle.getRadius(),
                        Math.min(newX, imgW - cropCircle.getRadius()));
                newY = Math.max(cropCircle.getRadius(),
                        Math.min(newY, imgH - cropCircle.getRadius()));

                cropCircle.setCenterX(newX);
                cropCircle.setCenterY(newY);
            });

            // ✅ Scroll — Circle Size ချိန်
            cropPane.setOnScroll(e -> {
                double delta = e.getDeltaY() / 100.0;
                double newRadius = cropCircle.getRadius() + delta * 10;
                newRadius = Math.max(30, Math.min(newRadius, Math.min(imgW, imgH) / 2));
                cropCircle.setRadius(newRadius);
            });

            VBox root = new VBox(15);
            root.setPadding(new Insets(15));
            root.setAlignment(Pos.CENTER);
            root.getChildren().add(cropPane);

            Label hint = new Label("🖱️ Drag to move • Scroll to resize");
            hint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            root.getChildren().add(hint);

            cropDialog.getDialogPane().setContent(root);

            // ✅ Crop နှိပ်ရင်
            final String[] resultPath = {null};
            cropDialog.showAndWait().ifPresent(response -> {
                if (response == cropButtonType) {
                    try {
                        SnapshotParameters params = new SnapshotParameters();
                        params.setViewport(new Rectangle2D(
                                cropCircle.getCenterX() - cropCircle.getRadius(),
                                cropCircle.getCenterY() - cropCircle.getRadius(),
                                cropCircle.getRadius() * 2,
                                cropCircle.getRadius() * 2));

                        WritableImage croppedImage = cropPreview.snapshot(params, null);

                        File tempFile = File.createTempFile("avatar_", ".png");
                        tempFile.deleteOnExit();

                        ImageIO.write(
                                SwingFXUtils.fromFXImage(croppedImage, null),
                                "png", tempFile);

                        resultPath[0] = tempFile.toURI().toString();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            return resultPath[0];

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}