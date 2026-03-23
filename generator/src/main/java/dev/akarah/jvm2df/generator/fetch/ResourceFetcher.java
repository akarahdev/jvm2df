package dev.akarah.jvm2df.generator.fetch;

import java.util.List;

public interface ResourceFetcher<T> {
    List<? extends ResourceFetcher<?>> FETCHERS = List.of(
            ActiondumpFetch.INSTANCE
    );

    T fetch();
}
