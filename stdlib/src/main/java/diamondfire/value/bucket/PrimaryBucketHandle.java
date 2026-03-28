package diamondfire.value.bucket;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public class PrimaryBucketHandle implements BucketHandle {
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

    protected PrimaryBucketHandle(String name) {
        this.name = name;
    }

    @Override
    public String load() {
        return CodeBlocks.setVar(
                "LoadBucket",
                VarItemGen.lineVar(),
                NAMESPACE,
                this.name
        );
    }

    @Override
    public String save() {
        return CodeBlocks.setVar(
                "SaveBucket",
                VarItemGen.lineVar(),
                NAMESPACE,
                this.name
        );
    }

    @Override
    public String saveAndUnload() {
        return CodeBlocks.setVar(
                "SaveUnloadBucket",
                VarItemGen.lineVar(),
                NAMESPACE,
                this.name
        );
    }

    @Override
    public <T> void store(BucketVariableKey<T> variable, T value) {
        var out = CodeBlocks.setVar(
                "=",
                VarItemGen.bucketVar(NAMESPACE, this.name, variable.key()),
                value
        );
    }

    @Override
    public <T> T read(BucketVariableKey<T> key) {
        return CodeBlocks.setVar(
                "=",
                VarItemGen.lineVar(),
                VarItemGen.bucketVar(NAMESPACE, this.name, key.key())
        );
    }
}
