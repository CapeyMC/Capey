package hu.jgj52.capey.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import hu.jgj52.capey.screen.ConfigScreen;
import net.minecraft.network.chat.Component;

public class ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(Component.empty(), parent).createScreen();
    }
}
