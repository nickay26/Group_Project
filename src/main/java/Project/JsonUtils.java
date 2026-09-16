package Project;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {

    public static List<Project> parseProjectsJson(String json) {
        List<Project> projects = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return projects;

        // Extract individual JSON objects {...} from the array
        Pattern objectPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = objectPattern.matcher(json);

        while (matcher.find()) {
            String objectBody = matcher.group(1);
            Project project = parseObjectBody(objectBody);
            if (project != null) {
                projects.add(project);
            }
        }
        return projects;
    }

    public static Project parseSingleProjectJson(String json) {
        if (json == null || json.trim().isEmpty()) return null;

        Pattern objectPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = objectPattern.matcher(json);

        if (matcher.find()) {
            return parseObjectBody(matcher.group(1));
        }
        return null;
    }

    private static Project parseObjectBody(String body) {
        int projectId = parseInteger(extractKey(body, "project_id"), 0);
        int userId = parseInteger(extractKey(body, "User_id"), 1);
        String title = unescapeJson(extractKey(body, "title"));
        String language = extractKey(body, "Language");
        String codeContent = unescapeJson(extractKey(body, "Code_Content"));

        return new Project(projectId, userId, title, language, codeContent, null, null);
    }

    static String extractKey(String jsonBody, String key) {
        // Matches "key": "value" OR "key": 123 OR "key": null
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\"([^\"]*)\"|([0-9]+)|null)");
        Matcher matcher = pattern.matcher(jsonBody);

        if (matcher.find()) {
            if (matcher.group(2) != null) return matcher.group(2); // String value
            if (matcher.group(3) != null) return matcher.group(3); // Numeric value
        }
        return "";
    }

    private static int parseInteger(String val, int fallback) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String unescapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public static List<Course> parseCoursesJson(String json) {
        List<Course> courses = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return courses;

        // Extract individual JSON objects {...} from the array
        Pattern objectPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = objectPattern.matcher(json);

        while (matcher.find()) {
            String objectBody = matcher.group(1);
            Course course = parseCourseObjectBody(objectBody);
            if (course != null) {
                courses.add(course);
            }
        }
        return courses;
    }

    private static Course parseCourseObjectBody(String body) {
        String courseId = unescapeJson(extractKey(body, "course_id"));
        String title = unescapeJson(extractKey(body, "title"));
        String description = unescapeJson(extractKey(body, "description"));
        String language = extractKey(body, "language");
        int totalLessons = parseInteger(extractKey(body, "total_lessons"), 0);
        int completedLessons = parseInteger(extractKey(body, "completed_lessons"), 0);

        return new Course(courseId, title, description, language, totalLessons, completedLessons);
    }
}