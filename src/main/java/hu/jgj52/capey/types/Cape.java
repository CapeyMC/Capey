package hu.jgj52.capey.types;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.
//? >= 1.21.11 {
Identifier
//? } else {
/*ResourceLocation
*///? }
;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cape {
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Map<UUID, Cape> capes = new ConcurrentHashMap<>();
    public static Cape of(UUID uuid) {
        return capes.computeIfAbsent(uuid, Cape::new);
    }

    private final UUID uuid;
    private final Path path;
    private volatile
    //? >= 1.21.11 {
    Identifier
    //? } else {
    /*ResourceLocation
     *///? }
    identifier;

    private Cape(UUID uuid) {
        this.uuid = uuid;
        path = Path.of(FabricLoader.getInstance().getGameDir().toString(), "capey", uuid.toString());
        File file = new File(path.toUri());
        if (file.exists()) {
            registerTextures();
        } else {
            fetcher.submit(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("https://capey.jgj52.hu/v1/cape/" + uuid))
                            .GET()
                            .build();

                    HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(path));
                    if (response.statusCode() == 404) {
                        identifier = null;
                    }
                    if (response.statusCode() == 200) {
                        registerTextures();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void registerTextures() {
        try {
            NativeImage img = NativeImage.read(Files.newInputStream(path));
            identifier = //? >= 1.21.11 {
                    Identifier
                    //? } else {
                    /*ResourceLocation
                     *///? }
                    .fromNamespaceAndPath("capey", "capes/" + uuid);
            mc.execute(() -> {
                DynamicTexture texture = new DynamicTexture(() -> "", img);
                texture.upload();
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public
    //? >= 1.21.11 {
    Identifier
    //? } else {
    /*ResourceLocation
     *///? }
    getIdentifier() {
        return identifier;
    }
}
