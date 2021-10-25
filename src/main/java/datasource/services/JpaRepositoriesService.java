package datasource.services;

import datasource.repositories.LecturesRepository;
import datasource.repositories.UsersRepository;
import entities.Lecture;
import entities.User;
import entities.enums.WeekCount;
import entities.enums.WeekDay;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class JpaRepositoriesService implements DBService {

    private final UsersRepository usersRepository;
    private final LecturesRepository lecturesRepository;

    public JpaRepositoriesService(UsersRepository usersRepository, LecturesRepository lecturesRepository) {
        this.usersRepository = usersRepository;
        this.lecturesRepository = lecturesRepository;
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

    // lectures

    @Override
    public List<Lecture> getAllLectures(WeekDay weekDay, WeekCount weekCount) {
        return lecturesRepository.getAllByWeekDayAndWeekCount(weekDay, weekCount).stream()
                .sorted(Comparator.comparing(Lecture::getLectureCount))
                .toList();
    }
}
