package entities;

import utils.Formatter;

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

    public Long getChatId() {
        return chatId;
    }

    public String getNameWithLink() {
        if (chatId != null && chatId != 0) {
            return "[" + getFormattedName() + "](tg://user?id=" + chatId + ")";
        }
        if (username != null && !"".equals(username)) {
            return "[" + getFormattedName() + "](https://t.me/" + username + ")";
        }
        return getFormattedName();
    }

    public String getFormattedName() {
        return Formatter.formatTelegramText(name);
    }

    public String getFormattedSurname() {
        return Formatter.formatTelegramText(surname);
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
        return Formatter.formatTelegramText("/" + (username != null ? (username + "_") : "") + "s_dr");
    }

    // core

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!chatId.equals(user.chatId)) return false;
        if (!name.equals(user.name)) return false;
        if (!Objects.equals(surname, user.surname)) return false;
        if (!Objects.equals(username, user.username)) return false;
        return Objects.equals(birthday, user.birthday);
    }

    @Override
    public int hashCode() {
        int result = chatId.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", username='" + username + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}
