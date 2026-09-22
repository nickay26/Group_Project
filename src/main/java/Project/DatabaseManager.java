package Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/group_project?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root"; // Update with your MySQL Workbench username
    private static final String PASSWORD = "root"; // Update with your MySQL Workbench password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Ensure mysql-connector-j is in build path.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ==========================================
    // 1. USER AUTHENTICATION & SYNC
    // ==========================================

    // Replace findOrCreateGoogleUser in DatabaseManager.java
    public static User findOrCreateGoogleUser(String email, String name, String profilePicUrl) {
        String selectSql = "SELECT * FROM User_table WHERE Email = ?";
        String updateSql = "UPDATE User_table SET last_login_date = CURRENT_DATE() WHERE User_ID = ?";
        String insertSql = "INSERT INTO User_table (Name, Email, Password, Profile, last_login_date, XP, Streak, coin, brain_count) " +
                "VALUES (?, ?, 'GOOGLE_AUTH', ?, CURRENT_DATE(), 0, 0, 0, 5)";

        try (Connection conn = getConnection()) {
            // 1. Search for existing Google user in local MySQL database
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = mapResultSetToUser(rs); // Map user record FIRST

                        // Update last_login_date using mapped User_ID
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, user.getUserId());
                            updateStmt.executeUpdate();
                        }
                        return user;
                    }
                }
            }

            // 2. Insert new user if email does not exist yet
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, name != null ? name : "Google User");
                insertStmt.setString(2, email);
                insertStmt.setString(3, profilePicUrl);
                insertStmt.executeUpdate();

                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newUserId = keys.getInt(1);
                        return new User(newUserId, name, email, profilePicUrl, 0, 0, 0, 5);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User authenticateUser(String email, String password) {
        String sql = "SELECT * FROM User_table WHERE Email = ? AND Password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User registerUser(String name, String email, String password) {
        String sql = "INSERT INTO User_table (Name, Email, Password, last_login_date) VALUES (?, ?, ?, CURRENT_DATE())";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return new User(keys.getInt(1), name, email, null, 0, 0, 0, 5);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==========================================
    // 2. COURSES & PROGRESS
    // ==========================================

    public static List<Course> getCoursesForUser(int userId) {
        List<Course> courses = new ArrayList<>();
        String sql = """
        SELECT 
            c.Course_ID, 
            c.Course_Name, 
            COUNT(DISTINCT l.Lesson_ID) AS total_lessons,
            COALESCE(MAX(up.completed_lesson), 0) AS completed_lessons
        FROM Course_table c
        LEFT JOIN Lesson_table l ON c.Course_ID = l.Course_ID
        LEFT JOIN user_progress up ON c.Course_ID = up.Course_id AND up.User_id = ?
        GROUP BY c.Course_ID, c.Course_Name
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String courseId = rs.getString("Course_ID");
                String title = rs.getString("Course_Name");
                int total = rs.getInt("total_lessons");
                int completed = rs.getInt("completed_lessons");

                courses.add(new Course(
                        courseId,
                        title,
                        "Master " + title + " with interactive lessons.",
                        courseId.split("_")[0],
                        total,
                        completed
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public static boolean updateUserProgress(int userId, String courseId, int completedLessons) {
        String sql = """
            INSERT INTO user_progress (User_id, Course_id, completed_lesson, status)
            VALUES (?, ?, ?, 'IN_PROGRESS')
            ON DUPLICATE KEY UPDATE completed_lesson = ?, status = IF(? >= (SELECT COUNT(*) FROM Lesson_table WHERE Course_ID = ?), 'COMPLETED', 'IN_PROGRESS')
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, courseId);
            stmt.setInt(3, completedLessons);
            stmt.setInt(4, completedLessons);
            stmt.setInt(5, completedLessons);
            stmt.setString(6, courseId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==========================================
    // 3. USER PROJECTS (PLAYGROUND)
    // ==========================================

    public static boolean saveUserProject(int userId, String title, String language, String codeContent) {
        String sql = "INSERT INTO user_projects (User_id, title, Language, Code_Content) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, title);
            stmt.setString(3, language);
            stmt.setString(4, codeContent);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("User_ID"),
                rs.getString("Name"),
                rs.getString("Email"),
                rs.getString("Profile"),
                rs.getInt("XP"),
                rs.getInt("Streak"),
                rs.getInt("coin"),
                rs.getInt("brain_count")
        );
    }

    public static List<Lesson> getLessonsForCourse(String courseId) {
        List<Lesson> lessons = new ArrayList<>();
        String sql = "SELECT * FROM lesson_table WHERE Course_ID = ? ORDER BY Module_Order ASC, Lesson_order ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lessons.add(new Lesson(
                        rs.getString("Lesson_ID"),
                        rs.getString("Course_ID"),
                        rs.getInt("Lesson_order"),
                        rs.getString("Title"),
                        rs.getInt("XP"),
                        rs.getString("Lesson_type"),
                        rs.getString("Module_Name"),
                        false,            // isPremium အတွက် Default Value (Database ထဲမပါသေးပါက)
                        new ArrayList<>() // slides အတွက် Empty List
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lessons;
    }

    public static Course getCourseById(String courseId) {
        String query = "SELECT Course_ID, Course_Name, description, language FROM course_table WHERE Course_ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Course(
                        rs.getString("Course_ID"),
                        rs.getString("Course_Name"),
                        rs.getString("description"),
                        rs.getString("language")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Fetch how many lessons the user has completed for a course
    public static int getCompletedLessonCount(int userId, String courseId) {
        String sql = "SELECT COALESCE(completed_lesson, 0) FROM user_progress WHERE User_id = ? AND Course_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Save progress and award XP atomically when finishing a lesson
    public static void completeLessonAndAddXP(int userId, String courseId, int lessonOrder, int xpAmount) {
        if (userId <= 0) {
            System.err.println("Progress Save Skipped: Invalid User ID (" + userId + ")");
            return;
        }

        String checkUserSql = "SELECT COUNT(*) FROM user_table WHERE User_ID = ?";

        String progressSql = """
        INSERT INTO user_progress (User_id, Course_id, completed_lesson, status, completed_time)
        VALUES (?, ?, ?, 'IN_PROGRESS', CURRENT_TIMESTAMP)
        ON DUPLICATE KEY UPDATE 
            completed_lesson = GREATEST(completed_lesson, ?),
            completed_time = CURRENT_TIMESTAMP,
            status = IF(GREATEST(completed_lesson, ?) >= (SELECT COUNT(*) FROM lesson_table WHERE Course_ID = ?), 'COMPLETED', 'IN_PROGRESS')
    """;

        String xpSql = "UPDATE user_table SET XP = XP + ? WHERE User_ID = ?";

        try (Connection conn = getConnection()) {
            // Verify user exists in user_table
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.err.println("Progress Save Skipped: User_ID " + userId + " does not exist in user_table.");
                    return;
                }
            }

            conn.setAutoCommit(false);

            // 1. Save progress
            try (PreparedStatement stmt = conn.prepareStatement(progressSql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, courseId);
                stmt.setInt(3, lessonOrder);
                stmt.setInt(4, lessonOrder);
                stmt.setInt(5, lessonOrder);
                stmt.setString(6, courseId);
                stmt.executeUpdate();
            }

            // 2. Award XP
            try (PreparedStatement stmt = conn.prepareStatement(xpSql)) {
                stmt.setInt(1, xpAmount);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getLastActiveCourseId(int userId) {
        String sql = "SELECT Course_id FROM user_progress WHERE User_id = ? ORDER BY completed_time DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Course_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Default fallback if the user has no recorded progress yet
        return "CPP_BASIC";
    }

    // User ၏ Projects များအားလုံးကို Database မှ ဆွဲယူခြင်း
    public static List<Project> getUserProjects(int userId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM user_projects WHERE User_id = ? ORDER BY project_id DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                projects.add(new Project(
                        rs.getInt("project_id"),
                        rs.getInt("User_id"),
                        rs.getString("title"),
                        rs.getString("Language"),
                        rs.getString("Code_Content")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    // Project အသစ်ဖန်တီးပြီး Database ထဲ သိမ်းဆည်းခြင်း
    public static Project createProject(int userId, String title, String language, String codeContent) {
        String sql = "INSERT INTO user_projects (User_id, title, Language, Code_Content) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setString(2, title);
            stmt.setString(3, language);
            stmt.setString(4, codeContent);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                return new Project(newId, userId, title, language, codeContent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateProjectCode(int projectId, String codeContent) {
        String sql = "UPDATE user_projects SET Code_Content = ? WHERE project_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codeContent);
            stmt.setInt(2, projectId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}