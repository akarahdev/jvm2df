package dev.akarah.jvm2df.generator.gen;

import java.io.IOException;
import java.util.List;

public interface ResourceGenerator {
    List<ResourceGenerator> GENERATORS = List.of(
            ParticleGenerator.INSTANCE,
            SoundGenerator.INSTANCE,
            CodeBlocksAPIGenerator.INSTANCE
    );

    void generate() throws IOException;
}
