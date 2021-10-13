package datasource.repositories;

import entities.Lecture;
import entities.enums.WeekCount;
import entities.enums.WeekDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LecturesRepository extends JpaRepository<Lecture, Integer> {

    @Query("SELECT lecture FROM Lecture lecture WHERE lecture.weekDay = ?1 AND lecture.weekCount = ?2")
    List<Lecture> getAllByWeekDayAndWeekCount(WeekDay weekDay, WeekCount weekCount);
}
