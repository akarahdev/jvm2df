package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SoundItem(double pitch, double volume, String sound) implements VarItem<SoundItem> {
    public static Codec<SoundItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("pitch").forGetter(SoundItem::pitch),
            Codec.DOUBLE.fieldOf("vol").forGetter(SoundItem::volume),
            Codec.STRING.fieldOf("sound").forGetter(SoundItem::sound)
    ).apply(instance, SoundItem::new));

    @Override
    public Codec<SoundItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "snd";
    }
}
