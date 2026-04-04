package diamondfire.value.selection;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.value.selection.generic.GameValues;
import diamondfire.value.selection.playerimpl.AppearancePlayerImpl;
import diamondfire.value.selection.playerimpl.CommunicationPlayerImpl;
import diamondfire.value.selection.playerimpl.MovementPlayerImpl;
import diamondfire.value.selection.playerimpl.StatisticsPlayerImpl;

public final class PlayerSelection implements
        AppearancePlayerImpl, MovementPlayerImpl, StatisticsPlayerImpl, CommunicationPlayerImpl, GameValues {
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

    @Override
    public void setSelection() {
        CodeBlocks.selectObject("PlayerName", VarItemGen.readField(this, "inner"));
    }
}
