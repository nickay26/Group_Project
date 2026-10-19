package Project.Controllers;

import Project.API_Database.DatabaseManager;
import Project.Getters_Setters.Course;
import Project.Getters_Setters.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CoursesPageController {

    @FXML
    private FlowPane coursesContainer;

    private static final Map<String, Image> LOGO_CACHE = new HashMap<>();

    @FXML
    public void initialize() {
        int currentUserId = (UserSession.getInstance() != null)
                ? UserSession.getInstance().getUserId()
                : 1;

        loadCourses(currentUserId);
    }

    private void loadCourses(int userId) {
        CompletableFuture.supplyAsync(() -> DatabaseManager.getCoursesForUser(userId))
                .thenAccept(courses -> Platform.runLater(() -> renderCourseCards(courses)))
                .exceptionally(ex -> {
                    System.err.println("Error loading courses:");
                    ex.printStackTrace();
                    return null;
                });
    }

    private void renderCourseCards(List<Course> courses) {
        coursesContainer.getChildren().clear();

        if (courses.isEmpty()) {
            Label emptyLabel = new Label("No courses available at the moment.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94A3B8;");
            coursesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Course course : courses) {
            Node cardNode = createCourseCard(course);
            coursesContainer.getChildren().add(cardNode);
        }
    }

    private Node createCourseCard(Course course) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(360, 240);

        String defaultStyle = "-fx-background-color: #1E293B; -fx-background-radius: 24px; -fx-border-color: #334155; -fx-border-radius: 24px; -fx-border-width: 1.5px;";
        String hoverStyle = "-fx-background-color: #1E293B; -fx-background-radius: 24px; -fx-border-color: #3B82F6; -fx-border-radius: 24px; -fx-border-width: 1.5px;";

        card.setStyle(defaultStyle);

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));

        Rectangle clip = new Rectangle(360, 240);
        clip.setArcWidth(48);
        clip.setArcHeight(48);
        card.setClip(clip);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        AnchorPane.setTopAnchor(header, 20.0);
        AnchorPane.setLeftAnchor(header, 20.0);

        Node logoNode = getLanguageLogoNode(course.getLanguage());

        Label titleLabel = new Label(course.getTitle());
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        header.getChildren().addAll(logoNode, titleLabel);

        Label descLabel = new Label(course.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-alignment: center;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(280);
        descLabel.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(descLabel, 95.0);
        AnchorPane.setLeftAnchor(descLabel, 40.0);
        AnchorPane.setRightAnchor(descLabel, 40.0);

        Polygon ribbon = new Polygon();
        ribbon.getPoints().addAll(new Double[]{
                0.0, 240.0,
                130.0, 240.0,
                0.0, 110.0
        });
        ribbon.setStyle("-fx-fill: #2563EB;");

        // ⭐ ရာခိုင်နှုန်းအလိုက် စာသားနှင့် နေရာကို ပြောင်းလဲခြင်း
        int progress = course.getProgressPercent();
        String ribbonLabelString;
        double textX = 10;
        double textY = 192;

        if (progress >= 100) {
            ribbonLabelString = "Completed!";
            textX = 20;  // စာသားတိုသွားလို့ ညာဘက်ကို နည်းနည်းရွှေ့မယ်
            textY = 192; // အပေါ်ကို နည်းနည်းတင်မယ်
        } else if (progress > 0) {
            ribbonLabelString = "In Progress";
            textX = 22;
            textY = 192;
        } else {
            ribbonLabelString = "Start Learning";
        }

        Label ribbonText = new Label(ribbonLabelString);
        ribbonText.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 13px;");
        ribbonText.setRotate(45.0);
        ribbonText.setLayoutX(textX);
        ribbonText.setLayoutY(textY);

        Group cornerButton = new Group(ribbon, ribbonText);
        cornerButton.setCursor(Cursor.HAND);

        // Click လုပ်လိုက်ပါက Loading မပြတော့ဘဲ သင်ခန်းစာသို့ တိုက်ရိုက်သွားမည်
        cornerButton.setOnMouseClicked(e -> launchLessonViewDirectly(course));

        StackPane progressRing = createProgressRing(course);
        AnchorPane.setBottomAnchor(progressRing, 20.0);
        AnchorPane.setRightAnchor(progressRing, 25.0);

        card.getChildren().addAll(header, descLabel, cornerButton, progressRing);
        return card;
    }

    private Node getLanguageLogoNode(String language) {
        String langKey = language.toLowerCase();

        if (LOGO_CACHE.containsKey(langKey)) {
            ImageView cachedView = new ImageView(LOGO_CACHE.get(langKey));
            cachedView.setFitWidth(70);
            cachedView.setFitHeight(70);
            cachedView.setPreserveRatio(true);
            return cachedView;
        }

        String fileName = switch (langKey) {
            case "java" -> "java.png";
            case "cpp" -> "c++.png";
            case "python" -> "python.png";
            default -> "code-logo.png";
        };

        InputStream is = getClass().getResourceAsStream("/images/" + fileName);
        if (is != null) {
            Image img = new Image(is);
            LOGO_CACHE.put(langKey, img);
            ImageView logoView = new ImageView(img);
            logoView.setFitWidth(70);
            logoView.setFitHeight(70);
            logoView.setPreserveRatio(true);
            return logoView;
        }

        return createVectorBadge(language);
    }

    private Node createVectorBadge(String language) {
        StackPane badge = new StackPane();
        badge.setPrefSize(100, 100);

        Polygon hex = new Polygon();
        hex.getPoints().addAll(new Double[]{
                27.5, 0.0,
                55.0, 15.0,
                55.0, 40.0,
                27.5, 55.0,
                0.0, 40.0,
                0.0, 15.0
        });
        hex.setStyle("-fx-fill: #2563EB; -fx-stroke: #3B82F6; -fx-stroke-width: 2;");

        String labelText = language.length() > 3 ? language.substring(0, 3).toUpperCase() : language.toUpperCase();
        Label langText = new Label(labelText);
        langText.setStyle("-fx-text-fill: #F8FAFC; -fx-font-weight: bold; -fx-font-size: 11px;");

        badge.getChildren().addAll(hex, langText);
        return badge;
    }

    private StackPane createProgressRing(Course course) {
        StackPane container = new StackPane();

        Circle ring = new Circle(30);
        ring.setFill(Color.web("#0F172A"));
        ring.setStroke(Color.web("#3B82F6"));
        ring.setStrokeWidth(5);

        int percentage = course.getProgressPercent();
        Label text = new Label(course.getCompletedLessons() > 0
                ? percentage + "%"
                : "Progress");
        text.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #3B82F6;");

        container.getChildren().addAll(ring, text);
        return container;
    }

    // ⭐ Loading Dialog မပါဘဲ Learn Page သို့ တိုက်ရိုက် သွားမည့် Logic
    private void launchLessonViewDirectly(Course course) {
        // 1. Session ထဲတွင် Active Course ID သတ်မှတ်ခြင်း
        UserSession.setActiveCourseId(course.getCourseId());

        // 2. Main Layout Header ပေါ်ရှိ Course Title စာသားကို ချက်ချင်း Update ပြုလုပ်ခြင်း
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().updateCourseTitle();
            // 3. Learn Page FXML သို့ တိုက်ရိုက် Switch ပြုလုပ်မည်
            MainLayoutController.getInstance().loadPage("LearnPage.fxml");
        }
    }
}