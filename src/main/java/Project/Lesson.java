package Project;

import java.util.ArrayList;
import java.util.List;

public class Lesson {
    private String lessonId;
    private String courseId;
    private int lessonOrder;
    private String title;
    private int xp;
    private String lessonType;
    private String moduleName;
    private boolean isPremium;
    private List<Slide> slides; // Slide မေးခွန်းများ သိမ်းဆည်းရန် List

    // Gson အတွက် Default Constructor
    public Lesson() {
        this.slides = new ArrayList<>();
    }

    public Lesson(String lessonId, String courseId, int lessonOrder, String title, int xp, String lessonType, String moduleName, boolean isPremium, List<Slide> slides) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.lessonOrder = lessonOrder;
        this.title = title;
        this.xp = xp;
        this.lessonType = lessonType;
        this.moduleName = moduleName;
        this.isPremium = isPremium;
        this.slides = slides != null ? slides : new ArrayList<>();
    }

    // Getters and Setters
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public int getLessonOrder() { return lessonOrder; }
    public void setLessonOrder(int lessonOrder) { this.lessonOrder = lessonOrder; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public String getLessonType() { return lessonType; }
    public void setLessonType(String lessonType) { this.lessonType = lessonType; }

    public String getModuleName() { return moduleName != null ? moduleName : "Section 1: Basics"; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean isPremium) { this.isPremium = isPremium; }

    public List<Slide> getSlides() { return slides; }
    public void setSlides(List<Slide> slides) { this.slides = slides; }
}
