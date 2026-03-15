package diamondfire;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public class Selection {
    Object uuidList;

    private Selection() {
        this.uuidList = CodeBlocks.setVar(
                "CreateList",
                this.uuidList
        );
    }

    private Selection(Object uuid) {
        this.uuidList = CodeBlocks.setVar(
                "CreateList",
                this.uuidList,
                uuid
        );
    }

    public static Selection defaultTarget() {
        return new Selection(VarItemGen.gameValue("UUID", "Default"));
    }

    public void sendMessage(String text) {
        CodeBlocks.selectObject(
                "PlayerName",
                this.uuidList
        );
        CodeBlocks.playerAction(
                "SendMessage",
                text,
                VarItemGen.tag("Inherit Styles", "True"),
                VarItemGen.tag("Text Value Merging", "Add spaces"),
                VarItemGen.tag("Alignment Mode", "Regular")
        );
        CodeBlocks.selectObject(
                "Reset"
        );
    }
}
