package datasource.repositories;

import entities.Queue;
import entities.User;
import entities.enums.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface QueueRepository extends JpaRepository<Queue, Long> {

    @Query("SELECT queue FROM Queue queue WHERE queue.user = ?1 AND queue.discipline = ?2 AND queue.labNumber = ?3")
    Queue getByUser(User user, Discipline discipline, int labNumber);

    @Query("""
        SELECT queue
        FROM Queue queue
        WHERE queue.discipline = ?1 AND queue.queueNumber > 3
            AND queue.labNumber = (SELECT MAX(maxQueue.labNumber) FROM Queue maxQueue WHERE maxQueue.labNumber <= ?2 AND maxQueue.queueNumber > 3)
            AND queue.queueNumber = (SELECT MAX(maxQueue.queueNumber) FROM Queue maxQueue WHERE
                    maxQueue.labNumber = (SELECT MAX(maxLabQueue.labNumber) FROM Queue maxLabQueue WHERE maxLabQueue.labNumber <= ?2 AND maxLabQueue.queueNumber > 3))""")
    Queue getLastByDisciplineAndLabNumber(Discipline discipline, int labNumber);

    @Query("SELECT queue FROM Queue queue WHERE queue.discipline = ?1 AND queue.queueNumber <= 3 ORDER BY queue.queueNumber")
    List<Queue> getFirst3(Discipline discipline);

    @Query("SELECT queue.labNumber FROM Queue queue WHERE queue.user = ?1 ORDER BY queue.labNumber")
    List<Integer> getAllLabNumbersByUser(User user);

    @Query("SELECT queue FROM Queue queue WHERE queue.discipline = ?1 ORDER BY queue.queueNumber")
    List<Queue> getAllByDiscipline(Discipline discipline);

    @Modifying
    @Transactional
    @Query("UPDATE Queue queue SET queue.queueNumber = queue.queueNumber + 1 WHERE queue.discipline = ?1 AND queue.queueNumber >= ?2")
    void moveAllQueueNumbersDown(Discipline discipline, int queueNumber);

    @Modifying
    @Transactional
    @Query("UPDATE Queue queue SET queue.queueNumber = queue.queueNumber - 1 WHERE queue.discipline = ?1 AND queue.queueNumber >= ?2")
    void moveAllQueueNumbersUp(Discipline discipline, int queueNumber);

    @Query("SELECT COUNT(queue) FROM Queue queue WHERE queue.discipline = ?1")
    int countQueue(Discipline discipline);

    @Query("SELECT COUNT(queue) FROM Queue queue WHERE queue.user = ?1 AND queue.discipline = ?2 AND queue.labNumber = ?3")
    int countByUser(User user, Discipline discipline, int labNumber);
}
