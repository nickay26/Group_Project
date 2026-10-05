package Project;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class SeasonManager {

    public static String getCurrentSeasonId() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int week = now.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        return "SEASON_" + year + "_W" + week;
    }

    public static String getStartDate() {
        return LocalDate.now().with(DayOfWeek.MONDAY).toString(); // e.g., "2026-09-28"
    }

    public static String getEndDate() {
        return LocalDate.now().with(DayOfWeek.SUNDAY).toString(); // e.g., "2026-10-04"
    }
}