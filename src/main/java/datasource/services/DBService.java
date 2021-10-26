package datasource.services;

import entities.Lecture;
import entities.Queue;
import entities.User;
import entities.enums.Discipline;
import entities.enums.WeekCount;
import entities.enums.WeekDay;

import java.util.List;

public interface DBService {

    // users

    User getUser(Long chatId);

    List<User> getAllUsers();

    List<User> getUsersByBirthday(String birthday);

    boolean isUserOfIP14(Long chatId);

    // lectures

    List<Lecture> getAllLectures(WeekDay weekDay, WeekCount weekCount);

    // queue

    Queue getUserQuery(User user, Discipline discipline, int labNumber);

    Queue getLastQueue(Discipline discipline, int labNumber);

    List<Integer> getAllUserQueuedLabNumbers(User user);

    List<Queue> getFullQueue(Discipline discipline);

    List<Queue> getFirst3(Discipline discipline);

    void addQueue(Queue queue);

    void removeQueue(Queue queue);

    int countQueue(Discipline discipline);

    int countUserQueues(User user, Discipline discipline, int labNumber);

    boolean isQueueHasUser(User user, Discipline discipline, int labNumber);
}
