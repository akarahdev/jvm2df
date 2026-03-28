package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.internal.annotation.NativeValue;

@NativeValue
public final class Text {
    public static Text of(Object s) {
        return CodeBlocks.setVarR(
                "StyledText",
                VarItemGen.lineVar(),
                s,
                VarItemGen.tag("Inherit Styles", "True"),
                VarItemGen.tag("Text Value Merging", "No spaces")
        );
    }

    public static Text of(String s) {
        return CodeBlocks.setVarR(
                "ParseMiniMessage",
                VarItemGen.lineVar(),
                s,
                VarItemGen.tag("Parse Legacy Color Codes", "False"),
                VarItemGen.tag("Allowed Tags", "Style Only")
        );
    }

    public static Text parseDynamic(String s) {
        return CodeBlocks.setVarR(
                "ParseMiniMessage",
                VarItemGen.lineVar(),
                s,
                VarItemGen.tag("Parse Legacy Color Codes", "False"),
                VarItemGen.tag("Allowed Tags", "Dynamic")
        );
    }

    public static Text parseFull(String s) {
        return CodeBlocks.setVarR(
                "ParseMiniMessage",
                VarItemGen.lineVar(),
                s,
                VarItemGen.tag("Parse Legacy Color Codes", "False"),
                VarItemGen.tag("Allowed Tags", "Full")
        );
    }
}
