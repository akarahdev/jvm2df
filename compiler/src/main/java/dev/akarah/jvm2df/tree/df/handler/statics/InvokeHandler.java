package dev.akarah.jvm2df.tree.df.handler.statics;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface InvokeHandler {
    List<InvokeHandler> INVOKE_HANDLERS = List.of(
            new DFCodeBlocksHandler("control", "control", ActionBlock::control),
            new DFCodeBlocksHandler("playerAction", "player_action", ActionBlock::playerAction),
            new DFCodeBlocksHandler("setVar", "set_var", ActionBlock::setVar),
            new DFCodeBlocksHandler("ifVar", "if_var", ActionBlock::ifVar),
            new DFCodeBlocksHandler("selectObject", "select_obj", ActionBlock::selectObject),
            new DFCodeBlocksHandler("ifPlayer", "if_player", ActionBlock::ifPlayer),
            new DFCodeBlocksHandler("entityAction", "entity_action", ActionBlock::entityAction),
            new DFCodeBlocksHandler("ifEntity", "if_entity", ActionBlock::ifEntity),
            new VarItemGenHandler(),
            new BoxedPrimitiveHandler(),
            new BracketHandler(),
            new VarItemOptimizationHandler(),
            new ForceReturnHandler(),
            new ThreadHandler()
    );

    Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke);
}
