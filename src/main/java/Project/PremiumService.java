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
}