package hu.jgj52.capey;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hu.jgj52.capey.config.WebSocket;
import hu.jgj52.capey.types.Cape;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Capey implements ModInitializer {
    public static WebSocket webSocket;
    public static Map<UUID, String> keys = new HashMap<>();
    public static JsonObject config;

    @Override
    public void onInitialize() {
        webSocket = new WebSocket();
        Cape.all(true);
        Gson gson = new Gson();
        try (FileInputStream fin = new FileInputStream(
                new File(Path.of(FabricLoader.getInstance().getConfigDir().toString(), "capey", "tokens.json").toUri())
        )) {
            JsonObject o = gson.fromJson(new InputStreamReader(fin), JsonObject.class);
            for (String key : o.keySet()) {
                keys.put(
                        UUID.fromString(key),
                        o.get(key).getAsString()
                );
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        try (FileInputStream fin = new FileInputStream(
                new File(Path.of(FabricLoader.getInstance().getGameDir().toString(), "capey", "config.json").toUri())
        )) {
            config = gson.fromJson(new InputStreamReader(fin), JsonObject.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
