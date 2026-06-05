package hu.jgj52.capey.screen;

import com.mojang.authlib.GameProfile;
import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Player;
import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;
import java.util.function.Supplier;

public class ConfigScreen extends BetterScreen {
    private static class FakePlayer extends RemotePlayer {
        private final Supplier<PlayerSkin> skin;

        public FakePlayer(ClientLevel level, GameProfile gameProfile, Supplier<PlayerSkin> skin) {
            super(level, gameProfile);
            this.skin = skin;

            byte skinParts = 0;
            for (PlayerModelPart part : PlayerModelPart.values()) {
                skinParts |= (byte) part.getMask();
            }
            getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, skinParts);
        }

        @Override
        public @NotNull PlayerSkin getSkin() {
            return skin.get();
        }
    }

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
        Cape.all(false).forEach(capeO -> {
            Player player = Player.of(UUID.fromString(capeO.get("uploader").getAsString()));
            Cape cape = Cape.of(UUID.fromString(capeO.get("uuid").getAsString()));
            GameProfile profile = new GameProfile(player.getUUID(), "");
            Supplier<PlayerSkin> skinWithCape = cape.fromSkin(player.getSkin());

            FakePlayer mcPlayer = new FakePlayer(
                    mc.level,
                    profile,
                    skinWithCape
            );
            EntityRenderState state = mc.getEntityRenderDispatcher().getRenderer(mcPlayer).createRenderState();

            guiGraphicsExtractor.entity(
                    state,
                    40,
                    new Vector3f(0, 0, 0),
                    new Quaternionf(),
                    null,
                    10,
                    10,
                    200,
                    200
            );
        });
    }

    @Override
    protected void createWidgets(Font font) {

    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}