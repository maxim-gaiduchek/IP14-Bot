package datasource.services;

import datasource.repositories.LecturesRepository;
import datasource.repositories.QueueRepository;
import datasource.repositories.UsersRepository;
import entities.Lecture;
import entities.Queue;
import entities.User;
import entities.enums.Discipline;
import entities.enums.WeekCount;
import entities.enums.WeekDay;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class JpaRepositoriesService implements DBService {

    private final UsersRepository usersRepository;
    private final LecturesRepository lecturesRepository;
    private final QueueRepository queueRepository;

    public JpaRepositoriesService(UsersRepository usersRepository, LecturesRepository lecturesRepository,
                                  QueueRepository queueRepository) {
        this.usersRepository = usersRepository;
        this.lecturesRepository = lecturesRepository;
        this.queueRepository = queueRepository;
    }

    // users

    @Override
    public User getUser(Long chatId) {
        return usersRepository.findById(chatId).orElse(null);
    }

    @Override
    public List<User> getUsersByBirthday(String birthday) {
        return usersRepository.getAllByBirthday(birthday);
    }

    @Override
    public boolean isUserOfIP14(Long chatId) {
        return usersRepository.existsById(chatId);
    }

    // lectures

    @Override
    public List<Lecture> getAllLectures(WeekDay weekDay, WeekCount weekCount) {
        return lecturesRepository.getAllByWeekDayAndWeekCount(weekDay, weekCount).stream()
                .sorted(Comparator.comparing(Lecture::getLectureCount))
                .toList();
    }

    // queue

    @Override
    public Queue getUserQuery(User user, Discipline discipline, int labNumber) {
        return queueRepository.getByUser(user, discipline, labNumber);
    }

    @Override
    public Queue getLastQueue(Discipline discipline, int labNumber) {
        return queueRepository.getLastByDisciplineAndLabNumber(discipline, labNumber);
    }

    @Override
    public List<Integer> getAllUserQueuedLabNumbers(User user) {
        return queueRepository.getAllLabNumbersByUser(user);
    }

    @Override
    public List<Queue> getFullQueue(Discipline discipline) {
        return queueRepository.getAllByDiscipline(discipline);
    }

    @Override
    public List<Queue> getFirst3(Discipline discipline) {
        return queueRepository.getFirst3(discipline);
    }

    @Override
    public void addQueue(Queue queue) {
        queueRepository.moveAllQueueNumbersDown(queue.getDiscipline(), queue.getQueueNumber());
        queueRepository.save(queue);
    }

    @Override
    public void removeQueue(Queue queue) {
        Discipline discipline = queue.getDiscipline();
        int queueNumber = queue.getQueueNumber();

        queueRepository.delete(queue);
        queueRepository.moveAllQueueNumbersUp(discipline, queueNumber);
    }

    @Override
    public int countQueue(Discipline discipline) {
        return queueRepository.countQueue(discipline);
    }

    @Override
    public boolean isQueueHasUser(User user, Discipline discipline, int labNumber) {
        return queueRepository.countByUser(user, discipline, labNumber) > 0;
    }
}
