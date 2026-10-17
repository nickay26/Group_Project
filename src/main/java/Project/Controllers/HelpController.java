package Project.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class HelpController {

    // ============================================
    // ✅ C++
    // ============================================
    @FXML
    private void showCppCourse() {
        loadOverviewWithBackButton("CppOverviewView.fxml");
    }

    // ============================================
    // ✅ JAVA
    // ============================================
    @FXML
    private void showJavaCourse() {
        loadOverviewWithBackButton("JavaOverviewView.fxml");
    }

    // ============================================
    // ✅ PYTHON
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

            // ✅ "Back to Welcome" — node ကို ရှာ + ဖျောက်
            hideBackToWelcome(overviewView);

            // ✅ Back Button — Help page ပြန်
            Button backBtn = new Button("← Back to Help");
            backBtn.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #60A5FA; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 5 10;"
            );
            backBtn.setOnAction(e -> {
                MainLayoutController.getInstance().loadPage("HelpView.fxml");
            });

            // ✅ Padding လျှော့ — အပေါ်တိုး
            HBox backBar = new HBox(backBtn);
            backBar.setAlignment(Pos.CENTER_LEFT);
            backBar.setStyle("-fx-padding: 5 15 0 15; -fx-background-color: #0F172A;");

            VBox wrapper = new VBox(backBar, overviewView);
            VBox.setVgrow(overviewView, Priority.ALWAYS);

            StackPane stackWrapper = new StackPane(wrapper);
            stackWrapper.setStyle("-fx-background-color: #0F172A;");
            stackWrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            MainLayoutController.getInstance().loadPageWithWrapper(stackWrapper);

        } catch (IOException e) {
            System.out.println("❌ Error loading: " + fxmlFile);
            e.printStackTrace();
        }
    }

    // ============================================
    // ✅ "Back to Welcome" — node ကို ရှာ + ဖျောက်
    // ============================================
    private void hideBackToWelcome(Parent root) {
        // ✅ Recursive — node အားလုံး ရှာ
        findAndHideBackToWelcome(root);
    }

    private void findAndHideBackToWelcome(Node node) {
        // ✅ Button — "Back to Welcome" စာသား ပါရင်
        if (node instanceof Button) {
            Button btn = (Button) node;
            String text = btn.getText();
            if (text != null && text.contains("Back to Welcome")) {
                btn.setVisible(false);
                btn.setManaged(false);
                System.out.println("✅ Hidden Back to Welcome button");
            }
        }

        // ✅ Label — "Back to Welcome" စာသား ပါရင်
        if (node instanceof Label) {
            Label lbl = (Label) node;
            String text = lbl.getText();
            if (text != null && text.contains("Back to Welcome")) {
                lbl.setVisible(false);
                lbl.setManaged(false);
                System.out.println("✅ Hidden Back to Welcome label");
            }
        }

        // ✅ Children — recursive
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                findAndHideBackToWelcome(child);
            }
        }
    }

    // ============================================
    // ✅ TUTORIALS / GLOSSARY
    // ============================================
    @FXML private void showCppTutorial() { }
    @FXML private void showJavaTutorial() { }
    @FXML private void showPythonTutorial() { }
    @FXML private void showCppGlossary() { }
    @FXML private void showJavaGlossary() { }
    @FXML private void showPythonGlossary() { }
    @FXML
    private void showTutorials() {
        MainLayoutController.getInstance().loadPage("TutorialsView.fxml");
    }

    @FXML
    private void showGlossary() {
        MainLayoutController.getInstance().loadPage("GlossaryView.fxml");
    }
    @FXML
    private void handleClose() {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.handleClosePage();
        }
    }
}