package Project;

public class Lesson {
    private String lessonId;
    private String courseId;
    private int lessonOrder;
    private String title;
    private int xp;
    private String lessonType;
    private String moduleName;

    public Lesson(String lessonId, String courseId, int lessonOrder, String title, int xp, String lessonType, String moduleName) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.lessonOrder = lessonOrder;
        this.title = title;
        this.xp = xp;
        this.lessonType = lessonType;
        this.moduleName = moduleName;
    }

    public String getLessonId() { return lessonId; }
    public String getCourseId() { return courseId; }
    public int getLessonOrder() { return lessonOrder; }
    public String getTitle() { return title; }
    public int getXp() { return xp; }
    public String getLessonType() { return lessonType; }
    public String getModuleName() { return moduleName != null ? moduleName : "Section 1: Basics"; }
}