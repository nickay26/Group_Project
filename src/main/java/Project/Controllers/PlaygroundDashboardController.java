package Project.Controllers;

import Project.API_Database.DatabaseManager;
import Project.Helpers.DialogUtils;
import Project.Helpers.PremiumService;
import Project.Getters_Setters.Project;
import Project.Getters_Setters.UserSession;
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
    @FXML private Button createProjectBtn;

    @FXML
    public void initialize() {
        languageSelect.setItems(FXCollections.observableArrayList("C++", "Python", "Java"));
        languageSelect.setValue("C++");
        loadUserProjects();
        updateCreateButton();
    }
    private void updateCreateButton() {
        int userId = UserSession.getUserId();
        boolean isPremium = PremiumService.isPremium(userId);

        if (isPremium) {
            createProjectBtn.setText("➕ Create New Project");
        } else {
            int remaining = PremiumService.getRemainingProjectSlots(userId);
            createProjectBtn.setText("➕ Create New Project (" + remaining + "/3)");
        }
    }

    private void showPremiumAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Premium Required");
        alert.setHeaderText("Project Limit Reached");
        alert.setContentText(
                "Free users can only create up to 3 projects.\n\n" +
                        "Want to create unlimited projects by getting Premium?"
        );

        ButtonType goPremium = new ButtonType("Go Premium");
        ButtonType cancel = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());

        alert.getButtonTypes().setAll(goPremium, cancel);

        alert.showAndWait().ifPresent(response -> {
            if (response == goPremium) {
                MainLayoutController mainLayout = MainLayoutController.getInstance();
                if (mainLayout != null) {
                    mainLayout.loadPage("PremiumView.fxml");
                }
            }
        });
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
        card.setPrefHeight(290.0); // အရွယ်အစားကို အနည်းငယ် တိုးပေးနိုင်သည်
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 16, 20, 16));
        card.setSpacing(10);

        card.getStyleClass().add("liquid-project-card");

        // 1. Language Icon Container
        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("card-icon-container");

        ImageView iconView = new ImageView();
        iconView.setFitWidth(75);
        iconView.setFitHeight(75);
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
        openBtn.setPrefHeight(34);
        openBtn.getStyleClass().add("btn-card-action");
        openBtn.setOnAction(e -> openWorkspace(project));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setPrefWidth(180);
        deleteBtn.setPrefHeight(28);
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px;");
        deleteBtn.setOnAction(e -> {
            boolean confirmed = DialogUtils.showConfirmationDialog(
                    "Delete Project",
                    "Are you sure you want to delete '" + project.getTitle() + "'?\nThis action cannot be undone."
            );

            if (confirmed) {
                boolean success = DatabaseManager.deleteProject(project.getProjectId());
                if (success) {
                    loadUserProjects(); // ဖျက်ပြီးပါက ကဒ်များကို အသစ်ပြန်လည် Load လုပ်မည်[cite: 17]
                    updateCreateButton(); // ပရောဂျက် အရေအတွက် ကိုတာ စစ်ဆေးမှုကိုပါ Update လုပ်မည်[cite: 17]
                }
            }
        });

        // Add elements to VBox
        card.getChildren().addAll(iconContainer, textContainer, openBtn, deleteBtn);
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
        int userId = UserSession.getUserId();

        if (!PremiumService.canCreateProject(userId)) {
            showPremiumAlert();
            return;
        }

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

        if (currentUserId <= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "User session expired. Please log in again.");
            alert.show();
            return;
        }

        if (!PremiumService.canCreateProject(currentUserId)) {
            closeCreateModal();
            showPremiumAlert();
            return;
        }

        String defaultCode = getBoilerplate(lang);
        Project newProject = DatabaseManager.createProject(currentUserId, title, lang, defaultCode);
        if (newProject != null) {
            closeCreateModal();
            updateCreateButton();
            openWorkspace(newProject);
        }
    }

    private void openWorkspace(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Project/CodeEditorView.fxml"));
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