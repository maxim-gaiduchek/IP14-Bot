package datasource.services;

import entities.Lecture;
import entities.User;
import entities.enums.WeekCount;
import entities.enums.WeekDay;

import java.util.List;

public interface DBService {

    // users

    User getUser(Long chatId);

    List<User> getUsersByBirthday(String birthday);

    // lectures

    List<Lecture> getAllLectures(WeekDay weekDay, WeekCount weekCount);
}
