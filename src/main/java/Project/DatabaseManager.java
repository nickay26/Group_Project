package Project;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseManager {

    private static final String BASE_URL = "https://nickayjohn26.pythonanywhere.com";
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    // ==========================================
    // 1. USER AUTHENTICATION & SYNC
    // ==========================================

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

    // User ၏ နောက်ဆုံး Active ဖြစ်ခဲ့သော Course ID ကို API မှတစ်ဆင့် ရယူခြင်း
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

    // Save profile information (Name, Email)
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

    // Change user password
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
                    String courseId = obj.optString("Course_ID", obj.optString("course_id", ""));
                    String title = obj.optString("Course_Name", obj.optString("course_name", obj.optString("title", "Course")));
                    int total = obj.optInt("total_lessons", 0);
                    int completed = obj.optInt("completed_lessons", 0);

                    courses.add(new Course(
                            courseId,
                            title,
                            "Master " + title + " with interactive lessons.",
                            courseId.contains("_") ? courseId.split("_")[0] : courseId,
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

                    lessons.add(new Lesson(
                            lessonId,
                            cId,
                            lessonOrder,
                            title,
                            xp,
                            lessonType,
                            moduleName,
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

            client.send(request, HttpResponse.BodyHandlers.ofString());

            if (coinsAmount > 0) {
                addCoinsToUser(userId, coinsAmount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            return response.statusCode() == 200 || response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean refillBrainsWithCoins(int userId, int cost, int maxBrain) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("cost", cost);
            body.put("max_brain", maxBrain);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/user/refill-brains"))
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
                return new Project(
                        projectId,
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

    // ============================================
    // UPDATE USER PROFILE
    // ============================================
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

            if (response.statusCode() == 200) {
                System.out.println("Profile updated successfully: " + response.body());
                return true;
            } else {
                System.err.println("Failed to update profile. Server responded with status: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error updating profile in DatabaseManager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // HELPER METHODS
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

    // JSON Key စာလုံးကြီး/သေး နှစ်မျိုးလုံးကို လက်ခံပေးမည့် Flexible Helper Mapping Method
    private static User mapJsonToUser(JSONObject obj) {
        int userId = obj.has("User_ID") ? obj.getInt("User_ID") : obj.optInt("user_id", -1);
        String name = obj.has("Name") ? obj.getString("Name") : obj.optString("name", "User");
        String email = obj.has("Email") ? obj.getString("Email") : obj.optString("email", "");

        String profile = obj.optString("Profile", obj.optString("profile", null));
        int xp = obj.optInt("XP", obj.optInt("xp", 0));
        int streak = obj.optInt("Streak", obj.optInt("streak", 0));

        // Coin Key စစ်ဆေးမှုများကို စုံလင်အောင် ပြင်ဆင်ထားသည်
        int coin = 0;
        if (obj.has("coins") && !obj.isNull("coins")) coin = obj.optInt("coins");
        else if (obj.has("Coin") && !obj.isNull("Coin")) coin = obj.optInt("Coin");
        else if (obj.has("Coins") && !obj.isNull("Coins")) coin = obj.optInt("Coins");
        else if (obj.has("coin") && !obj.isNull("coin")) coin = obj.optInt("coin");

        int brainCount = obj.optInt("brain_count", obj.optInt("Brain_Count", 5));

        return new User(userId, name, email, profile, xp, streak, coin, brainCount);
    }

    // ==========================================
    // ADD LESSON TO REVIEW TABLE
    // ==========================================
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

    // ============================================
    // GET LESSONS FROM REVIEW TABLE
    // ============================================
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

                    // Course ID အပေါ်မူတည်၍ Language သတ်မှတ်ခြင်း
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
    // 4. PREMIUM & PAYMENT
    // ==========================================

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
    // 5. BRAIN & ENERGY SYSTEM
    // ==========================================

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

    // ==========================================
    // 6. LEADERBOARD SYSTEM (SYNCHRONOUS)
    // ==========================================

    public static JSONArray getLeaderboard() {
        return getLeaderboard(null, 1);
    }

    public static JSONArray getLeaderboard(String seasonId) {
        return getLeaderboard(seasonId, 1);
    }

    public static JSONArray getLeaderboard(String seasonId, int leagueId) {
        try {
            String path = "/api/leaderboard";
            if (seasonId != null && !seasonId.trim().isEmpty() && !seasonId.equalsIgnoreCase("current")) {
                path += "/" + URLEncoder.encode(seasonId, StandardCharsets.UTF_8);
            }
            path += "?league_id=" + leagueId;

            String response = sendGetRequest(path);
            if (!response.isEmpty()) {
                if (response.trim().startsWith("{")) {
                    JSONObject json = new JSONObject(response);
                    return json.optJSONArray("rankings");
                } else if (response.trim().startsWith("[")) {
                    return new JSONArray(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public static JSONObject getUserLeagueStatus(int userId) {
        return getUserLeagueStatus(userId, null);
    }

    public static JSONObject getUserLeagueStatus(int userId, String seasonId) {
        try {
            String path = "/api/user/" + userId + "/league-status";
            if (seasonId != null && !seasonId.trim().isEmpty() && !seasonId.equalsIgnoreCase("current")) {
                path += "/" + URLEncoder.encode(seasonId, StandardCharsets.UTF_8);
            }

            String response = sendGetRequest(path);
            if (!response.isEmpty()) {
                return new JSONObject(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addUserXp(int userId, int xp) {
        return addUserXp(userId, null, xp, 1);
    }

    public static boolean addUserXp(int userId, String seasonId, int xp) {
        return addUserXp(userId, seasonId, xp, 1);
    }

    public static boolean addUserXp(int userId, String seasonId, int xp, int leagueId) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("xp", xp);
            body.put("league_id", leagueId);
            if (seasonId != null && !seasonId.trim().isEmpty()) {
                body.put("season_id", seasonId);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/user/add-xp"))
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
}