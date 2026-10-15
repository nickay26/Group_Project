package Project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserAnalyticsController {


        @FXML
        private Label totalUsersLabel;
        @FXML private Label activeTodayLabel;
        @FXML private Label premiumUsersLabel;
        @FXML private Label lessonsCompletedLabel;

        @FXML private TableView<UserRow> userTableView;
        @FXML private TableColumn<UserRow, Integer> idCol;
        @FXML private TableColumn<UserRow, String> nameCol;
        @FXML private TableColumn<UserRow, String> emailCol;
        @FXML private TableColumn<UserRow, Integer> xpCol;
        @FXML private TableColumn<UserRow, Integer> streakCol;
        @FXML private TableColumn<UserRow, Integer> coinCol;
        @FXML private TableColumn<UserRow, String> planCol;
        @FXML private TableColumn<UserRow, String> lastLoginCol;

        @FXML
        public void initialize() {
            setupTableColumns();
            loadAnalyticsData();

            // Center-align numeric columns
            xpCol.setStyle("-fx-alignment: CENTER;");
            streakCol.setStyle("-fx-alignment: CENTER;");
            coinCol.setStyle("-fx-alignment: CENTER;");
            planCol.setStyle("-fx-alignment: CENTER;");
            lastLoginCol.setStyle("-fx-alignment: CENTER;");

// Proportional column resizing
            idCol.setMaxWidth(60);
            planCol.setMaxWidth(90);
        }

        private void setupTableColumns() {
            idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
            xpCol.setCellValueFactory(new PropertyValueFactory<>("xp"));
            streakCol.setCellValueFactory(new PropertyValueFactory<>("streak"));
            coinCol.setCellValueFactory(new PropertyValueFactory<>("coin"));
            planCol.setCellValueFactory(new PropertyValueFactory<>("planType"));
            lastLoginCol.setCellValueFactory(new PropertyValueFactory<>("lastLoginDate"));
        }

        private void loadAnalyticsData() {
            new Thread(() -> {
                JSONObject analytics = DatabaseManager.getAdminAnalytics();
                if (analytics == null) return;

                int totalUsers = analytics.optInt("total_users", 0);
                int activeToday = analytics.optInt("active_today", 0);
                int premiumUsers = analytics.optInt("premium_users", 0);
                int lessonsCompleted = analytics.optInt("total_lessons_completed", 0);

                ObservableList<UserRow> userList = FXCollections.observableArrayList();
                JSONArray usersArr = analytics.optJSONArray("users");

                if (usersArr != null) {
                    for (int i = 0; i < usersArr.length(); i++) {
                        JSONObject u = usersArr.getJSONObject(i);
                        userList.add(new UserRow(
                                u.optInt("User_ID", -1),
                                u.optString("Name", "User"),
                                u.optString("Email", "-"),
                                u.optInt("XP", 0),
                                u.optInt("Streak", 0),
                                u.optInt("coin", 0),
                                u.optString("plan_type", "FREE"),
                                u.optString("role", "USER"),
                                u.optString("last_login_date", "-")
                        ));
                    }
                }

                javafx.application.Platform.runLater(() -> {
                    if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(totalUsers));
                    if (activeTodayLabel != null) activeTodayLabel.setText(String.valueOf(activeToday));
                    if (premiumUsersLabel != null) premiumUsersLabel.setText(String.valueOf(premiumUsers));
                    if (lessonsCompletedLabel != null) lessonsCompletedLabel.setText(String.valueOf(lessonsCompleted));

                    if (userTableView != null) {
                        userTableView.setItems(userList);
                    }
                });
            }).start();
        }

        // Table Row Model
        public static class UserRow {
            private final int userId;
            private final String name;
            private final String email;
            private final int xp;
            private final int streak;
            private final int coin;
            private final String planType;
            private final String role;
            private final String lastLoginDate;

            public UserRow(int userId, String name, String email, int xp, int streak, int coin, String planType, String role, String lastLoginDate) {
                this.userId = userId;
                this.name = name;
                this.email = email;
                this.xp = xp;
                this.streak = streak;
                this.coin = coin;
                this.planType = planType;
                this.role = role;
                this.lastLoginDate = lastLoginDate;
            }

            public int getUserId() { return userId; }
            public String getName() { return name; }
            public String getEmail() { return email; }
            public int getXp() { return xp; }
            public int getStreak() { return streak; }
            public int getCoin() { return coin; }
            public String getPlanType() { return planType; }
            public String getRole() { return role; }
            public String getLastLoginDate() { return lastLoginDate; }
        }
    }
