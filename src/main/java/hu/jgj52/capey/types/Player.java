package hu.jgj52.capey.types;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class Player {
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Map<UUID, Player> players = new ConcurrentHashMap<>();
    public static Player of(UUID uuid) {
        return players.computeIfAbsent(uuid, Player::new);
    }
    public static void reFetchAll() {
        players.values().forEach(Player::reFetch);
    }
    private static final Semaphore semaphore = new Semaphore(5); // i like the server alive

    private final UUID uuid;
    private volatile UUID cape;

    private Player(UUID uuid) {
        this.uuid = uuid;
    }

    public void reFetch() {
        fetcher.submit(() -> {
            try {
                semaphore.acquire();

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
            } finally {
                semaphore.release();
            }
        }, fetcher);
    }

    public UUID getUUID() {
        return uuid;
    }

    public Cape getCape() {
        if (cape == null) return null;
        return Cape.of(cape);
    }
}
