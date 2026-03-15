package dev.akarah.jvm2df.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Map;

public interface CodeBlock<T extends CodeBlock<T>> {
    Map<String, MapCodec<?>> CODE_BLOCKS = Map.ofEntries(
            Map.entry("block", ActionBlock.CODEC),
            Map.entry("bracket", Bracket.CODEC)
    );

    @SuppressWarnings("unchecked")
    Codec<CodeBlock<?>> GENERIC_CODEC = Codec.STRING.dispatch(
            "id",
            CodeBlock::id,
            id -> (MapCodec<? extends CodeBlock<?>>) CODE_BLOCKS.get(id)
    );

    MapCodec<T> mapCodec();

    String id();
}
