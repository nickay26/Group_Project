package Project.API_Database;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import Project.Controllers.MainLayoutController;
import Project.Controllers.PracticeController;
import Project.Getters_Setters.*;
import org.json.JSONArray;
import org.json.JSONObject;

import static java.sql.DriverManager.getConnection;

public class DatabaseManager {

    private static final String BASE_URL = "https://nickayjohn26.pythonanywhere.com";
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    // ==========================================
    // 1. ADMIN ANALYTICS
    // ==========================================

    public static JSONObject getAdminAnalytics() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/admin/analytics"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new JSONObject(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==========================================
    // 2. USER AUTHENTICATION & PROFILE MANAGEMENT
    // ==========================================

    public static JSONObject requestPasswordReset(String email) {
        try {
            String res = ApiClient.requestPasswordResetAsync(email).join();
            if (res != null) return new JSONObject(res);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static boolean confirmPasswordReset(String email, String code, String newPassword) {
        try {
            String res = ApiClient.confirmPasswordResetAsync(email, code, newPassword).join();
            if (res != null) {
                JSONObject json = new JSONObject(res);
                return json.optBoolean("success", false);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public static boolean updatePassword(int userId, String currentPassword, String newPassword) {
        try {
            String res = ApiClient.updatePasswordAsync(userId, currentPassword, newPassword).join();
            if (res != null) {
                JSONObject json = new JSONObject(res);
                return json.optBoolean("success", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static User getUserById(int userId) {
        if (userId <= 0) return null;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/" + userId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                JSONObject userObj = json.has("user") ? json.getJSONObject("user") : json;
                return mapJsonToUser(userObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User findOrCreateGoogleUser(String name, String email, String profilePicUrl) {
        try {
            JSONObject body = new JSONObject();
            body.put("name", name != null ? name : "Google User");
            body.put("email", email);
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

    public static boolean updateUserInfo(int userId, String name, String email) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("name", name);
            body.put("email", email);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/user/update"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean resetPasswordByEmail(String email, String newPassword) {
        try {
            String response = ApiClient.resetPasswordAsync(email, newPassword).join();
            if (response != null && !response.isEmpty()) {
                JSONObject json = new JSONObject(response);
                return json.has("message") && json.getString("message").contains("successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateUserInfoByAdmin(int userId, String name, String email, String role, String planType, int xp, int coin) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("name", name);
            body.put("email", email);
            body.put("role", role);
            body.put("plan_type", planType);
            body.put("xp", xp);
            body.put("coin", coin);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/admin/user/update"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUserProfile(int userId, String name, String profile) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("name", name);
            if (profile != null) {
                body.put("profile", profile);
            } else {
                body.put("profile", JSONObject.NULL);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/user/update"))
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

    public static boolean updateUserProfile(int userId, String imagePath) {
        try {
            String url = BASE_URL + "/api/user/update-profile-path";
            String safePath = (imagePath != null) ? imagePath.replace("\\", "\\\\") : "";
            String jsonInputString = String.format("{\"user_id\": %d, \"profile_path\": \"%s\"}", userId, safePath);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUserProfilePath(int userId, String imagePath) {
        try {
            String url = BASE_URL + "/api/user/update-profile-path";
            String safePath = (imagePath != null) ? imagePath.replace("\\", "\\\\") : "";
            String jsonInputString = String.format("{\"user_id\": %d, \"profile_path\": \"%s\"}", userId, safePath);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean changePassword(int userId, String oldPassword, String newPassword) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("old_password", oldPassword);
            body.put("new_password", newPassword);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/user/change-password"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteAccount(int userId) {
        try {
            String response = ApiClient.deleteAccountAsync(userId).join();
            if (response != null && !response.trim().isEmpty()) {
                String trimmed = response.trim();
                // Server က JSON ပြန်လာရင်
                if (trimmed.startsWith("{")) {
                    JSONObject json = new JSONObject(trimmed);
                    return json.has("message") && !json.has("error");
                }
                // Server က Plain Text (ဥပမာ- "Account deleted successfully") ပြန်လာရင်
                else if (trimmed.toLowerCase().contains("success") || trimmed.toLowerCase().contains("deleted")) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==========================================
    // 3. COURSES, LESSONS & REVIEWS
    // ==========================================

    public static String getLastActiveCourseId(int userId) {
        if (userId <= 0) return "CPP_BASIC";

        try {
            String response = sendGetRequest("/api/user/last-course/" + userId);
            if (!response.isEmpty()) {
                JSONObject json = new JSONObject(response);
                if (json.has("course_id")) {
                    return json.getString("course_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "CPP_BASIC";
    }

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
            String encodedCourseId = URLEncoder.encode(courseId, StandardCharsets.UTF_8);
            String url = BASE_URL + "/api/courses/" + encodedCourseId + "/lessons";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray arr = new JSONArray(response.body());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);

                    String lessonId = obj.optString("Lesson_ID", obj.optString("lesson_id", ""));
                    String cId = obj.optString("Course_ID", obj.optString("course_id", courseId));
                    int lessonOrder = obj.has("Lesson_order") ? obj.getInt("Lesson_order") : obj.optInt("lesson_order", i + 1);
                    String title = obj.optString("Title", obj.optString("title", "Lesson " + lessonOrder));
                    int xp = obj.has("XP") ? obj.getInt("XP") : obj.optInt("xp", 10);
                    String lessonType = obj.optString("lesson_type", obj.optString("Lesson_Type", "LESSON"));
                    String moduleName = obj.optString("Module_Name", obj.optString("module_name", "General"));

                    // 🔍 Check local JSON file in resources for is_premium
                    boolean isPremium = false;
                    try {
                        String jsonFileName = "/lessons/" + lessonId + ".json";
                        java.io.InputStream inputStream = DatabaseManager.class.getResourceAsStream(jsonFileName);
                        if (inputStream != null) {
                            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                            JSONObject localJson = new JSONObject(jsonContent);
                            isPremium = localJson.optBoolean("is_premium", localJson.optBoolean("isPremium", false));
                        }
                    } catch (Exception e) {
                        // Fallback if local file isn't found
                    }

                    lessons.add(new Lesson(
                            lessonId,
                            cId,
                            lessonOrder,
                            title,
                            xp,
                            lessonType,
                            moduleName,
                            isPremium,
                            new ArrayList<>()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lessons;
    }

    public static int getCompletedLessonCount(int userId, String courseId) {
        List<Course> courses = getCoursesForUser(userId);
        for (Course c : courses) {
            if (c.getCourseId().equalsIgnoreCase(courseId)) {
                return c.getCompletedLessons();
            }
        }
        return 0;
    }

    public static List<Course> getCompletedCourses(int userId) {
        List<Course> completed = new ArrayList<>();
        List<Course> allCourses = getCoursesForUser(userId);

        for (Course c : allCourses) {
            if (c.getProgressPercent() >= 100) {
                completed.add(c);
            }
        }
        return completed;
    }

    public static void completeLessonAndAddXP(int userId, String courseId, int lessonOrder, int xpAmount, int coinsAmount) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("course_id", courseId);
            body.put("lesson_order", lessonOrder);
            body.put("xp", xpAmount);
            body.put("coins", coinsAmount);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/lessons/complete"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                User updatedUser = getUserById(userId);
                if (updatedUser != null && UserSession.getCurrentUser() != null && UserSession.getUserId() == userId) {
                    UserSession.setCurrentUser(updatedUser);

                    javafx.application.Platform.runLater(() -> {
                        if (MainLayoutController.getInstance() != null) {
                            MainLayoutController.getInstance().updateHeaderStats();
                        }
                    });
                }
            }

            if (coinsAmount > 0 && response.statusCode() != 200 && response.statusCode() != 201) {
                addCoinsToUser(userId, coinsAmount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean addLessonToReview(int userId, String courseId, String lessonId) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("course_id", courseId);
            body.put("lesson_id", lessonId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/reviews"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<PracticeController.ReviewData> getReviewLessons(int userId) {
        List<PracticeController.ReviewData> reviewList = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/reviews/" + userId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    String lessonId = obj.optString("lesson_id", obj.optString("Lesson_ID", ""));
                    String courseId = obj.optString("course_id", obj.optString("Course_ID", ""));
                    String title = obj.optString("Lesson_title", obj.optString("title", obj.optString("Title", lessonId)));

                    String language = "C++";
                    if (courseId.toUpperCase().contains("JAVA")) {
                        language = "Java";
                    } else if (courseId.toUpperCase().contains("PYTHON")) {
                        language = "Python";
                    } else if (courseId.toUpperCase().contains("CPP") || courseId.toUpperCase().contains("C++")) {
                        language = "C++";
                    }

                    reviewList.add(new PracticeController.ReviewData(lessonId, courseId, title, language));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviewList;
    }

    // ==========================================
    // 4. PLAYGROUND PROJECTS
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
                            obj.has("project_id") ? obj.getInt("project_id") : obj.optInt("Project_ID", 0),
                            obj.has("User_id") ? obj.getInt("User_id") : obj.optInt("user_id", userId),
                            obj.optString("title", obj.optString("Title", "Untitled Project")),
                            obj.optString("Language", obj.optString("language", "C++")),
                            obj.optString("Code_Content", obj.optString("code_content", ""))
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
                int projectId = res.optInt("project_id", res.optInt("Project_ID", 0));
                return new Project(projectId, userId, title, language, codeContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteProject(int projectId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/projects/" + projectId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    // ==========================================
    // 5. BRAINS, COINS & ENERGY SYSTEM
    // ==========================================

    public static boolean addCoinsToUser(int userId, int coinsToAdd) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("coins", coinsToAdd);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/user/add-coins"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200 || response.statusCode() == 201;

            if (success && UserSession.getCurrentUser() != null && UserSession.getUserId() == userId) {
                User user = UserSession.getCurrentUser();
                user.setCoin(user.getCoin() + coinsToAdd);

                javafx.application.Platform.runLater(() -> {
                    if (MainLayoutController.getInstance() != null) {
                        MainLayoutController.getInstance().updateHeaderStats();
                    }
                });
            }

            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JSONObject getBrainStatus(int userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/" + userId + "/brains"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new JSONObject(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject deductBrain(int userId) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/brains/deduct"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new JSONObject(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject refillBrains(int userId, int cost) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("cost", cost);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/brains/refill"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new JSONObject(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean refillBrainsWithCoins(int userId, int costCoins, int addBrains) {
        if (userId <= 0 && UserSession.getCurrentUser() != null) {
            userId = UserSession.getCurrentUser().getUserId();
        }

        try {
            String response = ApiClient.refillBrainsAsync(userId, costCoins).join();

            if (response != null && !response.toLowerCase().contains("error")) {
                updateLocalSession(costCoins, addBrains);
                return true;
            }
        } catch (Exception e) {
            System.err.println("API Connection Failed: " + e.getMessage());
        }

        if (UserSession.getCurrentUser() != null) {
            int currentCoins = UserSession.getCurrentUser().getCoin();
            if (currentCoins >= costCoins) {
                updateLocalSession(costCoins, addBrains);
                return true;
            }
        }

        return false;
    }

    private static void updateLocalSession(int costCoins, int addBrains) {
        if (UserSession.getCurrentUser() != null) {
            int newCoins = Math.max(0, UserSession.getCurrentUser().getCoin() - costCoins);
            int currentBrains = UserSession.getCurrentUser().getBrainCount();

            UserSession.getCurrentUser().setCoin(newCoins);
            UserSession.getCurrentUser().setBrainCount(Math.min(5, currentBrains + addBrains));
        }
    }

    public static boolean updateBrainCount(int userId, int newCount) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("brain_count", newCount);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/user/brain/update"))
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

    // ==========================================
    // 6. AI ASK COUNTS & PREMIUM / PAYMENTS
    // ==========================================

    public static int getTodayAIAskCount(int userId) {
        try {
            String response = sendGetRequest("/api/users/" + userId + "/ai-ask-count");
            if (!response.isEmpty()) {
                JSONObject json = new JSONObject(response);
                return json.optInt("ask_count", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void incrementAIAskCount(int userId) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/" + userId + "/ai-ask"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean subscribePremium(int userId, int planId, String durationType) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("plan_id", planId);
            body.put("duration_type", durationType);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/premium/subscribe"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isPremium(int userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/" + userId + "/premium"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject res = new JSONObject(response.body());
                return res.optBoolean("is_premium", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static JSONObject checkPremiumStatus(int userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/" + userId + "/check-premium"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new JSONObject(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean savePaymentScreenshot(int userId, String fileName, String method, int amount) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("screenshot_path", fileName);
            body.put("method", method);
            body.put("amount", amount);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/payment/upload"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==========================================
    // 7. CERTIFICATES & ASYNC WRAPPERS
    // ==========================================

    public static String checkAndIssueCertificate(int userId, String courseId) {
        try {
            String response = ApiClient.checkAndIssueCertificateAsync(userId, courseId).join();
            if (response != null && !response.isEmpty()) {
                JSONObject json = new JSONObject(response);
                if (json.optBoolean("earned", false)) {
                    return json.optString("certificate_id", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getUserCertificates(int userId) {
        try {
            String response = ApiClient.getUserCertificatesAsync(userId).join();
            if (response != null && !response.isEmpty()) {
                return new JSONArray(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public static CompletableFuture<User> getUserByIdAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> getUserById(userId));
    }

    public static CompletableFuture<String> getLastActiveCourseIdAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> getLastActiveCourseId(userId));
    }

    // ==========================================
    // 8. PRIVATE UTILITY & MAPPING METHODS
    // ==========================================

    private static String sendGetRequest(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        }
        return "";
    }

    private static User mapJsonToUser(JSONObject obj) {
        // ⭐ Name နှင့် Email ကို Case-insensitive ဖြင့် သေချာစွာ ရှာယူခြင်း
        int userId = obj.has("User_ID") ? obj.getInt("User_ID") : obj.optInt("user_id", -1);

        String name = "User";
        if (obj.has("Name") && !obj.isNull("Name")) name = obj.getString("Name");
        else if (obj.has("name") && !obj.isNull("name")) name = obj.getString("name");

        String email = "";
        if (obj.has("Email") && !obj.isNull("Email")) email = obj.getString("Email");
        else if (obj.has("email") && !obj.isNull("email")) email = obj.getString("email");

        String profile = obj.optString("Profile", obj.optString("profile", null));
        int xp = obj.optInt("XP", obj.optInt("xp", 0));
        int streak = obj.optInt("Streak", obj.optString("streak", "0").isEmpty() ? 0 : Integer.parseInt(obj.optString("streak", "0")));

        int coin = 0;
        if (obj.has("coins") && !obj.isNull("coins")) coin = obj.optInt("coins");
        else if (obj.has("Coin") && !obj.isNull("Coin")) coin = obj.optInt("Coin");
        else if (obj.has("Coins") && !obj.isNull("Coins")) coin = obj.optInt("Coins");
        else if (obj.has("coin") && !obj.isNull("coin")) coin = obj.optInt("coin");

        int brainCount = obj.optInt("brain_count", obj.optInt("Brain_Count", 5));
        String planType = obj.optString("plan_type", obj.optString("Plan_Type", "FREE"));
        String role = obj.optString("role", obj.optString("Role", "USER"));

        String subscriptionType = obj.optString("subscription_type", obj.optString("Subscription_Type", "free"));
        String premiumExpiry = obj.optString("premium_expiry", obj.optString("Premium_Expiry", null));

        // ⭐ User Constructor ထဲသို့ (userId, name, email, ...) အစဉ်အတိုင်း မှန်ကန်စွာ ထည့်သွင်းခြင်း
        User user = new User(userId, name, email, profile, xp, streak, coin, brainCount);
        user.setPlanType(planType);
        user.setRole(role);
        user.setSubscriptionType(subscriptionType);
        user.setPremiumExpiry(premiumExpiry);

        boolean serverIsPremium = obj.optBoolean("is_premium", obj.optBoolean("isPremium", false));
        boolean isPremium = serverIsPremium
                || "PREMIUM".equalsIgnoreCase(planType)
                || "premium".equalsIgnoreCase(planType)
                || "monthly".equalsIgnoreCase(subscriptionType)
                || "yearly".equalsIgnoreCase(subscriptionType);

        user.setPremium(isPremium);

        return user;
    }
}