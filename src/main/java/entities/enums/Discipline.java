package entities.enums;

public enum Discipline {

    DB("БД", 5),
    AP("ПА", 6),
    WEB("Веб", 7);

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
