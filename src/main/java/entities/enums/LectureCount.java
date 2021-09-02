package entities.enums;

public enum LectureCount {

    FIRST(1, "23:38", "23:39"),
    SECOND(2, "10:25", "12:00"),
    THIRD(3, "12:20", "13:55"),
    FORTH(4, "14:15", "15:50"),
    FIFTH(5, "16:10", "17:45"),
    SIXTH(6, "18:30", "20:05");

    private final int count;
    private final String startTime, endTime;

    LectureCount(int count, String startTime, String endTime) {
        this.count = count;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getCount() {
        return count;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
