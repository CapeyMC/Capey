package hu.jgj52.capey.types;

import net.minecraft.client.Minecraft;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Player {
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Map<UUID, Player> players = new ConcurrentHashMap<>();
    public static Player of(UUID uuid) {
        return players.computeIfAbsent(uuid, Player::new);
    }

    private final UUID uuid;
    private volatile UUID cape;

    private Player(UUID uuid) {
        this.uuid = uuid;
        reFetch();
    }

    public void reFetch() {
        fetcher.submit(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://capey.jgj52.hu/v1/player/" + uuid))
                        .GET()
                        .build();

                HttpResponse<String> cape = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (cape.statusCode() == 404) {
                    this.cape = null;
                }
                if (cape.statusCode() == 200) {
                    this.cape = UUID.fromString(cape.body());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public UUID getUUID() {
        return uuid;
    }

    public Cape getCape() {
        return Cape.of(cape);
    }
}
