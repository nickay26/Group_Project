package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class PaymentController {

    @FXML private VBox kpayBox;
    @FXML private VBox waveBox;
    @FXML private Label selectedPlanLabel;
    @FXML private Label selectedPriceLabel;
    @FXML private ImageView qrImageView;
    @FXML private Label phoneLabel;
    @FXML private Label screenshotStatusLabel;

    private String selectedMethod = "KPay";
    private File uploadedScreenshot;

    private static String planType = "MONTHLY";
    private static int amount = 1500;

    public static void setPlanInfo(String type, int price) {
        planType = type;
        amount = price;
    }

    @FXML
    public void initialize() {
        selectedPlanLabel.setText("Plan: Intermediate " +
                (planType.equals("MONTHLY") ? "Monthly" : "Yearly"));
        selectedPriceLabel.setText("Amount: " + amount + " MMK");

        selectKPay();
        kpayBox.setOnMouseClicked(e -> selectKPay());
        waveBox.setOnMouseClicked(e -> selectWavePay());
    }

    private void selectKPay() {
        selectedMethod = "KPay";
        kpayBox.getStyleClass().add("payment-method-box-selected");
        waveBox.getStyleClass().remove("payment-method-box-selected");

        try {
            Image qr = new Image(getClass().getResourceAsStream("/Images/kpayqr.jpg"));
            qrImageView.setImage(qr);
        } catch (Exception e) {
            System.out.println("KPay QR not found");
        }
        phoneLabel.setText("Phone: 09-123-456-789 (KPay)");
    }

    private void selectWavePay() {
        selectedMethod = "WavePay";
        waveBox.getStyleClass().add("payment-method-box-selected");
        kpayBox.getStyleClass().remove("payment-method-box-selected");

        try {
            Image qr = new Image(getClass().getResourceAsStream("/Images/waveqr.jpg"));
            qrImageView.setImage(qr);
        } catch (Exception e) {
            System.out.println("WavePay QR not found");
        }
        phoneLabel.setText("Phone: 09-987-654-321 (WavePay)");
    }

    @FXML
    private void handleChooseScreenshot() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Payment Screenshot");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            uploadedScreenshot = file;
            screenshotStatusLabel.setText("✅ Screenshot selected: " + file.getName());
        }
    }

    @FXML
    private void handleVerifyPayment() {
        if (uploadedScreenshot == null) {
            showAlert("Error", "Please upload a payment screenshot first.");
            return;
        }

        LocalDateTime fileTime = getFileCreationTime(uploadedScreenshot);
        long minutesDiff = ChronoUnit.MINUTES.between(fileTime, LocalDateTime.now());

        if (minutesDiff > 5) {
            showAlert("Invalid Screenshot",
                    "ဒီ Screenshot က လွန်ခဲ့တဲ့ " + minutesDiff + " မိနစ်က ရိုက်ထားတာပါ။\n" +
                            "ကျေးဇူးပြု၍ ခုရိုက်ထားတဲ့ Screenshot အသစ်ကို Upload လုပ်ပါ။");
            return;
        }

        try {
            String uploadDir = "src/main/resources/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String newFileName = "payment_" + System.currentTimeMillis() + "_" + uploadedScreenshot.getName();
            File destFile = new File(uploadDir + newFileName);
            Files.copy(uploadedScreenshot.toPath(), destFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            int userId = UserSession.getUserId();
            int planId = planType.equals("MONTHLY") ? 1 : 2;
            boolean success = PremiumService.subscribePremium(userId, planId, planType);

            if (success) {
                DatabaseManager.savePaymentScreenshot(userId, newFileName, selectedMethod, amount);

                showAlert("Success",
                        "Payment Verified!\n" +
                                "Method: " + selectedMethod + "\n" +
                                "Amount: " + amount + " MMK\n" +
                                "Premium Activated: " + planType);
                MainLayoutController mainLayout = MainLayoutController.getInstance();
                if (mainLayout != null) {
                    mainLayout.loadPage("PlaygroundDashboardView.fxml");
                }
            } else {
                showAlert("Error", "Premium activation failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to process payment: " + e.getMessage());
        }
    }

    private LocalDateTime getFileCreationTime(File file) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(
                    file.toPath(), BasicFileAttributes.class
            );
            return LocalDateTime.ofInstant(attrs.creationTime().toInstant(),
                    ZoneId.systemDefault());
        } catch (Exception e) {
            return LocalDateTime.now().minusHours(1);
        }
    }

    @FXML
    private void handleBack() {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadPage("PremiumView.fxml");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}