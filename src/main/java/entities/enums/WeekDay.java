package entities.enums;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public enum WeekDay {

    MONDAY(1, "Понедельник", "пн"),
    TUESDAY(2, "Вторник", "вт"),
    WEDNESDAY(3, "Среда", "ср"),
    THURSDAY(4, "Четверг", "чт"),
    FRIDAY(5, "Пятница", "пт"),
    SATURDAY(6, "Суббота", "сб"),
    SUNDAY(7, "Воскресенье", "вс");

    private final int count;
    private final String dayName;
    private final String prefix;

    private static final DateFormat FORMAT = new SimpleDateFormat("EEE");

    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+3"));
    }

    WeekDay(int count, String dayName, String prefix) {
        this.count = count;
        this.dayName = dayName;
        this.prefix = prefix;
    }

    public int getCount() {
        return count;
    }

    public String getDayName() {
        return dayName;
    }

    public static WeekDay getCurrentWeekDay() {
        return getWeekDay(new Date());
    }

    public static WeekDay getNextWeekDay() {
        return getWeekDay(new Date(new Date().getTime() + 24 * 60 * 60 * 1000));
    }

    public static WeekDay getWeekDayByCounter(int count) {
        count %= 7;

        for (WeekDay day : values()) {
            if (day.count == count) return day;
        }

        return null;
    }

    public static WeekDay getWeekDay(Date date) {
        String prefix = FORMAT.format(date);

        for (WeekDay weekDay : values()) {
            if (weekDay.prefix.equals(prefix) || weekDay.name().startsWith(prefix.toUpperCase())) {
                return weekDay;
            }
        }

        return null;
    }
}
