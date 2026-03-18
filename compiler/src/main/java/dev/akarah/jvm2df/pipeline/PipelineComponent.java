package dev.akarah.jvm2df.pipeline;

import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;

import java.util.List;

public interface PipelineComponent {
    List<CodeLine> generate(Pipeline pipeline);
}
