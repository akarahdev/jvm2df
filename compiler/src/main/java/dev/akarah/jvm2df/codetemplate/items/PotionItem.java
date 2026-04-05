package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PotionItem(
        String pot,
        int dur,
        int amp
) implements VarItem<PotionItem> {

    public static Codec<PotionItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("pot").forGetter(PotionItem::pot),
            Codec.INT.fieldOf("dur").forGetter(PotionItem::dur),
            Codec.INT.fieldOf("amp").forGetter(PotionItem::amp)
    ).apply(instance, PotionItem::new));

    @Override
    public Codec<PotionItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "pot";
    }
}
