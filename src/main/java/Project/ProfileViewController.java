package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;

public class ProfileViewController {

    @FXML private ImageView avatarImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label streakLabel;
    @FXML private Label xpLabel;
    @FXML private Label coinLabel;
    @FXML private Label brainLabel;
    @FXML private VBox coursesProgressContainer;

    @FXML
    public void initialize() {
        Circle clip = new Circle(60, 60, 60);
        avatarImageView.setClip(clip);

        // Avatar ကို Hover လုပ်လျှင် Hand Cursor ဖြစ်စေရန်
        avatarImageView.setCursor(Cursor.HAND);

        // Avatar ပုံကို နှိပ်ပါက ပုံသစ် Upload လုပ်မည့် Event
        avatarImageView.setOnMouseClicked(event -> handleUploadAvatar());

        loadUserData();
        loadCoursesProgress();
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

            // Profile photo URL/Filename မှ ပုံဆွဲတင်ခြင်း
            if (currentUser.getProfile() != null && !currentUser.getProfile().trim().isEmpty()) {
                String fullImageUrl = ApiClient.getProfileImageUrl(currentUser.getProfile());
                try {
                    Image img = new Image(fullImageUrl, true); // background loading
                    avatarImageView.setImage(img);
                } catch (Exception e) {
                    System.out.println("Could not load avatar: " + e.getMessage());
                }
            }

            Circle clip = new Circle(60, 60, 60);
            avatarImageView.setClip(clip);
        }
    }

    // ============================================
    // AVATAR CLICK & UPLOAD LOGIC
    // ============================================
    private void handleUploadAvatar() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) avatarImageView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // ၁။ Client UI တွင် Local Image Preview ကို ချက်ချင်းပြသမည်
            try {
                Image localImg = new Image(selectedFile.toURI().toString());
                avatarImageView.setImage(localImg);
            } catch (Exception e) {
                System.err.println("Preview Image Error: " + e.getMessage());
            }

            // ၂။ Server သို့ Async Upload တင်မည်
            ApiClient.uploadProfileImageAsync(currentUser.getUserId(), selectedFile)
                    .thenAccept(response -> {
                        if (response != null) {
                            try {
                                JSONObject json = new JSONObject(response);
                                String savedFilename = null;

                                if (json.has("profile_pic")) {
                                    savedFilename = json.getString("profile_pic");
                                } else if (json.has("filename")) {
                                    savedFilename = json.getString("filename");
                                } else if (json.has("url")) {
                                    savedFilename = json.getString("url");
                                }

                                if (savedFilename != null) {
                                    String finalFilename = savedFilename;
                                    currentUser.setProfile(finalFilename);

                                    // JavaFX UI Thread ပေါ်တွင် Server URL ဖြင့် အတည်ပြုပြသမည်
                                    Platform.runLater(() -> {
                                        String fullUrl = ApiClient.getProfileImageUrl(finalFilename);
                                        if (fullUrl != null) {
                                            avatarImageView.setImage(new Image(fullUrl, true));
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                System.err.println("Parse Upload Response Error: " + e.getMessage());
                            }
                        }
                    });
        }
    }

    // ============================================
    // LOAD COURSES PROGRESS
    // ============================================
    private void loadCoursesProgress() {
        coursesProgressContainer.getChildren().clear();

        String[][] courses = {
                {"C++",    "Introduction to C++",    "C++",  "#3B82F6", "3"},
                {"PYTHON", "Introduction to Python", "PYTHON",   "#3B82F6", "3"},
                {"SQL",    "Introduction to SQL",    "SQL",  "#22C55E", "0"}
        };

        for (String[] c : courses) {
            HBox item = createCourseItem(c[0], c[1], c[2], c[3], Integer.parseInt(c[4]));
            coursesProgressContainer.getChildren().add(item);
        }
    }

    // ============================================
    // CREATE COURSE ITEM
    // ============================================
    private HBox createCourseItem(String courseId, String title, String icon, String color, int progressPercent) {
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(60, 60);
        iconCircle.setMinSize(60, 60);
        iconCircle.setMaxSize(60, 60);
        iconCircle.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-background-radius: 30px;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        iconCircle.getChildren().add(iconLabel);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Label statusLabel = new Label("In Progress");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        VBox textBox = new VBox(4, titleLabel, statusLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        StackPane progressCircle = createCircularProgress(progressPercent);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox item = new HBox(15, iconCircle, textBox, spacer, progressCircle);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-padding: 15; " +
                        "-fx-cursor: hand;"
        );

        item.setOnMouseClicked(e -> goToLearnPage(courseId));

        return item;
    }

    private void goToLearnPage(String courseId) {
        UserSession.setActiveCourseId(courseId);

        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadPage("LearnPage.fxml");
        }
    }

    private StackPane createCircularProgress(int percent) {
        double radius = 22;
        double strokeWidth = 5;

        Circle bgCircle = new Circle(radius);
        bgCircle.setFill(Color.TRANSPARENT);
        bgCircle.setStroke(Color.web("#E2E8F0"));
        bgCircle.setStrokeWidth(strokeWidth);

        Arc progressArc = new Arc(0, 0, radius, radius, 90, -percent * 3.6);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(Color.TRANSPARENT);
        progressArc.setStroke(Color.web("#3B82F6"));
        progressArc.setStrokeWidth(strokeWidth);
        progressArc.setStrokeLineCap(StrokeLineCap.ROUND);

        Label percentLabel = new Label(percent + "%");
        percentLabel.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; " +
                        "-fx-text-fill: #3B82F6;"
        );

        StackPane stack = new StackPane();
        stack.setPrefSize(radius * 2 + strokeWidth, radius * 2 + strokeWidth);
        stack.setMinSize(radius * 2 + strokeWidth, radius * 2 + strokeWidth);
        stack.setMaxSize(radius * 2 + strokeWidth, radius * 2 + strokeWidth);

        Group group = new Group(bgCircle, progressArc);
        stack.getChildren().addAll(group, percentLabel);
        stack.setAlignment(Pos.CENTER);

        return stack;
    }
}