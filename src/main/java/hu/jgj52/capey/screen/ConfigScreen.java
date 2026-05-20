package hu.jgj52.capey.screen;

import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Player;
import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.UUID;

public class ConfigScreen extends BetterScreen {
    private static final Minecraft mc = Minecraft.getInstance();
    private final Screen parent;
    public ConfigScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    protected int getScrollSpeed() {
        return 10;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float v) {
        graphics.fill(40, 20, width - 40, height - 20, 0x80000000);
    }

    @Override
    protected void createWidgets(Font font) {
        Cape.all(false).forEach(cape -> {
            Player player = Player.of(UUID.fromString(cape.get("uploader").getAsString()));
            widget(new PlayerSkinWidget(
                    72,
                    144,
                    mc.getEntityModels(),
                    player.fromSkin()
            ));
        });
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}
