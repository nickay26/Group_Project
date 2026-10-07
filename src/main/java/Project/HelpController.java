package Project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class HelpController {

    // ============================================
    // ✅ C++ — Overview + Back to Help
    // ============================================
    @FXML
    private void showCppCourse() {
        loadOverviewWithBackButton("CppOverviewView.fxml");
    }

    // ============================================
    // ✅ JAVA — Overview + Back to Help
    // ============================================
    @FXML
    private void showJavaCourse() {
        loadOverviewWithBackButton("JavaOverviewView.fxml");
    }

    // ============================================
    // ✅ PYTHON — Overview + Back to Help
    // ============================================
    @FXML
    private void showPythonCourse() {
        loadOverviewWithBackButton("PythonOverviewView.fxml");
    }

    // ============================================
    // ✅ HELPER — Overview + Back Button
    // ============================================
    private void loadOverviewWithBackButton(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent overviewView = loader.load();

            // ✅ Back Button — Help page ပြန်
            Button backBtn = new Button("← Back to Help");
            backBtn.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #60A5FA; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 15;"
            );
            backBtn.setOnAction(e -> {
                MainLayoutController.getInstance().loadPage("HelpView.fxml");
            });

            HBox backBar = new HBox(backBtn);
            backBar.setAlignment(Pos.CENTER_LEFT);
            backBar.setStyle("-fx-padding: 20 30 0 30; -fx-background-color: #0F172A;");

            VBox wrapper = new VBox(backBar, overviewView);
            VBox.setVgrow(overviewView, Priority.ALWAYS);
            wrapper.setStyle("-fx-background-color: #0F172A;");

            // ✅ MainLayoutController ထဲ ထည့်
            MainLayoutController.getInstance().loadPageWithWrapper(wrapper);

        } catch (IOException e) {
            System.out.println("❌ Error loading: " + fxmlFile);
            e.printStackTrace();
        }
    }

    // ============================================
    // ✅ TUTORIALS
    // ============================================
    @FXML private void showCppTutorial() { }
    @FXML private void showJavaTutorial() { }
    @FXML private void showPythonTutorial() { }

    // ============================================
    // ✅ GLOSSARY
    // ============================================
    @FXML private void showCppGlossary() { }
    @FXML private void showJavaGlossary() { }
    @FXML private void showPythonGlossary() { }
}