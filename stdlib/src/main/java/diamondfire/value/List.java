package diamondfire.value;

import diamondfire.Control;
import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public class List<T> {
    Object inner;

    private List() {
        this.inner = CodeBlocks.setVar(
                "CreateList",
                VarItemGen.lineVar()
        );
        Control.debug(this.inner);
    }

    public static <T> List<T> of() {
        return new List<>();
    }

    public void add(T obj) {
        Control.debug(this.inner);
        this.inner = CodeBlocks.setVar("AppendValue", this.inner, obj);
        Control.debug(this.inner);
    }

    public void extend(List<T> obj) {
        this.inner = CodeBlocks.setVar("AppendList", this.inner, obj);
    }

    public int length() {
        return CodeBlocks.setVar("ListLength", VarItemGen.lineVar(), this.inner);
    }

    public T get(int idx) {
        checkIndex(idx);
        return CodeBlocks.setVar("GetListValue", VarItemGen.lineVar(), this.inner, idx + 1);
    }

    public void set(T value, int idx) {
        checkIndex(idx);
        this.inner = CodeBlocks.setVar("SetListValue", this.inner, idx + 1, value);
    }

    public void insert(T value, int idx) {
        checkIndex(idx);
        this.inner = CodeBlocks.setVar("InsertListValue", this.inner, idx + 1, value);
    }

    public void removeAtIndex(int idx) {
        checkIndex(idx);
        this.inner = CodeBlocks.setVar("RemoveListIndex", this.inner, idx + 1);
    }

    public T removeValue(T value) {
        return CodeBlocks.setVar("RemoveListValue", this.inner, value);
    }

    public int indexOf(T value) {
        return (int) CodeBlocks.setVar(
                "GetValueIndex",
                VarItemGen.lineVar(),
                this.inner,
                value,
                VarItemGen.tag("Search Order", "Ascending (first index)")
        ) - 1;
    }

    public void dedup() {
        this.inner = CodeBlocks.setVar("DedupList", this.inner);
    }

    public void reverse() {
        this.inner = CodeBlocks.setVar("ReverseList", this.inner);
    }

    public void randomize() {
        this.inner = CodeBlocks.setVar("RandomizeList", this.inner);
    }

    public void trim(int start, int end) {
        checkIndex(start);
        checkIndex(end);
        this.inner = CodeBlocks.setVar("DedupList", this.inner, start, end);
    }

    public void checkIndex(int idx) {
        if (idx > this.length()) {
            Control.panic("Index out of bounds");
        }
        if (idx < 0) {
            Control.panic("Indices must be > 0");
        }
    }

    public boolean contains(T obj) {
        CodeBlocks.ifVar(
                "ListContains",
                this.inner,
                obj,
                VarItemGen.tag("Check Mode", "Has Any Value")
        );
        CodeBlocks.openNormal();
        CodeBlocks.ret(true);
        CodeBlocks.closeNormal();
        return false;
    }
}
