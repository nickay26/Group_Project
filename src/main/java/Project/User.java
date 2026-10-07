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

    // ⭐ Premium Fields
    private boolean premium = false;
    private String subscriptionType = "free";
    private String planType = "FREE";

    public User(int userId, String name, String email, String profile,
                int xp, int streak, int coin, int brainCount) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profile = profile;
        this.xp = xp;
        this.streak = streak;
        this.coin = coin;
        this.brainCount = brainCount;
    }

    // Default Constructor တွင် Brain ကို 5 ပေးထားပါမည်
    public User(int userId, String name, String email) {
        this(userId, name, email, "", 0, 0, 0, 5);
    }

    // Setters
    public void setPremium(boolean premium) { this.premium = premium; }
    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }
    public void setPlanType(String planType) { this.planType = planType; }

    // Getters
    // --- Getters ---
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getProfile() { return profile; }
    public int getXp() { return xp; }
    public int getStreak() { return streak; }
    public int getCoin() { return coin; }
    public int getBrainCount() { return brainCount; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setProfile(String profile) { this.profile = profile; }
    public void setXp(int xp) { this.xp = xp; }
    public void setStreak(int streak) { this.streak = streak; }
    public void setCoin(int coin) { this.coin = coin; }
    public void setBrainCount(int brainCount) { this.brainCount = brainCount; }

    public String getUsername() {
        return name;
    }

    public String getProfilePicture() {
        return profile;
    }
    public boolean isPremium() { return premium; }
    public String getSubscriptionType() { return subscriptionType; }
}