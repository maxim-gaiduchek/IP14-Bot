package entities.enums;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public enum WeekDay {

    MONDAY(1, "Понеділок", "пн"),
    TUESDAY(2, "Вівторок", "вт"),
    WEDNESDAY(3, "Середа", "ср"),
    THURSDAY(4, "Четвер", "чт"),
    FRIDAY(5, "П'ятниця", "пт"),
    SATURDAY(6, "Субота", "сб"),
    SUNDAY(7, "Неділя", "нд");

    private final int count;
    private final String dayName;
    private final String prefix;

    private static final DateFormat FORMAT = new SimpleDateFormat("EEE");

    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
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

        if (count == 0) count = 7;

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
