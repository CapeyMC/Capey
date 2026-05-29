package hu.jgj52.capey.types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class Config {
    private static final Gson gson = new Gson();
    private static final File dir = Path.of(FabricLoader.getInstance().getConfigDir().toString(), "capey").toFile();

    private final File file;
    private final JsonObject content;
    public Config(String name) {
        file = new File(dir, name + ".json");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (file.exists()) {
            try (FileInputStream fin = new FileInputStream(file)) {
                content = gson.fromJson(new InputStreamReader(fin), JsonObject.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            content = new JsonObject();
        }
    }

    public JsonObject getContent() {
        return content;
    }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(gson.toJson(content).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
