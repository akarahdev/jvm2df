package dev.akarah.jvm2df.codeclient;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;

public class CodeClientAPI extends WebSocketClient {
    public CodeClientAPI(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        this.send("auth default inventory movement read_plot write_code clear_plot");
    }

    @Override
    public void onMessage(String message) {
        switch (message) {
            case "auth" -> {
                this.send("clear");
                this.send("size");
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
