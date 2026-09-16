package Project;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession(User user) {
        this.currentUser = user;
    }

    public static void initSession(User user) {
        instance = new UserSession(user);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public int getUserId() {
        return currentUser != null ? currentUser.getUserId() : 1;
    }

    public static void cleanUserSession() {
        instance = null;
    }
}