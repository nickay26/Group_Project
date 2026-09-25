package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseManager {

    private static final String BASE_URL = "https://NickayJohn26.pythonanywhere.com";
    private static final HttpClient client = HttpClient.newHttpClient();

    // ==========================================
    // 1. USER AUTHENTICATION & SYNC
    // ==========================================

    public static User findOrCreateGoogleUser(String email, String name, String profilePicUrl) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("name", name != null ? name : "Google User");
            body.put("profile_pic", profilePicUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/google"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JSONObject res = new JSONObject(response.body());
                return mapJsonToUser(res.getJSONObject("user"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User authenticateUser(String email, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject res = new JSONObject(response.body());
                return mapJsonToUser(res.getJSONObject("user"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User registerUser(String name, String email, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("email", email);
            body.put("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                JSONObject res = new JSONObject(response.body());
                return mapJsonToUser(res.getJSONObject("user"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // User ၏ နောက်ဆုံး Active ဖြစ်ခဲ့သော Course ID ကို API မှတစ်ဆင့် ရယူခြင်း
    public static String getLastActiveCourseId(int userId) {
        if (userId <= 0) return "CPP_BASIC";

        try {
            List<Course> courses = getCoursesForUser(userId);
            if (courses != null && !courses.isEmpty()) {
                return courses.get(0).getCourseId(); // ပထမဆုံး Course ကို Default ယူခြင်း
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "CPP_BASIC"; // Connection မရပါက သို့မဟုတ် Dataset မရှိပါက Default ပေးမည့် Course ID
    }

    // ==========================================
    // 2. COURSES & LESSONS
    // ==========================================

    public static List<Course> getCoursesForUser(int userId) {
        List<Course> courses = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/" + userId + "/courses"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray arr = new JSONArray(response.body());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String courseId = obj.getString("Course_ID");
                    String title = obj.getString("Course_Name");
                    int total = obj.optInt("total_lessons", 0);
                    int completed = obj.optInt("completed_lessons", 0);

                    courses.add(new Course(
                            courseId,
                            title,
                            "Master " + title + " with interactive lessons.",
                            courseId.split("_")[0],
                            total,
                            completed
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return courses;
    }

    public static List<Lesson> getLessonsForCourse(String courseId) {
        List<Lesson> lessons = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/courses/" + courseId + "/lessons"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray arr = new JSONArray(response.body());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    lessons.add(new Lesson(
                            obj.getString("Lesson_ID"),
                            obj.getString("Course_ID"),
                            obj.getInt("Lesson_order"),
                            obj.getString("Title"),
                            obj.getInt("XP"),
                            obj.optString("lesson_type", "LESSON"),
                            obj.optString("Module_Name", "General"),
                            false,
                            new ArrayList<>()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lessons;
    }

    public static void completeLessonAndAddXP(int userId, String courseId, int lessonOrder, int xpAmount) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("course_id", courseId);
            body.put("lesson_order", lessonOrder);
            body.put("xp", xpAmount);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/lessons/complete"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // User ၏ Course တစ်ခုအတွက် ပြီးစီးပြီးသော Lesson အရေအတွက်ကို API မှတစ်ဆင့် ယူခြင်း
    public static int getCompletedLessonCount(int userId, String courseId) {
        List<Course> courses = getCoursesForUser(userId);
        for (Course c : courses) {
            if (c.getCourseId().equalsIgnoreCase(courseId)) {
                return c.getCompletedLessons();
            }
        }
        return 0;
    }

    // ==========================================
    // 3. PLAYGROUND PROJECTS
    // ==========================================

    public static List<Project> getUserProjects(int userId) {
        List<Project> projects = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/" + userId + "/projects"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray arr = new JSONArray(response.body());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    projects.add(new Project(
                            obj.getInt("project_id"),
                            obj.getInt("User_id"),
                            obj.getString("title"),
                            obj.getString("Language"),
                            obj.getString("Code_Content")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return projects;
    }

    public static Project createProject(int userId, String title, String language, String codeContent) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("title", title);
            body.put("language", language);
            body.put("code_content", codeContent);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/projects/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                JSONObject res = new JSONObject(response.body());
                return new Project(
                        res.getInt("project_id"),
                        userId,
                        title,
                        language,
                        codeContent
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateProjectCode(int projectId, String codeContent) {
        try {
            JSONObject body = new JSONObject();
            body.put("project_id", projectId);
            body.put("code_content", codeContent);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/projects/update"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper Mapping Method
    private static User mapJsonToUser(JSONObject obj) {
        return new User(
                obj.getInt("User_ID"),
                obj.getString("Name"),
                obj.getString("Email"),
                obj.optString("Profile", null),
                obj.optInt("XP", 0),
                obj.optInt("Streak", 0),
                obj.optInt("coin", 0),
                obj.optInt("brain_count", 5)
        );
    }

    public static Connection getConnection() {
        return null;
    }
}