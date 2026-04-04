package df.value;

import df.Control;
import df.internal.CodeBlocks;
import df.internal.VarItemGen;

import java.util.Iterator;

public final class List<T> implements Iterable<T> {
    Object inner;

    private List() {
        this.inner = CodeBlocks.setVarR(
                "CreateList",
                VarItemGen.lineVar()
        );
    }

    public static <T> List<T> of() {
        return new List<>();
    }

    public static <T> List<T> byArray(T[] array) {
        var l = new List<T>();
        for (var elem : array) {
            l.add(elem);
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        var arr = new Object[this.length()];
        for (int i = 0; i < this.length(); i++) {
            arr[i] = this.get(i);
        }
        return (T[]) arr;
    }

    public void add(T obj) {
        this.inner = CodeBlocks.setVarR("AppendValue", this.inner, obj);
    }

    public void extend(List<T> obj) {
        this.inner = CodeBlocks.setVarR("AppendList", this.inner, obj);
    }

    public int length() {
        return CodeBlocks.setVarR("ListLength", VarItemGen.lineVar(), this.inner);
    }

    public T get(int idx) {
        checkIndex(idx);
        return CodeBlocks.setVarR("GetListValue", VarItemGen.lineVar(), this.inner, idx + 1);
    }

    public void set(T value, int idx) {
        checkIndex(idx);
        this.inner = CodeBlocks.setVarR("SetListValue", this.inner, idx + 1, value);
    }

    public void insert(T value, int idx) {
        checkIndex(idx);
        this.inner = CodeBlocks.setVarR("InsertListValue", this.inner, idx + 1, value);
    }

    public void removeAtIndex(int idx) {
        checkIndex(idx);
        this.inner = CodeBlocks.setVarR("RemoveListIndex", this.inner, idx + 1);
    }

    public T removeValue(T value) {
        return CodeBlocks.setVarR("RemoveListValue", this.inner, value);
    }

    public int indexOf(T value) {
        return CodeBlocks.setVarI(
                "GetValueIndex",
                VarItemGen.lineVar(),
                this.inner,
                value,
                VarItemGen.tag("Search Order", "Ascending (first index)")
        ) - 1;
    }

    public void dedup() {
        this.inner = CodeBlocks.setVarR("DedupList", this.inner);
    }

    public void reverse() {
        this.inner = CodeBlocks.setVarR("ReverseList", this.inner);
    }

    public void randomize() {
        this.inner = CodeBlocks.setVarR("RandomizeList", this.inner);
    }

    public void trim(int start, int end) {
        checkIndex(start);
        checkIndex(end);
        this.inner = CodeBlocks.setVarR("DedupList", this.inner, start, end);
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

    public Object raw() {
        return this.inner;
    }

    @Override
    public Iterator<T> iterator() {
        return new ListIterator<>(this);
    }

    private static class ListIterator<T> implements Iterator<T> {
        List<T> inner;
        int idx;

        public ListIterator(List<T> list) {
            this.inner = list;
            this.idx = 0;
        }

        @Override
        public boolean hasNext() {
            return idx < inner.length();
        }

        @Override
        public T next() {
            var value = this.inner.get(idx);
            this.idx += 1;
            return value;
        }
    }
}
