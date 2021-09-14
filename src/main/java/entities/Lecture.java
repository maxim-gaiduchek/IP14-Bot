package entities;

import entities.enums.LectureCount;
import entities.enums.LectureType;
import entities.enums.WeekCount;
import entities.enums.WeekDay;

import java.util.Objects;

public class Lecture {

    private final String name;
    private final LectureType type;
    private final String lecturer;
    private final String room;
    private final String link;

    private final WeekDay weekDay;
    private final LectureCount lectureCount;
    private final WeekCount weekCount;

    public Lecture(WeekDay weekDay, LectureCount lectureCount, WeekCount weekCount,
                   String name, LectureType type, String lecturer, String room, String link) {
        this.name = name;
        this.type = type;
        this.lecturer = lecturer;
        this.room = room;
        this.link = link;
        this.weekDay = weekDay;
        this.lectureCount = lectureCount;
        this.weekCount = weekCount;
    }

    // getters

    public String getLectureInfo() {
        return "*" + name + "*\n" +
               type.getName() + "\n" +
               "Аудитория: " + room + ", препод: " + lecturer + "\n" +
               (type == LectureType.LECTURE ? ("Онлайн, " + (link != null ? ("[линк тут](" + link + ")") : "ссылки нет")) : "Практика очная");
    }

    public WeekDay getWeekDay() {
        return weekDay;
    }

    public LectureCount getLectureCount() {
        return lectureCount;
    }

    public WeekCount getWeekCount() {
        return weekCount;
    }

    // core

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lecture lecture)) return false;

        if (type != lecture.type) return false;
        if (weekDay != lecture.weekDay) return false;
        if (lectureCount != lecture.lectureCount) return false;
        if (!Objects.equals(name, lecture.name)) return false;
        if (!Objects.equals(lecturer, lecture.lecturer)) return false;
        if (!Objects.equals(room, lecture.room)) return false;
        if (!Objects.equals(link, lecture.link)) return false;
        return weekCount == lecture.weekCount;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (lecturer != null ? lecturer.hashCode() : 0);
        result = 31 * result + (room != null ? room.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (weekDay != null ? weekDay.hashCode() : 0);
        result = 31 * result + (lectureCount != null ? lectureCount.hashCode() : 0);
        result = 31 * result + (weekCount != null ? weekCount.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Lecture{" +
               "name='" + name + '\'' +
               ", type='" + type + '\'' +
               ", lecturer='" + lecturer + '\'' +
               ", room='" + room + '\'' +
               ", link='" + link + '\'' +
               ", weekDay=" + weekDay +
               ", lectureCount=" + lectureCount +
               ", weekCount=" + weekCount +
               '}';
    }
}
