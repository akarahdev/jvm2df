package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ParameterItem(String name, String type, boolean plural, boolean optional) implements VarItem<ParameterItem> {
    public static Codec<ParameterItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(ParameterItem::name),
            Codec.STRING.fieldOf("type").forGetter(ParameterItem::type),
            Codec.BOOL.fieldOf("plural").forGetter(ParameterItem::plural),
            Codec.BOOL.fieldOf("optional").forGetter(ParameterItem::optional)
    ).apply(instance, ParameterItem::new));

    @Override
    public Codec<ParameterItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "pn_el";
    }
}
