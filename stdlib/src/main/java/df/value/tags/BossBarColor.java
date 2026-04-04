package df.value.tags;

public enum BossBarColor {
    RED("Red"),
    PURPLE("Purple"),
    PINK("Pink"),
    BLUE("Blue"),
    GREEN("Green"),
    YELLOW("Yellow"),
    WHITE("White");

    final String color;

    BossBarColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return this.color;
    }
}
