package hu.jgj52.capey;

import hu.jgj52.capey.types.Cape;
import net.fabricmc.api.ModInitializer;

public class Capey implements ModInitializer {
    public static String key = "a";

    @Override
    public void onInitialize() {
        Cape.all(true);
    }
}
