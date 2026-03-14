package dev.akarah.jvm2df.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.BlockTagItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;

import java.util.List;
import java.util.Optional;

public record ActionBlock(
        String block,
        Optional<String> data,
        Optional<String> action,
        Optional<String> subAction,
        Optional<String> attribute,
        Optional<String> target,
        Optional<Args> args
) implements CodeBlock<ActionBlock> {
    public static MapCodec<ActionBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("block").forGetter(ActionBlock::block),
            Codec.STRING.optionalFieldOf("data").forGetter(ActionBlock::data),
            Codec.STRING.optionalFieldOf("action").forGetter(ActionBlock::action),
            Codec.STRING.optionalFieldOf("subAction").forGetter(ActionBlock::subAction),
            Codec.STRING.optionalFieldOf("attribute").forGetter(ActionBlock::attribute),
            Codec.STRING.optionalFieldOf("target").forGetter(ActionBlock::target),
            Args.CODEC.optionalFieldOf("args").forGetter(ActionBlock::args)
    ).apply(instance, ActionBlock::new));

    @Override
    public MapCodec<ActionBlock> mapCodec() {
        return CODEC;
    }

    @Override
    public String id() {
        return "block";
    }

    public static ActionBlock playerEvent(String event) {
        return new ActionBlock(
                "event",
                Optional.empty(),
                Optional.of(event),
                Optional.empty(),
                Optional.of("LS-CANCEL"),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static ActionBlock function(String data, List<VarItem<?>> params) {
        return new ActionBlock(
                "func",
                Optional.of(data),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(
                        Args.byVarItems(params)
                )
        );
    }

    public static ActionBlock callFunction(String data, List<VarItem<?>> params) {
        return new ActionBlock(
                "call_func",
                Optional.of(data),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(
                        Args.byVarItems(params)
                )
        );
    }

    public static ActionBlock playerAction(String action, Args args) {
        return new ActionBlock(
                "player_action",
                Optional.empty(),
                Optional.of(action),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(args)
        );
    }

    public static ActionBlock setVar(String action, Args args) {
        return new ActionBlock(
                "set_var",
                Optional.empty(),
                Optional.of(action),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(args)
        );
    }

    public static ActionBlock ifVar(String action, Args args) {
        return new ActionBlock(
                "if_var",
                Optional.empty(),
                Optional.of(action),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(args)
        );
    }

    public static ActionBlock else_() {
        return new ActionBlock(
                "else",
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static ActionBlock control(String action, Args args) {
        return new ActionBlock(
                "control",
                Optional.empty(),
                Optional.of(action),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(args)
        );
    }

    public static ActionBlock repeat(String action, Args args) {
        return new ActionBlock(
                "repeat",
                Optional.empty(),
                Optional.of(action),
                Optional.empty(),
                Optional.of(""),
                Optional.of(""),
                Optional.of(args)
        );
    }

    public ActionBlock storeTagInSlot(int slot, String tag, String option) {
        this.args.ifPresent(args -> {
            args.arguments().add(new Args.Argument(
                    new BlockTagItem(
                            option,
                            tag,
                            this.action().orElse("dynamic"),
                            this.block()
                    ),
                    slot
            ));
        });
        return this;
    }

}
