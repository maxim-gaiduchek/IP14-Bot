package entities.enums;

public enum LectureCount {

    FIRST(1, "5:30", "07:05"),
    SECOND(2, "07:25", "09:00"),
    THIRD(3, "09:20", "11:55"),
    FORTH(4, "11:15", "13:50"),
    FIFTH(5, "13:10", "14:45"),
    SIXTH(6, "15:30", "17:05");

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
