package Project;

public class PremiumService {

    // ==========================================
    // ၁။ User Premium ဟုတ်/မဟုတ် စစ်ဆေးခြင်း
    //    (HTTP API ကနေ စစ်)
    // ==========================================
    public static boolean isPremium(int userId) {
        return DatabaseManager.isPremium(userId);
    }

    // ==========================================
    // ၂။ Premium ဝယ်ခြင်း
    //    (HTTP API ကို ခေါ်)
    // ==========================================
    public static boolean subscribePremium(int userId, int planId, String durationType) {
        return DatabaseManager.subscribePremium(userId, planId, durationType);
    }

    // ==========================================
    // ၃။ Lesson ဖွင့်ခွင့် ရှိ/မရှိ စစ်ဆေးခြင်း
    //    (Java, C++, Python အားလုံးအတွက်)
    // ==========================================
    public static boolean canAccessLesson(int userId, int lessonOrder) {
        if (isPremium(userId)) {
            return true;
        }
        return lessonOrder <= 3;
    }
    // ==========================================
    // ၄။ Project Create လုပ်ခွင့် ရှိ/မရှိ စစ်ဆေးခြင်း
    // ==========================================
    public static boolean canCreateProject(int userId) {
        if (isPremium(userId)) {
            return true;
        }
        int currentProjectCount = DatabaseManager.getUserProjects(userId).size();
        return currentProjectCount < 3;
    }

    // ==========================================
    // ၅။ Free User အတွက် ကျန်တဲ့ Project အရေအတွက်
    // ==========================================
    public static int getRemainingProjectSlots(int userId) {
        if (isPremium(userId)) {
            return -1;
        }
        int currentProjectCount = DatabaseManager.getUserProjects(userId).size();
        int remaining = 3 - currentProjectCount;
        return Math.max(remaining, 0);
    }

}