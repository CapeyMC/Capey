package hu.jgj52.capey.config; // idk why put this here

import hu.jgj52.capey.types.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocket implements java.net.http.WebSocket.Listener {
    private static final HttpClient client = HttpClient.newHttpClient();
    private final java.net.http.WebSocket webSocket;

    public WebSocket() {
        try {
            webSocket = client.newWebSocketBuilder()
                    .buildAsync(URI.create("wss://api.capey.app/v1"), this)
                    .join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<java.net.http.WebSocket> sendText(CharSequence text, boolean b) {
        return webSocket.sendText(text, b);
    }

    @Override
    public void onOpen(java.net.http.WebSocket webSocket) {
        java.net.http.WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        String[] strings = message.split(" ");
        switch (strings[0]) {
            case "cape":
                UUID uuid = UUID.fromString(strings[1]);
                Player.of(uuid).reFetch();
                break;
        }
        return java.net.http.WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
        return java.net.http.WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }
}
