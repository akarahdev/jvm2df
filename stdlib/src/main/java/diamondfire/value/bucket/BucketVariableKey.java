package diamondfire.value.bucket;

public class BucketVariableKey<T> {
    String key;

    public BucketVariableKey(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }
}
