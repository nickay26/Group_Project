package Project;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.SVGPath;

import java.util.ArrayList;
import java.util.List;

public class LearnPageController {

    @FXML private Button learnBtn;
    @FXML private Button coursesBtn;
    @FXML private Button playgroundBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button practiceBtn;
    @FXML private Button gamesBtn;

    @FXML private Pane roadmapPane;

    // Node dimensions & spacing configuration
    private static final double NODE_WIDTH = 100.0;
    private static final double NODE_HEIGHT = 116.0;
    private static final double VERTICAL_SPACING = 160.0;
    private static final double AMPLITUDE = 85.0;

    // Drag tracking helper
    private static class DragContext {
        double mouseAnchorX;
        double mouseAnchorY;
        double initialLayoutX;
        double initialLayoutY;
    }

    @FXML
    public void initialize() {
        // Default course track (replace with active session course if available)
        loadCoursePath("CPP_BASIC");
    }

    public void loadCoursePath(String courseId) {
        // Fetch real lessons from database
        List<Lesson> lessons = DatabaseManager.getLessonsForCourse(courseId);

        if (lessons == null || lessons.isEmpty()) {
            roadmapPane.getChildren().clear();
            Label emptyMsg = new Label("No lessons found in database for course: " + courseId);
            emptyMsg.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 30px;");
            roadmapPane.getChildren().add(emptyMsg);
            return;
        }

        renderRoadmap(lessons);
    }

    private void renderRoadmap(List<Lesson> lessons) {
        roadmapPane.getChildren().clear();

        double centerX = 200.0;
        List<StackPane> nodes = new ArrayList<>();
        List<CubicCurve> curves = new ArrayList<>();

        // 1. Generate Hexagon Nodes for database rows
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            double currentX = centerX + Math.sin(i * 0.95) * AMPLITUDE;
            double currentY = 60.0 + (i * VERTICAL_SPACING);

            StackPane hexNode = createHexagonNode(lesson);
            hexNode.setLayoutX(currentX - (NODE_WIDTH / 2.0));
            hexNode.setLayoutY(currentY - (NODE_HEIGHT / 2.0));
            nodes.add(hexNode);
        }

        // 2. Connect Nodes with Bezier Curves
        for (int i = 0; i < nodes.size() - 1; i++) {
            CubicCurve curve = new CubicCurve();
            curve.setFill(null);
            curve.setStroke(Color.BLACK);
            curve.setStrokeWidth(2.0);

            curves.add(curve);
            roadmapPane.getChildren().add(curve);
            updateCurve(curve, nodes.get(i), nodes.get(i + 1));
        }

        // 3. Attach Drag & Click Listeners
        for (int i = 0; i < nodes.size(); i++) {
            StackPane node = nodes.get(i);
            final int index = i;

            DragContext dragContext = new DragContext();

            node.setOnMousePressed(e -> {
                dragContext.mouseAnchorX = e.getSceneX();
                dragContext.mouseAnchorY = e.getSceneY();
                dragContext.initialLayoutX = node.getLayoutX();
                dragContext.initialLayoutY = node.getLayoutY();
                node.toFront();
            });

            node.setOnMouseDragged(e -> {
                double deltaX = e.getSceneX() - dragContext.mouseAnchorX;
                double deltaY = e.getSceneY() - dragContext.mouseAnchorY;

                node.setLayoutX(dragContext.initialLayoutX + deltaX);
                node.setLayoutY(dragContext.initialLayoutY + deltaY);

                if (index > 0) {
                    updateCurve(curves.get(index - 1), nodes.get(index - 1), node);
                }
                if (index < curves.size()) {
                    updateCurve(curves.get(index), node, nodes.get(index + 1));
                }
            });

            roadmapPane.getChildren().add(node);
        }

        roadmapPane.setPrefHeight(120.0 + (lessons.size() * VERTICAL_SPACING));
    }

    private void updateCurve(CubicCurve curve, StackPane fromNode, StackPane toNode) {
        double startX = fromNode.getLayoutX() + (NODE_WIDTH / 2.0);
        double startY = fromNode.getLayoutY() + NODE_HEIGHT; // Bottom vertex of upper node
        double endX = toNode.getLayoutX() + (NODE_WIDTH / 2.0);
        double endY = toNode.getLayoutY();                  // Top vertex of lower node

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

    private StackPane createHexagonNode(Lesson lesson) {
        StackPane stack = new StackPane();
        stack.setStyle("-fx-cursor: hand;");

        // Enlarged SVG Path (100px W x 116px H)
        SVGPath hexPath = new SVGPath();
        hexPath.setContent("M 50 0 L 100 29 L 100 87 L 50 116 L 0 87 L 0 29 Z");
        hexPath.setFill(Color.WHITE);
        hexPath.setStroke(Color.BLACK);
        hexPath.setStrokeWidth(2.0);

        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER);
        content.setMaxSize(85, 85);

        // Icon selected by lesson_type column
        String icon = "EXAM".equalsIgnoreCase(lesson.getLessonType()) ? "📝" : "⭐";
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #000000;");

        Label textLabel = new Label(lesson.getTitle());
        textLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #000000; -fx-alignment: center;");
        textLabel.setWrapText(true);

        content.getChildren().addAll(iconLabel, textLabel);
        stack.getChildren().addAll(hexPath, content);

        stack.setOnMouseClicked(e -> {
            if (!e.isStillSincePress()) return; // Ignore drag clicks
            System.out.println("Opening Lesson JSON content for: " + lesson.getLessonId());
        });

        return stack;
    }
}