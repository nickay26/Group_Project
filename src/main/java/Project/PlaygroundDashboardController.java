package Project;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaygroundDashboardController {

    @FXML private StackPane playgroundRoot;
    @FXML private FlowPane projectsGrid;
    @FXML private VBox createModal;
    @FXML private TextField titleInput;
    @FXML private ComboBox<String> languageSelect;

    private final int currentUserId = 1; // Replace with logged-in User_id context

    @FXML
    public void initialize() {
        languageSelect.setItems(FXCollections.observableArrayList("C++", "Python", "Java"));
        languageSelect.setValue("C++");
        loadUserProjects();
    }

    public void loadUserProjects() {
        ApiClient.fetchUserProjectsAsync(currentUserId)
                .thenAccept(jsonResponse -> Platform.runLater(() -> {
                    List<Project> projects = JsonUtils.parseProjectsJson(jsonResponse);
                    projectsGrid.getChildren().clear();
                    for (Project p : projects) {
                        projectsGrid.getChildren().add(createProjectCard(p));
                    }
                }));
    }

    private HBox createProjectCard(Project project) {
        HBox card = new HBox();
        card.setPrefWidth(320.0);
        card.setPrefHeight(60.0);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #2FBDAE; -fx-background-radius: 10px; -fx-cursor: hand;");

        Label label = new Label("[" + project.getLanguage() + "] " + project.getTitle());
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000000;");

        card.getChildren().add(label);
        card.setOnMouseClicked(e -> openWorkspace(project));
        return card;
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

        ApiClient.createProjectAsync(currentUserId, title, lang)
                .thenAccept(jsonResponse -> Platform.runLater(() -> {
                    closeCreateModal();
                    Project newProject = JsonUtils.parseSingleProjectJson(jsonResponse);
                    if (newProject != null) {
                        openWorkspace(newProject);
                    }
                }));
    }

    private void openWorkspace(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CodeEditorView.fxml"));
            Parent workspaceView = loader.load();

            CodeEditorController controller = loader.getController();
            controller.initData(project);

            // Swap view inside the main contentArea container
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