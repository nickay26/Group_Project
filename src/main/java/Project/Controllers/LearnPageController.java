package Project.Controllers;

import Project.API_Database.DatabaseManager;
import Project.Helpers.DialogUtils;
import Project.Getters_Setters.Lesson;
import Project.Getters_Setters.User;
import Project.Getters_Setters.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.SVGPath;

import java.util.*;

public class LearnPageController {

    @FXML private ScrollPane roadmapScrollPane;
    @FXML private Pane roadmapPane;

    private static final double NODE_WIDTH = 130.0;
    private static final double NODE_HEIGHT = 150.0;
    private static final double VERTICAL_SPACING = 200.0;
    private static final double AMPLITUDE = 85.0;
    private static final double CANVAS_WIDTH = 540.0;

    private final Set<String> expandedModules = new HashSet<>();
    private List<Lesson> currentLessons = new ArrayList<>();

    @FXML
    public void initialize() {
        refreshUserData();
        String activeCourseId = UserSession.getActiveCourseId();
        loadCoursePath(activeCourseId);
        Platform.runLater(this::scrollToActiveLesson);
    }

    private boolean isUserPremium() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            System.out.println("[Debug] UserSession.getCurrentUser() is NULL!");
            return false;
        }
        System.out.println("[Debug] User found -> Name: " + user.getName() +
                ", isPremium(): " + user.isPremium() +
                ", PlanType: " + user.getPlanType() +
                ", SubscriptionType: " + user.getSubscriptionType());
        return user.isPremium();
    }

    private int getActiveLessonOrder(int completedCount) {
        boolean premiumUser = isUserPremium();
        System.out.println("🔍 [Debug] getActiveLessonOrder: completedCount=" + completedCount + ", isUserPremium=" + premiumUser);

        for (Lesson lesson : currentLessons) {
            System.out.println("   - Checking lesson: order=" + lesson.getLessonOrder() + ", title=" + lesson.getTitle() + ", isPremium=" + lesson.isPremium());
            if (lesson.getLessonOrder() > completedCount) {
                if (!premiumUser && lesson.isPremium()) {
                    System.out.println("     ⏩ Skipping premium lesson: " + lesson.getTitle());
                    continue;
                }
                System.out.println("     ⭐ Selected active lesson: " + lesson.getTitle() + " (Order: " + lesson.getLessonOrder() + ")");
                return lesson.getLessonOrder();
            }
        }
        return completedCount + 1;
    }

    private void scrollToActiveLesson() {
        if (currentLessons == null || currentLessons.isEmpty()) return;

        int userId = UserSession.getUserId();
        int completedCount = DatabaseManager.getCompletedLessonCount(
                userId, UserSession.getActiveCourseId());

        int activeOrder = getActiveLessonOrder(completedCount);

        int targetIndex = -1;
        for (int i = 0; i < currentLessons.size(); i++) {
            Lesson lesson = currentLessons.get(i);
            if (lesson.getLessonOrder() == activeOrder) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            targetIndex = 0;
        }

        double targetY = 40.0 + (targetIndex * VERTICAL_SPACING);

        if (roadmapScrollPane != null) {
            double totalHeight = roadmapPane.getPrefHeight();
            if (totalHeight > 0) {
                double scrollValue = targetY / totalHeight;
                roadmapScrollPane.setVvalue(Math.min(scrollValue, 1.0));
            }
        }
    }

    private void refreshUserData() {
        int userId = UserSession.getUserId();
        if (userId > 0) {
            new Thread(() -> {
                User updatedUser = DatabaseManager.getUserById(userId);
                if (updatedUser != null) {
                    UserSession.setCurrentUser(updatedUser);

                    Platform.runLater(() -> {
                        if (MainLayoutController.getInstance() != null) {
                            MainLayoutController.getInstance().updateHeaderStats();
                        }
                    });
                }
            }).start();
        }
    }

    public void loadCoursePath(String courseId) {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().updateCourseTitle();
            MainLayoutController.getInstance().updateHeaderStats();
        }
        currentLessons = DatabaseManager.getLessonsForCourse(courseId);

        if (currentLessons == null || currentLessons.isEmpty()) {
            roadmapPane.getChildren().clear();
            Label emptyMsg = new Label("No lessons found for course: " + courseId);
            emptyMsg.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-padding: 30px;");
            roadmapPane.getChildren().add(emptyMsg);
            return;
        }

        if (expandedModules.isEmpty() && !currentLessons.isEmpty()) {
            for (Lesson lesson : currentLessons) {
                expandedModules.add(lesson.getModuleName());
            }
        }

        renderRoadmap();
    }

    private void renderRoadmap() {
        roadmapPane.getChildren().clear();
        roadmapPane.setPrefWidth(CANVAS_WIDTH);

        double currentY = 40.0;
        double centerX = (CANVAS_WIDTH - NODE_WIDTH) / 2.0;

        String currentModule = "";
        StackPane previousNode = null;

        int userId = UserSession.getUserId();
        int completedCount = DatabaseManager.getCompletedLessonCount(userId, UserSession.getActiveCourseId());

        int moduleIndex = 0;

        for (int i = 0; i < currentLessons.size(); i++) {
            Lesson lesson = currentLessons.get(i);
            String moduleName = lesson.getModuleName();

            if (!moduleName.equals(currentModule)) {
                currentModule = moduleName;
                boolean isExpanded = expandedModules.contains(currentModule);
                final String modName = currentModule;

                long totalInMod = currentLessons.stream()
                        .filter(l -> l.getModuleName().equals(modName)).count();
                long completedInMod = currentLessons.stream()
                        .filter(l -> l.getModuleName().equals(modName) && l.getLessonOrder() <= completedCount).count();

                double progress = totalInMod > 0 ? (double) completedInMod / totalInMod : 0.0;

                VBox banner = createModuleBanner(currentModule, isExpanded, progress);

                double bannerXOffset = (moduleIndex % 2 == 0) ? -60.0 : 60.0;
                double bannerX = ((CANVAS_WIDTH - 360.0) / 2.0) + bannerXOffset;

                banner.setLayoutX(bannerX);
                banner.setLayoutY(currentY);
                roadmapPane.getChildren().add(banner);

                moduleIndex++;
                currentY += 80.0;
                previousNode = null;
            }

            if (!expandedModules.contains(moduleName)) {
                continue;
            }

            double xOffset = Math.sin(i * 1.1) * AMPLITUDE;
            double layoutX = centerX + xOffset;
            double layoutY = currentY;

            String status = getLessonStatus(lesson, completedCount);
            StackPane node = createHexagonNode(lesson, status);
            node.setLayoutX(layoutX);
            node.setLayoutY(layoutY);

            if (previousNode != null) {
                CubicCurve curve = new CubicCurve();
                curve.setStroke(Color.web("#3B82F6"));
                curve.setStrokeWidth(4.0);
                curve.setFill(null);
                updateCurve(curve, previousNode, node);
                roadmapPane.getChildren().add(0, curve);
            }

            roadmapPane.getChildren().add(node);
            previousNode = node;

            currentY += VERTICAL_SPACING;
        }

        roadmapPane.setPrefHeight(currentY + 60.0);
    }

    private VBox createModuleBanner(String moduleTitle, boolean isExpanded, double progress) {
        Label label = new Label(moduleTitle);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #F8FAFC;");

        int progressPercent = (int) Math.round(progress * 100);
        Label percentLabel = new Label(progressPercent + "%");
        percentLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #60A5FA; -fx-padding: 0 8 0 0;");

        Label arrowLabel = new Label(isExpanded ? "▲" : "▼");
        arrowLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #60A5FA;");

        HBox rightInfo = new HBox(6, percentLabel, arrowLabel);
        rightInfo.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(label, spacer, rightInfo);
        topRow.setAlignment(Pos.CENTER_LEFT);

        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(7);
        progressBar.getStyleClass().add("custom-progress-bar");

        VBox banner = new VBox(8, topRow, progressBar);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(12, 18, 12, 18));

        banner.setPrefWidth(380.0);
        banner.setMinWidth(380.0);
        banner.setMaxWidth(380.0);

        banner.setStyle(
                "-fx-background-color: #1E293B; " +
                        "-fx-border-color: #3B82F6; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-background-radius: 16px; " +
                        "-fx-border-radius: 16px; " +
                        "-fx-cursor: hand;"
        );

        banner.setOnMouseClicked(e -> {
            if (expandedModules.contains(moduleTitle)) {
                expandedModules.remove(moduleTitle);
            } else {
                expandedModules.add(moduleTitle);
            }
            renderRoadmap();
        });

        return banner;
    }

    private String getLessonStatus(Lesson lesson, int completedCount) {
        if (lesson.getLessonOrder() <= completedCount) {
            return "COMPLETED";
        }

        int activeOrder = getActiveLessonOrder(completedCount);
        if (lesson.getLessonOrder() == activeOrder) {
            return "ACTIVE";
        } else {
            // ⭐ အကယ်၍ သင်ခန်းစာက Premium ဖြစ်ပြီး၊ User ကိုယ်တိုင်ကလည်း Premium မဟုတ်မှသာလျှင် PREMIUM_LOCKED ဖြစ်ရပါမည်
            if (lesson.isPremium() && !isUserPremium()) {
                return "PREMIUM_LOCKED";
            }
            return "LOCKED";
        }
    }

    private StackPane createHexagonNode(Lesson lesson, String status) {
        StackPane stack = new StackPane();

        SVGPath hexPath = new SVGPath();
        hexPath.setContent("M 65 0 L 130 37.5 L 130 112.5 L 65 150 L 0 112.5 L 0 37.5 Z");
        hexPath.setStrokeWidth(3.5);

        String icon;
        Color fillColor;
        Color strokeColor;

        DropShadow glow = new DropShadow();

        switch (status) {
            case "COMPLETED":
                fillColor = Color.web("#10B981");
                strokeColor = Color.web("#34D399");
                icon = "✓";
                glow.setColor(Color.web("#10B981", 0.5));
                glow.setRadius(18);
                stack.setEffect(glow);
                stack.setStyle("-fx-cursor: hand;");
                break;

            case "ACTIVE":
                fillColor = Color.web("#2563EB");
                strokeColor = Color.web("#60A5FA");
                icon = "⭐";
                glow.setColor(Color.web("#3B82F6", 0.8));
                glow.setRadius(25);
                stack.setEffect(glow);
                stack.setStyle("-fx-cursor: hand;");
                break;

            case "PREMIUM_LOCKED": // ⭐ ပရီမီယံ လော့ခ်ကျနေသော သင်ခန်းစာများအတွက် ရွှေရောင်စတိုင်
                fillColor = Color.web("#1E293B");
                strokeColor = Color.web("#F59E0B"); // ရွှေရောင်/လိမ္မော်ရောင် ဘောင်
                icon = "⭐"; // သို့မဟုတ် 👑 ပုံစံသုံးနိုင်ပါသည်
                glow.setColor(Color.web("#F59E0B", 0.5));
                glow.setRadius(18);
                stack.setEffect(glow);
                stack.setStyle("-fx-cursor: hand;");
                break;

            default: // LOCKED (ရိုးရိုး အခမဲ့သင်ခန်းစာ လော့ခ်ကျခြင်း)
                fillColor = Color.web("#1E293B");
                strokeColor = Color.web("#475569");
                icon = "🔒";
                stack.setStyle("-fx-cursor: default;");
                break;
        }

        hexPath.setFill(fillColor);
        hexPath.setStroke(strokeColor);

        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER);
        content.setMaxSize(110, 110);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: " + (status.equals("LOCKED") ? "#94A3B8;" : "#FFFFFF;"));

        Label textLabel = new Label(lesson.getTitle());
        textLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + (status.equals("LOCKED") ? "#94A3B8;" : "#FFFFFF;") + " -fx-alignment: center;");
        textLabel.setWrapText(true);

        content.getChildren().addAll(iconLabel, textLabel);
        stack.getChildren().addAll(hexPath, content);

        stack.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (status.equals("PREMIUM_LOCKED")) {
                    // ပရီမီယံသင်ခန်းစာကို နှိပ်မိပါက Upgrade Dialog ကို ခေါ်ပြရန်
                    DialogUtils.showUpgradePromptDialog("This next lesson is a Supernova Premium feature! Upgrade your plan to continue.");
                } else if (!status.equals("LOCKED")) {
                    openLessonPage(lesson);
                }
            } else if (event.getButton() == MouseButton.SECONDARY) {
                showLessonContextMenu(stack, lesson, status, event.getScreenX(), event.getScreenY());
            }
        });

        return stack;
    }

    private void showLessonContextMenu(StackPane node, Lesson lesson, String status, double screenX, double screenY) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem startItem = new MenuItem("📖 Start Lesson");
        if (status.equals("LOCKED")) {
            startItem.setDisable(true);
        } else {
            startItem.setOnAction(e -> openLessonPage(lesson));
        }

        MenuItem addToReviewItem = new MenuItem("📌 Add to Review");
        addToReviewItem.setOnAction(e -> handleAddToReview(lesson));

        MenuItem infoItem = new MenuItem("ℹ️ View Details & XP");
        infoItem.setOnAction(e -> showLessonDetails(lesson, status));

        contextMenu.getItems().addAll(startItem, addToReviewItem, infoItem);
        contextMenu.show(node, screenX, screenY);
    }

    private void handleAddToReview(Lesson lesson) {
        int userId = UserSession.getUserId();
        String courseId = UserSession.getActiveCourseId();

        if (userId <= 0) {
            DialogUtils.showMessageDialog("Error", "Please log in to save lessons to review.", true);
            return;
        }

        boolean success = DatabaseManager.addLessonToReview(userId, courseId, lesson.getLessonId());

        if (success) {
            DialogUtils.showMessageDialog("Success", "'" + lesson.getTitle() + "' has been added to your Review list!", false);
        } else {
            DialogUtils.showMessageDialog("Error", "Failed to add to Review (or it might already exist in your review list).", true);
        }
    }

    private void showLessonDetails(Lesson lesson, String status) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Lesson Title", lesson.getTitle());
        details.put("Module", lesson.getModuleName());
        details.put("XP Reward", lesson.getXp() + " XP");
        details.put("Status", status);

        DialogUtils.showDetailsDialog("Lesson Details", "Lesson Overview", details);
    }

    private void openLessonPage(Lesson lesson) {
        FXMLLoader loader = MainLayoutController.getInstance().loadPage("LessonPlayerView.fxml");

        if (loader != null) {
            LessonPlayerController controller = loader.getController();
            if (controller != null) {
                controller.startLesson(lesson);
            }
        }
    }

    private void updateCurve(CubicCurve curve, StackPane fromNode, StackPane toNode) {
        double startX = fromNode.getLayoutX() + (NODE_WIDTH / 2.0);
        double startY = fromNode.getLayoutY() + NODE_HEIGHT;
        double endX = toNode.getLayoutX() + (NODE_WIDTH / 2.0);
        double endY = toNode.getLayoutY();

        curve.setStartX(startX);
        curve.setStartY(startY);

        double midY = startY + (endY - startY) * 0.5;
        curve.setControlX1(startX);
        curve.setControlY1(midY);
        curve.setControlX2(endX);
        curve.setControlY2(midY);

        curve.setEndX(endX);
        curve.setEndY(endY);
    }
}