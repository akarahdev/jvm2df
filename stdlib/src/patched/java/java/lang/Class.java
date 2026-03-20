package java.lang;

public class Class<T> {
    String className;

    public Class(String className) {
        this.className = className;
    }

    public static Class<?> forName(String name) {
        return new Class<>(name);
    }

    @Override
    public String toString() {
        return "Class{".concat(this.className).concat("}");
    }
}
