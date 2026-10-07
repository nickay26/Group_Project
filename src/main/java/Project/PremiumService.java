package Project;

public class PremiumService {

    public static final int MAX_FREE_BRAINS = 5;

    // ==========================================
    // UI အတွက် (User object သုံး — HTTP မခေါ်)
    // ==========================================

    public static boolean isPremium(User user) {
        if (user == null) return false;
        return user.isPremium() ||
                (!"free".equalsIgnoreCase(user.getSubscriptionType()));
    }

    // ⭐ Header မှာ ∞ / 5 ပြဖို့
    public static String getBrainDisplay(User user) {
        if (user == null) return "0";
        return isPremium(user) ? "∞" : String.valueOf(user.getBrainCount());
    }

    public static boolean canUseBrain(User user) {
        if (user == null) return false;
        if (isPremium(user)) return true;
        return user.getBrainCount() > 0;
    }

    // ⭐ Brain လျှော့ (Premium ဆိုရင် မလျှော့)
    public static void consumeBrain(User user) {
        if (user == null) return;
        if (isPremium(user)) return;

        int current = user.getBrainCount();
        if (current > 0) {
            user.setBrainCount(current - 1);
            DatabaseManager.updateBrainCount(user.getUserId(), user.getBrainCount());
        }
    }

    public static void activatePremium(User user, String planType) {
        if (user == null) return;
        user.setPremium(true);
        user.setSubscriptionType(planType);
    }

    // ==========================================
    // မူလ method များ (HTTP API သုံး)
    // ==========================================

    public static boolean isPremium(int userId) {
        return DatabaseManager.isPremium(userId);
    }

    public static boolean subscribePremium(int userId, int planId, String durationType) {
        return DatabaseManager.subscribePremium(userId, planId, durationType);
    }

    public static boolean canAccessLesson(int userId, int lessonOrder) {
        if (isPremium(userId)) return true;
        return lessonOrder <= 3;
    }

    public static boolean canCreateProject(int userId) {
        if (isPremium(userId)) return true;
        int currentProjectCount = DatabaseManager.getUserProjects(userId).size();
        return currentProjectCount < 3;
    }

    public static int getRemainingProjectSlots(int userId) {
        if (isPremium(userId)) return -1;
        int currentProjectCount = DatabaseManager.getUserProjects(userId).size();
        return Math.max(3 - currentProjectCount, 0);
    }
}