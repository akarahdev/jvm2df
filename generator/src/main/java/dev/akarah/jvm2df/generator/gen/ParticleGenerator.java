package dev.akarah.jvm2df.generator.gen;

import dev.akarah.jvm2df.generator.fetch.ActiondumpFetch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class ParticleGenerator implements ResourceGenerator {
    public static ParticleGenerator INSTANCE = new ParticleGenerator();

    private ParticleGenerator() {
    }

    public void generate() throws IOException {
        var instance = ActiondumpFetch.INSTANCE.fetch();

        var dedup = new HashSet<String>();

        var sb = new StringBuilder();
        sb.append("package diamondfire.presets;\n\n");
        sb.append("public class ParticleTypes {\n");
        for (var particle : instance.particles()) {
            var idName = particle.particle();
            if (dedup.contains(idName)) {
                continue;
            }
            dedup.add(idName);
            var regularName = particle.icon().name();
            sb.append("    public static final String ")
                    .append(idName)
                    .append(" = ")
                    .append('"')
                    .append(regularName)
                    .append('"')
                    .append(";\n");
        }
        sb.append("\n}");

        var path = Path.of("../stdlib/src/generated/java/diamondfire/presets/ParticleTypes.java");
        Files.deleteIfExists(path);
        Files.writeString(path, sb.toString());
    }
}
