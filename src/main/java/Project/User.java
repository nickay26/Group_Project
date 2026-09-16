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

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getProfile() { return profile; }
    public int getXp() { return xp; }
    public int getStreak() { return streak; }
    public int getCoin() { return coin; }
    public int getBrainCount() { return brainCount; }
}