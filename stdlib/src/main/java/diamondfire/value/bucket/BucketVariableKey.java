package diamondfire.value.bucket;

public final class BucketVariableKey<T> {
    String key;

    public BucketVariableKey(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }
}
