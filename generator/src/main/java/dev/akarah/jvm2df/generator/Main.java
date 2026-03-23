package dev.akarah.jvm2df.generator;

import dev.akarah.jvm2df.generator.fetch.ResourceFetcher;
import dev.akarah.jvm2df.generator.gen.ResourceGenerator;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    static void main(String[] args) {
        executeFetchers();
        executeGenerators();
    }

    public static void executeFetchers() {
        try (var tg = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var fetcher : ResourceFetcher.FETCHERS) {
                tg.submit(fetcher::fetch);
            }
            tg.shutdown();
            var _ = tg.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void executeGenerators() {
        try (var tg = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var generator : ResourceGenerator.GENERATORS) {
                tg.submit(() -> {
                    try {
                        generator.generate();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
            }
            tg.shutdown();
            var _ = tg.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
