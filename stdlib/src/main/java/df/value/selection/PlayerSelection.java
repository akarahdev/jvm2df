package df.value.selection;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;
import df.value.selection.generic.GameValues;
import df.value.selection.playerimpl.AppearancePlayerImpl;
import df.value.selection.playerimpl.CommunicationPlayerImpl;
import df.value.selection.playerimpl.MovementPlayerImpl;
import df.value.selection.playerimpl.StatisticsPlayerImpl;

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
