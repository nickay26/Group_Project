package Project;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javax.imageio.ImageIO;
import java.io.File;

public class AvatarCropDialog {

    public static String showCropDialog(File originalFile) {
        try {
            Image originalImage = new Image(originalFile.toURI().toString());

            Dialog<ButtonType> cropDialog = new Dialog<>();
            cropDialog.setTitle("Crop Image");
            cropDialog.setHeaderText("Drag to move • Scroll to resize");

            ButtonType cropButtonType = new ButtonType("Crop", ButtonBar.ButtonData.OK_DONE);
            cropDialog.getDialogPane().getButtonTypes().addAll(cropButtonType, ButtonType.CANCEL);

            // ✅ Image Size — တကယ့် အရွယ်
            double maxSize = 400;
            double origW = originalImage.getWidth();
            double origH = originalImage.getHeight();

            double scale = Math.min(maxSize / origW, maxSize / origH);
            double imgW = origW * scale;
            double imgH = origH * scale;

            ImageView cropPreview = new ImageView(originalImage);
            cropPreview.setPreserveRatio(true);
            cropPreview.setFitWidth(imgW);
            cropPreview.setFitHeight(imgH);

            // ✅ Circle Crop (အဝိုင်း)
            Circle cropCircle = new Circle(Math.min(imgW, imgH) / 3);
            cropCircle.setCenterX(imgW / 2);
            cropCircle.setCenterY(imgH / 2);
            cropCircle.setFill(Color.rgb(56, 189, 248, 0.15));
            cropCircle.setStroke(Color.web("#38bdf8"));
            cropCircle.setStrokeWidth(2);
            cropCircle.setCursor(Cursor.MOVE);

            // ✅ Pane — Image Size အတိုင်း
            Pane cropPane = new Pane();
            cropPane.setPrefSize(imgW, imgH);
            cropPane.setMinSize(imgW, imgH);
            cropPane.setMaxSize(imgW, imgH);
            cropPane.getChildren().addAll(cropPreview, cropCircle);
            cropPane.setStyle("-fx-background-color: black;");

            cropCircle.toFront();

            // ✅ Circle ရွှေ့
            final double[] dragDelta = new double[2];
            cropCircle.setOnMousePressed(e -> {
                dragDelta[0] = cropCircle.getCenterX() - e.getSceneX();
                dragDelta[1] = cropCircle.getCenterY() - e.getSceneY();
            });
            cropCircle.setOnMouseDragged(e -> {
                double newX = e.getSceneX() + dragDelta[0];
                double newY = e.getSceneY() + dragDelta[1];

                newX = Math.max(cropCircle.getRadius(),
                        Math.min(newX, imgW - cropCircle.getRadius()));
                newY = Math.max(cropCircle.getRadius(),
                        Math.min(newY, imgH - cropCircle.getRadius()));

                cropCircle.setCenterX(newX);
                cropCircle.setCenterY(newY);
            });

            // ✅ Scroll — Circle Size ချိန်
            cropPane.setOnScroll(e -> {
                double delta = e.getDeltaY() / 100.0;
                double newRadius = cropCircle.getRadius() + delta * 10;
                newRadius = Math.max(30, Math.min(newRadius, Math.min(imgW, imgH) / 2));
                cropCircle.setRadius(newRadius);
            });

            VBox root = new VBox(15);
            root.setPadding(new Insets(15));
            root.setAlignment(Pos.CENTER);
            root.getChildren().add(cropPane);

            Label hint = new Label("🖱️ Drag to move • Scroll to resize");
            hint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            root.getChildren().add(hint);

            cropDialog.getDialogPane().setContent(root);

            // ✅ Crop နှိပ်ရင်
            final String[] resultPath = {null};
            cropDialog.showAndWait().ifPresent(response -> {
                if (response == cropButtonType) {
                    try {
                        SnapshotParameters params = new SnapshotParameters();
                        params.setViewport(new Rectangle2D(
                                cropCircle.getCenterX() - cropCircle.getRadius(),
                                cropCircle.getCenterY() - cropCircle.getRadius(),
                                cropCircle.getRadius() * 2,
                                cropCircle.getRadius() * 2));

                        WritableImage croppedImage = cropPreview.snapshot(params, null);

                        File tempFile = File.createTempFile("avatar_", ".png");
                        tempFile.deleteOnExit();

                        ImageIO.write(
                                SwingFXUtils.fromFXImage(croppedImage, null),
                                "png", tempFile);

                        resultPath[0] = tempFile.toURI().toString();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            return resultPath[0];

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}