package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Map;

public interface VarItem<T extends VarItem<T>> {
    public static Map<String, Codec<?>> VAR_ITEMS = Map.ofEntries(
            Map.entry("num", LiteralItem.CODEC),
            Map.entry("txt", LiteralItem.CODEC),
            Map.entry("comp", LiteralItem.CODEC),
            Map.entry("var", VariableItem.CODEC),
            Map.entry("bl_tag", BlockTagItem.CODEC),
            Map.entry("pn_el", ParameterItem.CODEC)
    );

    @SuppressWarnings("unchecked")
    public static Codec<VarItem<?>> GENERIC_CODEC = Codec.STRING.dispatch(
            "id",
            VarItem::id,
            id -> (MapCodec<? extends VarItem<?>>) VAR_ITEMS.get(id).fieldOf("data")
    );

    Codec<T> dataCodec();
    String id();
}
