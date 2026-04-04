package df.value.selection;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;
import df.value.selection.entityimpl.AppearanceEntityImpl;
import df.value.selection.entityimpl.MovementEntityImpl;
import df.value.selection.entityimpl.StatisticsEntityImpl;
import df.value.selection.generic.GameValues;

public class EntitySelection implements
        AppearanceEntityImpl, MovementEntityImpl, StatisticsEntityImpl, GameValues {
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

    public static EntitySelection lastSpawnedEntity() {
        var s = new EntitySelection();
        s.inner = CodeBlocks.setVarR(
                "CreateList",
                VarItemGen.lineVar(),
                VarItemGen.gameValue("UUID", "LastEntity")
        );
        return s;
    }

    @Override
    public void setSelection() {
        CodeBlocks.selectObject("EntityUUID", VarItemGen.readField(this, "inner"));
    }
}
