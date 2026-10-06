package Project;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;

import java.io.InputStream;
import java.util.List;

public class ProfileViewController {

    @FXML private ImageView avatarImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label streakLabel;
    @FXML private Label xpLabel;
    @FXML private Label coinLabel;
    @FXML private Label brainLabel;
    @FXML private GridPane coursesProgressContainer;

    @FXML
    public void initialize() {
        Circle clip = new Circle(60, 60, 60);
        avatarImageView.setClip(clip);

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

            // ★★★ ဒီနေရာမှာ Premium Check ထည့်ပါ ★★★
            String planType = currentUser.getPlanType();
            boolean isPremium = "PREMIUM".equals(planType);

            if (isPremium) {
                brainLabel.setText("∞");   // Premium → Unlimited
            } else {
                brainLabel.setText(String.valueOf(currentUser.getBrainCount()));  // Free → 5
            }
            // ★★★ ဒီအထိပဲ ★★★

            if (currentUser.getProfile() != null && !currentUser.getProfile().isEmpty()) {
                try {
                    Image img = new Image(currentUser.getProfile(), 0, 0, true, true, true);
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
    // LOAD COURSES PROGRESS
    // ============================================
    private void loadCoursesProgress() {
        coursesProgressContainer.getChildren().clear();

        int userId = UserSession.getUserId();
        List<Course> courseList = DatabaseManager.getCoursesForUser(userId);

        if (courseList == null || courseList.isEmpty()) {
            Label empty = new Label("No courses available.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-padding: 20;");
            coursesProgressContainer.add(empty, 0, 0);
            return;
        }

        int col = 0;
        int row = 0;

        for (Course course : courseList) {
            int percent = course.getProgressPercent();

            System.out.println("📊 " + course.getTitle()
                    + " | Completed: " + course.getCompletedLessons()
                    + " | Total: " + course.getTotalLessons()
                    + " | Percent: " + percent + "%");

            HBox item = createCourseItem(
                    course.getCourseId(),
                    course.getTitle(),
                    course.getLanguage(),
                    getCourseColor(course.getLanguage()),
                    percent
            );

            // ✅ 2 columns — 2 ခု တစ်တန်း
            coursesProgressContainer.add(item, col, row);

            col++;
            if (col == 2) {
                col = 0;
                row++;
            }
        }
    }

    private String getCourseColor(String language) {
        if (language == null) return "#64748B";
        String lang = language.toLowerCase();

        if (lang.contains("cpp") || lang.contains("c++")) return "#3B82F6";
        if (lang.contains("python") || lang.contains("py")) return "#3B82F6";
        if (lang.contains("sql")) return "#22C55E";
        if (lang.contains("java")) return "#F59E0B";

        return "#64748B";
    }

    // ============================================
    // CREATE COURSE ITEM
    // ============================================
    private HBox createCourseItem(String courseId, String title, String icon, String color, int progressPercent) {
        Node iconNode;
        boolean isPython = courseId != null && courseId.toUpperCase().contains("PYTHON");

        if (isPython) {
            // ✅ Python — py.png ပုံ
            ImageView iv = new ImageView();
            try {
                InputStream is = getClass().getResourceAsStream("/Images/py.png");
                if (is != null) {
                    Image img = new Image(is, 180, 180, true, true);
                    iv.setImage(img);
                    iv.setFitWidth(58);
                    iv.setFitHeight(58);
                    iv.setPreserveRatio(true);
                    iv.setSmooth(true);
                    iv.setCache(true);
                }
            } catch (Exception e) {
                System.out.println("py.png not found: " + e.getMessage());
            }

            StackPane iconWrapper = new StackPane(iv);
            iconWrapper.setPrefSize(60, 60);
            iconWrapper.setMinSize(60, 60);
            iconWrapper.setMaxSize(60, 60);
            iconWrapper.setAlignment(Pos.CENTER);

            iconNode = iconWrapper;

        } else {
            // ✅ C++, Java, SQL — စာလုံး (အဝိုင်း background)
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

            iconNode = iconCircle;
        }

        // Title + Status
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Label statusLabel = new Label(progressPercent >= 100 ? "Completed" : "In Progress");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        VBox textBox = new VBox(4, titleLabel, statusLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        StackPane progressCircle = createCircularProgress(progressPercent);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox item = new HBox(15, iconNode, textBox, spacer, progressCircle);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPrefWidth(400);              // ✅ width သတ်မှတ် (2 columns)
        item.setMaxWidth(400);
        item.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-padding: 15;"
        );

        return item;
    }

    // ============================================
    // ✅ CIRCULAR PROGRESS
    // ============================================
    private StackPane createCircularProgress(int percent) {
        double radius = 22;
        double strokeWidth = 5;

        Circle bgCircle = new Circle(radius);
        bgCircle.setFill(Color.TRANSPARENT);
        bgCircle.setStroke(Color.web("#E2E8F0"));
        bgCircle.setStrokeWidth(strokeWidth);

        double arcLength = -percent * 3.6;
        Arc progressArc = new Arc(0, 0, radius, radius, 90, arcLength);
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