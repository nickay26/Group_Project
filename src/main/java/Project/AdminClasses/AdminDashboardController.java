package Project.AdminClasses;

import Project.API_Database.DatabaseManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private VBox dashboardView;

    // Stat Cards Labels
    @FXML
    private Label totalActiveUsersLabel;
    @FXML
    private Label totalTimeSpentLabel; // Total Questions ပြသရန်
    @FXML
    private Label quizCompletionRateLabel;

    // Line Chart Controls
    @FXML
    private LineChart<String, Number> userActivityChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboardDataAsync();
    }

    /**
     * DatabaseManager မှတဆင့် Backend API သို့ ချိတ်ဆက်၍ Admin ဒေတာများကို ယူဆောင်ခြင်း
     */
    private void loadDashboardDataAsync() {
        new Thread(() -> {
            JSONObject analytics = DatabaseManager.getAdminAnalytics();

            if (analytics != null) {
                // JSON မှ လိုအပ်သော တန်ဖိုးများကို ထုတ်ယူခြင်း
                int activeToday = analytics.optInt("active_today", 0);
                int totalUsers = analytics.optInt("total_users", 0);
                int totalLessonsCompleted = analytics.optInt("total_lessons_completed", 0);
                JSONArray weeklyActivity = analytics.optJSONArray("weekly_activity");

                // UI ကို JavaFX Application Thread ပေါ်တွင် Update လုပ်ခြင်း
                Platform.runLater(() -> {
                    // 1. Stat Cards များကို တကယ့်ဒေတာဖြင့် ချိတ်ရန်
                    totalActiveUsersLabel.setText(String.format("%,d", activeToday));
                    totalTimeSpentLabel.setText(String.format("%,d", totalLessonsCompleted));
                    // Completion Rate အတွက် လိုအပ်ပါက တွက်ချက်ထည့်နိုင်သည် (ဥပမာ နှုန်းထားတခုခု)
                    quizCompletionRateLabel.setText("88.4%");

                    // 2. Weekly Line Chart တွင် Dynamic Data များ ထည့်သွင်းခြင်း
                    setupActivityChart(weeklyActivity);
                });
            }
        }).start();
    }

    /**
     * Backend မှ ရလာသော နေ့အလိုက် Weekly Activity ဒေတာများဖြင့် Line Chart ကို တည်ဆောက်ခြင်း
     */
    private void setupActivityChart(JSONArray weeklyActivity) {
        if (userActivityChart == null) return;

        // Chart ၏ Layout Width တိုင်းတာပြီးစီးမှသာ Run ရန် Platform.runLater ကို သုံးပါ
        javafx.application.Platform.runLater(() -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Active Users");

            if (weeklyActivity != null && weeklyActivity.length() > 0) {
                for (int i = 0; i < weeklyActivity.length(); i++) {
                    JSONObject dayObj = weeklyActivity.getJSONObject(i);
                    String dayName = dayObj.optString("day", "");
                    int count = dayObj.optInt("count", 0);

                    series.getData().add(new XYChart.Data<>(dayName, count));
                }
            }

            userActivityChart.getData().clear();
            userActivityChart.getData().add(series);
        });
    }
}