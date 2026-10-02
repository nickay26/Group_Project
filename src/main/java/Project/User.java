package Project;

public class User {
    private int userId;
    private String name;
    private String email;
    private String profile;
    private int xp;
    private int streak;
    private int coin;
    private int brainCount;

    public User(int userId, String name, String email, String profile, int xp, int streak, int coin, int brainCount) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profile = profile;
        this.xp = xp;
        this.streak = streak;
        this.coin = coin;
        this.brainCount = brainCount;
    }

    public User(int userId, String name, String email) {
        this(userId, name, email, "", 0, 0, 0, 0);
    }

    // --- Getters ---
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getProfile() { return profile; }
    public int getXp() { return xp; }
    public int getStreak() { return streak; }
    public int getCoin() { return coin; }
    public int getBrainCount() { return brainCount; }

    // --- Setters ---
    public void setName(String name) { this.name = name; }
    public void setProfile(String profile) { this.profile = profile; }
    public void setXp(int xp) { this.xp = xp; }
    public void setStreak(int streak) { this.streak = streak; }
    public void setCoin(int coin) { this.coin = coin; }
    public void setBrainCount(int brainCount) { this.brainCount = brainCount; }
}