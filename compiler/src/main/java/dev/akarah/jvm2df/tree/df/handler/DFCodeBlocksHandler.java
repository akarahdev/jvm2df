package dev.akarah.jvm2df.tree.df.handler;

import dev.akarah.jvm2df.codetemplate.blocks.CodeBlock;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.BlockTagItem;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public record DFCodeBlocksHandler(String functionName, String codeBlock,
                                  BiFunction<String, Args, CodeBlock<?>> mapper) implements InvokeHandler {
    @Override
    public Optional<Function<CodeBlockTransformer, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/CodeBlocks")
                && invoke.outline().name().equals(functionName)) {
            var codeArguments = new ArrayList<>(invoke.args());
            var action = codeArguments.removeFirst();
            if (!(action instanceof CodeTree.Constant(var constantDesc))) {
                return Optional.empty();
            }
            return Optional.of(transformer -> {
                List<BlockTagItem> tags = new ArrayList<>();
                var norms = new ArrayList<VarItem<?>>();
                for (var arg : codeArguments) {
                    var compiledArg = transformer.convertCodeTree(arg);
                    if (compiledArg instanceof BlockTagItem blockTagItem) {
                        tags.add(new BlockTagItem(
                                blockTagItem.option(),
                                blockTagItem.tag(),
                                constantDesc.toString(),
                                codeBlock
                        ));
                    } else {
                        norms.add(compiledArg);
                    }
                }
                tags = tags.reversed();

                var arguments = new ArrayList<Args.Argument>();
                for (int i = 0; i < norms.size(); i++) {
                    arguments.add(new Args.Argument(
                            norms.get(i),
                            i
                    ));
                }
                for (int i = 0; i < tags.size(); i++) {
                    arguments.add(new Args.Argument(
                            tags.get(i),
                            26 - i
                    ));
                }

                transformer.appendCodeBlock(this.mapper.apply(
                        constantDesc.toString(),
                        new Args(arguments)
                ));

                if (norms.isEmpty()) {
                    return LiteralItem.number("0");
                } else {
                    return norms.getFirst();
                }
            });
        }
        return Optional.empty();
    }
}
