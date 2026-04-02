package diamondfire.value.selection;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.value.selection.entityimpl.AppearanceEntityImpl;
import diamondfire.value.selection.entityimpl.MovementEntityImpl;
import diamondfire.value.selection.entityimpl.StatisticsEntityImpl;

public class EntitySelection implements
        AppearanceEntityImpl, MovementEntityImpl, StatisticsEntityImpl {
    Object inner;

    public static EntitySelection defaultTarget() {
        var s = new EntitySelection();
        s.inner = CodeBlocks.setVarR(
                "CreateList",
                VarItemGen.lineVar(),
                VarItemGen.gameValue("UUID", "Default")
        );
        return s;
    }
}
