package hu.jgj52.capey.screen;

import com.mojang.authlib.GameProfile;
import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Player;
import hu.jgj52.capey.widget.PlayerWithCapeWidget;
import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.UUID;
import java.util.function.Supplier;

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
        Cape.all(false).forEach(capeO -> {
            Player player = Player.of(UUID.fromString(capeO.get("uploader").getAsString()));
            Cape cape = Cape.of(UUID.fromString(capeO.get("uuid").getAsString()));
            GameProfile profile = new GameProfile(player.getUUID(), "");
            Supplier<PlayerSkin> skinWithCape = cape.fromSkin(player.getSkin());

            PlayerWithCapeWidget.FakePlayer p = new PlayerWithCapeWidget.FakePlayer(
                    mc.level,
                    profile,
                    skinWithCape
            );

            widget(new PlayerWithCapeWidget(
                    10,
                    10,
                    200,
                    200,
                    p
            ));
        });
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}