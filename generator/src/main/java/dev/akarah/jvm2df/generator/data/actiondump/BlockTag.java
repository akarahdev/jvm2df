package dev.akarah.jvm2df.generator.data.actiondump;

public record BlockTag(
        String name,
        Option[] options,
        int slot
) {
    public record Option(
            String name,
            Icon icon,
            String[] aliases
    ) {

    }
}
