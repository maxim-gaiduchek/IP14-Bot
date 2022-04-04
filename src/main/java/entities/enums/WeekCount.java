package entities.enums;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public enum WeekCount {

    FIRST, SECOND;

    private static final DateFormat FORMAT_WEEK_COUNT = new SimpleDateFormat("ww");
    private static final int WEEK_START_COUNT;

    static {
        FORMAT_WEEK_COUNT.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));

        if (System.getenv("WEEK_START_COUNT") != null) {
            WEEK_START_COUNT = Integer.parseInt(System.getenv("WEEK_START_COUNT")) % 2;
        } else {
            WEEK_START_COUNT = 0;
        }
    }

    public static WeekCount getCurrentWeekCount() {
        return getWeekCount(new Date());
    }

    public static WeekCount getWeekCount(Date date) {
        int week = Integer.parseInt(FORMAT_WEEK_COUNT.format(date));

        if (WeekDay.getCurrentWeekDay() == WeekDay.SUNDAY) {
            return week % 2 == WEEK_START_COUNT ? SECOND : FIRST;
        }

        return week % 2 == WEEK_START_COUNT ? FIRST : SECOND;
    }
}
