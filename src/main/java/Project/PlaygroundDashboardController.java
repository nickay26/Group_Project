package Project;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PlaygroundDashboardController {

    @FXML private StackPane playgroundRoot;
    @FXML private FlowPane projectsGrid;
    @FXML private VBox createModal;
    @FXML private TextField titleInput;
    @FXML private ComboBox<String> languageSelect;

    @FXML
    public void initialize() {
        languageSelect.setItems(FXCollections.observableArrayList("C++", "Python", "Java"));
        languageSelect.setValue("C++");
        loadUserProjects();
    }

    public void loadUserProjects() {
        int currentUserId = UserSession.getUserId();
        if (currentUserId <= 0) return;

        List<Project> projects = DatabaseManager.getUserProjects(currentUserId);
        projectsGrid.getChildren().clear();
        for (Project p : projects) {
            projectsGrid.getChildren().add(createProjectCard(p));
        }
    }

    private VBox createProjectCard(Project project) {
        VBox card = new VBox();
        card.setPrefWidth(220.0);
        card.setPrefHeight(270.0);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 16, 20, 16));
        card.setSpacing(14);

        // Apply base Liquid CSS class
        card.getStyleClass().add("liquid-project-card");

        // 1. Language Icon Container
        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("card-icon-container");

        ImageView iconView = new ImageView();
        iconView.setFitWidth(70);
        iconView.setFitHeight(70);
        iconView.setPreserveRatio(true);

        String iconPath = getLanguageIconPath(project.getLanguage());
        try {
            InputStream is = getClass().getResourceAsStream(iconPath);
            if (is != null) {
                iconView.setImage(new Image(is));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        iconContainer.getChildren().add(iconView);

        // 2. Project Title & Language Labels
        VBox textContainer = new VBox(4);
        textContainer.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(project.getTitle());
        titleLabel.getStyleClass().add("card-title");

        Label langLabel = new Label(project.getLanguage() != null ? project.getLanguage() : "C++");
        langLabel.getStyleClass().add("card-subtitle");

        textContainer.getChildren().addAll(titleLabel, langLabel);

        // 3. Open Project Action Button
        Button openBtn = new Button("Open Project ➔");
        openBtn.setPrefWidth(180);
        openBtn.setPrefHeight(38);
        openBtn.getStyleClass().add("btn-card-action");
        openBtn.setOnAction(e -> openWorkspace(project));

        // Click anywhere on the card to open workspace
        card.setOnMouseClicked(e -> openWorkspace(project));

        // Add elements to VBox
        card.getChildren().addAll(iconContainer, textContainer, openBtn);
        return card;
    }

    private String getLanguageIconPath(String lang) {
        if (lang == null) return "/c++.png";
        return switch (lang.trim()) {
            case "Python" -> "/Images/python.png";
            case "Java" -> "/Images/java.png";
            case "C++" -> "/Images/c++.png";
            default -> "/c++.png";
        };
    }

    @FXML
    public void openCreateModal() {
        titleInput.clear();
        createModal.setVisible(true);
    }

    @FXML
    public void closeCreateModal() {
        createModal.setVisible(false);
    }

    @FXML
    public void handleCreateProject() {
        String title = titleInput.getText().trim();
        String lang = languageSelect.getValue();
        if (title.isEmpty()) return;

        int currentUserId = UserSession.getUserId();

        // ❌ User Session မရှိပါက (-1 ဖြစ်နေပါက) Project ဖန်တီးခွင့် မပြုရန် တားဆီးခြင်း
        if (currentUserId <= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "User session expired. Please log in again.");
            alert.show();
            return;
        }

        String defaultCode = getBoilerplate(lang);
        Project newProject = DatabaseManager.createProject(currentUserId, title, lang, defaultCode);
        if (newProject != null) {
            closeCreateModal();
            openWorkspace(newProject);
        }
    }

    private void openWorkspace(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CodeEditorView.fxml"));
            Parent workspaceView = loader.load();

            CodeEditorController controller = loader.getController();
            controller.initData(project);

            StackPane contentArea = (StackPane) playgroundRoot.getParent();
            if (contentArea != null) {
                contentArea.getChildren().setAll(workspaceView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getBoilerplate(String lang) {
        return switch (lang) {
            case "C++" -> "#include <iostream>\n\nint main() {\n    std::cout << \"Hello World!\";\n    return 0;\n}";
            case "Python" -> "print('Hello World!')";
            case "Java" -> "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World!\");\n    }\n}";
            default -> "";
        };
    }
}