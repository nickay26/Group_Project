package Project;

public class UserSession {
    private static UserSession instance;
    private final int userId;
    private final String email;
    private final String name;

    private UserSession(int userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public static void createSession(int userId, String email, String name) {
        instance = new UserSession(userId, email, name);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static void destroySession() {
        instance = null;
    }

    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}