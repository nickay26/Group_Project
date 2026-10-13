package Project;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
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
    @FXML private Label premiumBadgeLabel;
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
        if (currentUser == null) return;

        // ✅ SERVER က premium ဟုတ်/မဟုတ် စစ်
        boolean serverPremium = DatabaseManager.isPremium(currentUser.getUserId());
        System.out.println("Server premium: " + serverPremium);

        // ✅ Local User object — update
        if (serverPremium && !currentUser.isPremium()) {
            currentUser.setPremium(true);
            currentUser.setPlanType("PREMIUM");
            System.out.println("✅ Local user updated: premium=true, planType=PREMIUM");
        }

        String planType = currentUser.getSubscriptionType();
        boolean isPremium = "PREMIUM".equalsIgnoreCase(planType) || currentUser.isPremium();

        System.out.println("Final planType: " + planType);
        System.out.println("Final isPremium: " + isPremium);

        userNameLabel.setText(currentUser.getName() != null ? currentUser.getName() : "User");
        userEmailLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");

        // ✅ Premium Badge
        if (isPremium) {
            premiumBadgeLabel.setVisible(true);
            premiumBadgeLabel.setManaged(true);
            startPremiumAnimation();
            System.out.println("✅ PRO badge SHOWN");
        } else {
            premiumBadgeLabel.setVisible(false);
            premiumBadgeLabel.setManaged(false);
            System.out.println("❌ PRO badge HIDDEN");
        }

        streakLabel.setText(String.valueOf(currentUser.getStreak()));
        xpLabel.setText(String.valueOf(currentUser.getXp()));
        coinLabel.setText(String.valueOf(currentUser.getCoin()));

        if (isPremium) {
            brainLabel.setText("∞");
        } else {
            brainLabel.setText(String.valueOf(currentUser.getBrainCount()));
        }

        // ✅ Avatar — format path
        String profilePath = currentUser.getProfile();
        if (profilePath != null && !profilePath.trim().isEmpty() && !"null".equalsIgnoreCase(profilePath.trim())) {
            try {
                String fullImageUrl = ApiClient.getProfileImageUrl(profilePath);
                if (fullImageUrl != null) {
                    Image img = new Image(fullImageUrl, true);
                    avatarImageView.setImage(img);
                }
            } catch (Exception e) {
                System.err.println("Could not load avatar: " + e.getMessage());
            }
        }

        Circle clip = new Circle(60, 60, 60);
        avatarImageView.setClip(clip);
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
        List<Course> certificateList = DatabaseManager.getCompletedCourses(userId);

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

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

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

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(course.getTitle());
        titleLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label subTitle = new Label("Certificate of Completion");
        subTitle.setStyle("-fx-text-fill: #60A5FA; -fx-font-size: 11px;");

        titleBox.getChildren().addAll(titleLabel, subTitle);
        topRow.getChildren().addAll(iconView, titleBox);

        Label descLabel = new Label(course.getDescription());
        descLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(340);

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

        viewBtn.setOnAction(e -> showCertificate(course));

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
    // ✅ Card Icon
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
        dialog.setTitle("Certificate of Achievement");
        dialog.setHeaderText(null);

        // ✅ Certificate Container — StackPane (ပုံ + စာ)
        StackPane certStack = new StackPane();
        certStack.setPrefSize(720, 500);
        certStack.setMinSize(720, 500);
        certStack.setMaxSize(720, 500);

        // ✅ Certificate Template ပုံ — 2x size နဲ့ load → ရှင်းလင်း
        try {
            InputStream is = getClass().getResourceAsStream("/Images/certificate_template.png");
            if (is != null) {
                // ✅ 2x size — smooth
                Image certImg = new Image(is, 1440, 1000, true, true);
                ImageView certBg = new ImageView(certImg);
                certBg.setFitWidth(720);
                certBg.setFitHeight(500);
                certBg.setPreserveRatio(false);
                certBg.setSmooth(true);
                certBg.setCache(true);
                certStack.getChildren().add(certBg);
            }
        } catch (Exception e) {
            System.out.println("Certificate template not found");
        }

        // ✅ Overlay Content — စာ အားလုံး
        VBox overlay = new VBox(8);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(140, 60, 40, 60));   // ✅ စာ အောက်ကို ချ
        overlay.setMaxWidth(720);
        overlay.setMaxHeight(500);

       /* // ✅ Issued By — Sodex Platform
        Label issuedBy = new Label("SODEX PLATFORM");
        issuedBy.setStyle(
                "-fx-text-fill: #94A3B8; " +
                        "-fx-font-size: 9px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-letter-spacing: 2px;"
        );*/

        // ✅ "This is to certify that"
        Label certifyText = new Label("This is to certify that");
        certifyText.setStyle(
                "-fx-text-fill: #475569; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-style: italic; " +
                        "-fx-font-family: 'Georgia', serif;"
        );

        // ✅ User Name — ကြီး
        Label nameLabel = new Label(user.getName());
        nameLabel.setStyle(
                "-fx-text-fill: #0F172A; " +
                        "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-family: 'Georgia', serif; " +
                        "-fx-padding: 5 0 5 0;"
        );

        // ✅ "has successfully completed the course"
        Label completedText = new Label("has successfully completed the course");
        completedText.setStyle(
                "-fx-text-fill: #475569; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-style: italic; " +
                        "-fx-font-family: 'Georgia', serif;"
        );

        // ✅ Course Name
        Label courseLabel = new Label(course.getTitle());
        courseLabel.setStyle(
                "-fx-text-fill: #1E40AF; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-family: 'Georgia', serif; " +
                        "-fx-padding: 3 0 3 0;"
        );

        // ✅ Description — Mimo/Sololearn လို
        Label descLabel = new Label(
                "in recognition of successfully mastering the course curriculum, " +
                        "demonstrating proficiency in problem-solving, and completing all lessons and quizzes."
        );
        descLabel.setStyle(
                "-fx-text-fill: #64748B; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-style: italic; " +
                        "-fx-font-family: 'Georgia', serif; " +
                        "-fx-text-alignment: center;"
        );
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(480);
        descLabel.setAlignment(Pos.CENTER);



        // ✅ Date — "Issued on"
        Label dateLabel = new Label("Issued on " + java.time.LocalDate.now());
        dateLabel.setStyle(
                "-fx-text-fill: #475569; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-family: 'Georgia', serif;"
        );

        // ✅ Certificate ID
        String certId = "CERT-" + java.time.Year.now() + "-" +
                course.getCourseId().toUpperCase() + "-" +
                String.format("%04d", user.getUserId());

        Label certIdLabel = new Label("Certificate ID: " + certId);
        certIdLabel.setStyle(
                "-fx-text-fill: #94A3B8; " +
                        "-fx-font-size: 8px; " +
                        "-fx-font-family: 'Georgia', serif;"
        );

        // ✅ Verified Badge
        Label verifiedLabel = new Label("✔ Verified");
        verifiedLabel.setStyle(
                "-fx-text-fill: #10B981; " +
                        "-fx-font-size: 9px; " +
                        "-fx-font-weight: bold;"
        );

        // ✅ Overlay ထဲ ထည့် — အစီအစဉ်
        overlay.getChildren().addAll(
              //  issuedBy,           // SODEX PLATFORM
                certifyText,        // This is to certify that
                nameLabel,          // Kary
                completedText,      // has successfully completed
                courseLabel,        // C++ Basic
                descLabel,          // Description
                //skillsLabel,        // Skills
                new Label(" "),
                dateLabel,          // Issued on
                certIdLabel,        // Certificate ID
                verifiedLabel       // ✔ Verified
        );

        // ✅ StackPane ထဲ — ပုံပေါ်မှာ စာ
        certStack.getChildren().add(overlay);
        StackPane.setAlignment(overlay, Pos.CENTER);

        // ✅ Dialog
        dialog.getDialogPane().setContent(certStack);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #0F172A;");

        // ✅ Close Button Style
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE)
                .setStyle(
                        "-fx-background-color: #1E40AF; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-padding: 6 20; " +
                                "-fx-cursor: hand;"
                );

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
    // ✅ CIRCULAR PROGRESS
    // ============================================
    private StackPane createCircularProgressForCard(int percent, String courseId) {
        double radius = 22;
        double strokeWidth = 5;

        String darkerColor = getCardDarkerColor(courseId);
        String lighterColor = getCardLighterColor(courseId);

        Circle bgCircle = new Circle(radius);
        bgCircle.setFill(Color.web(darkerColor, 0.9));
        bgCircle.setStroke(Color.web("#E2E8F0"));
        bgCircle.setStrokeWidth(strokeWidth);

        double arcLength = -percent * 3.6;
        Arc progressArc = new Arc(0, 0, radius, radius, 90, arcLength);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(Color.TRANSPARENT);
        progressArc.setStroke(Color.web(lighterColor));   // အပြာ
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

        stack.getChildren().addAll(bgCircle, progressArc, percentLabel);
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
    @FXML
    private void handleClose() {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.handleClosePage();   // ✅ currentPage ပြန်
        }
    }
}