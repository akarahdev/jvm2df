package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VariableItem(String name, String scope) implements VarItem<VariableItem> {
    public static Codec<VariableItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(VariableItem::name),
            Codec.STRING.fieldOf("scope").forGetter(VariableItem::scope)
    ).apply(instance, VariableItem::new));

    @Override
    public Codec<VariableItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "var";
    }
}
