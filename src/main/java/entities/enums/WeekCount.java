package entities.enums;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public enum WeekCount {

    FIRST, SECOND;

    private static final DateFormat FORMAT_WEEK_COUNT = new SimpleDateFormat("ww");

    static {
        FORMAT_WEEK_COUNT.setTimeZone(TimeZone.getTimeZone("GMT+3"));
    }

    public static WeekCount getCurrentWeekCount() {
        return getWeekCount(new Date());
    }

    public static WeekCount getWeekCount(Date date) {
        return Integer.parseInt(FORMAT_WEEK_COUNT.format(date)) % 2 == 0 ? FIRST : SECOND;
    }
}
