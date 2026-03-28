package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BucketVariableItem(String name, String bucket, String namespace) implements VarItem<BucketVariableItem> {
    public static Codec<BucketVariableItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(BucketVariableItem::name),
            Codec.STRING.fieldOf("key").forGetter(BucketVariableItem::bucket),
            Codec.STRING.fieldOf("namespace_alias").forGetter(BucketVariableItem::namespace)
    ).apply(instance, BucketVariableItem::new));

    @Override
    public Codec<BucketVariableItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "bucket_var";
    }
}
