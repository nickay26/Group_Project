package Project;

import java.util.List;

public class UserActivity {
    private int user_id;
    private int current_streak;
    private boolean is_active_today;
    private int lessons_today;
    private List<DayActivity> weekly_history;

    // Getters and Setters
    public int getUserId() { return user_id; }
    public int getCurrentStreak() { return current_streak; }
    public boolean isActiveToday() { return is_active_today; }
    public int getLessonsToday() { return lessons_today; }
    public List<DayActivity> getWeeklyHistory() { return weekly_history; }
}