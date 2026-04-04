package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Map;

public interface VarItem<T extends VarItem<T>> {
    Map<String, Codec<?>> VAR_ITEMS = Map.ofEntries(
            Map.entry("num", LiteralItem.CODEC),
            Map.entry("txt", LiteralItem.CODEC),
            Map.entry("comp", LiteralItem.CODEC),
            Map.entry("var", VariableItem.CODEC),
            Map.entry("bl_tag", BlockTagItem.CODEC),
            Map.entry("pn_el", ParameterItem.CODEC),
            Map.entry("g_val", GameValueItem.CODEC),
            Map.entry("item", VanillaItem.CODEC),
            Map.entry("snd", SoundItem.CODEC),
            Map.entry("part", ParticleItem.CODEC),
            Map.entry("bucket_var", BucketVariableItem.CODEC)
    );

    @SuppressWarnings("unchecked")
    Codec<VarItem<?>> GENERIC_CODEC = Codec.STRING.dispatch(
            "id",
            VarItem::id,
            id -> (MapCodec<? extends VarItem<?>>) VAR_ITEMS.get(id).fieldOf("data")
    );

    Codec<T> dataCodec();

    String id();

    default String percentCodeSafe() {
        return switch (this) {
            case VariableItem(String name, String scope) -> "%var(" + name + ")";
            case LiteralItem(String id, String value) -> value;
            default -> throw new RuntimeException("Can not convert " + this + " into % code.");
        };
    }
}

