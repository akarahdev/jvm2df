package dev.akarah.jvm2df.tree.df.handler.dynamic;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.df.VarPattern;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class HandleStringConcatFactory implements InvokeDynamicHandler {
    @Override
    public Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.InvokeDynamic invoke) {
        if (invoke.bootstrapMethodOwner().descriptorString().contains("StringConcatFactory")
                && invoke.bootstrapMethod().name().equals("makeConcatWithConstants")) {
            return Optional.of(transformer -> {
                var recipeString = (String) (((CodeTree.Constant) invoke.constantArgs().getFirst()).constantDesc());
                System.out.println(Arrays.toString(recipeString.getBytes(StandardCharsets.UTF_8)));

                var params = new ArrayList<VarItem<?>>();
                var sb = new StringBuilder();
                int idx = 0;
                int dynamicArgsIdx = 0;
                while (idx < recipeString.length()) {
                    if (recipeString.charAt(idx) == 1) {
                        if (!sb.isEmpty()) {
                            params.add(LiteralItem.string(sb.toString()));
                            sb = new StringBuilder();
                        }
                        params.add(transformer.convertCodeTree(
                                invoke.dynamicArgs().get(dynamicArgsIdx)
                        ));
                        dynamicArgsIdx += 1;
                    } else {
                        sb.append(recipeString.charAt(idx));
                    }
                    idx += 1;
                }
                if (!sb.isEmpty()) {
                    params.add(LiteralItem.string(sb.toString()));
                }

                var returnVariable = VarPattern.temporary("result");
                params.addFirst(returnVariable);
                transformer.builder().appendCodeBlock(ActionBlock.setVar(
                        "String",
                        Args.byVarItemsList(params)
                ).storeTagInSlot(26, "Text Value Merging", "No spaces"));
                return returnVariable;
            });
        }
        return Optional.empty();
    }
}
