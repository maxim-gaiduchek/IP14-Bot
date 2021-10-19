package entities;

import entities.enums.LectureCount;
import entities.enums.LectureType;
import entities.enums.WeekCount;
import entities.enums.WeekDay;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lectures")
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private LectureType type;

    @Column(name = "lecturer")
    private String lecturer;

    @Column(name = "room")
    private String room;

    @Column(name = "link")
    private String link;

    @Column(name = "week_day")
    @Enumerated(EnumType.ORDINAL)
    private WeekDay weekDay;

    @Column(name = "lecture_count")
    @Enumerated(EnumType.ORDINAL)
    private LectureCount lectureCount;

    @Column(name = "week_count")
    @Enumerated(EnumType.ORDINAL)
    private WeekCount weekCount;

    public Lecture() {

    }

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
        boolean isOnline = type == LectureType.LECTURE;

        return "(" + lectureCount.getStartTime() + "-" + lectureCount.getEndTime() + ") *" + name + "*\n" +
                type.getName() + "\n" +
                "*Препод:* " + lecturer + (!isOnline ? (", *аудитория:* " + room) : "") + "\n" +
                "*Онлайн*, " + (link != null ? ("[линк тут](" + link + ")") : "ссылки нет");

        /*return "(" + lectureCount.getStartTime() + "-" + lectureCount.getEndTime() + ") *" + name + "*\n" +
                type.getName() + "\n" +
                "*Препод:* " + lecturer + (!isOnline ? (", *аудитория:* " + room) : "") + "\n" +
                (isOnline ? ("*Онлайн*, " + (link != null ? ("[линк тут](" + link + ")") : "ссылки нет")) : "Практика *очная*");*/
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
