package dev.akarah.jvm2df.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.jvm2df.codetemplate.items.Args;

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


    public static ActionBlock playerAction(String action, Args args) {
        return new ActionBlock(
                "event",
                Optional.empty(),
                Optional.of(action),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(args)
        );
    }

}
