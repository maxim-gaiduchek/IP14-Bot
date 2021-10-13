package entities;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "username")
    private String username;

    @Column(name = "birthday")
    private String birthday;

    public User() {

    }

    public User(String name, Long chatId, String username, String birthday) {
        this.name = name;
        this.chatId = chatId;
        this.username = username;
        this.birthday = birthday;
    }

    // getters

    public String getNameWithLink() {
        if (chatId != null && chatId != 0) return "[" + name + "](tg://user?id=" + chatId + ")";
        if (username != null && !"".equals(username)) return "[" + name + "](https://t.me/" + username + ")";
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getBirthdayDate() {
        return birthday.substring(0, 5);
    }

    public int getAge(int currentYear) {
        return currentYear - Integer.parseInt(birthday.substring(6));
    }

    public String getBirthdayCommand() {
        return "/" + (username != null ? (username + "\\_") : "") + "s\\_dr";
    }

    // core

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        if (!name.equals(user.name)) return false;
        return Objects.equals(chatId, user.chatId);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();

        result = 31 * result + (chatId != null ? chatId.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "User{" +
               "name='" + name + '\'' +
               ", chatId=" + chatId +
               ", username='" + username + '\'' +
               ", birthday='" + birthday + '\'' +
               '}';
    }
}
