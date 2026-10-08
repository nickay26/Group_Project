package Project;

public class UserSession {
    private static UserSession instance;
    private static User currentUser;
    private static String activeCourseId;

    // Overview Page မှ နှိပ်ပြီး Login သို့ ရောက်လာခြင်း ရှိ/မရှိ မှတ်ရန် Flag
    private static boolean isRedirectedFromOverview = false;

    private UserSession(User user) {
        currentUser = user;
        if (user != null) {
            // ⭐️ ပြင်ဆင်ရန် အပိုင်း: activeCourseId တွင် တန်ဖိုးမရှိသေးပါကမှ Database မှ ယူမည်
            if (activeCourseId == null) {
                activeCourseId = DatabaseManager.getLastActiveCourseId(user.getUserId());
            }
        }
    }

    public static void initSession(User user) {
        currentUser = user;
        if (instance == null) {
            instance = new UserSession(user);
        } else {
            // Instance ရှိပြီးသားဖြစ်ပါကလည်း activeCourseId မရှိမှသာ DB မှယူမည်
            if (activeCourseId == null) {
                activeCourseId = DatabaseManager.getLastActiveCourseId(user.getUserId());
            }
        }
    }

    public static void setCurrentUser(User user) {
        if (user != null) {
            // အကယ်၍ DB မှ coin က 0 ဖြစ်နေပြီး local session မှာ coin ရှိနေရင် Local Coin ကို ထိန်းထားပေးမည်
            if (currentUser != null && user.getCoin() < currentUser.getCoin()) {
                user.setCoin(currentUser.getCoin());
            }
            currentUser = user;
            if (activeCourseId == null) {
                activeCourseId = DatabaseManager.getLastActiveCourseId(user.getUserId());
            }
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

    /**
     * Alias method for getCurrentUser() for codebase convenience.
     */
    public static User getUser() {
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

    // ==========================================
    // BRAIN (ENERGY) & ECONOMY DELEGATE METHODS
    // ==========================================

    public static int getBrainCount() {
        return currentUser != null ? currentUser.getBrainCount() : 0;
    }

    public static void setBrainCount(int count) {
        if (currentUser != null) {
            currentUser.setBrainCount(count);
        }
    }

    public static int getCoin() {
        return currentUser != null ? currentUser.getCoin() : 0;
    }

    public static void setCoin(int coin) {
        if (currentUser != null) {
            currentUser.setCoin(coin);
        }
    }

    // ==========================================
    // အသစ်ထည့်တဲ့ Method များ (Premium Status)
    // ==========================================

    // ⭐️ ဒါကို ထည့် (UserSession မှာ updateUser method မရှိသေးရင်)
    public static void updateUser(User user) {
        currentUser = user;
    }
    // ==========================================
    // အသစ်ထည့်တဲ့ Method (Clean Session)
    // ==========================================

    public static void cleanSession() {
        currentUser = null;
        activeCourseId = null;
        instance = null;
    }
}