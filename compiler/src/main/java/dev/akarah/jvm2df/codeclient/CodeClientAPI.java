package dev.akarah.jvm2df.codeclient;

import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.util.Beep;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.sound.sampled.LineUnavailableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CodeClientAPI extends WebSocketClient {
    List<CodeLine> functions;

    public CodeClientAPI(List<CodeLine> functions) throws URISyntaxException {
        super(new URI("ws://localhost:31375"));
        this.functions = new ArrayList<>(functions);
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        this.send("scopes read_plot write_code clear_plot");
        this.send("auth");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("<- " + message);
        switch (message) {
            case "auth" -> {
                this.send("mode code");
                this.send("clear");
                this.send("spawn");
                try {
                    Thread.sleep(100);
                } catch (Exception _) {
                    // ignored bruh
                }
                this.onMessage("MASSIVE"); // todo: get cc bug fixed
            }
            case "place done" -> {
                this.close();
                try {
                    Beep.tone(440, 100, 0.5);
                } catch (LineUnavailableException _) {
                    // ignore it, sound isn't necessary
                }
            }
            case "BASIC", "LARGE", "MASSIVE", "MEGA" -> {
                var size = switch (message) {
                    case "BASIC" -> 25;
                    case "LARGE" -> 50;
                    default -> 150;
                };
                this.foldFunctions(size, this.functions.size());
                this.send("place");
                for (var line : this.functions) {
                    this.send("place " + line.codeString());
                }
                this.send("place go");
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

    @Override
    public void send(String text) {
        System.out.println("-> " + text);
        super.send(text);
    }

    private void foldFunctions(int maxSize, int folds) {
        for (int foldCount = 0; foldCount < folds; foldCount++) {
            boolean folded = false;
            int startLine = 0;
            for (int currPointer = startLine; currPointer < this.functions.size(); currPointer++) {
                for (int chkPointer = startLine; chkPointer < this.functions.size(); chkPointer++) {
                    if (chkPointer == currPointer) {
                        continue;
                    }
                    var ls = this.functions.get(currPointer).codeBlocks().size();
                    var rs = this.functions.get(chkPointer).codeBlocks().size();
                    if (ls + rs < maxSize) {
                        var obj = this.functions.remove(chkPointer);
                        this.functions.get(currPointer).codeBlocks().addAll(obj.codeBlocks());
                        folded = true;
                        startLine += 1;
                        break;
                    }
                }
                if (folded)
                    break;
            }
        }
    }
}
