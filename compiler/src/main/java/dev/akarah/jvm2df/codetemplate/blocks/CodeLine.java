package dev.akarah.jvm2df.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public record CodeLine(List<CodeBlock<?>> codeBlocks) {
    public static Codec<CodeLine> CODEC = CodeBlock.GENERIC_CODEC.listOf().fieldOf("blocks")
            .xmap(CodeLine::new, CodeLine::codeBlocks).codec();

    public String codeString() {
        var json = CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
        try {
            var baos = new ByteArrayOutputStream();
            var gos = new GZIPOutputStream(baos);
            gos.write(json.toString().getBytes());
            gos.finish();

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
