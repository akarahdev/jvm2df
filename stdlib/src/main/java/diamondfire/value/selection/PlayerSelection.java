package diamondfire.value.selection;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.value.selection.playerimpl.AppearancePlayerImpl;
import diamondfire.value.selection.playerimpl.MovementPlayerImpl;
import diamondfire.value.selection.playerimpl.StatisticsPlayerImpl;

public final class PlayerSelection implements
        AppearancePlayerImpl, MovementPlayerImpl, StatisticsPlayerImpl {
    Object inner;

    public static PlayerSelection defaultTarget() {
        var s = new PlayerSelection();
        s.inner = CodeBlocks.setVarR(
                "CreateList",
                VarItemGen.lineVar(),
                VarItemGen.gameValue("UUID", "Default")
        );
        return s;
    }
}
