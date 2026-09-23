package Project;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {

    // ============================================
    // EXISTING — PROJECTS PARSER
    // ============================================
    public static List<Project> parseProjectsJson(String json) {
        List<Project> projects = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return projects;

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
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\"([^\"]*)\"|([0-9]+)|null)");
        Matcher matcher = pattern.matcher(jsonBody);

        if (matcher.find()) {
            if (matcher.group(2) != null) return matcher.group(2);
            if (matcher.group(3) != null) return matcher.group(3);
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


    // ============================================
    // ✅ အသစ် — PRACTICE QUESTIONS JSON
    // ============================================
    private static final String PRACTICE_PATH = "data/questions.json";

    // LOAD ALL
    public static List<QuestionData> loadPracticeQuestions() {
        List<QuestionData> questions = new ArrayList<>();
        File file = new File(PRACTICE_PATH);

        if (!file.exists()) {
            System.out.println("questions.json not found. Creating empty file...");
            try {
                file.getParentFile().mkdirs();
                try (FileWriter w = new FileWriter(file)) {
                    w.write("[]");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return questions;
        }

        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new FileReader(file, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }

            String content = sb.toString().trim();
            if (content.isEmpty()) return questions;

            org.json.JSONArray array = new org.json.JSONArray(content);

            for (int i = 0; i < array.length(); i++) {
                org.json.JSONObject obj = array.getJSONObject(i);

                List<String> blanks = new ArrayList<>();
                if (obj.has("blanks")) {
                    org.json.JSONArray bArr = obj.getJSONArray("blanks");
                    for (int j = 0; j < bArr.length(); j++) blanks.add(bArr.getString(j));
                }

                List<String> choices = new ArrayList<>();
                if (obj.has("choices")) {
                    org.json.JSONArray cArr = obj.getJSONArray("choices");
                    for (int j = 0; j < cArr.length(); j++) choices.add(cArr.getString(j));
                }

                questions.add(new QuestionData(
                        obj.optString("language", "Python"),
                        obj.optString("title", "Untitled"),
                        obj.optString("instructions", ""),
                        obj.optString("task", ""),
                        obj.optString("starterCode", ""),
                        obj.optString("expectedOutput", ""),
                        obj.optString("hint", ""),
                        obj.optString("solutionCode", ""),
                        blanks, choices,
                        obj.optString("questionType", "write-code"),
                        obj.optString("previewType", "console")
                ));
            }

            System.out.println("✅ Loaded " + questions.size() + " practice questions");

        } catch (Exception e) {
            System.err.println("❌ Error loading practice questions: " + e.getMessage());
            e.printStackTrace();
        }

        return questions;
    }

    // SAVE (add new)
    public static boolean savePracticeQuestion(QuestionData q) {
        List<QuestionData> all = loadPracticeQuestions();
        all.add(q);
        return saveAllPracticeQuestions(all);
    }

    // UPDATE
    public static boolean updatePracticeQuestion(String language, String oldTitle, QuestionData newQ) {
        List<QuestionData> all = loadPracticeQuestions();
        for (int i = 0; i < all.size(); i++) {
            QuestionData q = all.get(i);
            if (q.getLanguage().equalsIgnoreCase(language) && q.getTitle().equals(oldTitle)) {
                all.set(i, newQ);
                return saveAllPracticeQuestions(all);
            }
        }
        return false;
    }

    // DELETE
    public static boolean deletePracticeQuestion(String language, String title) {
        List<QuestionData> all = loadPracticeQuestions();
        boolean removed = all.removeIf(q ->
                q.getLanguage().equalsIgnoreCase(language) && q.getTitle().equals(title));
        if (removed) {
            return saveAllPracticeQuestions(all);
        }
        return false;
    }

    // GET BY LANGUAGE
    public static List<QuestionData> getPracticeByLanguage(String language) {
        List<QuestionData> result = new ArrayList<>();
        for (QuestionData q : loadPracticeQuestions()) {
            if (q.getLanguage().equalsIgnoreCase(language)) result.add(q);
        }
        return result;
    }

    // GET BY TITLE
    public static QuestionData getPracticeByTitle(String language, String title) {
        for (QuestionData q : loadPracticeQuestions()) {
            if (q.getLanguage().equalsIgnoreCase(language) && q.getTitle().equals(title)) {
                return q;
            }
        }
        return null;
    }

    // SAVE ALL
    private static boolean saveAllPracticeQuestions(List<QuestionData> questions) {
        try {
            org.json.JSONArray array = new org.json.JSONArray();

            for (QuestionData q : questions) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("language", q.getLanguage());
                obj.put("title", q.getTitle());
                obj.put("instructions", q.getInstructions());
                obj.put("task", q.getTask());
                obj.put("starterCode", q.getStarterCode());
                obj.put("expectedOutput", q.getExpectedOutput());
                obj.put("hint", q.getHint());
                obj.put("solutionCode", q.getSolutionCode());
                obj.put("blanks", q.getBlanks() != null ? q.getBlanks() : new ArrayList<>());
                obj.put("choices", q.getChoices() != null ? q.getChoices() : new ArrayList<>());
                obj.put("questionType", q.getQuestionType());
                obj.put("previewType", q.getPreviewType());
                array.put(obj);
            }

            File file = new File(PRACTICE_PATH);
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(array.toString(2));
            }

            System.out.println("✅ Saved " + questions.size() + " practice questions");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Save error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // saveQuestion → savePracticeQuestion
    public static boolean saveQuestion(QuestionData q) {
        return savePracticeQuestion(q);
    }

    // updateQuestion → updatePracticeQuestion
    public static boolean updateQuestion(String language, String oldTitle, QuestionData newQ) {
        return updatePracticeQuestion(language, oldTitle, newQ);
    }
    // deleteQuestion → deletePracticeQuestion
    public static boolean deleteQuestion(String language, String title) {
        return deletePracticeQuestion(language, title);
    }

    // getByLanguage → getPracticeByLanguage
    public static List<QuestionData> getByLanguage(String language) {
        return getPracticeByLanguage(language);
    }
    // getByTitle → getPracticeByTitle
    public static QuestionData getByTitle(String language, String title) {
        return getPracticeByTitle(language, title);
    }

    // loadAllQuestions → loadPracticeQuestions
    public static List<QuestionData> loadAllQuestions() {
        return loadPracticeQuestions();
    }
}