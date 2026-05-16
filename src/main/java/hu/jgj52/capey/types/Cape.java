package hu.jgj52.capey.types;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cape {
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Map<UUID, Cape> capes = new HashMap<>();
    public static Cape of(UUID uuid) {
        return capes.computeIfAbsent(uuid, Cape::new);
    }

    private volatile Identifier identifier;

    private Cape(UUID uuid) {
        fetcher.submit(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://capey.jgj52.hu/cape/" + uuid))
                        .GET()
                        .build();

                Path path = Path.of(FabricLoader.getInstance().getGameDir().toString(), "capey", uuid.toString());
                HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(path));
                if (response.statusCode() == 404) {
                    identifier = null;
                }
                if (response.statusCode() == 200) {

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
