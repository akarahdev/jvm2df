package dev.akarah.jvm2df.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Bracket(String direction, String type) implements CodeBlock<Bracket> {
    public static MapCodec<Bracket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("direct").forGetter(Bracket::direction),
            Codec.STRING.fieldOf("type").forGetter(Bracket::type)
    ).apply(instance, Bracket::new));

    @Override
    public MapCodec<Bracket> mapCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "bracket";
    }

    public static Bracket openNormal() {
        return new Bracket("open", "norm");
    }

    public static Bracket closeNormal() {
        return new Bracket("close", "norm");
    }

    public static Bracket openRepeat() {
        return new Bracket("open", "repeat");
    }

    public static Bracket closeRepeat() {
        return new Bracket("close", "repeat");
    }
}
