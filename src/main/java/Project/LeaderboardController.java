package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.JSONArray;
import org.json.JSONObject;

public class LeaderboardController {

    @FXML private Label leagueTitleLabel;

    @FXML private VBox nebulaContainer;
    @FXML private ImageView nebulaImg;

    @FXML private VBox protostarContainer;
    @FXML private ImageView protostarImg;

    @FXML private VBox supernovaContainer;
    @FXML private ImageView supernovaImg;

    // Leaderboard List Display Containers
    @FXML private VBox placeholderContainer;
    @FXML private VBox leaderboardListContainer;
    @FXML private VBox rankingItemsVBox;

    // "SEASON_1" အစား "current" သို့ ပြောင်းလဲထားပါသည် (Backend မှ Active Week ကို အလိုအလျောက် တွက်ယူရန်)
    private final String CURRENT_SEASON = "current";
    private int currentLeagueId = 1;

    @FXML
    public void initialize() {
        loadImages();
        loadUserProgressFromBackend();
    }

    private void loadImages() {
        try {
            if (nebulaImg != null && nebulaImg.getImage() == null) {
                nebulaImg.setImage(new Image(getClass().getResourceAsStream("/Images/nebula.png")));
            }
            if (protostarImg != null && protostarImg.getImage() == null) {
                protostarImg.setImage(new Image(getClass().getResourceAsStream("/Images/Protostar.png")));
            }
            if (supernovaImg != null && supernovaImg.getImage() == null) {
                supernovaImg.setImage(new Image(getClass().getResourceAsStream("/Images/Supernova.png")));
            }
        } catch (Exception e) {
            System.err.println("Images load error: " + e.getMessage());
        }
    }

    // Backend မှ Dynamic Leaderboard Ranking ရယူ၍ UI တည်ဆောက်ခြင်း
    private void fetchAndRenderLeaderboard() {
        int currentUserId = UserSession.getUserId();

        ApiClient.getLeaderboardAsync(currentLeagueId).thenAccept(response -> {
            if (response == null || response.trim().isEmpty()) {
                Platform.runLater(() -> showPlaceholderView(true));
                return;
            }

            try {
                String trimmed = response.trim();
                JSONArray leaderboardArray = null;

                // 1. Response သည် JSON Array [...] ဖြစ်ပါက တိုက်ရိုက်ယူမည်
                if (trimmed.startsWith("[")) {
                    leaderboardArray = new JSONArray(trimmed);
                }
                // 2. Response သည် JSON Object {...} ဖြစ်ပါက အထဲမှ Array Key ကို ရှာယူမည်
                else if (trimmed.startsWith("{")) {
                    JSONObject jsonObj = new JSONObject(trimmed);
                    if (jsonObj.has("rankings")) {
                        leaderboardArray = jsonObj.getJSONArray("rankings");
                    } else if (jsonObj.has("leaderboard")) {
                        leaderboardArray = jsonObj.getJSONArray("leaderboard");
                    } else if (jsonObj.has("data")) {
                        leaderboardArray = jsonObj.getJSONArray("data");
                    } else {
                        System.err.println("Backend Response JSON: " + response);
                    }
                }

                final JSONArray finalArray = leaderboardArray;

                Platform.runLater(() -> {
                    if (finalArray == null || finalArray.length() == 0) {
                        showPlaceholderView(true);
                        return;
                    }

                    showPlaceholderView(false);
                    rankingItemsVBox.getChildren().clear();

                    for (int i = 0; i < finalArray.length(); i++) {
                        JSONObject player = finalArray.getJSONObject(i);
                        int rank = i + 1;
                        int userId = player.optInt("User_ID", player.optInt("user_id", -1));
                        String name = player.optString("Name", player.optString("name", "User"));
                        // Backend မှ ရရှိသော weekly_xp Key ကို ဦးစားပေး ရယူပါသည်
                        int xp = player.optInt("weekly_xp", player.optInt("XP", player.optInt("xp", 0)));

                        boolean isCurrentUser = (userId == currentUserId);

                        HBox row = createRankRowCard(rank, name, xp, isCurrentUser);
                        rankingItemsVBox.getChildren().add(row);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error rendering leaderboard: " + e.getMessage());
                Platform.runLater(() -> showPlaceholderView(true));
            }
        });
    }

    // Rank 1 တိုင်းအတွက် Dynamic Row UI ဖန်တီးပေးသည့် Helper Method
    private HBox createRankRowCard(int rank, String name, int xp, boolean isCurrentUser) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));

        if (isCurrentUser) {
            row.setStyle("-fx-background-color: #2B3A60; -fx-background-radius: 10px; -fx-border-color: #3182CE; -fx-border-radius: 10px; -fx-border-width: 1.5px;");
        } else {
            row.setStyle("-fx-background-color: #252F4A; -fx-background-radius: 10px;");
        }

        Label rankLabel = new Label();
        rankLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        if (rank == 1) {
            rankLabel.setText("🥇 1");
            rankLabel.setTextFill(Color.web("#FFD700"));
        } else if (rank == 2) {
            rankLabel.setText("🥈 2");
            rankLabel.setTextFill(Color.web("#C0C0C0"));
        } else if (rank == 3) {
            rankLabel.setText("🥉 3");
            rankLabel.setTextFill(Color.web("#CD7F32"));
        } else {
            rankLabel.setText("#" + rank);
            rankLabel.setTextFill(Color.web("#A0AEC0"));
        }
        rankLabel.setPrefWidth(45);

        Label nameLabel = new Label(name + (isCurrentUser ? " (You)" : ""));
        nameLabel.setFont(Font.font("System", isCurrentUser ? FontWeight.BOLD : FontWeight.NORMAL, 14));
        nameLabel.setTextFill(Color.WHITE);

        // Spacer to push XP label to the right edge
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label xpLabel = new Label(xp + " XP");
        xpLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        xpLabel.setTextFill(Color.web("#63B3ED"));

        row.getChildren().addAll(rankLabel, nameLabel, spacer, xpLabel);
        return row;
    }

    private void showPlaceholderView(boolean showPlaceholder) {
        if (placeholderContainer != null && leaderboardListContainer != null) {
            placeholderContainer.setVisible(showPlaceholder);
            placeholderContainer.setManaged(showPlaceholder);

            leaderboardListContainer.setVisible(!showPlaceholder);
            leaderboardListContainer.setManaged(!showPlaceholder);
        }
    }

    private void loadUserProgressFromBackend() {
        int userId = UserSession.getUserId();
        String activeCourseId = UserSession.getActiveCourseId();

        if (userId <= 0) {
            updateUserProgress(0);
            return;
        }

        // Single API call to prevent duplicate network calls and race conditions
        ApiClient.getUserActivityAsync(userId).thenAccept(response -> {
            int userLevel = DatabaseManager.getCompletedLessonCount(userId, activeCourseId);

            if (response != null && !response.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(response);
                    userLevel = json.optInt("level", userLevel);
                } catch (Exception e) {
                    System.err.println("Error parsing user level: " + e.getMessage());
                }
            }

            final int finalLevel = userLevel;
            Platform.runLater(() -> updateUserProgress(finalLevel));
        });
    }

    public void updateUserProgress(int userLevel) {
        int NEBULA_LEVEL = 1;
        int PROTOSTAR_LEVEL = 5;
        int SUPERNOVA_LEVEL = 10;

        // Calculate League ID unconditionally before updating UI
        if (userLevel >= SUPERNOVA_LEVEL) {
            currentLeagueId = 3;
            if (leagueTitleLabel != null) leagueTitleLabel.setText("Supernova League");
        } else if (userLevel >= PROTOSTAR_LEVEL) {
            currentLeagueId = 2;
            if (leagueTitleLabel != null) leagueTitleLabel.setText("Protostar League");
        } else {
            currentLeagueId = 1;
            if (leagueTitleLabel != null) leagueTitleLabel.setText("Nebula League");
        }

        // 🌟 အသုံးပြုသူ လက်ရှိရောက်နေသော League Container ကိုသာ ပေါ်စေပြီး ကျန်သည်များကို ဖုံးကွယ်ခြင်း
        if (nebulaContainer != null) {
            boolean isNebula = (currentLeagueId == 1);
            nebulaContainer.setVisible(isNebula);
            nebulaContainer.setManaged(isNebula);
        }
        if (protostarContainer != null) {
            boolean isProtostar = (currentLeagueId == 2);
            protostarContainer.setVisible(isProtostar);
            protostarContainer.setManaged(isProtostar);
        }
        if (supernovaContainer != null) {
            boolean isSupernova = (currentLeagueId == 3);
            supernovaContainer.setVisible(isSupernova);
            supernovaContainer.setManaged(isSupernova);
        }

        // Send updated league and fetch correct leaderboard
        ApiClient.addUserXpAsync(UserSession.getUserId(), null, 0, currentLeagueId);
        fetchAndRenderLeaderboard();
    }

    private void setCardState(ImageView img, boolean isUnlocked) {
        if (img != null) {
            img.setOpacity(isUnlocked ? 1.0 : 0.0);
        }
    }

    @FXML
    private void handleStartLearning() {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPage("LearnPage.fxml");
        }
    }
}