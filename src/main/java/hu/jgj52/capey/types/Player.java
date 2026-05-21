package hu.jgj52.capey.types;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerSkin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Player {
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Minecraft mc = Minecraft.getInstance();
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

    public Supplier<PlayerSkin> getSkin() {
        Optional<GameProfile> opt = mc.services().profileResolver().fetchById(uuid);
        if (opt.isEmpty()) return null;
        GameProfile profile = opt.get();
        //? < 1.21.10 {
        /*AtomicReference<PlayerSkin> skin = new AtomicReference<>(mc.getSkinManager().getInsecureSkin(profile));
        CompletableFuture.runAsync(() -> {
            ProfileResult withTexturesRes = mc.getMinecraftSessionService().fetchProfile(id(profile), true);
            if (withTexturesRes != null) {
                GameProfile withTextures = withTexturesRes.profile();
                //? > 1.21.3 {
                CompletableFuture<Optional<PlayerSkin>> aopt = mc.getSkinManager().getOrLoad(withTextures);
                aopt.thenAccept(opt -> opt.ifPresent(skin::set));
                //? } else {
                /^CompletableFuture<PlayerSkin> a = mc.getSkinManager().getOrLoad(withTextures);
                a.thenAccept(skin::set);
                ^///? }
            }
        });
        *///? }
        return
                //? >= 1.21.10 {
                mc.getSkinManager().createLookup(profile, true)
                //? } else {
                /*skin::get
                 *///? }
        ;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Cape getCape() {
        if (cape == null) return null;
        return Cape.of(cape);
    }
}
