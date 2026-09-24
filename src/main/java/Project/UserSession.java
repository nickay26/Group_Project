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
            // ⭐ ပြင်ဆင်ရန် အပိုင်း: activeCourseId တွင် တန်ဖိုးမရှိသေးပါကမှ Database မှ ယူမည်
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
}