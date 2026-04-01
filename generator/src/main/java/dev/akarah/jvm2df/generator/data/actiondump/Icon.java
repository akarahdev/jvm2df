package dev.akarah.jvm2df.generator.data.actiondump;

public record Icon(
        String name
) {
    public record Argument(
            ArgumentType type,
            boolean plural,
            boolean optional,
            String text,
            String[] description
    ) {

    }

    public enum ArgumentType {
        NUMBER,
        COMPONENT,
        STRING,
        LOCATION,
        VECTOR,
        SOUND,
        PARTICLE,
        POTION,
        VARIABLE,
        LIST,
        DICTIONARY
    }
}
