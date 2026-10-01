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
    // PRACTICE QUESTIONS JSON — LANGUAGE FILES
    // ============================================

    // ✅ Language → File Path
    private static String getFilePath(String language) {
        if (language == null) return "data/python.json";

        switch (language.toLowerCase()) {
            case "java":   return "data/java.json";
            case "c++":
            case "cpp":    return "data/cpp.json";
            case "python":
            default:       return "data/python.json";
        }
    }

    // ✅ LOAD ALL — Language အားလုံး
    public static List<QuestionData> loadPracticeQuestions() {
        List<QuestionData> all = new ArrayList<>();
        all.addAll(loadFromFile("data/python.json"));
        all.addAll(loadFromFile("data/java.json"));
        all.addAll(loadFromFile("data/cpp.json"));
        return all;
    }

    // ✅ LOAD BY LANGUAGE
    public static List<QuestionData> loadPracticeQuestionsByLanguage(String language) {
        if (language == null || language.isEmpty()) {
            return loadPracticeQuestions();
        }
        return loadFromFile(getFilePath(language));
    }

    // ✅ LOAD FROM FILE
    private static List<QuestionData> loadFromFile(String path) {
        List<QuestionData> questions = new ArrayList<>();
        File file = new File(path);

        if (!file.exists()) {
            System.out.println(path + " not found. Creating empty file...");
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

            System.out.println("✅ Loaded " + questions.size() + " from " + path);

        } catch (Exception e) {
            System.err.println("❌ Error loading: " + e.getMessage());
            e.printStackTrace();
        }

        return questions;
    }

    // ============================================
    // SAVE (add new) — Language အလိုက် ဖိုင်
    // ============================================
    public static boolean savePracticeQuestion(QuestionData q) {
        String path = getFilePath(q.getLanguage());
        List<QuestionData> all = loadFromFile(path);
        all.add(q);
        return saveAllToFile(all, path);
    }

    // ============================================
    // UPDATE — Language အလိုက် ဖိုင်
    // ============================================
    public static boolean updatePracticeQuestion(String language, String oldTitle, QuestionData newQ) {
        String path = getFilePath(language);
        List<QuestionData> all = loadFromFile(path);

        for (int i = 0; i < all.size(); i++) {
            QuestionData q = all.get(i);
            if (q.getLanguage().equalsIgnoreCase(language) && q.getTitle().equals(oldTitle)) {
                all.set(i, newQ);
                return saveAllToFile(all, path);
            }
        }
        return false;
    }

    // ============================================
    // DELETE — Language အလိုက် ဖိုင်
    // ============================================
    public static boolean deletePracticeQuestion(String language, String title) {
        String path = getFilePath(language);
        List<QuestionData> all = loadFromFile(path);

        boolean removed = all.removeIf(q ->
                q.getLanguage().equalsIgnoreCase(language) && q.getTitle().equals(title));

        if (removed) {
            return saveAllToFile(all, path);
        }
        return false;
    }

    // ============================================
    // GET BY LANGUAGE
    // ============================================
    public static List<QuestionData> getPracticeByLanguage(String language) {
        return loadPracticeQuestionsByLanguage(language);
    }

    // ============================================
    // GET BY TITLE (Language + Title)
    // ============================================
    public static QuestionData getPracticeByTitle(String language, String title) {
        for (QuestionData q : loadPracticeQuestionsByLanguage(language)) {
            if (q.getLanguage().equalsIgnoreCase(language) &&
                    q.getTitle().equals(title)) {
                return q;
            }
        }
        return null;
    }

    // ============================================
    // GET BY TYPE (Language + QuestionType)
    // ============================================
    public static QuestionData getPracticeByType(String language, String type) {
        for (QuestionData q : loadPracticeQuestionsByLanguage(language)) {
            if (q.getLanguage().equalsIgnoreCase(language) &&
                    q.getQuestionType().equalsIgnoreCase(type)) {
                return q;
            }
        }
        return null;
    }

    // List အနေနဲ့ ပြန်ချင်ရင်
    public static List<QuestionData> getPracticeListByType(String language, String type) {
        List<QuestionData> result = new ArrayList<>();
        for (QuestionData q : loadPracticeQuestionsByLanguage(language)) {
            if (q.getLanguage().equalsIgnoreCase(language) &&
                    q.getQuestionType().equalsIgnoreCase(type)) {
                result.add(q);
            }
        }
        return result;
    }

    // ============================================
    // SAVE ALL TO FILE
    // ============================================
    private static boolean saveAllToFile(List<QuestionData> questions, String path) {
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

            File file = new File(path);
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(array.toString(2));
            }

            System.out.println("✅ Saved " + questions.size() + " to " + path);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Save error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ============================================
    // ALIAS METHODS
    // ============================================
    public static boolean saveQuestion(QuestionData q) {
        return savePracticeQuestion(q);
    }

    public static boolean updateQuestion(String language, String oldTitle, QuestionData newQ) {
        return updatePracticeQuestion(language, oldTitle, newQ);
    }

    public static boolean deleteQuestion(String language, String title) {
        return deletePracticeQuestion(language, title);
    }

    public static List<QuestionData> getByLanguage(String language) {
        return getPracticeByLanguage(language);
    }

    public static QuestionData getByTitle(String language, String title) {
        return getPracticeByTitle(language, title);
    }

    public static List<QuestionData> loadAllQuestions() {
        return loadPracticeQuestions();
    }
}