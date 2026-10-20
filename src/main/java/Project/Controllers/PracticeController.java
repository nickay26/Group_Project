package Project.Controllers;

import Project.API_Database.DatabaseManager;
import Project.Helpers.JsonUtils;
import Project.Getters_Setters.Lesson;
import Project.Getters_Setters.QuestionData;
import Project.Getters_Setters.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PracticeController {

    @FXML private ToggleButton practiceToggle;
    @FXML private ToggleButton reviewToggle;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private GridPane cardContainer;

    private ToggleGroup modeToggleGroup;

    private static class TopicCardData {
        String language;
        String topicName;
        List<QuestionData> questions;

        TopicCardData(String language, String topicName, List<QuestionData> questions) {
            this.language = language;
            this.topicName = topicName;
            this.questions = questions;
        }
    }

    private final java.util.Map<String, List<Lesson>> courseLessonsCache = new java.util.HashMap<>();
    private final List<TopicCardData> allCards = new ArrayList<>();

    public static class ReviewData {
        private String lessonId;
        private String courseId;
        private String title;
        private String language;

        public ReviewData(String lessonId, String courseId, String title, String language) {
            this.lessonId = lessonId;
            this.courseId = courseId;
            this.title = title;
            this.language = language;
        }

        public String getLessonId() { return lessonId; }
        public String getCourseId() { return courseId; }
        public String getTitle() { return title; }
        public String getLanguage() { return language; }
    }

    private static final double CARD_WIDTH = 280;
    private static final double CARD_HEIGHT = 260;

    @FXML
    public void initialize() {
        categoryCombo.getItems().addAll("All", "Java", "C++", "Python");
        categoryCombo.setValue("All");
        categoryCombo.setOnAction(e -> handleModeChange());

        modeToggleGroup = new ToggleGroup();
        practiceToggle.setToggleGroup(modeToggleGroup);
        reviewToggle.setToggleGroup(modeToggleGroup);

        modeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            } else {
                handleModeChange();
            }
        });

        List<QuestionData> loadedQuestions = JsonUtils.loadPracticeQuestions();

        Map<String, Map<String, List<QuestionData>>> groupedData = new HashMap<>();
        for (QuestionData q : loadedQuestions) {
            String lang = q.getLanguage();
            String topic = q.getQuestionType() != null ? q.getQuestionType() : "General";

            groupedData.computeIfAbsent(lang, k -> new HashMap<>())
                    .computeIfAbsent(topic, k -> new ArrayList<>())
                    .add(q);
        }

        for (String lang : groupedData.keySet()) {
            for (String topic : groupedData.get(lang).keySet()) {
                allCards.add(new TopicCardData(lang, topic, groupedData.get(lang).get(topic)));
            }
        }

        practiceToggle.setSelected(true);
        renderCards(allCards);
    }

    private void handleModeChange() {
        cardContainer.getChildren().clear();
        cardContainer.getColumnConstraints().clear();
        cardContainer.getRowConstraints().clear();
        cardContainer.setVisible(true);
        cardContainer.setManaged(true);

        if (practiceToggle.isSelected()) {
            filterCards();
        } else if (reviewToggle.isSelected()) {
            loadReviewCards();
        }
    }

    private void filterCards() {
        String selected = categoryCombo.getValue();
        if (selected == null || selected.equals("All")) {
            renderCards(allCards);
        } else {
            List<TopicCardData> filtered = new ArrayList<>();
            for (TopicCardData c : allCards) {
                if (c.language != null && c.language.equalsIgnoreCase(selected)) {
                    filtered.add(c);
                }
            }
            renderCards(filtered);
        }
    }

    private void loadReviewCards() {
        cardContainer.getChildren().clear();
        cardContainer.getColumnConstraints().clear();
        cardContainer.getRowConstraints().clear();
        courseLessonsCache.clear();

        cardContainer.setAlignment(Pos.TOP_CENTER);
        cardContainer.setHgap(40);
        cardContainer.setVgap(40);
        cardContainer.setPadding(new Insets(30));

        int userId = UserSession.getUserId();
        List<ReviewData> reviewList = null;
        try {
            reviewList = DatabaseManager.getReviewLessons(userId);
        } catch (Exception e) {
            System.err.println("❌ Error loading reviews: " + e.getMessage());
        }

        String selectedCategory = categoryCombo.getValue();
        List<ReviewData> filteredList = new ArrayList<>();
        if (reviewList != null) {
            for (ReviewData rev : reviewList) {
                if (rev == null) continue;
                boolean matchesCategory = selectedCategory == null
                        || selectedCategory.equalsIgnoreCase("All")
                        || (rev.getLanguage() != null && rev.getLanguage().equalsIgnoreCase(selectedCategory));

                if (matchesCategory) {
                    filteredList.add(rev);
                }
            }
        }

        if (filteredList.isEmpty()) {
            Label emptyLabel = new Label("No review lessons found.");
            emptyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            emptyLabel.setStyle("-fx-text-fill: #a0a0a0; -fx-padding: 20px;");
            cardContainer.add(emptyLabel, 0, 0);
            return;
        }

        int col = 0;
        int row = 0;
        for (ReviewData rev : filteredList) {
            StackPane hexCard = createPureHexagonCard(rev);
            cardContainer.add(hexCard, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private List<Lesson> getCachedLessonsForCourse(String courseId) {
        if (courseId == null) return null;
        if (!courseLessonsCache.containsKey(courseId)) {
            try {
                courseLessonsCache.put(courseId, DatabaseManager.getLessonsForCourse(courseId));
            } catch (Exception e) {
                courseLessonsCache.put(courseId, new ArrayList<>());
            }
        }
        return courseLessonsCache.get(courseId);
    }

    private String resolveLessonTitle(String courseId, String lessonId) {
        if (courseId == null || lessonId == null) {
            return (lessonId != null) ? lessonId : "Lesson";
        }
        try {
            List<Lesson> lessons = getCachedLessonsForCourse(courseId);
            if (lessons != null) {
                for (Lesson l : lessons) {
                    if (l != null && l.getLessonId() != null && l.getLessonId().equalsIgnoreCase(lessonId)) {
                        return l.getTitle() != null ? l.getTitle() : lessonId;
                    }
                }
            }
        } catch (Exception e) {}
        return lessonId;
    }

    private StackPane createPureHexagonCard(ReviewData data) {
        StackPane stack = new StackPane();
        stack.setPrefSize(140, 160);
        stack.setMinSize(140, 160);
        stack.setMaxSize(140, 160);
        stack.setStyle("-fx-cursor: hand;");

        SVGPath hexPath = new SVGPath();
        hexPath.setContent("M 70 0 L 140 40 L 140 120 L 70 160 L 0 120 L 0 40 Z");
        hexPath.setStrokeWidth(3.5);

        Color fillColor = Color.web("#2563EB");
        Color strokeColor = Color.web("#60A5FA");

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#3B82F6", 0.8));
        glow.setRadius(20);
        stack.setEffect(glow);

        hexPath.setFill(fillColor);
        hexPath.setStroke(strokeColor);

        String lessonTitle = resolveLessonTitle(data.getCourseId(), data.getLessonId());

        VBox content = new VBox(6);
        content.setAlignment(Pos.CENTER);
        content.setMaxSize(120, 120);

        Label iconLabel = new Label("🔖");
        iconLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: #FFFFFF;");

        Label titleLabel = new Label(lessonTitle);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-alignment: center;");
        titleLabel.setWrapText(true);

        content.getChildren().addAll(iconLabel, titleLabel);
        stack.getChildren().addAll(hexPath, content);

        stack.setOnMouseClicked(e -> openReviewLesson(data));
        return stack;
    }

    private void openReviewLesson(ReviewData data) {
        List<Lesson> courseLessons = DatabaseManager.getLessonsForCourse(data.getCourseId());
        Lesson targetLesson = null;

        if (courseLessons != null) {
            for (Lesson lesson : courseLessons) {
                if (lesson.getLessonId().equalsIgnoreCase(data.getLessonId())) {
                    targetLesson = lesson;
                    break;
                }
            }
        }

        if (targetLesson != null) {
            FXMLLoader loader = MainLayoutController.getInstance().loadPage("LessonPlayerView.fxml");
            if (loader != null) {
                LessonPlayerController controller = loader.getController();
                if (controller != null) {
                    controller.startLesson(targetLesson, true);
                }
            }
        }
    }

    private void renderCards(List<TopicCardData> cards) {
        cardContainer.getChildren().clear();
        cardContainer.getColumnConstraints().clear();
        cardContainer.getRowConstraints().clear();

        cardContainer.setAlignment(Pos.TOP_CENTER);
        cardContainer.setHgap(30);
        cardContainer.setVgap(30);
        cardContainer.setPadding(new Insets(20));

        int col = 0;
        int row = 0;
        for (TopicCardData c : cards) {
            VBox card = createCard(c, CARD_WIDTH, CARD_HEIGHT);
            cardContainer.add(card, col, row);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createCard(TopicCardData data, double width, double height) {
        VBox card = new VBox(10);
        card.setPrefSize(width, height);
        card.setMinSize(width, height);
        card.setMaxSize(width, height);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("glass-card");
        card.setPadding(new Insets(15));

        ImageView iconImage = new ImageView();
        String imagePath = getIconImage(data.language);

        if (imagePath != null) {
            try (InputStream is = getClass().getResourceAsStream(imagePath)) {
                if (is != null) {
                    iconImage.setImage(new Image(is));
                    iconImage.setFitWidth(100);
                    iconImage.setFitHeight(100);
                    iconImage.setPreserveRatio(true);
                    iconImage.setSmooth(true);
                }
            } catch (Exception e) {}
        }

        Label title = new Label(data.topicName);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button startBtn = new Button("Start (" + data.questions.size() + " Qs)");
        startBtn.getStyleClass().add("card-start-btn");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> openTopic(data));

        card.getChildren().addAll(iconImage, title, spacer, startBtn);
        return card;
    }

    // 🌟 မူရင်း UI အတိုင်း MonacoEditorView.fxml ကို ပြန်ခေါ်သုံးထားပါတယ်
    private void openTopic(TopicCardData data) {
        MainLayoutController layout = MainLayoutController.getInstance();
        if (layout != null) {
            FXMLLoader loader = layout.loadPage("MonacoEditorView.fxml"); // ဒီနေရာမှာ မူရင်း FXML ကို ပြန်ထည့်ထားပါတယ်
            if (loader != null) {
                CodeEditorController controller = loader.getController();
                if (controller != null && data.questions != null && !data.questions.isEmpty()) {
                    controller.initQuestions(data.questions); // CodeEditorController ထဲကို မေးခွန်း ၁၀ ခု ပို့မယ်
                }
            }
        }
    }

    private String getIconImage(String language) {
        if (language == null) return null;
        switch (language.toUpperCase()) {
            case "JAVA": return "/Images/java.png";
            case "C++":
            case "CPP_BASIC":
            case "CPP": return "/Images/c++.png";
            case "PYTHON": return "/Images/python.png";
            default: return null;
        }
    }
}