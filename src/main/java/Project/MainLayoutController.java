package Project;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class MainLayoutController {

    private static MainLayoutController instance;

    @FXML private StackPane contentArea;
    @FXML private Label currentCourseLabel;

    @FXML private Label topStreakLabel;
    @FXML private Label topBrainLabel;
    @FXML private Label topCoinLabel;

    @FXML private Button learnBtn;
    @FXML private Button coursesBtn;
    @FXML private Button playgroundBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button practiceBtn;
    @FXML private Button gamesBtn;
    @FXML private Button profileBtn;
    private ContextMenu profileMenu;
    @FXML private Button premiumBtn;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        instance = this;
        navButtons = Arrays.asList(learnBtn, coursesBtn, playgroundBtn, leaderboardBtn, practiceBtn, gamesBtn);

        if (topBrainLabel != null) {
            topBrainLabel.setOnMouseClicked(e -> showRefillBrainDialog());
            topBrainLabel.setStyle("-fx-cursor: hand;");
        }

        updateHeaderStats();
        updateCourseTitle();

        if (UserSession.isRedirectedFromOverview()) {
            handleNavLearn();
        } else {
            handleNavCourses();
        }
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    public void setCourseTitle(String title) {
        if (currentCourseLabel != null) {
            currentCourseLabel.setText(title != null ? title : "No Active Course");
        }
    }
    public void fetchAndUpdateUserActivity() {
        updateHeaderStats();
    }
    public void updateHeaderStats() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            // 1. Local Session ထဲက လက်ရှိ Coin တန်ဖိုးကို ချက်ချင်း UI မှာ ပြပါ
            final int currentLocalCoin = currentUser.getCoin();
            Platform.runLater(() -> {
                if (topStreakLabel != null) topStreakLabel.setText(" " + currentUser.getStreak());

                // ⭐ Brain Display — Premium ဆိုရင် ∞
                if (topBrainLabel != null) {
                    String brainDisplay = PremiumService.getBrainDisplay(currentUser);
                    topBrainLabel.setText(" " + brainDisplay);

                    if (PremiumService.isPremium(currentUser)) {
                        topBrainLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-cursor: hand;");
                    } else {
                        topBrainLabel.setStyle("-fx-text-fill: white; -fx-cursor: hand;");
                    }
                }

                if (topCoinLabel != null) topCoinLabel.setText(" " + currentLocalCoin);
            });

            // 2. Async ဘက်က တန်ဖိုး ပြန်ယူသည့်အခါ Local Coin ထက် နည်းနေပါက 0 မဖြစ်စေရန် Math.max() ဖြင့် ကာကွယ်ပါ
            new Thread(() -> {
                User updatedUser = DatabaseManager.getUserById(currentUser.getUserId());
                if (updatedUser != null) {
                    // Local Coin နဲ့ DB Coin အနက် ကြီးသည့် တန်ဖိုးကိုသာ ယူမည်
                    int safeCoin = Math.max(currentLocalCoin, updatedUser.getCoin());
                    updatedUser.setCoin(safeCoin);

                    UserSession.setCurrentUser(updatedUser);

                    Platform.runLater(() -> {
                        if (topStreakLabel != null) topStreakLabel.setText(" " + updatedUser.getStreak());

                        // ⭐ Brain Display — Premium ဆိုရင် ∞
                        if (topBrainLabel != null) {
                            String brainDisplay = PremiumService.getBrainDisplay(updatedUser);
                            topBrainLabel.setText(" " + brainDisplay);

                            if (PremiumService.isPremium(updatedUser)) {
                                topBrainLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-cursor: hand;");
                            } else {
                                topBrainLabel.setStyle("-fx-text-fill: white; -fx-cursor: hand;");
                            }
                        }

                        if (topCoinLabel != null) topCoinLabel.setText(" " + safeCoin);
                    });
                }
            }).start();
        }
    }

    public void updateCourseTitle() {
        String activeCourseId = UserSession.getActiveCourseId();
        int userId = UserSession.getUserId();

        if (activeCourseId != null) {
            new Thread(() -> {
                List<Course> courses = DatabaseManager.getCoursesForUser(userId);
                if (courses != null) {
                    for (Course c : courses) {
                        if (c.getCourseId().equalsIgnoreCase(activeCourseId)) {
                            Platform.runLater(() -> setCourseTitle(c.getTitle()));
                            return;
                        }
                    }
                }
                Platform.runLater(() -> setCourseTitle("No Active Course"));
            }).start();
        } else {
            setCourseTitle("No Active Course");
        }
    }

    @FXML
    public void showRefillBrainDialog() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        // ⭐ Premium User ဆိုရင် Refill Popup မပြဘဲ Alert ပဲ ပြ
        if (PremiumService.isPremium(user)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Premium Brains");
            alert.setHeaderText("🧠 Unlimited Brains");
            alert.setContentText(
                    "You are a Premium user. Enjoy unlimited brains — no refill needed!\n\n" +
                            "Your brain count is ∞ (infinity)."
            );
            alert.showAndWait();
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox dialogContent = new VBox(16);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setStyle(
                "-fx-background-color: #0F172A; " +
                        "-fx-border-color: #334155; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 20, 0, 0, 10);"
        );

        Label titleLabel = new Label("GET MORE BRAINS! 🧠");
        titleLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label descLabel = new Label("Refill your brains to keep learning without waiting!");
        descLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");

        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #1E293B; -fx-padding: 12; -fx-background-radius: 12px;");

        Label currentCoinLbl = new Label("Your Coins: 🪙 " + user.getCoin());
        currentCoinLbl.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label currentBrainLbl = new Label("Brains: 🧠 " + user.getBrainCount() + "/5");
        currentBrainLbl.setStyle("-fx-text-fill: #EC4899; -fx-font-size: 14px; -fx-font-weight: bold;");

        infoBox.getChildren().addAll(currentCoinLbl, currentBrainLbl);

        int refillCost = 100;

        Button refillBtn = new Button("Refill 5 Brains (🪙 " + refillCost + " Coins)");
        refillBtn.setStyle(
                "-fx-background-color: #2563EB; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20 10 20; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-cursor: hand;"
        );

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        refillBtn.setOnAction(e -> {
            if (user.getBrainCount() >= 5) {
                statusLabel.setText("Your brains are already full!");
                statusLabel.setStyle("-fx-text-fill: #34D399;");
                return;
            }

            if (user.getCoin() < refillCost) {
                statusLabel.setText("Not enough coins! You need " + refillCost + " coins.");
                statusLabel.setStyle("-fx-text-fill: #F87171;");
                return;
            }

            new Thread(() -> {
                boolean success = DatabaseManager.refillBrainsWithCoins(user.getUserId(), refillCost, 5);

                Platform.runLater(() -> {
                    if (success) {
                        user.setCoin(user.getCoin() - refillCost);
                        user.setBrainCount(5);
                        updateHeaderStats();

                        statusLabel.setText("Successfully refilled brains!");
                        statusLabel.setStyle("-fx-text-fill: #34D399;");

                        new Thread(() -> {
                            try { Thread.sleep(1200); } catch (Exception ignored) {}
                            Platform.runLater(dialog::close);
                        }).start();
                    } else {
                        statusLabel.setText("Transaction failed! Please try again.");
                        statusLabel.setStyle("-fx-text-fill: #F87171;");
                    }
                });
            }).start();
        });

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748B; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());

        dialogContent.getChildren().addAll(titleLabel, descLabel, infoBox, refillBtn, statusLabel, closeBtn);

        StackPane root = new StackPane(dialogContent);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65);");

        Scene scene = new Scene(root, 420, 330);
        scene.setFill(null);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public void loadPracticeQuestion(String language, String type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Project/PracticeQuestionView.fxml"));
            Parent view = loader.load();

            PracticeQuestionController controller = loader.getController();
            List<QuestionData> list = JsonUtils.getPracticeListByType(language, type);
            QuestionData question;

            if (list == null || list.isEmpty()) {
                question = new QuestionData(
                        language, type,
                        "Complete the task below.",
                        "Write code to solve this problem.",
                        getStarterCode(language),
                        "", "Think step by step.", "",
                        null, null, "write-code", "console"
                );
            } else {
                java.util.Random rand = new java.util.Random();
                question = list.get(rand.nextInt(list.size()));
            }

            controller.setQuestion(question);
            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStarterCode(String language) {
        if (language == null) return "";
        switch (language) {
            case "Java":
                return "public class Main {\n    public static void main(String[] args) {\n        // Write your code here\n    }\n}";
            case "C++":
                return "#include <iostream>\nusing namespace std;\n\nint main() {\n    // Write your code here\n    return 0;\n}";
            case "Python":
                return "# Write your Python code here\n";
            default:
                return "";
        }
    }

    @FXML
    public void handleNavLearn() {
        setActiveButton(learnBtn);
        loadPage("LearnPage.fxml");
    }

    @FXML
    public void handleNavCourses() {
        setActiveButton(coursesBtn);
        loadPage("CoursePage.fxml");
    }

    @FXML
    public void handleNavPlayground() {
        setActiveButton(playgroundBtn);
        loadPage("PlaygroundDashboardView.fxml");
    }

    @FXML
    public void handleLeaderBoard() {
        setActiveButton(leaderboardBtn);
        loadPage("LeaderboardView.fxml");
    }

    @FXML
    public void handleNavPractice() {
        setActiveButton(practiceBtn);
        loadPage("PracticeView.fxml");
    }

    @FXML
    private void handleGame() {
        setActiveButton(gamesBtn);
        loadPage("GameModeView.fxml");
    }

    @FXML
    private void handleProfileClick() {
        if (profileMenu == null) {
            profileMenu = new ContextMenu();

            MenuItem editProfileItem = new MenuItem("Go to profile");
            MenuItem settingsItem = new MenuItem("Settings");
            MenuItem helpItem = new MenuItem("Help");
            MenuItem logoutItem = new MenuItem("Log out");

            editProfileItem.setOnAction(e -> loadPage("ProfileView.fxml"));
            settingsItem.setOnAction(e -> loadPage("SettingsView.fxml"));
            helpItem.setOnAction(e -> loadPage("HelpView.fxml"));
            logoutItem.setOnAction(e -> handleLogout());

            profileMenu.getItems().addAll(editProfileItem, settingsItem, helpItem, logoutItem);
        }

        if (profileMenu.isShowing()) {
            profileMenu.hide();
        } else {
            profileMenu.show(profileBtn, Side.BOTTOM, -30, 8);
        }
    }

    @FXML
    public void handlePremium() {
        loadPage("PremiumView.fxml");
    }

    private void handleLogout() {
        UserSession.clearSession();
        SceneManager.switchScene("WelcomeView.fxml");
    }

    public FXMLLoader loadPage(String fxmlFileName) {
        updateSidebarHighlight(fxmlFileName);

        try {
            String resourcePath = fxmlFileName.startsWith("/") ? fxmlFileName : "/Project/" + fxmlFileName;
            URL resource = getClass().getResource(resourcePath);

            if (resource == null) resource = getClass().getResource("/fxml/" + fxmlFileName);
            if (resource == null) resource = getClass().getResource("/" + fxmlFileName);

            if (resource == null) {
                throw new IOException("Could not find FXML file: " + fxmlFileName);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            // 1. Loading UI ဖန်တီးပါ
            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setStyle("-fx-progress-color: #38BDF8;");
            spinner.setMaxSize(40, 40);

            Label titleLabel = new Label("🚀 Loading...");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");

            VBox loadingCard = new VBox(12, spinner, titleLabel);
            loadingCard.setAlignment(Pos.CENTER);
            loadingCard.setPadding(new Insets(24, 32, 24, 32));
            loadingCard.setStyle(
                    "-fx-background-color: #1E293B; " +
                            "-fx-background-radius: 16px; " +
                            "-fx-border-color: #334155; " +
                            "-fx-border-radius: 16px; " +
                            "-fx-border-width: 1px;"
            );

            StackPane overlay = new StackPane(loadingCard);
            overlay.setStyle("-fx-background-color: #0F172A;");

            // 2. Loading Overlay ကို အရင်ပြပါ
            contentArea.getChildren().setAll(overlay);

            // 3. UI မျက်စိမနောက်ဘဲ Smooth ဖြစ်စေရန် 150ms အကြာတွင် Real View သို့ အစားထိုးပါ
            PauseTransition delay = new PauseTransition(Duration.millis(150));
            delay.setOnFinished(e -> contentArea.getChildren().setAll(view));
            delay.play();

            // 4. Controller ယူသုံးမည့် LessonPlayer များ မပျက်စီးစေရန် Loader ကို Synchronous ပြန်ပေးပါ
            return loader;

        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("❌ Failed to load page: " + fxmlFileName);
            errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-font-weight: bold;");
            contentArea.getChildren().setAll(errorLabel);
            return null;
        }
    }

    private void updateSidebarHighlight(String fxmlFileName) {
        if (fxmlFileName.contains("LearnPage")) {
            setActiveButton(learnBtn);
        } else if (fxmlFileName.contains("Course")) {
            setActiveButton(coursesBtn);
        } else if (fxmlFileName.contains("Playground")) {
            setActiveButton(playgroundBtn);
        } else if (fxmlFileName.contains("Leaderboard")) {
            setActiveButton(leaderboardBtn);
        } else if (fxmlFileName.contains("Practice")) {
            setActiveButton(practiceBtn);
        } else if (fxmlFileName.contains("Game")) {
            setActiveButton(gamesBtn);
        }
    }

    private void setActiveButton(Button activeButton) {
        if (navButtons == null || activeButton == null) return;

        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-button-active");
            if (!btn.getStyleClass().contains("nav-button")) {
                btn.getStyleClass().add("nav-button");
            }
        }

        activeButton.getStyleClass().remove("nav-button");
        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    public void loadPageWithWrapper(Parent wrapper) {
        if (contentArea != null) {
            contentArea.getChildren().setAll(wrapper);
        }
    }
}