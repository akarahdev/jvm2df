package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.internal.annotation.NativeValue;

@NativeValue
public final class ItemStack {
    public static ItemStack of(String material) {
        return CodeBlocks.setVar(
                "SetItemType",
                VarItemGen.lineVar(),
                VarItemGen.vanillaItem("{DF_NBT:4671,count:1,id:'minecraft:dirt'}"),
                material
        );
    }
}
