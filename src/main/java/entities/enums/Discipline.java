package entities.enums;

public enum Discipline {

    DB("БД", 5);

    private final String title;
    private final int maxLabs;

    Discipline(String title, int maxLabs) {
        this.title = title;
        this.maxLabs = maxLabs;
    }

    public String getTitle() {
        return title;
    }

    public int getMaxLabs() {
        return maxLabs;
    }
}
