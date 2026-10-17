package Project.Getters_Setters;

import java.util.List;

public class QuestionData {
    private String language;
    private String title;
    private String instructions;
    private String task;
    private String starterCode;
    private String expectedOutput;
    private String hint;
    private String solutionCode;
    private List<String> blanks;
    private List<String> choices;
    private String questionType;   // "write-code" | "fill-blank" | "drag-drop"
    private String previewType;    // "console" | "browser"

    // Constructor ၁ — Write Code (arguments ၈ ခု)
    public QuestionData(String language, String title, String instructions,
                        String task, String starterCode, String expectedOutput,
                        String hint, String solutionCode) {
        this(language, title, instructions, task, starterCode,
                expectedOutput, hint, solutionCode,
                null, null, "write-code", "console");
    }

    // Constructor ၂ — Fill-Blank (arguments ၁၂ ခု)
    public QuestionData(String language, String title, String instructions,
                        String task, String starterCode, String expectedOutput,
                        String hint, String solutionCode,
                        List<String> blanks, List<String> choices,
                        String questionType, String previewType) {
        this.language = language;
        this.title = title;
        this.instructions = instructions;
        this.task = task;
        this.starterCode = starterCode;
        this.expectedOutput = expectedOutput;
        this.hint = hint;
        this.solutionCode = solutionCode;
        this.blanks = blanks;
        this.choices = choices;
        this.questionType = questionType;
        this.previewType = previewType;
    }

    // Getters
    public String getLanguage() { return language; }
    public String getTitle() { return title; }
    public String getInstructions() { return instructions; }
    public String getTask() { return task; }
    public String getStarterCode() { return starterCode; }
    public String getExpectedOutput() { return expectedOutput; }
    public String getHint() { return hint; }
    public String getSolutionCode() { return solutionCode; }
    public List<String> getBlanks() { return blanks; }
    public List<String> getChoices() { return choices; }
    public String getQuestionType() { return questionType; }
    public String getPreviewType() { return previewType; }
}