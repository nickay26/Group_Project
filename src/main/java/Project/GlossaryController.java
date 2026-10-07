package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GlossaryController {

    @FXML private VBox contentArea;
    @FXML private ListView<String> termListView;

    // ✅ Term data — Map
    private Map<String, List<String>> termData = new LinkedHashMap<>();

    @FXML
    private void handleBack() {
        MainLayoutController.getInstance().loadPage("HelpView.fxml");
    }

    @FXML private void showCppGlossary() { loadContent("cpp"); }
    @FXML private void showJavaGlossary() { loadContent("java"); }
    @FXML private void showPythonGlossary() { loadContent("python"); }

    // ============================================
    // ✅ Load Content
    // ============================================
    private void loadContent(String language) {
        contentArea.getChildren().clear();
        termData.clear();

        String filePath = "/glossary/" + language.toLowerCase() + ".txt";

        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                Label error = new Label("❌ Content not found: " + filePath);
                error.setStyle("-fx-text-fill: #EF4444;");
                contentArea.getChildren().add(error);
                return;
            }

            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            // ✅ Parse — Term + Full Definition + Example
            parseGlossary(content);

            // ✅ Left ListView — Terms
            termListView.getItems().clear();
            termListView.getItems().addAll(termData.keySet());

            // ✅ ListView Click — content ပြ
            termListView.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            showTermContent(newVal);
                        }
                    }
            );

            // ✅ ပထမ term auto-select
            if (!termData.isEmpty()) {
                termListView.getSelectionModel().select(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================
    // ✅ Parse — Term + Definition + Example
    // ============================================
    private void parseGlossary(String content) {
        String[] lines = content.split("\n");
        String currentTerm = null;
        List<String> currentLines = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();

            // ✅ Separator — term ပြီး
            if (trimmed.startsWith("━")) {
                if (currentTerm != null) {
                    termData.put(currentTerm, new ArrayList<>(currentLines));
                    currentTerm = null;
                    currentLines = new ArrayList<>();
                }
                continue;
            }

            // ✅ UPPERCASE line — term
            if (trimmed.matches("^[A-Z][A-Z\\s\\(\\)\\*_]+$")) {
                if (currentTerm != null) {
                    termData.put(currentTerm, new ArrayList<>(currentLines));
                }
                currentTerm = trimmed;
                currentLines = new ArrayList<>();
            } else {
                if (currentTerm != null) {
                    currentLines.add(line);
                }
            }
        }

        // ✅ Last term
        if (currentTerm != null) {
            termData.put(currentTerm, new ArrayList<>(currentLines));
        }
    }

    // ============================================
    // ✅ Show Term Content — Mimo လို
    // ============================================
    private void showTermContent(String term) {
        contentArea.getChildren().clear();

        List<String> lines = termData.get(term);
        if (lines == null) return;

        TextFlow flow = new TextFlow();
        flow.setLineSpacing(4);

        // ✅ Title — term
        Text title = new Text(term + "\n\n");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setFill(Color.web("#F8FAFC"));
        flow.getChildren().add(title);

        // ✅ Content — line အလိုက်
        for (String line : lines) {
            Text text = parseLine(line);
            flow.getChildren().add(text);
        }

        contentArea.getChildren().add(flow);
    }

    // ============================================
    // ✅ Parse Line — အရောင်
    // ============================================
    private Text parseLine(String line) {
        Text text;

        String trimmed = line.trim();

        // ✅ "Example:" — section
        if (trimmed.startsWith("Example:")) {
            text = new Text(line + "\n\n");
            text.setFont(Font.font("System", FontWeight.BOLD, 14));
            text.setFill(Color.web("#60A5FA"));         // အပြာ
        }
        // ✅ Code line — 4 space / 2 space
        else if (line.startsWith("    ") || line.startsWith("  ")) {
            text = new Text(line + "\n");
            text.setFont(Font.font("Consolas", 13));
            text.setFill(Color.web("#4ADE80"));         // ✅ အစိမ်း
        }
        // ✅ Comment line — #
        else if (trimmed.startsWith("#")) {
            text = new Text(line + "\n");
            text.setFont(Font.font("Consolas", 13));
            text.setFill(Color.web("#64748B"));         // မီးခိုး
        }
        // ✅ Normal definition
        else {
            text = new Text(line + "\n");
            text.setFont(Font.font("System", 14));
            text.setFill(Color.web("#CBD5E1"));
        }

        return text;
    }
}