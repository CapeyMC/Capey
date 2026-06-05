package hu.jgj52.capey.widget;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class PlayerWithCapeWidget extends AbstractWidget {
    private static final Minecraft mc = Minecraft.getInstance();
    public static class FakePlayer extends RemotePlayer {
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

    private final FakePlayer player;
    public PlayerWithCapeWidget(int x, int y, int width, int height, FakePlayer player) {
        super(x, y, width, height, Component.empty());
        this.player = player;
    }

    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        EntityRenderer<FakePlayer, EntityRenderState> renderer = (EntityRenderer<FakePlayer, EntityRenderState>) mc.getEntityRenderDispatcher().getRenderer(player);
        EntityRenderState state = renderer.createRenderState();
        renderer.extractRenderState(player, state, a);

        graphics.entity(
                state,
                40,
                new Vector3f(0, 0, 0),
                new Quaternionf().rotateY(60),
                new Quaternionf(),
                10,
                10,
                200,
                200
        );
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {

    }
}
