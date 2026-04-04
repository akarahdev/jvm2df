package dev.akarah.jvm2df.codetemplate.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record Args(List<Argument> arguments) {
    public record Argument(VarItem<?> varItem, int slot) {
        public static Codec<Argument> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                VarItem.GENERIC_CODEC.fieldOf("item").forGetter(Argument::varItem),
                Codec.INT.fieldOf("slot").forGetter(Argument::slot)
        ).apply(instance, Argument::new));
    }

    public static Args byVarItemsList(List<? extends VarItem<?>> varItems) {
        var list = new ArrayList<Argument>();
        int idx = 0;
        for (var item : varItems) {
            list.add(new Argument(
                    item,
                    idx
            ));
            idx += 1;
        }
        return new Args(list);
    }

    public static Args byVarItems(VarItem<?>... varItems) {
        return byVarItemsList(Arrays.asList(varItems));
    }

    public static Codec<Args> CODEC = Argument.CODEC.listOf().xmap(Args::new, Args::arguments).fieldOf("items").codec();
}
