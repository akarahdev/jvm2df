package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;

public record ParticleItem(
        String particle,
        Cluster cluster,
        Data data
) implements VarItem<ParticleItem> {
    public record Cluster(
            int amount,
            double horizontal,
            double vertical
    ) {
        public static Codec<Cluster> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("amount").forGetter(Cluster::amount),
                Codec.DOUBLE.fieldOf("horizontal").forGetter(Cluster::horizontal),
                Codec.DOUBLE.fieldOf("vertical").forGetter(Cluster::vertical)
        ).apply(instance, Cluster::new));
    }

    public record Data() {
        public static Codec<Data> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING)
                .xmap(x -> new Data(), x -> new HashMap<>());
    }

    public static Codec<ParticleItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("particle").forGetter(ParticleItem::particle),
            Cluster.CODEC.fieldOf("cluster").forGetter(ParticleItem::cluster),
            Data.CODEC.fieldOf("data").forGetter(ParticleItem::data)
    ).apply(instance, ParticleItem::new));

    @Override
    public Codec<ParticleItem> dataCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "part";
    }
}
