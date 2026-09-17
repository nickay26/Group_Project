package Project;

public class Lesson {
    private String lessonId;
    private String courseId;
    private int lessonOrder;
    private String title;
    private int xp;
    private String content;   // JSON stored as String
    private String lessonType; // ENUM value ('LESSON', 'EXAM', etc.)

    public Lesson(String lessonId, String courseId, int lessonOrder, String title, int xp, String content, String lessonType) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.lessonOrder = lessonOrder;
        this.title = title;
        this.xp = xp;
        this.content = content;
        this.lessonType = lessonType;
    }

    public String getLessonId() { return lessonId; }
    public String getCourseId() { return courseId; }
    public int getLessonOrder() { return lessonOrder; }
    public String getTitle() { return title; }
    public int getXp() { return xp; }
    public String getContent() { return content; }
    public String getLessonType() { return lessonType; }
}