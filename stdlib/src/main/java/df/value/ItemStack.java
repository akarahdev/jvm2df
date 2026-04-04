package df.value;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;
import df.internal.annotation.NativeValue;

@NativeValue
public final class ItemStack {
    Object inner;

    public ItemStack(String material) {
        this.inner = CodeBlocks.setVarR(
                "SetItemType",
                VarItemGen.lineVar(),
                VarItemGen.vanillaItem("{DF_NBT:4671,count:1,id:'minecraft:dirt'}"),
                material
        );
    }

    public Object raw() {
        return this.inner;
    }
}
