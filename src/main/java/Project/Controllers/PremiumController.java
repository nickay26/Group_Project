package Project.Controllers;

import javafx.fxml.FXML;

public class PremiumController {

    @FXML
    private void handleStartLearning() {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.handleNavCourses();
        }
    }

    @FXML
    private void handleMonthlyPlan() {
        PaymentController.setPlanInfo("MONTHLY", 1500);
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadPage("PaymentView.fxml");
        }
    }

    @FXML
    private void handleYearlyPlan() {
        PaymentController.setPlanInfo("YEARLY", 15000);
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadPage("PaymentView.fxml");
        }
    }

    // ★ Cross Button Handler ★
    @FXML
    private void handleClose() {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.handleNavLearn();  // Learn Page ကို ပြန်သွား
        }
    }

}