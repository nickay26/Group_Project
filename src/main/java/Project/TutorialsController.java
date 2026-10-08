package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TutorialsController {

    @FXML private VBox contentArea;

    @FXML
    private void handleBack() {
        MainLayoutController.getInstance().loadPage("HelpView.fxml");
    }

    @FXML
    private void showCppTutorial() {
        loadContent("cpp");
    }

    @FXML
    private void showJavaTutorial() {
        loadContent("java");
    }

    @FXML
    private void showPythonTutorial() {
        loadContent("python");
    }

    // ============================================
    // ✅ Load Content
    // ============================================
    private void loadContent(String language) {
        contentArea.getChildren().clear();

        String filePath = "/tutorials/" + language.toLowerCase() + ".txt";

        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                Label error = new Label("❌ Content not found: " + filePath);
                error.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px;");
                contentArea.getChildren().add(error);
                return;
            }

            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            TextFlow flow = new TextFlow();
            flow.setLineSpacing(4);
            flow.setStyle(
                    "-fx-padding: 20; " +
                            "-fx-background-color: #1E293B; " +
                            "-fx-background-radius: 10px;"
            );

            String[] lines = content.split("\n");
            for (String line : lines) {
                Text text = parseLine(line);
                flow.getChildren().add(text);
            }

            contentArea.getChildren().add(flow);

        } catch (Exception e) {
            e.printStackTrace();
            Label error = new Label("❌ Error: " + e.getMessage());
            error.setStyle("-fx-text-fill: #EF4444;");
            contentArea.getChildren().add(error);
        }
    }

    // ============================================
    // ✅ Parse Line — တူညီတဲ့ အရောင်
    // ============================================
    private Text parseLine(String line) {
        Text text;

        // ✅ Header — UPPERCASE (PYTHON TUTORIAL)
        if (line.matches("^[A-Z][A-Z\\s]+$")) {
            text = new Text(line + "\n");
            text.setFont(Font.font("System", FontWeight.BOLD, 16));
            text.setFill(Color.web("#F8FAFC"));
        }
        // ✅ Separator line
        else if (line.trim().startsWith("━")) {
            text = new Text(line + "\n");
            text.setFont(Font.font("System", 13));
            text.setFill(Color.web("#475569"));
        }
        // ✅ Section Title — "How to Use Python" (Title Case)
        else if (line.matches("^[A-Z][a-zA-Z\\s]+$") && !line.startsWith(" ")) {
            text = new Text(line + "\n");
            text.setFont(Font.font("System", FontWeight.BOLD, 15));
            text.setFill(Color.web("#60A5FA"));      // ✅ အပြာ
        }
        // ✅ Code line — "    " (4 space) သို့ "  " (2 space)
        else if (line.startsWith("    ") || line.startsWith("  ")) {
            text = new Text(line + "\n");
            text.setFont(Font.font("Consolas", 13));
            text.setFill(Color.web("#4ADE80"));      // ✅ အစိမ်း
        }
        // ✅ Comment line — # နဲ့ စ
        else if (line.trim().startsWith("#")) {
            text = new Text(line + "\n");
            text.setFont(Font.font("Consolas", 13));
            text.setFill(Color.web("#64748B"));      // မီးခိုး
        }
        // ✅ List item — • နဲ့ စ
        else if (line.trim().startsWith("•")) {
            text = new Text(line + "\n");
            text.setFont(Font.font("System", 13));
            text.setFill(Color.web("#CBD5E1"));
        }
        // ✅ Normal text
        else {
            text = new Text(line + "\n");
            text.setFont(Font.font("System", 13));
            text.setFill(Color.web("#CBD5E1"));
        }

        return text;
    }
}