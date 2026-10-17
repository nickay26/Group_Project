package Project.AdminClasses;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private VBox dashboardView;

    // Stat Cards Labels
    @FXML private Label totalActiveUsersLabel;
    @FXML private Label totalTimeSpentLabel;
    @FXML private Label quizCompletionRateLabel;

    // Line Chart Controls
    @FXML private LineChart<String, Number> userActivityChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboardData();
        setupActivityChart();
    }

    /**
     * Stat Card များအတွက် Data များကို ဖြည့်သွင်းပေးသည့် Method
     */
    private void loadDashboardData() {
        // API သို့မဟုတ် Database မှ Data ယူ၍ ပြင်ဆင်နိုင်ပါသည်
        totalActiveUsersLabel.setText("1,248");
        totalTimeSpentLabel.setText("450");
        quizCompletionRateLabel.setText("88.4%");
    }

    /**
     * User Activity Line Chart ထဲသို့ Sample Data ဖြည့်သွင်းပေးသည့် Method
     */
    private void setupActivityChart() {
        if (userActivityChart == null) return;

        // Chart Data Series ဖန်တီးခြင်း
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Active Users");

        // တစ်ပတ်စာ Sample Data များထည့်ခြင်း
        series.getData().add(new XYChart.Data<>("Mon", 850));
        series.getData().add(new XYChart.Data<>("Tue", 920));
        series.getData().add(new XYChart.Data<>("Wed", 1100));
        series.getData().add(new XYChart.Data<>("Thu", 1050));
        series.getData().add(new XYChart.Data<>("Fri", 1248));
        series.getData().add(new XYChart.Data<>("Sat", 980));
        series.getData().add(new XYChart.Data<>("Sun", 890));

        // Chart ထဲသို့ Series ထည့်သွင်းခြင်း
        userActivityChart.getData().clear();
        userActivityChart.getData().add(series);
    }
}