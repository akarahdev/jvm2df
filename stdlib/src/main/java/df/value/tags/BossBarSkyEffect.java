package df.value.tags;

public enum BossBarSkyEffect {
    NONE("None"),
    CREATE_FOG("Create fog"),
    DARKEN_SKY("Darken sky"),
    BOTH("Both");

    final String tag;

    BossBarSkyEffect(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return this.tag;
    }
}
