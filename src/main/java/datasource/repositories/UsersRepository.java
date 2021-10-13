package datasource.repositories;

import entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UsersRepository extends JpaRepository<User, Long> {

    @Query("SELECT user FROM User user WHERE user.birthday LIKE CONCAT(?1, '%') ")
    List<User> getAllByBirthday(String birthday);
}
