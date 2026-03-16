package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VanillaItem(String item) implements VarItem<VanillaItem> {
    public static Codec<VanillaItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("item").forGetter(VanillaItem::item)
    ).apply(instance, VanillaItem::new));

    @Override
    public Codec<VanillaItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "item";
    }
}
