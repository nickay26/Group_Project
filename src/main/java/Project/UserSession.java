package Project;

public class UserSession {
    private static UserSession instance;
    private static User currentUser;
    private static String activeCourseId;

    private static boolean isRedirectedFromOverview = false;

    private UserSession(User user) {
        currentUser = user;
        if (user != null) {
            if (activeCourseId == null) {
                activeCourseId = DatabaseManager.getLastActiveCourseId(user.getUserId());
            }
        }
    }

    public static void initSession(User user) {
        if (currentUser != null && user != null) {
            // Local Session coin ကို မလျော့သွားစေရန် ထိန်းသိမ်းသည်
            int safeCoin = Math.max(currentUser.getCoin(), user.getCoin());
            user.setCoin(safeCoin);
        }
        currentUser = user;

        if (instance == null) {
            instance = new UserSession(user);
        } else {
            if (activeCourseId == null) {
                activeCourseId = DatabaseManager.getLastActiveCourseId(user.getUserId());
            }
        }
    }

    public static void setCurrentUser(User user) {
        if (currentUser != null && user != null) {
            // Backend မှ တန်ဖိုးနည်းပြီး ရောက်လာခဲ့ပါက Local coin အမြင့်ဆုံးတန်ဖိုးကိုပဲ ဆက်ထိန်းထားမည်
            int safeCoin = Math.max(currentUser.getCoin(), user.getCoin());
            user.setCoin(safeCoin);
        }
        currentUser = user;

        if (user != null && activeCourseId == null) {
            activeCourseId = DatabaseManager.getLastActiveCourseId(user.getUserId());
        }
        if (instance == null) {
            instance = new UserSession(user);
        }
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    // ⭐ Coin တိုက်ရိုက် ပေါင်းပေးမည့် မသဒ်
    public static void addCoins(int amount) {
        if (currentUser != null) {
            currentUser.setCoin(currentUser.getCoin() + amount);
        }
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

    public static boolean isRedirectedFromOverview() {
        return isRedirectedFromOverview;
    }

    public static void setRedirectedFromOverview(boolean redirected) {
        isRedirectedFromOverview = redirected;
    }

    public static void clearSession() {
        currentUser = null;
        activeCourseId = null;
        isRedirectedFromOverview = false;
        instance = null;
    }

    public static boolean isPremium() {
        if (currentUser == null) {
            return false;
        }
        return PremiumService.isPremium(currentUser.getUserId());
    }

    public static void cleanSession() {
        currentUser = null;
        activeCourseId = null;
        instance = null;
    }
}