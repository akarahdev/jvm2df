package dev.akarah.jvm2df.pipeline;

import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;

import java.util.ArrayList;
import java.util.List;

public class LocalAndGlobalContributions implements PipelineComponent {
    @Override
    public List<CodeLine> generate(Pipeline pipeline) {
        var list = new ArrayList<CodeLine>();

        list.addAll(pipeline.locals().codeLineContributions(pipeline));
        list.addAll(pipeline.globals().codeLineContributions(pipeline));
        return list;
    }
}
