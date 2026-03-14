package dev.akarah.jvm2df.codeclient;

import dev.akarah.jvm2df.codetemplate.blocks.CodeBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.util.Beep;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.sound.sampled.LineUnavailableException;
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
        this.send("scopes write_code clear_plot");
        this.send("auth");
    }

    @Override
    public void onMessage(String message) {
        switch (message) {
            case "auth" -> {
                this.send("mode code");
                this.send("clear");
                try {
                    Thread.sleep(1000);
                } catch (Exception _) {
                    // ignored bruh
                }
                this.send("place");
                for(var line : this.functions) {
                    this.send("place " + line.codeString());
                }
                this.send("place go");
            }
            case "place done" -> {
                this.close();
                try {
                    Beep.tone(440, 100, 0.5);
                } catch (LineUnavailableException _) {
                    // ignore it, sound isn't necessary
                }
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
