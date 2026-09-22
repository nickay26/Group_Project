package Project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class PracticeController {

    @FXML private ToggleButton practiceToggle;
    @FXML private ToggleButton reviewToggle;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private GridPane cardContainer;   // ← TilePane → GridPane

    private static class CardData {
        String language, title;
        CardData(String lang, String title) {
            this.language = lang;
            this.title = title;
        }
    }

    private final List<CardData> allCards = new ArrayList<>();

    // ကဒ်အရွယ် — ပုံသေ
    private static final double CARD_WIDTH = 280;
    private static final double CARD_HEIGHT = 260;

    @FXML
    public void initialize() {
        categoryCombo.getItems().addAll("All", "Java", "C++", "Python");
        categoryCombo.setValue("All");
        categoryCombo.setOnAction(e -> filterCards());

        allCards.add(new CardData("Java",   "Variables & Types"));
        allCards.add(new CardData("Java",   "Loops"));
        allCards.add(new CardData("Java",   "Classes & Objects"));
        allCards.add(new CardData("C++",    "Pointers"));
        allCards.add(new CardData("C++",    "Arrays"));
        allCards.add(new CardData("C++",    "Inheritance"));
        allCards.add(new CardData("Python", "Lists & Dicts"));
        allCards.add(new CardData("Python", "Functions"));
        allCards.add(new CardData("Python", "OOP Basics"));

        practiceToggle.setOnAction(e -> handleModeChange());
        reviewToggle.setOnAction(e -> handleModeChange());

        practiceToggle.setSelected(true);
        renderCards(allCards);
    }

    private void handleModeChange() {
        if (practiceToggle.isSelected()) {
            cardContainer.setVisible(true);
            cardContainer.setManaged(true);
            filterCards();
        } else if (reviewToggle.isSelected()) {
            cardContainer.getChildren().clear();
            cardContainer.setVisible(false);
            cardContainer.setManaged(false);
        }
    }

    private void filterCards() {
        String selected = categoryCombo.getValue();
        if (selected == null || selected.equals("All")) {
            renderCards(allCards);
        } else {
            List<CardData> filtered = new ArrayList<>();
            for (CardData c : allCards) {
                if (c.language.equals(selected)) filtered.add(c);
            }
            renderCards(filtered);
        }
    }

    // ============================================
    // RENDER — GridPane မှာ ၃ columns စီ
    // ============================================
    private void renderCards(List<CardData> cards) {
        cardContainer.getChildren().clear();

        int col = 0;
        int row = 0;
        for (CardData c : cards) {
            VBox card = createCard(c, CARD_WIDTH, CARD_HEIGHT);
            cardContainer.add(card, col, row);

            col++;
            if (col == 3) {       // ၃ ခုပြည့်ရင် နောက်တန်း ဆင်း
                col = 0;
                row++;
            }
        }
    }

    private VBox createCard(CardData data, double width, double height) {
        VBox card = new VBox(10);
        card.setPrefSize(width, height);
        card.setMinSize(width, height);
        card.setMaxSize(width, height);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("glass-card");
        card.setPadding(new Insets(15, 15, 15, 0));

        // Icon
        ImageView iconImage = new ImageView();
        try {
            String imagePath = getIconImage(data.language);
            if (imagePath != null) {
                java.io.InputStream is = getClass().getResourceAsStream(imagePath);
                if (is != null) {
                    Image img = new Image(is);
                    iconImage.setImage(img);
                    iconImage.setFitWidth(120);
                    iconImage.setFitHeight(120);
                    iconImage.setPreserveRatio(true);
                    iconImage.setSmooth(true);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }

        Label title = new Label(data.title);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button startBtn = new Button("Start Learning");
        startBtn.getStyleClass().add("card-start-btn");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> openQuestion(data));
        card.getChildren().addAll(iconImage, title, spacer, startBtn);
        return card;
    }
    private void openQuestion(CardData data) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Project/PracticeQuestion.fxml"));
            Parent root = loader.load();

            PracticeQuestionController controller = loader.getController();
            controller.loadQuestion(data.language, data.title);

            Stage stage = (Stage) cardContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Practice - " + data.title);
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getStarterCode(String language) {
        switch (language) {
            case "Java":
                return "public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Write your code here\n" +
                        "    }\n" +
                        "}";
            case "C++":
                return "#include <iostream>\nusing namespace std;\n\n" +
                        "int main() {\n" +
                        "    // Write your code here\n" +
                        "    return 0;\n" +
                        "}";
            case "Python":
                return "# Write your Python code here\n";
            default:
                return "";
        }
    }

    private String getIconImage(String language) {
        switch (language) {
            case "Java":   return "/Images/java.png";
            case "C++":    return "/Images/c++.png";
            case "Python": return "/Images/python.png";
            default:       return null;
        }
    }
}