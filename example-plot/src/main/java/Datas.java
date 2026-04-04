public enum Datas {
    A("A"),
    B("B");

    final String name;

    Datas(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
