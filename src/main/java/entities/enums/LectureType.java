package entities.enums;

public enum LectureType {

    LECTURE("Лекція"),
    PRACTICE("Практика"),
    LABORATORY("Лабораторка");

    private final String name;

    LectureType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
