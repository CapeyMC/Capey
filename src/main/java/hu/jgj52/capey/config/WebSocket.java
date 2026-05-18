package hu.jgj52.capey.config;

import hu.jgj52.capey.types.Player;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class WebSocket implements java.net.http.WebSocket.Listener {
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
