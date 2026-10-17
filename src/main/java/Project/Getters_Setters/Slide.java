package Project.Getters_Setters;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Slide {
    private String slideId;
    private String type; // CONCEPT, FILL_IN_BLANK, MULTIPLE_CHOICE
    private String title;
    private String prompt;
    private String codeSnippet;
    private List<String> options;
    private String correctAnswer;

    @SerializedName(value = "isInteractive", alternate = {"is_interactive", "is-interactive"})
    private boolean isInteractive;

    public Slide() {}

    public Slide(String slideId, String type, String title, String prompt, String codeSnippet, List<String> options, String correctAnswer) {
        this.slideId = slideId;
        this.type = type;
        this.title = title;
        this.prompt = prompt;
        this.codeSnippet = codeSnippet;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.isInteractive = false;
    }

    public Slide(String slideId, String type, String title, String prompt, String codeSnippet, List<String> options, String correctAnswer, boolean isInteractive) {
        this.slideId = slideId;
        this.type = type;
        this.title = title;
        this.prompt = prompt;
        this.codeSnippet = codeSnippet;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.isInteractive = isInteractive;
    }

    // Getters and Setters
    public String getSlideId() { return slideId; }
    public void setSlideId(String slideId) { this.slideId = slideId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public boolean isInteractive() { return isInteractive; }
    public void setInteractive(boolean isInteractive) { this.isInteractive = isInteractive; }
}