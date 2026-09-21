package Project;

public class UserSession {
    private static UserSession instance;
    private static User currentUser;
    private static String activeCourseId;

    private UserSession(User user) {
        currentUser = user;
        // Fetch last active course from DB for this user
        activeCourseId = DatabaseManager.getLastActiveCourseId(user.getUserId());
    }

    public static void initSession(User user) {
        currentUser = user;
        instance = new UserSession(user);
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        activeCourseId = DatabaseManager.getLastActiveCourseId(user.getUserId());
        if (instance == null) {
            instance = new UserSession(user);
        }
    }

    public static UserSession getInstance() {
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public static int getUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    public static String getActiveCourseId() {
        if (activeCourseId == null && currentUser != null) {
            activeCourseId = DatabaseManager.getLastActiveCourseId(currentUser.getUserId());
        }
        return activeCourseId != null ? activeCourseId : "CPP_BASIC";
    }

    public static void setActiveCourseId(String courseId) {
        activeCourseId = courseId;
    }
}