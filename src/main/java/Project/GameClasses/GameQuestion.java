package Project.GameClasses;

import java.util.List;

public class GameQuestion {
    private String type;          // "mcq"
    private String title;         // "Output Prediction"
    private String codeSnippet;   // Code အပိုင်းအစ
    private String description;   // "What will be printed?"
    private List<String> options; // ရွေးချယ်စရာ ၄ ခု
    private int correctAnswer;    // 0, 1, 2, 3
    private int timeLimit;        // စက္ကန့် (15s)

    // Default Constructor
    public GameQuestion() {}

    // Parameterized Constructor
    public GameQuestion(String type, String title, String codeSnippet, String description, List<String> options, int correctAnswer, int timeLimit) {
        this.type = type;
        this.title = title;
        this.codeSnippet = codeSnippet;
        this.description = description;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.timeLimit = timeLimit;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
}