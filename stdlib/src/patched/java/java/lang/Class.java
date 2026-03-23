package java.lang;

public class Class<T> {
    String descriptor;

    public Class(String descriptor) {
        this.descriptor = descriptor;
    }

    public static Class<?> forName(String name) {
        return new Class<>("L".concat(name).concat(";"));
    }

    @Override
    public String toString() {
        return "Class{".concat(this.descriptor).concat("}");
    }
}
