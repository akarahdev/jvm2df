package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record Args(List<Argument> arguments) {
    public record Argument(VarItem<?> varItem, int slot) {
        public static Codec<Argument> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                VarItem.GENERIC_CODEC.fieldOf("item").forGetter(Argument::varItem),
                Codec.INT.fieldOf("slot").forGetter(Argument::slot)
        ).apply(instance, Argument::new));
    }

    public static Codec<Args> CODEC = Argument.CODEC.listOf().xmap(Args::new, Args::arguments);
}
