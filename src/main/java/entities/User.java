package entities;

import java.util.Objects;

public class User {

    private String name;
    private String link;
    private String birthday;

    public User(String name, String link, String birthday) {
        this.name = name;
        this.link = link;
        this.birthday = birthday;
    }

    // getters

    public String getNameWithLink() {
        return link == null ? name : ("[" + name + "](https://t.me/" + link + ")");
    }

    public String getBirthday() {
        return birthday;
    }

    // core

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        if (!Objects.equals(name, user.name)) return false;
        if (!Objects.equals(link, user.link)) return false;
        return Objects.equals(birthday, user.birthday);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
               "name='" + name + '\'' +
               ", link='" + link + '\'' +
               ", birthday='" + birthday + '\'' +
               '}';
    }
}
