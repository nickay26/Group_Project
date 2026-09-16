package Project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class LanguageDetailController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label detailsLabel;
    @FXML private TextArea codeSampleArea;

    private Course currentCourse;

    /**
     * Initializes the controller using a Course object from CoursesPageController
     */
    public void initData(Course course) {
        this.currentCourse = course;

        if (titleLabel != null) titleLabel.setText(course.getTitle());
        if (subtitleLabel != null) subtitleLabel.setText(course.getLanguage() + " Track");
        if (detailsLabel != null) detailsLabel.setText(course.getDescription());
        if (codeSampleArea != null) {
            codeSampleArea.setText(
                    "// Welcome to " + course.getLanguage() + "!\n" +
                            "// Lessons completed: " + course.getCompletedLessons() + "/" + course.getTotalLessons()
            );
        }
    }

    public void setLanguageData(String title, String subtitle, String details, String codeSample) {
        if (titleLabel != null) titleLabel.setText(title);
        if (subtitleLabel != null) subtitleLabel.setText(subtitle);
        if (detailsLabel != null) detailsLabel.setText(details);
        if (codeSampleArea != null) codeSampleArea.setText(codeSample);
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("CoursePage.fxml", "Mimo Platform - Courses");
    }
}