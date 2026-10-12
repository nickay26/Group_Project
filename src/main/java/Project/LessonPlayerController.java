package Project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static Project.ApiClient.cleanAndFormatAiText;

public class LessonPlayerController {

    @FXML private Button closeBtn;
    @FXML private ProgressBar progressBar;
    @FXML private StackPane cardContainer;
    @FXML private Button continueBtn;

    private JSONArray slides;
    private int currentSlideIndex = 0;
    private Lesson currentLesson;

    private int totalQuestionCount = 0;
    private int correctAnswerCount = 0;

    private String selectedAnswer = null;
    private boolean isAnswerChecked = false;
    private VBox feedbackBox;
    private VBox optionsBox;

    private String originalCodeSnippet = null;
    private String originalPrompt = null;
    private TextArea interactiveCodeEditor = null;
    private Label staticCodeLabel = null;
    private Label currentPromptLabel = null;

    // ⭐ AI Daily Limit for Free Users
    private static final int FREE_DAILY_AI_LIMIT = 5;

    @FXML
    public void initialize() {
        if (continueBtn != null) {
            continueBtn.setOnMouseEntered(e ->
                    continueBtn.setStyle("-fx-background-color: #1D4ED8; -fx-text-fill: #FFFFFF; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-cursor: hand;")
            );
            continueBtn.setOnMouseExited(e ->
                    continueBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: #FFFFFF; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-cursor: hand;")
            );
        }

        if (closeBtn != null) {
            closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-cursor: hand;"));
            closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #94A3B8; -fx-cursor: hand;"));
        }
    }

    public void startLesson(Lesson lesson) {
        this.currentLesson = lesson;
        startLessonById(lesson.getLessonId());
    }

    public void startLesson(String lessonId) {
        startLessonById(lessonId);
    }

    private void startLessonById(String lessonId) {
        // 🧠 Check if user is out of energy before starting
        User user = UserSession.getCurrentUser();
        if (user != null && !user.isPremium() && user.getBrainCount() <= 0) {
            showOutOfBrainsDialog();
            return;
        }

        InputStream is = getClass().getResourceAsStream("/lessons/" + lessonId + ".json");
        if (is == null) {
            is = getClass().getResourceAsStream("/Project/lessons/" + lessonId + ".json");
        }

        if (is == null) {
            renderErrorCard("Lesson file not found: " + lessonId);
            return;
        }

        try {
            String jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject lessonJson = new JSONObject(jsonText);
            this.slides = lessonJson.getJSONArray("slides");
            this.currentSlideIndex = 0;

            this.totalQuestionCount = 0;
            this.correctAnswerCount = 0;

            for (int i = 0; i < slides.length(); i++) {
                JSONObject s = slides.getJSONObject(i);
                if (s.has("correctAnswer") && !s.isNull("correctAnswer")) {
                    totalQuestionCount++;
                }
            }

            renderCurrentSlide();

        } catch (Exception e) {
            e.printStackTrace();
            renderErrorCard("Failed to load lesson content.");
        }
    }

    private void renderCurrentSlide() {
        if (slides == null || currentSlideIndex >= slides.length()) {
            handleLessonCompletion();
            return;
        }

        selectedAnswer = null;
        isAnswerChecked = false;
        interactiveCodeEditor = null;
        staticCodeLabel = null;

        if (progressBar != null) {
            double progress = (double) (currentSlideIndex + 1) / slides.length();
            progressBar.setProgress(progress);
        }

        cardContainer.getChildren().clear();

        JSONObject slide = slides.getJSONObject(currentSlideIndex);
        String title = slide.optString("title", "");
        originalPrompt = slide.optString("prompt", "");
        originalCodeSnippet = slide.optString("codeSnippet", slide.optString("starterCode", null));
        String language = slide.optString("language", "cpp");

        boolean hasQuestion = slide.has("correctAnswer") && !slide.isNull("correctAnswer");
        boolean isInteractive = slide.optBoolean("interactive", false) || slide.optBoolean("isInteractive", false);

        if (continueBtn != null) {
            continueBtn.setText(hasQuestion ? "CHECK" : "CONTINUE");
            continueBtn.setDisable(hasQuestion);
        }

        VBox cardBox = new VBox(14);
        cardBox.setAlignment(Pos.TOP_LEFT);
        cardBox.setPadding(new Insets(24));
        cardBox.setMaxWidth(680);
        cardBox.setStyle(
                "-fx-background-color: #1E293B; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-border-color: #334155; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-border-width: 1.5px;"
        );

        String badgeText = isInteractive ? "INTERACTIVE CODE" : (hasQuestion ? "QUESTION" : "CONCEPT");
        String badgeBg = isInteractive ? "#10B98122;" : (hasQuestion ? "#7C3AED22;" : "#2563EB22;");
        String badgeFg = isInteractive ? "#34D399;" : (hasQuestion ? "#C084FC;" : "#60A5FA;");

        Label typeBadge = new Label(badgeText);
        typeBadge.setStyle(
                "-fx-background-color: " + badgeBg +
                        "-fx-text-fill: " + badgeFg +
                        "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-background-radius: 8px;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        titleLabel.setWrapText(true);

        currentPromptLabel = new Label(originalPrompt);
        currentPromptLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #94A3B8; -fx-line-spacing: 3px;");
        currentPromptLabel.setWrapText(true);

        cardBox.getChildren().addAll(typeBadge, titleLabel, currentPromptLabel);

        if (originalCodeSnippet != null && !originalCodeSnippet.isEmpty()) {
            if (isInteractive) {
                renderInteractiveCodeSpace(cardBox, slide, originalCodeSnippet);
            } else {
                staticCodeLabel = new Label(originalCodeSnippet);
                staticCodeLabel.setWrapText(true);
                staticCodeLabel.setMinHeight(Region.USE_COMPUTED_SIZE);
                staticCodeLabel.getStyleClass().add("code-snippet-box");
                staticCodeLabel.setMaxWidth(Double.MAX_VALUE);
                cardBox.getChildren().add(staticCodeLabel);
            }
        }

        if (!hasQuestion && !isInteractive) {
            renderConceptAiExplainer(cardBox, language, originalCodeSnippet != null ? originalCodeSnippet : originalPrompt);
        }

        if (slide.has("options")) {
            JSONArray optionsJson = slide.getJSONArray("options");
            List<String> optionsList = new ArrayList<>();
            for (int i = 0; i < optionsJson.length(); i++) {
                optionsList.add(optionsJson.getString(i));
            }
            Collections.shuffle(optionsList);

            optionsBox = new VBox(10);

            for (String optionText : optionsList) {
                Button optionBtn = new Button(optionText);
                optionBtn.setMaxWidth(Double.MAX_VALUE);
                optionBtn.setMinHeight(42);
                optionBtn.setWrapText(true);
                optionBtn.getStyleClass().add("option-button");

                optionBtn.setOnAction(e -> {
                    if (isAnswerChecked) return;

                    selectedAnswer = optionText;
                    if (continueBtn != null) continueBtn.setDisable(false);

                    if (originalCodeSnippet != null && originalCodeSnippet.contains("___")) {
                        String filledCode = originalCodeSnippet.replace("___", optionText);
                        if (interactiveCodeEditor != null) {
                            interactiveCodeEditor.setText(filledCode);
                        }
                        if (staticCodeLabel != null) {
                            staticCodeLabel.setText(filledCode);
                        }
                    }

                    if (originalPrompt != null && originalPrompt.contains("___") && currentPromptLabel != null) {
                        currentPromptLabel.setText(originalPrompt.replace("___", optionText));
                    }

                    for (Node node : optionsBox.getChildren()) {
                        if (node instanceof Button) {
                            Button btn = (Button) node;
                            btn.getStyleClass().remove("option-button-selected");
                            if (!btn.getStyleClass().contains("option-button")) {
                                btn.getStyleClass().add("option-button");
                            }
                        }
                    }
                    optionBtn.getStyleClass().add("option-button-selected");
                });

                optionsBox.getChildren().add(optionBtn);
            }
            cardBox.getChildren().add(optionsBox);
        }

        feedbackBox = new VBox(6);
        feedbackBox.setPadding(new Insets(12));
        feedbackBox.setVisible(false);
        cardBox.getChildren().add(feedbackBox);

        ScrollPane scrollPane = new ScrollPane(cardBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMaxWidth(700);
        scrollPane.setStyle(
                "-fx-background: transparent; " +
                        "-fx-background-color: transparent; " +
                        "-fx-padding: 0;"
        );

        cardContainer.getChildren().add(scrollPane);
    }

    private void handleLessonCompletion() {
        if (continueBtn != null) {
            continueBtn.setDisable(true);
            continueBtn.setText("SAVING...");
        }

        new Thread(() -> {
            if (currentLesson != null) {
                int userId = UserSession.getUserId();
                String courseId = UserSession.getActiveCourseId();

                int earnedXP = currentLesson.getXp();
                int earnedCoins = 10;

                if (totalQuestionCount > 0) {
                    double percentage = ((double) correctAnswerCount / totalQuestionCount) * 100.0;
                    if (percentage >= 50.0) {
                        earnedCoins = 10;
                    } else {
                        earnedCoins = 5;
                    }
                }

                User currentUser = UserSession.getCurrentUser();
                if (currentUser != null) {
                    currentUser.setCoin(currentUser.getCoin() + earnedCoins);
                    currentUser.setXp(currentUser.getXp() + earnedXP);
                    UserSession.setCurrentUser(currentUser);
                }

                DatabaseManager.completeLessonAndAddXP(
                        userId,
                        courseId,
                        currentLesson.getLessonOrder(),
                        earnedXP,
                        earnedCoins
                );
            }

            Platform.runLater(() -> {
                if (MainLayoutController.getInstance() != null) {
                    MainLayoutController.getInstance().updateHeaderStats();
                    MainLayoutController.getInstance().loadPage("LearnPage.fxml");
                }
            });
        }).start();
    }

    private void renderConceptAiExplainer(VBox cardBox, String language, String codeContent) {
        Button deepDiveBtn = new Button("✨ Deep Dive with AI");
        deepDiveBtn.setStyle(
                "-fx-background-color: #312E81; " +
                        "-fx-text-fill: #A5B4FC; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 6 12 6 12; " +
                        "-fx-cursor: hand;"
        );

        VBox aiOutputBox = new VBox(8);
        aiOutputBox.setPadding(new Insets(12));
        aiOutputBox.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 10px; -fx-border-color: #4338CA; -fx-border-radius: 10px;");
        aiOutputBox.setManaged(false);
        aiOutputBox.setVisible(false);

        Label aiLabel = new Label();
        aiLabel.setWrapText(true);
        aiLabel.setStyle("-fx-text-fill: #E0E7FF; -fx-font-size: 13px; -fx-line-spacing: 2px;");
        aiOutputBox.getChildren().add(aiLabel);

        deepDiveBtn.setOnAction(e -> {
            if (!canAskAI()) {
                showAILimitDialog();
                return;
            }
            recordAIAsk();

            deepDiveBtn.setDisable(true);
            deepDiveBtn.setText("✨ AI Thinking...");

            ApiClient.reviewCodeAsync(language, codeContent).thenAccept(response -> {
                Platform.runLater(() -> {
                    deepDiveBtn.setDisable(false);
                    deepDiveBtn.setText("✨ Refresh AI Breakdown");
                    if (response != null && !response.isEmpty()) {
                        String cleanedResponse = cleanAndFormatAiText(response);
                        aiLabel.setText(cleanedResponse);
                    } else {
                        aiLabel.setText("Unable to fetch AI explanation. Please check your internet connection.");
                    }
                });
            });
        });

        cardBox.getChildren().addAll(deepDiveBtn, aiOutputBox);
    }

    private void renderInteractiveCodeSpace(VBox parentCard, JSONObject slide, String starterCode) {
        VBox codeContainer = new VBox(8);
        codeContainer.setStyle("-fx-background-color: #0F172A; -fx-padding: 12; -fx-background-radius: 12px; -fx-border-color: #334155; -fx-border-radius: 12px;");

        Label editorLabel = new Label("💻 Interactive Code Playground");
        editorLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-font-weight: bold;");

        interactiveCodeEditor = new TextArea(starterCode);
        interactiveCodeEditor.setPrefRowCount(8);
        interactiveCodeEditor.setWrapText(true);
        interactiveCodeEditor.setStyle(
                "-fx-control-inner-background: #020617; " +
                        "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                        "-fx-font-size: 13px; " +
                        "-fx-text-fill: #E2E8F0; " +
                        "-fx-highlight-fill: #334155;"
        );

        HBox actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER_RIGHT);

        Button runBtn = new Button("▶ Run Code");
        runBtn.setStyle(
                "-fx-background-color: #10B981; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 6 16 6 16; " +
                        "-fx-cursor: hand;"
        );
        actionBar.getChildren().add(runBtn);

        Label consoleHeader = new Label("Terminal Output:");
        consoleHeader.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px; -fx-font-weight: bold;");

        TextArea consoleOutput = new TextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setPrefRowCount(4);
        consoleOutput.setPromptText("Click 'Run Code' to execute your program...");
        consoleOutput.setStyle(
                "-fx-control-inner-background: #020617; " +
                        "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                        "-fx-font-size: 12px; " +
                        "-fx-text-fill: #38BDF8;"
        );

        Button explainErrorBtn = new Button("💡 Explain Error with AI");
        explainErrorBtn.setStyle(
                "-fx-background-color: #7C2D12; " +
                        "-fx-text-fill: #FFEDD5; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 4 10 4 10; " +
                        "-fx-cursor: hand;"
        );
        explainErrorBtn.setManaged(false);
        explainErrorBtn.setVisible(false);

        explainErrorBtn.setOnAction(e -> {
            if (!canAskAI()) {
                showAILimitDialog();
                return;
            }
            recordAIAsk();

            explainErrorBtn.setDisable(true);
            explainErrorBtn.setText("💡 AI Analyzing Error...");

            String rawCode = interactiveCodeEditor.getText();
            String lang = slide.optString("language", "cpp");
            String fullContext = "Code:\n" + rawCode + "\n\nTerminal Error Output:\n" + consoleOutput.getText();

            ApiClient.reviewPracticeCodeAsync(lang, fullContext, "Explain why this code errored and how to fix it.").thenAccept(aiExplanation -> {
                Platform.runLater(() -> {
                    explainErrorBtn.setDisable(false);
                    explainErrorBtn.setText("💡 Explain Error with AI");
                    if (aiExplanation != null && !aiExplanation.isEmpty()) {
                        String cleanedExplanation = cleanAndFormatAiText(aiExplanation);
                        consoleOutput.setText("=== AI ERROR EXPLANATION ===\n" + cleanedExplanation);
                        consoleOutput.setStyle("-fx-control-inner-background: #1C1917; -fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px; -fx-text-fill: #FDBA74;");
                    }
                });
            });
        });

        runBtn.setOnAction(e -> {
            runBtn.setDisable(true);
            runBtn.setText("Executing...");
            consoleOutput.setText("Sending request to server...");
            explainErrorBtn.setManaged(false);
            explainErrorBtn.setVisible(false);

            String rawCode = interactiveCodeEditor.getText();
            String lang = slide.optString("language", "cpp");
            String executableCode = wrapInBoilerplateIfNeeded(rawCode, lang);

            ApiClient.executeCodeAsync(lang, executableCode).thenAccept(responseBody -> {
                Platform.runLater(() -> {
                    runBtn.setDisable(false);
                    runBtn.setText("▶ Run Code");

                    if (responseBody == null || responseBody.isEmpty()) {
                        consoleOutput.setText("Error: Connection failed.");
                        consoleOutput.setStyle("-fx-control-inner-background: #450A0A; -fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px; -fx-text-fill: #FCA5A5;");
                        return;
                    }

                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        String outputText = responseJson.optString("output",
                                responseJson.optString("result",
                                        responseJson.optString("error", responseBody)));

                        if (outputText.trim().isEmpty()) {
                            outputText = "[Program executed successfully with no output]";
                        }

                        boolean hasError = outputText.toLowerCase().contains("error") || outputText.toLowerCase().contains("exception");
                        if (hasError) {
                            consoleOutput.setStyle("-fx-control-inner-background: #450A0A; -fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px; -fx-text-fill: #FCA5A5;");
                            explainErrorBtn.setManaged(true);
                            explainErrorBtn.setVisible(true);
                        } else {
                            consoleOutput.setStyle("-fx-control-inner-background: #020617; -fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px; -fx-text-fill: #38BDF8;");
                        }

                        consoleOutput.setText(outputText);

                    } catch (Exception parseEx) {
                        consoleOutput.setText(responseBody);
                        consoleOutput.setStyle("-fx-control-inner-background: #020617; -fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px; -fx-text-fill: #38BDF8;");
                    }
                });
            });
        });

        HBox consoleTitleBar = new HBox(10, consoleHeader, explainErrorBtn);
        consoleTitleBar.setAlignment(Pos.CENTER_LEFT);

        codeContainer.getChildren().addAll(editorLabel, interactiveCodeEditor, actionBar, consoleTitleBar, consoleOutput);
        parentCard.getChildren().add(codeContainer);
    }

    private String wrapInBoilerplateIfNeeded(String code, String language) {
        if (code == null) return "";
        String lowerLang = language.toLowerCase();

        if (lowerLang.contains("cpp") || lowerLang.contains("c++") || lowerLang.contains("c")) {
            if (!code.contains("main")) {
                return "#include <iostream>\n#include <string>\nusing namespace std;\n\nint main() {\n    "
                        + code.replace("\n", "\n    ")
                        + "\n    return 0;\n}";
            }
        }

        if (lowerLang.contains("java")) {
            if (!code.contains("class") && !code.contains("main")) {
                return "public class Main {\n    public static void main(String[] args) {\n        "
                        + code.replace("\n", "\n        ")
                        + "\n    }\n}";
            }
        }

        return code;
    }

    @FXML
    private void handleContinue() {
        if (slides == null || currentSlideIndex >= slides.length()) {
            handleLessonCompletion();
            return;
        }

        JSONObject slide = slides.getJSONObject(currentSlideIndex);
        boolean hasQuestion = slide.has("correctAnswer") && !slide.isNull("correctAnswer");

        if (!hasQuestion) {
            currentSlideIndex++;
            renderCurrentSlide();
            return;
        }

        if (!isAnswerChecked) {
            String correctAnswer = slide.getString("correctAnswer");
            String explanation = slide.optString("explanation", "");

            boolean isCorrect = selectedAnswer != null && selectedAnswer.trim().equalsIgnoreCase(correctAnswer.trim());

            if (isCorrect) {
                correctAnswerCount++;
            } else {
                // 🧠 DEDUCT BRAIN ON WRONG ANSWER
                User user = UserSession.getCurrentUser();
                if (user != null && !user.isPremium()) {
                    UserSession.deductOneBrain(); // Decrements RAM & calls ApiClient.deductBrainAsync

                    if (MainLayoutController.getInstance() != null) {
                        MainLayoutController.getInstance().updateHeaderStats();
                    }
                }
            }

            if (optionsBox != null) {
                for (Node node : optionsBox.getChildren()) {
                    if (node instanceof Button) {
                        Button btn = (Button) node;
                        btn.setDisable(true);

                        String btnText = btn.getText().trim();

                        if (btnText.equalsIgnoreCase(correctAnswer.trim())) {
                            btn.setStyle("-fx-background-color: #064E3B; -fx-text-fill: #34D399; -fx-border-color: #059669; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-padding: 10 16 10 16; -fx-opacity: 1.0;");
                        } else if (selectedAnswer != null && btnText.equalsIgnoreCase(selectedAnswer.trim()) && !isCorrect) {
                            btn.setStyle("-fx-background-color: #7F1D1D; -fx-text-fill: #FCA5A5; -fx-border-color: #DC2626; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-padding: 10 16 10 16; -fx-opacity: 1.0;");
                        } else {
                            btn.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #64748B; -fx-border-color: #1E293B; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-size: 14px; -fx-alignment: CENTER-LEFT; -fx-padding: 10 16 10 16; -fx-opacity: 0.5;");
                        }
                    }
                }
            }

            if (isCorrect) {
                showFeedback(true, "Correct! Great job.", explanation);
            } else {
                showFeedback(false, "Incorrect! Correct answer: " + correctAnswer, explanation);

                // 🧠 Check if user ran out of brains
                User user = UserSession.getCurrentUser();
                if (user != null && !user.isPremium() && user.getBrainCount() <= 0) {
                    isAnswerChecked = true;
                    if (continueBtn != null) continueBtn.setText("CONTINUE");
                    showOutOfBrainsDialog();
                    return;
                }
            }

            isAnswerChecked = true;
            if (continueBtn != null) continueBtn.setText("CONTINUE");
            return;
        }

        currentSlideIndex++;
        renderCurrentSlide();
    }

    private void showFeedback(boolean isCorrect, String message, String explanation) {
        if (feedbackBox == null) return;

        feedbackBox.getChildren().clear();
        feedbackBox.setVisible(true);

        String bgColor = isCorrect ? "#064E3B" : "#7F1D1D";
        String borderColor = isCorrect ? "#059669" : "#DC2626";
        String titleColor = isCorrect ? "#34D399" : "#FCA5A5";
        String textColor = isCorrect ? "#A7F3D0" : "#FECACA";

        feedbackBox.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-border-width: 1.5px;",
                bgColor, borderColor
        ));

        Label titleLabel = new Label((isCorrect ? "✓ " : "✕ ") + message);
        titleLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 14px;", titleColor));
        feedbackBox.getChildren().add(titleLabel);

        if (explanation != null && !explanation.isEmpty()) {
            Label expLabel = new Label(explanation);
            expLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 13px;", textColor));
            expLabel.setWrapText(true);
            feedbackBox.getChildren().add(expLabel);
        }

        if (!isCorrect) {
            Button askAiHintBtn = new Button("💡 Ask AI Why My Answer Was Wrong");
            askAiHintBtn.setStyle(
                    "-fx-background-color: #991B1B; " +
                            "-fx-text-fill: #FEF2F2; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 6px; " +
                            "-fx-cursor: hand;"
            );

            Label aiHintResult = new Label();
            aiHintResult.setWrapText(true);
            aiHintResult.setStyle("-fx-text-fill: #FDE8E8; -fx-font-size: 12px; -fx-padding: 4 0 0 0;");

            JSONObject slide = slides.getJSONObject(currentSlideIndex);
            String lang = slide.optString("language", "cpp");

            askAiHintBtn.setOnAction(e -> {
                if (!canAskAI()) {
                    showAILimitDialog();
                    return;
                }
                recordAIAsk();

                askAiHintBtn.setDisable(true);
                askAiHintBtn.setText("💡 AI Thinking...");

                String taskPrompt = "Question Prompt: " + originalPrompt +
                        " | User Selected Wrong Answer: " + selectedAnswer +
                        " | Explain gently why this choice is incorrect.";

                ApiClient.reviewPracticeCodeAsync(lang, selectedAnswer, taskPrompt).thenAccept(hint -> {
                    Platform.runLater(() -> {
                        askAiHintBtn.setDisable(false);
                        askAiHintBtn.setText("💡 Ask AI Why My Answer Was Wrong");
                        if (hint != null && !hint.isEmpty()) {
                            String cleanedHint = cleanAndFormatAiText(hint);
                            aiHintResult.setText("AI Feedback:\n" + cleanedHint);
                        }
                    });
                });
            });

            feedbackBox.getChildren().addAll(askAiHintBtn, aiHintResult);
        }
    }

    @FXML
    private void handleClose() {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPage("LearnPage.fxml");
        }
    }

    private void renderErrorCard(String message) {
        if (cardContainer != null) {
            cardContainer.getChildren().clear();
            Label errorLabel = new Label(message);
            errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 16px; -fx-font-weight: bold;");
            cardContainer.getChildren().add(errorLabel);
        }
    }

    private boolean canAskAI() {
        User user = UserSession.getCurrentUser();
        if (user == null) return false;

        if (PremiumService.isPremium(user)) return true;

        int todayCount = DatabaseManager.getTodayAIAskCount(user.getUserId());
        return todayCount < FREE_DAILY_AI_LIMIT;
    }

    private void recordAIAsk() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;
        if (PremiumService.isPremium(user)) return;

        DatabaseManager.incrementAIAskCount(user.getUserId());
    }

    private void showAILimitDialog() {
        User user = UserSession.getCurrentUser();
        int todayCount = user != null ? DatabaseManager.getTodayAIAskCount(user.getUserId()) : 0;
        int remaining = Math.max(0, FREE_DAILY_AI_LIMIT - todayCount);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Daily AI Limit");
        alert.setHeaderText("🚀 You've reached today's AI limit!");

        if (remaining <= 0) {
            alert.setContentText(
                    "Free users can ask AI " + FREE_DAILY_AI_LIMIT + " times per day.\n\n" +
                            "Upgrade to Premium for UNLIMITED AI questions, " +
                            "unlimited brains, and more!"
            );
        } else {
            alert.setContentText("You have " + remaining + " AI questions left today.");
        }

        ButtonType upgradeBtn = new ButtonType("Upgrade to Premium", ButtonBar.ButtonData.OK_DONE);
        ButtonType laterBtn = new ButtonType("Maybe Later", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(upgradeBtn, laterBtn);

        alert.showAndWait().ifPresent(bt -> {
            if (bt == upgradeBtn && MainLayoutController.getInstance() != null) {
                MainLayoutController.getInstance().handlePremium();
            }
        });
    }

    // 🧠 Refill Dialog on Out of Brains
    private void showOutOfBrainsDialog() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            handleClose();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Out of Energy!");
        alert.setHeaderText("🧠 You have 0 Brains left!");
        alert.setContentText("Would you like to refill 5 Brains for 100 Coins?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (user.getCoin() >= 100) {
                    boolean success = DatabaseManager.refillBrainsWithCoins(user.getUserId(), 100, 5);
                    if (success) {
                        user.setBrainCount(5);
                        user.setCoin(user.getCoin() - 100);

                        if (MainLayoutController.getInstance() != null) {
                            MainLayoutController.getInstance().updateHeaderStats();
                        }

                        Alert info = new Alert(Alert.AlertType.INFORMATION, "⚡ Brains refilled to 5!");
                        info.showAndWait();
                    } else {
                        Alert err = new Alert(Alert.AlertType.ERROR, "Failed to refill brains on server.");
                        err.showAndWait();
                        handleClose();
                    }
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "❌ Insufficient coins! You need 100 coins.");
                    err.showAndWait();
                    handleClose();
                }
            } else {
                handleClose();
            }
        });
    }
}