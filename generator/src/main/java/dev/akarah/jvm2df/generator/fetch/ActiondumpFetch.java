package dev.akarah.jvm2df.generator.fetch;

import com.google.gson.Gson;
import dev.akarah.jvm2df.generator.data.actiondump.ActionDump;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class ActiondumpFetch implements ResourceFetcher<ActionDump> {
    public static ActiondumpFetch INSTANCE = new ActiondumpFetch();

    private ActionDump DUMP;

    private ActiondumpFetch() {
    }

    public synchronized ActionDump fetch() {
        try (var client = HttpClient.newBuilder().build()) {
            if (DUMP != null) {
                return DUMP;
            }
            var cachePath = Path.of("../build/actiondump.json");
            if (Files.exists(cachePath)) {
                var body = Files.readString(cachePath);
                DUMP = new Gson().fromJson(body, ActionDump.class);
                return DUMP;
            }
            var request = client.send(
                    HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create("https://api.dftooling.dev/api/actiondump/v0/latest?formatting=none"))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            var body = request.body();
            Files.writeString(cachePath, body);
            DUMP = new Gson().fromJson(body, ActionDump.class);
            return DUMP;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
