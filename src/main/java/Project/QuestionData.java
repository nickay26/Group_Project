package Project;

import java.util.List;

public class QuestionData {
    private String language;
    private String title;
    private String instructions;    // 📖 ရှင်းလင်းချက်
    private String task;            // 📝 လုပ်ရမယ့်အလုပ်
    private String starterCode;     // Code အစ
    private String expectedOutput;  // မျှော်မှန်းရလဒ်
    private String hint;            // 💡 အကြံပြုချက်
    private String solutionCode;    // ✅ အဖြေ
    private List<String> blanks;
    private List<String> choices;
    private String questionType;
    private String previewType;
    public QuestionData(String language, String title, String instructions,
                        String task, String starterCode, String expectedOutput,
                        String hint, String solutionCode,List<String> blanks, List<String> choices,
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