package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BlockTagItem(String option, String tag, String action, String block) implements VarItem<BlockTagItem> {
    public static Codec<BlockTagItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("option").forGetter(BlockTagItem::option),
            Codec.STRING.fieldOf("tag").forGetter(BlockTagItem::tag),
            Codec.STRING.fieldOf("action").forGetter(BlockTagItem::action),
            Codec.STRING.fieldOf("block").forGetter(BlockTagItem::block)
    ).apply(instance, BlockTagItem::new));

    @Override
    public Codec<BlockTagItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "bl_tag";
    }
}
