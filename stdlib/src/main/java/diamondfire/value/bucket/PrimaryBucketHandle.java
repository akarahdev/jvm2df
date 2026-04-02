package diamondfire.value.bucket;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public final class PrimaryBucketHandle implements BucketHandle {
    static final String NAMESPACE = "primary";

    String name;

    @Override
    public String namespace() {
        return "primary";
    }

    @Override
    public String bucketName() {
        return this.name;
    }

    PrimaryBucketHandle(String name) {
        this.name = name;
    }

    @Override
    public String load() {
        return CodeBlocks.setVarR(
                "LoadBucket",
                VarItemGen.lineVar(),
                NAMESPACE,
                this.name
        );
    }

    @Override
    public String save() {
        return CodeBlocks.setVarR(
                "SaveBucket",
                VarItemGen.lineVar(),
                NAMESPACE,
                this.name
        );
    }

    @Override
    public String saveAndUnload() {
        return CodeBlocks.setVarR(
                "SaveUnloadBucket",
                VarItemGen.lineVar(),
                NAMESPACE,
                this.name
        );
    }

    @Override
    public <T> void store(BucketVariableKey<T> variable, T value) {
//        CodeBlocks.setVar(
//                "=",
//                VarItemGen.bucketVar(NAMESPACE, this.name, variable.key()),
//                value
//        );
    }

    @Override
    public <T> T read(BucketVariableKey<T> key) {
//        return CodeBlocks.setVarR(
//                "=",
//                VarItemGen.lineVar(),
//                VarItemGen.bucketVar(NAMESPACE, this.name, key.key())
//        );
        return null;
    }
}
