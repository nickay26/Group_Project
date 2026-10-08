package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseManager {

    // PythonAnywhere SSL Redirect Error ကာကွယ်ရန် URL Domain Name ကို စာလုံးသေး ပြောင်းထားပါသည်
    private static final String BASE_URL = "https://nickayjohn26.pythonanywhere.com";
    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ==========================================
    // 1. USER AUTHENTICATION & SYNC
    // ==========================================

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
            String url = BASE_URL + "/api/courses/" + courseId + "/lessons";
            System.out.println("🔍 GET: " + url);   // ✅ Debug
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/courses/" + courseId + "/lessons"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("🔍 Status: " + response.statusCode());   // ✅ Debug
            System.out.println("🔍 Body: " + response.body());            // ✅ Debug

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

    // ============================================
    // UPDATE USER PROFILE
    // ============================================
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
            return false;
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    // GET Request များ ခေါ်ယူရာတွင် အသုံးပြုရန် Generic Helper
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
        int coin = obj.optInt("coin", obj.optInt("Coin", 0));
        int brainCount = obj.optInt("brain_count", obj.optInt("Brain_Count", 5));

        User user = new User(userId, name, email, profile, xp, streak, coin, brainCount);

        // ⭐ Premium fields ထည့်
        String planType = obj.optString("plan_type", "FREE");   // ★ ဒါ ထည့်ပါ ★
        user.setPremium("PREMIUM".equals(planType));            // ★ ဒါ ပြင်ပါ ★
        user.setSubscriptionType(planType);
        user.setPlanType(planType);                             // ★ ဒါ ထည့်ပါ ★

        return user;
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

                    String lessonId = obj.optString("lesson_id", "");
                    String courseId = obj.optString("course_id", "");
                    String title = obj.optString("Lesson_title", lessonId);

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

    public static boolean savePaymentScreenshot(int userId, String fileName,
                                                String method, int amount) {
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

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==========================================
    // 5. BRAIN & ENERGY SYSTEM
    // ==========================================

    /**
     * Retrieves current brain count, coins, and premium status for a user.
     */
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

    /**
     * Deducts 1 brain upon an incorrect answer.
     * Premium users are automatically exempted on the backend.
     */
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

    /**
     * Spends user coins to refill brains to max capacity (5).
     */
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

    /**
     * Fetch active season leaderboard
     */
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
                path += "/" + seasonId;
            }
            path += "?league_id=" + leagueId;

            String response = sendGetRequest(path);
            if (!response.isEmpty()) {
                // Extracts "rankings" array if backend returns {"season_id": "...", "rankings": [...]}
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

    /**
     * Fetch User League Status
     */
    public static JSONObject getUserLeagueStatus(int userId) {
        return getUserLeagueStatus(userId, null);
    }

    public static JSONObject getUserLeagueStatus(int userId, String seasonId) {
        try {
            String path = "/api/user/" + userId + "/league-status";
            if (seasonId != null && !seasonId.trim().isEmpty() && !seasonId.equalsIgnoreCase("current")) {
                path += "/" + seasonId;
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

    /**
     * Add XP to User & Auto-Enroll in Active Season
     */
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

    // ==========================================
// 5. GET USER BY ID (Brain Count အတွက်)
// ==========================================

    public static User getUserById(int userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/" + userId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject res = new JSONObject(response.body());
                return mapJsonToUser(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ============================================
// ⭐ AI ASK LIMIT TRACKING
// ============================================

    /**
     * ဒီနေ့ AI မေးပြီးသား အရေအတွက် ယူခြင်း
     */
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

    /**
     * AI မေးတဲ့အခါ Count တိုးခြင်း
     */
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

}