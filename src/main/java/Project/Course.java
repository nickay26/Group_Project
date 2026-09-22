package Project;

public class Course {
    private String courseId;
    private String title;
    private String description;
    private String language;
    private int totalLessons;
    private int completedLessons;

    public Course(String courseId, String title, String description, String language) {
        this(courseId, title, description, language, 0, 0);
    }

    public Course(String courseId, String title, String description, String language, int totalLessons, int completedLessons) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.language = language;
        this.totalLessons = totalLessons;
        this.completedLessons = completedLessons;
    }

    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLanguage() { return language; }
    public int getTotalLessons() { return totalLessons; }
    public int getCompletedLessons() { return completedLessons; }

    public double getProgressPercentage() {
        if (totalLessons == 0) return 0.0;
        return (double) completedLessons / totalLessons;
    }
}