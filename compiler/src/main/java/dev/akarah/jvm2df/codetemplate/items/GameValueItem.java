package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GameValueItem(String type, String target) implements VarItem<GameValueItem> {
    public static Codec<GameValueItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(GameValueItem::type),
            Codec.STRING.fieldOf("target").forGetter(GameValueItem::target)
    ).apply(instance, GameValueItem::new));

    @Override
    public Codec<GameValueItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "g_val";
    }
}
