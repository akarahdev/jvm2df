package dev.akarah.jvm2df.codeclient;

import dev.akarah.jvm2df.codetemplate.blocks.CodeBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class CodeClientAPI extends WebSocketClient {
    List<CodeLine> functions;

    public CodeClientAPI(List<CodeLine> functions) throws URISyntaxException {
        super(new URI("ws://localhost:31375"));
        this.functions = functions;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        this.send("scopes default inventory movement read_plot write_code clear_plot");
        this.send("auth");
    }

    @Override
    public void onMessage(String message) {
        switch (message) {
            case "auth" -> {
                this.send("clear");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.send("spawn");
                this.send("place");
                for(var line : this.functions) {
                    this.send("place " + line.codeString());
                }
                this.send("place go");
            }
            case "done" -> {
                this.close();
            }
            case "basic", "large", "massive", "mega" -> {

            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
