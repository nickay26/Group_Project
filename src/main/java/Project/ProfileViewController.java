package Project;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.geometry.Insets;
import java.io.InputStream;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
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
    @FXML private GridPane certificatesContainer;
    @FXML
    public void initialize() {
        Circle clip = new Circle(60, 60, 60);
        avatarImageView.setClip(clip);

        loadUserData();
        loadCoursesProgress();
        loadCertificates();
    }

    // ============================================
    // LOAD USER DATA
    // ============================================
    private void loadUserData() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getName() != null ? currentUser.getName() : "User");
            userEmailLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            String planType = currentUser.getSubscriptionType();
            boolean isPremium = "PREMIUM".equalsIgnoreCase(planType) || currentUser.isPremium();

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
            // ★★★ ဒီအထိပဲ ★★★

            // ✅ FIXED: Format server filename into full HTTP URL
            String profilePath = currentUser.getProfile();
            if (profilePath != null && !profilePath.trim().isEmpty() && !"null".equalsIgnoreCase(profilePath.trim())) {
                try {
                    String fullImageUrl = ApiClient.getProfileImageUrl(profilePath);
                    if (fullImageUrl != null) {
                        Image img = new Image(fullImageUrl, true); // Async loading
                        avatarImageView.setImage(img);
                    }
                } catch (Exception e) {
                    System.err.println("Could not load avatar: " + e.getMessage());
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
// ✅ LOAD CERTIFICATES
// ============================================
    private void loadCertificates() {
        certificatesContainer.getChildren().clear();

        int userId = UserSession.getUserId();

        // ✅ Database ကနေ certificate list ယူ
        // (သင့် DatabaseManager မှာ method ရှိရင် ခေါ်)
        List<Course> certificateList = DatabaseManager.getCompletedCourses(userId);

        // ✅ Certificate မရှိရင် — Empty State
        if (certificateList == null || certificateList.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPrefWidth(830);
            emptyBox.setPrefHeight(150);
            emptyBox.setStyle(
                    "-fx-background-color: #1E293B; " +
                            "-fx-background-radius: 12px; " +
                            "-fx-border-color: #334155; " +
                            "-fx-border-width: 1.5px; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-border-style: dashed;"
            );

            Label icon = new Label("🏆");
            icon.setStyle("-fx-font-size: 40px;");

            Label msg = new Label("No certificates yet");
            msg.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label sub = new Label("Complete a course to earn your certificate!");
            sub.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");

            emptyBox.getChildren().addAll(icon, msg, sub);
            certificatesContainer.add(emptyBox, 0, 0);
            return;
        }

        // ✅ Certificate ရှိရင် — Card တွေ
        int col = 0;
        int row = 0;

        for (Course course : certificateList) {
            StackPane certCard = createCertificateCard(course);
            certificatesContainer.add(certCard, col, row);

            col++;
            if (col == 2) {
                col = 0;
                row++;
            }
        }
    }

    // ============================================
// ✅ CREATE CERTIFICATE CARD
// ============================================
    private StackPane createCertificateCard(Course course) {
        // ✅ Card Container
        VBox card = new VBox(0);
        card.setPrefSize(400, 200);
        card.setMinSize(400, 200);
        card.setMaxSize(400, 200);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0F172A, #1E3A8A); " +
                        "-fx-background-radius: 12px; " +
                        "-fx-border-color: #3B82F6; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-border-radius: 12px; " +
                        "-fx-padding: 20;"
        );

        // ✅ Top Row — Icon + Title
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // ✅ Icon
        ImageView iconView = new ImageView();
        try {
            String iconFile = getCardIconName(course.getCourseId());
            InputStream is = getClass().getResourceAsStream("/Images/" + iconFile);
            if (is != null) {
                Image icon = new Image(is, 60, 60, true, true);
                iconView.setImage(icon);
                iconView.setFitWidth(60);
                iconView.setFitHeight(60);
                iconView.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.out.println("Icon not found");
        }

        // ✅ Title + Subtitle
        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(course.getTitle());
        titleLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label subTitle = new Label("Certificate of Completion");
        subTitle.setStyle("-fx-text-fill: #60A5FA; -fx-font-size: 11px;");

        titleBox.getChildren().addAll(titleLabel, subTitle);

        topRow.getChildren().addAll(iconView, titleBox);

        // ✅ Description
        Label descLabel = new Label(course.getDescription());
        descLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(340);

        // ✅ View Certificate Button
        Button viewBtn = new Button("📜  View Certificate  →");
        viewBtn.setStyle(
                "-fx-background-color: #1E40AF; " +
                        "-fx-text-fill: #F8FAFC; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-border-color: #3B82F6; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-padding: 10 25; " +
                        "-fx-cursor: hand;"
        );
        viewBtn.setMaxWidth(Double.MAX_VALUE);

        // ✅ Button Hover
        viewBtn.setOnMouseEntered(e -> viewBtn.setStyle(
                "-fx-background-color: #2563EB; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-border-color: #60A5FA; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-padding: 10 25; " +
                        "-fx-cursor: hand;"
        ));
        viewBtn.setOnMouseExited(e -> viewBtn.setStyle(
                "-fx-background-color: #1E40AF; " +
                        "-fx-text-fill: #F8FAFC; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-border-color: #3B82F6; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-padding: 10 25; " +
                        "-fx-cursor: hand;"
        ));

        // ✅ Button နှိပ် → Certificate
        viewBtn.setOnAction(e -> showCertificate(course));

        // ✅ Card ထဲ ထည့်
        VBox.setMargin(topRow, new Insets(0, 0, 10, 0));
        VBox.setMargin(descLabel, new Insets(0, 0, 15, 0));

        card.getChildren().addAll(topRow, descLabel, viewBtn);

        StackPane wrapper = new StackPane(card);
        wrapper.setPrefSize(400, 200);
        wrapper.setMinSize(400, 200);
        wrapper.setMaxSize(400, 200);

        return wrapper;
    }

    // ============================================
// ✅ Card Icon — Course အလိုက်
// ============================================
    private String getCardIconName(String courseId) {
        if (courseId == null) return "default.png";
        String upper = courseId.toUpperCase();

        if (upper.contains("CPP") || upper.contains("C++")) return "c++.png";
        if (upper.contains("JAVA")) return "java.png";
        if (upper.contains("PYTHON") || upper.contains("PY")) return "py.png";
        if (upper.contains("SQL")) return "sql.png";

        return "default.png";
    }

    // ============================================
// ✅ SHOW CERTIFICATE — Popup
// ============================================
    private void showCertificate(Course course) {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Certificate");
        dialog.setHeaderText(null);

        VBox certBox = new VBox(15);
        certBox.setAlignment(Pos.CENTER);
        certBox.setPadding(new Insets(40));
        certBox.setPrefWidth(500);
        certBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0F172A, #1E293B); " +
                        "-fx-border-color: #F59E0B; " +
                        "-fx-border-width: 3px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-border-radius: 15px;"
        );

        Label certHeader = new Label("COURSE CERTIFICATE");
        certHeader.setStyle("-fx-text-fill: #FBBF24; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label thisIsToCertify = new Label("This is to certify that");
        thisIsToCertify.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");

        Label nameLabel = new Label(user.getName());
        nameLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label hasCompleted = new Label("has successfully completed the course");
        hasCompleted.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");

        Label courseLabel = new Label(course.getTitle());
        courseLabel.setStyle("-fx-text-fill: #60A5FA; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label sealLabel = new Label("🏆");
        sealLabel.setStyle("-fx-font-size: 60px;");

        Label dateLabel = new Label("Issued: " + java.time.LocalDate.now());
        dateLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

        certBox.getChildren().addAll(
                certHeader, thisIsToCertify, nameLabel,
                hasCompleted, courseLabel, sealLabel, dateLabel
        );

        dialog.getDialogPane().setContent(certBox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #0F172A;");

        dialog.showAndWait();
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

        // ✅ Position — အပေါ်ကို ထပ်တိုး
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

    // ============================================
    // ✅ CIRCULAR PROGRESS — ဒုတိယပုံလို အရောင်
    // ============================================
    private StackPane createCircularProgressForCard(int percent, String courseId) {
        double radius = 22;
        double strokeWidth = 5;

        // ✅ အရောင် ၂ မျိုး — အရင့် + ပေါ့
        String darkerColor = getCardDarkerColor(courseId);
        String lighterColor = getCardLighterColor(courseId);

        // ✅ Background Circle — အရင့်ရောင်
        Circle bgCircle = new Circle(radius);
        bgCircle.setFill(Color.web(darkerColor, 0.9));
        bgCircle.setStroke(Color.web(lighterColor, 1.0));
        bgCircle.setStrokeWidth(strokeWidth);

        // ✅ Progress Arc — အဖြူ
        double arcLength = -percent * 3.6;
        Arc progressArc = new Arc(0, 0, radius, radius, 90, arcLength);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(Color.TRANSPARENT);
        progressArc.setStroke(Color.WHITE);
        progressArc.setStrokeWidth(strokeWidth);
        progressArc.setStrokeLineCap(StrokeLineCap.ROUND);

        // ✅ Percentage Label
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

    // ============================================
    // ✅ Card ရဲ့ အရင့်ရောင် (background အတွက်)
    // ============================================
    private String getCardDarkerColor(String courseId) {
        if (courseId == null) return "#334155";
        String upper = courseId.toUpperCase();

        if (upper.contains("CPP") || upper.contains("C++")) return "#1E3A8A";
        if (upper.contains("JAVA")) return "#7C2D12";
        if (upper.contains("PYTHON") || upper.contains("PY")) return "#064E3B";
        if (upper.contains("SQL")) return "#14532D";

        return "#334155";
    }

    // ============================================
    // ✅ Card ရဲ့ ပေါ့တဲ့ အရောင် (stroke အတွက်)
    // ============================================
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