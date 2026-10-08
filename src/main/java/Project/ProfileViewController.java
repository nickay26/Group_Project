package Project;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

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
    @FXML private Label premiumBadgeLabel;   // ✅ Label

    @FXML
    public void initialize() {
        Circle clip = new Circle(60, 60, 60);
        avatarImageView.setClip(clip);

        loadUserData();
        loadCoursesProgress();
    }

    // ============================================
    // LOAD USER DATA
    // ============================================
    private void loadUserData() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            String planType = currentUser.getSubscriptionType();
            boolean isPremium = "PREMIUM".equalsIgnoreCase(planType);

            userNameLabel.setText(currentUser.getName());

            // ✅ Premium Badge — Label
            if (isPremium) {
                premiumBadgeLabel.setVisible(true);
                premiumBadgeLabel.setManaged(true);
                startPremiumAnimation();
            } else {
                premiumBadgeLabel.setVisible(false);
                premiumBadgeLabel.setManaged(false);
            }

            userEmailLabel.setText(currentUser.getEmail());
            streakLabel.setText(String.valueOf(currentUser.getStreak()));
            xpLabel.setText(String.valueOf(currentUser.getXp()));
            coinLabel.setText(String.valueOf(currentUser.getCoin()));

            if (isPremium) {
                brainLabel.setText("∞");
            } else {
                brainLabel.setText(String.valueOf(currentUser.getBrainCount()));
            }

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
    // ✅ Premium Badge — Animation
    // ============================================
    private void startPremiumAnimation() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#FBBF24"));
        glow.setRadius(6);
        glow.setSpread(0.2);
        premiumBadgeLabel.setEffect(glow);

        Timeline glowTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 3)),
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(glow.radiusProperty(), 10)),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(glow.radiusProperty(), 3))
        );
        glowTimeline.setCycleCount(Animation.INDEFINITE);
        glowTimeline.play();

        Timeline scaleTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(premiumBadgeLabel.scaleXProperty(), 1.0),
                        new KeyValue(premiumBadgeLabel.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(premiumBadgeLabel.scaleXProperty(), 1.08),
                        new KeyValue(premiumBadgeLabel.scaleYProperty(), 1.08)),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(premiumBadgeLabel.scaleXProperty(), 1.0),
                        new KeyValue(premiumBadgeLabel.scaleYProperty(), 1.0))
        );
        scaleTimeline.setCycleCount(Animation.INDEFINITE);
        scaleTimeline.play();
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

            StackPane item = createCourseItem(
                    course.getCourseId(),
                    course.getTitle(),
                    course.getLanguage(),
                    getCourseColor(course.getLanguage()),
                    percent
            );

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
    private StackPane createCourseItem(String courseId, String title, String language, String color, int progressPercent) {
        String cardFile = getCardImageName(courseId);

        ImageView cardImage = new ImageView();
        try {
            InputStream is = getClass().getResourceAsStream("/Images/" + cardFile);
            if (is != null) {
                Image img = new Image(is, 800, 260, true, true);
                cardImage.setImage(img);
                cardImage.setFitWidth(400);
                cardImage.setFitHeight(130);
                cardImage.setPreserveRatio(false);
                cardImage.setSmooth(true);
            }
        } catch (Exception e) {
            System.out.println("Card not found: " + cardFile);
        }

        StackPane progressCircle = createCircularProgressForCard(progressPercent, courseId);

        StackPane.setAlignment(progressCircle, Pos.CENTER_RIGHT);
        progressCircle.setTranslateX(-20);
        progressCircle.setTranslateY(-5);
        progressCircle.setMouseTransparent(true);

        StackPane cardContainer = new StackPane();
        cardContainer.setPrefSize(400, 130);
        cardContainer.setMinSize(400, 130);
        cardContainer.setMaxSize(400, 130);
        cardContainer.getChildren().addAll(cardImage, progressCircle);

        return cardContainer;
    }

    private String getCardImageName(String courseId) {
        if (courseId == null) return "default.png";
        String upper = courseId.toUpperCase();

        if (upper.contains("CPP") || upper.contains("C++")) {
            return upper.contains("INTERMEDIATE") ? "cpp_intermediate.png" : "cpp_basic.png";
        }
        if (upper.contains("JAVA")) {
            return upper.contains("INTERMEDIATE") ? "java_intermediate.png" : "java_basic.png";
        }
        if (upper.contains("PYTHON") || upper.contains("PY")) {
            return upper.contains("INTERMEDIATE") ? "python_intermediate.png" : "python_basic.png";
        }
        if (upper.contains("SQL")) {
            return "sql_basic.png";
        }
        return "default.png";
    }

    private StackPane createCircularProgressForCard(int percent, String courseId) {
        double radius = 22;
        double strokeWidth = 5;

        String darkerColor = getCardDarkerColor(courseId);
        String lighterColor = getCardLighterColor(courseId);

        Circle bgCircle = new Circle(radius);
        bgCircle.setFill(Color.web(darkerColor, 0.9));
        bgCircle.setStroke(Color.web(lighterColor, 1.0));
        bgCircle.setStrokeWidth(strokeWidth);

        double arcLength = -percent * 3.6;
        Arc progressArc = new Arc(0, 0, radius, radius, 90, arcLength);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(Color.TRANSPARENT);
        progressArc.setStroke(Color.WHITE);
        progressArc.setStrokeWidth(strokeWidth);
        progressArc.setStrokeLineCap(StrokeLineCap.ROUND);

        Label percentLabel = new Label(percent + "%");
        percentLabel.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
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

    private String getCardDarkerColor(String courseId) {
        if (courseId == null) return "#334155";
        String upper = courseId.toUpperCase();

        if (upper.contains("CPP") || upper.contains("C++")) return "#1E3A8A";
        if (upper.contains("JAVA")) return "#7C2D12";
        if (upper.contains("PYTHON") || upper.contains("PY")) return "#064E3B";
        if (upper.contains("SQL")) return "#14532D";

        return "#334155";
    }

    private String getCardLighterColor(String courseId) {
        if (courseId == null) return "#64748B";
        String upper = courseId.toUpperCase();

        if (upper.contains("CPP") || upper.contains("C++")) return "#3B82F6";
        if (upper.contains("JAVA")) return "#EA580C";
        if (upper.contains("PYTHON") || upper.contains("PY")) return "#10B981";
        if (upper.contains("SQL")) return "#22C55E";

        return "#64748B";
    }
}