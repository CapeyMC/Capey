package hu.jgj52.capey.screen;

import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

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
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {

    }

    @Override
    protected void createWidgets(Font font) {

    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}
