package dev.akarah.jvm2df.codetemplate;

import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;

public record CodeTemplateData(
        String author,
        String name,
        String code,
        int version
) {
    public CodeTemplateData(CodeLine code) {
        this("Auto", "Auto", code.codeString(), 1);
    }
}
