package java.lang;

public final class Class<T> {
    String descriptor;

    public Class(String descriptor) {
        this.descriptor = descriptor;
    }

    public static Class<?> forName(String name) {
        return new Class<>("L".concat(name).concat(";"));
    }

    @Override
    public String toString() {
        return this.descriptor;
    }
}
