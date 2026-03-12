package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import dev.akarah.jvm2df.codetemplate.Fail;

public record LiteralItem(String id, String value) implements VarItem<LiteralItem> {
    public static Codec<LiteralItem> CODEC = Codec.STRING.fieldOf("name")
            .xmap(Fail::fail, LiteralItem::value).codec();

    @Override
    public Codec<LiteralItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return this.id;
    }

    public static LiteralItem unknown(String value) {
        return new LiteralItem("?", value);
    }

    public static LiteralItem number(String value) {
        return new LiteralItem("num", value);
    }

    public static LiteralItem string(String value) {
        return new LiteralItem("txt", value);
    }

    public static LiteralItem text(String value) {
        return new LiteralItem("comp", value);
    }
}
