package dev.akarah.jvm2df.generator.data.actiondump;

public record ActionDump(
        Particle[] particles,
        Sound[] sounds,
        Action[] actions
) {
}
