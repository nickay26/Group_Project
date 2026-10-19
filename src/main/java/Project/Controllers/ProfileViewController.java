package Project.Controllers;

import Project.API_Database.ApiClient;
import Project.API_Database.DatabaseManager;
import Project.Getters_Setters.Course;
import Project.Getters_Setters.User;
import Project.Getters_Setters.UserSession;
import Project.Helpers.DialogUtils; // ⭐ ဒီ Import လိုင်း ထည့်ထားပါတယ်
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
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

        boolean serverPremium = DatabaseManager.isPremium(currentUser.getUserId());
        System.out.println("Server premium: " + serverPremium);

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
        VBox overlay = new VBox(6);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(130, 60, 30, 60));
        overlay.setMaxWidth(720);
        overlay.setMaxHeight(500);

        // ✅ "This is to certify that"
        Label certifyText = new Label("This is to certify that");
        certifyText.setStyle(
                "-fx-text-fill: #475569; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-style: italic; " +
                        "-fx-font-family: 'Georgia', serif;"
        );

        // ✅ User Name — ကြီး
        Label nameLabel = new Label(user.getName());
        nameLabel.setStyle(
                "-fx-text-fill: #0F172A; " +
                        "-fx-font-size: 26px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-family: 'Georgia', serif; " +
                        "-fx-padding: 3 0 3 0;"
        );

        // ✅ "has successfully completed the course"
        Label completedText = new Label("has successfully completed the course");
        completedText.setStyle(
                "-fx-text-fill: #475569; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-style: italic; " +
                        "-fx-font-family: 'Georgia', serif;"
        );

        // ✅ Course Name
        Label courseLabel = new Label(course.getTitle());
        courseLabel.setStyle(
                "-fx-text-fill: #1E40AF; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-family: 'Georgia', serif; " +
                        "-fx-padding: 2 0 2 0;"
        );

        // ✅ Description — Mimo/Sololearn လို
        Label descLabel = new Label(
                "in recognition of successfully mastering the course curriculum, " +
                        "demonstrating proficiency in problem-solving, and completing all lessons and quizzes."
        );
        descLabel.setStyle(
                "-fx-text-fill: #64748B; " +
                        "-fx-font-size: 9.5px; " +
                        "-fx-font-style: italic; " +
                        "-fx-font-family: 'Georgia', serif; " +
                        "-fx-text-alignment: center;"
        );
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(480);
        descLabel.setAlignment(Pos.CENTER);

        // ============================================
        // ✅ Sodexa ပုံနှစ်ပုံကို ဘေးချင်းယှဉ်ထည့်ရန် (Logo + Word)
        // ============================================
        HBox logoBox = new HBox(8);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(5, 0, 2, 0));

        ImageView logoIcon = new ImageView();
        ImageView logoWord = new ImageView();

        try {
            // ပထမပုံ (Sodexa.png - သင်္ကေတ လိုဂို)
            InputStream is1 = getClass().getResourceAsStream("/Images/Sodexa.png");
            if (is1 != null) {
                Image img1 = new Image(is1, 35, 35, true, true);
                logoIcon.setImage(img1);
                logoIcon.setFitWidth(30);
                logoIcon.setFitHeight(30);
                logoIcon.setPreserveRatio(true);
            }

            // ဒုတိယပုံ (sodexword.png - စာသား လိုဂို)
            InputStream is2 = getClass().getResourceAsStream("/Images/sodexword.png");
            if (is2 != null) {
                Image img2 = new Image(is2, 100, 35, true, true);
                logoWord.setImage(img2);
                logoWord.setFitWidth(85);
                logoWord.setFitHeight(25);
                logoWord.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.out.println("Logo images not found: " + e.getMessage());
        }

        logoBox.getChildren().addAll(logoIcon, logoWord);

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

        // ✅ Overlay ထဲ ထည့် — အစီအစဉ် (Issued on ရဲ့ အပေါ်မှာ logoBox ထည့်ထားပါတယ်)
        overlay.getChildren().addAll(
                certifyText,
                nameLabel,
                completedText,
                courseLabel,
                descLabel,
                logoBox,
                dateLabel,
                certIdLabel,
                verifiedLabel
        );

        // ✅ StackPane ထဲ — ပုံပေါ်မှာ စာ
        certStack.getChildren().add(overlay);
        StackPane.setAlignment(overlay, Pos.CENTER);

        // ✅ Download Image Button
        Button downloadBtn = new Button("📥 Download Image");
        downloadBtn.setStyle(
                "-fx-background-color: #10B981; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 10 25; " +
                        "-fx-cursor: hand;"
        );

        downloadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Certificate");
            fileChooser.setInitialFileName("Certificate_" +
                    course.getTitle().replaceAll("\\s+", "_") + ".png");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG Image", "*.png")
            );

            File file = fileChooser.showSaveDialog(dialog.getOwner());
            if (file != null) {
                try {
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    javafx.scene.image.WritableImage snapshot = certStack.snapshot(params, null);

                    javax.imageio.ImageIO.write(
                            javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null),
                            "png", file
                    );

                    // ⭐ Alert အစား DialogUtils ကို ခေါ်သုံးခြင်း
                    DialogUtils.showSuccessDialog("Certificate saved successfully!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    // ⭐ Alert အစား DialogUtils ကို ခေါ်သုံးခြင်း
                    DialogUtils.showErrorDialog("Failed to save: " + ex.getMessage());
                }
            }
        });

        // ✅ Button Bar — Download ပဲ
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(15, 25, 15, 25));
        buttonBar.setStyle("-fx-background-color: #0F172A;");
        buttonBar.getChildren().add(downloadBtn);

        // ✅ Dialog Content — ပုံ + စာ + Button
        VBox dialogContent = new VBox(0);
        dialogContent.getChildren().addAll(certStack, buttonBar);
        dialogContent.setStyle("-fx-background-color: #0F172A;");

        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #0F172A;");

        // ✅ "Close" button ဖျောက် (X cross — ဒီအတိုင်း ရှိနေမယ်)
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setManaged(false);

        dialog.showAndWait();
    }

    // ============================================
    // ✅ Skills — Course အလိုက်
    // ============================================
    private String getSkillsForCourse(String courseId) {
        if (courseId == null) return "Programming Fundamentals";
        String upper = courseId.toUpperCase();

        if (upper.contains("CPP") || upper.contains("C++")) {
            return "Syntax · Pointers · OOP · Memory Management";
        }
        if (upper.contains("JAVA")) {
            return "OOP · Collections · Exception Handling · Multithreading";
        }
        if (upper.contains("PYTHON") || upper.contains("PY")) {
            return "Syntax · Data Structures · Functions · Modules";
        }
        return "Programming Fundamentals";
    }

    // ============================================
    // LOAD COURSES PROGRESS
    // ============================================
    private void loadCoursesProgress() {
        coursesProgressContainer.getChildren().clear();

        int userId = UserSession.getUserId();
        List<Course> courseList = DatabaseManager.getCoursesForUser(userId);

        // Update UI back on JavaFX Application Thread
        Platform.runLater(() -> {
            coursesProgressContainer.getChildren().clear();

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
        });
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
    // CREATE COURSE ITEM (ကတ်ပြားအလိုက် နေရာတိကျစွာ ချိန်ညှိပြီး)
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

        // ✅ Circular Progress (ညာဘက်ခြမ်း)
        StackPane progressCircle = createCircularProgressForCard(progressPercent, courseId);

        // ✅ Status Label (Start, In Progress, Completed) - အရောင်အသစ်များဖြင့်
        String statusText;
        String statusStyle;

        if (progressPercent <= 0) {
            statusText = "Start";
            // လိမ်မော်ရောင် (Orange Background & Text)
            statusStyle = "-fx-background-color: #7C2D12; -fx-text-fill: #F97316; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;";
        } else if (progressPercent < 100) {
            statusText = "In Progress";
            // အပြာရောင် (Blue Background & Text)
            statusStyle = "-fx-background-color: #1E3A8A; -fx-text-fill: #60A5FA; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;";
        } else {
            statusText = "Completed";
            // အစိမ်းရောင် (Green Background & Text)
            statusStyle = "-fx-background-color: #064E3B; -fx-text-fill: #34D399; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;";
        }

        Label statusLabel = new Label(statusText);
        statusLabel.setStyle(statusStyle);
        statusLabel.setMouseTransparent(true);

        Pane contentPane = new Pane();
        contentPane.setPrefSize(400, 130);
        contentPane.setMinSize(400, 130);
        contentPane.setMaxSize(400, 130);
        contentPane.setMouseTransparent(true);

        // ✅ C++ Basic နှင့် Java Intermediate တို့အတွက် နေရာတိကျစွာ ချိန်ညှိခြင်း
        double statusY = 92; // Default
        String upperCourseId = courseId != null ? courseId.toUpperCase() : "";

        if (upperCourseId.contains("CPP") && upperCourseId.contains("BASIC")) {
            statusY = 80; // C++ Basic အတွက်
        } else if (upperCourseId.contains("JAVA") && upperCourseId.contains("INTERMEDIATE")) {
            statusY = 86; // Java Intermediate အတွက် အောက်ဘက်သို့ နည်းငယ်ချရန် (86 သို့မဟုတ် 88 ဖြင့် ချိန်ညှိနိုင်ပါသည်)
        } else if (upperCourseId.contains("CPP") || upperCourseId.contains("PYTHON") || upperCourseId.contains("PY")) {
            statusY = 82;
        }

        statusLabel.setLayoutX(115);
        statusLabel.setLayoutY(statusY);

        progressCircle.setLayoutX(320);
        progressCircle.setLayoutY(38);

        contentPane.getChildren().addAll(statusLabel, progressCircle);

        StackPane cardContainer = new StackPane();
        cardContainer.setPrefSize(400, 130);
        cardContainer.setMinSize(400, 130);
        cardContainer.setMaxSize(400, 130);

        cardContainer.getChildren().addAll(cardImage, contentPane);

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
    // ✅ CIRCULAR PROGRESS (အပြီးသတ် ပြင်ဆင်ချက်)
    // ============================================
    private StackPane createCircularProgressForCard(int percent, String courseId) {
        double radius = 22;
        double strokeWidth = 5;

        String darkerColor = getCardDarkerColor(courseId);
        String lighterColor = getCardLighterColor(courseId);

        // ✅ Background Circle — Track (အနောက်ခံ မီးခိုးရောင်အဝိုင်း)
        Circle bgCircle = new Circle(radius, radius, radius);
        bgCircle.setFill(Color.TRANSPARENT);
        bgCircle.setStroke(Color.web("#E2E8F0"));
        bgCircle.setStrokeWidth(strokeWidth);

        // ✅ Progress Arc — safe percent (100 မကျော်ရန်)
        int safePercent = Math.min(percent, 100);
        double arcLength = -safePercent * 3.6;

        // centerX နှင့် centerY ကို radius တန်ဖိုးအတိုင်း ထည့်ပေးခြင်းဖြင့် ဘောင်ပေါ်တွင် တိကျစွာ ပေါ်စေရန်
        Arc progressArc = new Arc(radius, radius, radius, radius, 90, arcLength);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(Color.TRANSPARENT);
        progressArc.setStroke(Color.web(lighterColor));
        progressArc.setStrokeWidth(strokeWidth);
        progressArc.setStrokeLineCap(StrokeLineCap.ROUND); // အနားသတ် ဝိုင့်ဝိုင်းလေးဖြစ်ရန်

        // ✅ ဤ Pane သည် background နှင့် arc နှစ်ခုစလုံးကို အရွယ်အစား တူညီစွာ တစေးတည်း ထိန်းပေးပါမည်
        Pane canvas = new Pane();
        canvas.setPrefSize(radius * 2, radius * 2);
        canvas.setMinSize(radius * 2, radius * 2);
        canvas.setMaxSize(radius * 2, radius * 2);
        canvas.getChildren().addAll(bgCircle, progressArc);

        // ✅ Label (ရာခိုင်နှုန်း စာသား)
        Label percentLabel = new Label(safePercent + "%");
        percentLabel.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );

        StackPane stack = new StackPane();
        double totalSize = radius * 2 + strokeWidth;
        stack.setPrefSize(totalSize, totalSize);
        stack.setMinSize(totalSize, totalSize);
        stack.setMaxSize(totalSize, totalSize);

        // ✅ Canvas (စက်ဝိုင်းပုံများ) နှင့် Label (စာသား) ကို တစ်ထပ်တည်း ထည့်ခြင်း
        stack.getChildren().addAll(canvas, percentLabel);
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
            mainLayout.handleClosePage();
        }
    }
}