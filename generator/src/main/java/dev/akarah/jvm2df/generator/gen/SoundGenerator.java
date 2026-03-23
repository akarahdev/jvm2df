package dev.akarah.jvm2df.generator.gen;

import dev.akarah.jvm2df.generator.fetch.ActiondumpFetch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class SoundGenerator implements ResourceGenerator {
    public static SoundGenerator INSTANCE = new SoundGenerator();

    private SoundGenerator() {
    }

    public void generate() throws IOException {
        var instance = ActiondumpFetch.INSTANCE.fetch();

        var dedup = new HashSet<String>();

        var sb = new StringBuilder();
        sb.append("package diamondfire.presets;\n\n");
        sb.append("public class SoundTypes {\n");
        for (var particle : instance.sounds()) {
            var idName = particle.sound();
            if (dedup.contains(idName)) {
                continue;
            }
            dedup.add(idName);
            var regularName = particle.soundId();
            sb.append("    public static final String ")
                    .append(idName)
                    .append(" = ")
                    .append('"')
                    .append(regularName)
                    .append('"')
                    .append(";\n");
        }
        sb.append("\n}");

        var path = Path.of("../stdlib/src/generated/java/diamondfire/presets/SoundTypes.java");
        Files.deleteIfExists(path);
        Files.writeString(path, sb.toString());
    }
}
