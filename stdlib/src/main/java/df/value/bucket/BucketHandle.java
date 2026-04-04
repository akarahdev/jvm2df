package df.value.bucket;

public interface BucketHandle {
    String namespace();

    String bucketName();

    String load();

    String save();

    String saveAndUnload();

    <T> void store(BucketVariableKey<T> variable, T value);

    <T> T read(BucketVariableKey<T> key);

    static BucketHandle primary(String bucketName) {
        return new PrimaryBucketHandle(bucketName);
    }
}
