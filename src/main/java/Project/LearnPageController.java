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
import java.util.Arrays;
import java.util.List;

public class LearnPageController {

    @FXML private Button learnBtn;
    @FXML private Button coursesBtn;
    @FXML private Button playgroundBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button practiceBtn;
    @FXML private Button gamesBtn;

    @FXML private Pane roadmapPane;

    public static class LessonData {
        private String title;
        private String icon;

        public LessonData(String title, String icon) {
            this.title = title;
            this.icon = icon;
        }

        public String getTitle() { return title; }
        public String getIcon() { return icon; }
    }

    // Tracks coordinates during mouse drag operations
    private static class DragContext {
        double mouseAnchorX;
        double mouseAnchorY;
        double initialLayoutX;
        double initialLayoutY;
    }

    @FXML
    public void initialize() {
        List<LessonData> lessons = Arrays.asList(
                new LessonData("Level Test", "📝"),
                new LessonData("Lesson 1", "⭐"),
                new LessonData("Lesson 2", "⭐"),
                new LessonData("Premium", "👑"),
                new LessonData("Lesson 3", "⭐"),
                new LessonData("Lesson 4", "⭐"),
                new LessonData("Final Test", "🏆")
        );

        renderRoadmap(lessons);
    }

    private void renderRoadmap(List<LessonData> lessons) {
        roadmapPane.getChildren().clear();

        double centerX = 200.0;
        double verticalSpacing = 110.0;
        double amplitude = 75.0;

        List<StackPane> nodes = new ArrayList<>();
        List<CubicCurve> curves = new ArrayList<>();

        // 1. Instantiate nodes and calculate starting positions
        for (int i = 0; i < lessons.size(); i++) {
            LessonData lesson = lessons.get(i);
            double currentX = centerX + Math.sin(i * 0.95) * amplitude;
            double currentY = 50.0 + (i * verticalSpacing);

            StackPane hexNode = createHexagonNode(lesson);
            hexNode.setLayoutX(currentX - 35.0);
            hexNode.setLayoutY(currentY - 40.0);
            nodes.add(hexNode);
        }

        // 2. Build curve connections between consecutive nodes
        for (int i = 0; i < nodes.size() - 1; i++) {
            CubicCurve curve = new CubicCurve();
            curve.setFill(null);
            curve.setStroke(Color.BLACK);
            curve.setStrokeWidth(1.5);

            curves.add(curve);
            roadmapPane.getChildren().add(curve);
            updateCurve(curve, nodes.get(i), nodes.get(i + 1));
        }

        // 3. Attach drag listeners & update curves live on movement
        for (int i = 0; i < nodes.size(); i++) {
            StackPane node = nodes.get(i);
            final int index = i;

            DragContext dragContext = new DragContext();

            node.setOnMousePressed(e -> {
                dragContext.mouseAnchorX = e.getSceneX();
                dragContext.mouseAnchorY = e.getSceneY();
                dragContext.initialLayoutX = node.getLayoutX();
                dragContext.initialLayoutY = node.getLayoutY();
                node.toFront(); // Keep dragged node above curve lines
            });

            node.setOnMouseDragged(e -> {
                double deltaX = e.getSceneX() - dragContext.mouseAnchorX;
                double deltaY = e.getSceneY() - dragContext.mouseAnchorY;

                node.setLayoutX(dragContext.initialLayoutX + deltaX);
                node.setLayoutY(dragContext.initialLayoutY + deltaY);

                // Live-update incoming bezier curve
                if (index > 0) {
                    updateCurve(curves.get(index - 1), nodes.get(index - 1), node);
                }
                // Live-update outgoing bezier curve
                if (index < curves.size()) {
                    updateCurve(curves.get(index), node, nodes.get(index + 1));
                }
            });

            roadmapPane.getChildren().add(node);
        }

        roadmapPane.setPrefHeight(100.0 + (lessons.size() * verticalSpacing));
    }

    private void updateCurve(CubicCurve curve, StackPane fromNode, StackPane toNode) {
        double startX = fromNode.getLayoutX() + 35.0;
        double startY = fromNode.getLayoutY() + 80.0; // Bottom point of upper hexagon
        double endX = toNode.getLayoutX() + 35.0;
        double endY = toNode.getLayoutY();             // Top point of lower hexagon

        curve.setStartX(startX);
        curve.setStartY(startY);

        // Control points recalculate dynamically to keep curve smooth
        double midY = startY + (endY - startY) * 0.5;
        curve.setControlX1(startX);
        curve.setControlY1(midY);
        curve.setControlX2(endX);
        curve.setControlY2(midY);

        curve.setEndX(endX);
        curve.setEndY(endY);
    }

    private StackPane createHexagonNode(LessonData lesson) {
        StackPane stack = new StackPane();
        stack.setStyle("-fx-cursor: move;");

        SVGPath hexPath = new SVGPath();
        hexPath.setContent("M 35 0 L 70 20 L 70 60 L 35 80 L 0 60 L 0 20 Z");
        hexPath.setFill(Color.WHITE);
        hexPath.setStroke(Color.BLACK);
        hexPath.setStrokeWidth(1.5);

        VBox content = new VBox(2);
        content.setAlignment(Pos.CENTER);
        content.setMaxSize(60, 60);

        Label iconLabel = new Label(lesson.getIcon());
        iconLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #000000;");

        Label textLabel = new Label(lesson.getTitle());
        textLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        content.getChildren().addAll(iconLabel, textLabel);
        stack.getChildren().addAll(hexPath, content);

        return stack;
    }
}