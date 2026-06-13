package hu.jgj52.capey.widget;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    private final int scale;
    private boolean background = false;
    private float rotationX = 160;
    private float rotationY = 200;
    public PlayerWithCapeWidget(int x, int y, int width, int height, FakePlayer player, int scale) {
        super(x, y, width, height, Component.empty());
        this.player = player;
        this.scale = scale;
    }
    public PlayerWithCapeWidget(int x, int y, int width, int height, FakePlayer player) {
        this(x, y, width, height, player, 40);
    }

    public FakePlayer getPlayer() {
        return player;
    }

    public void background(boolean background) {
        this.background = background;
    }

    public boolean background() {
        return background;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (background) {
            graphics.fill(
                    getX(),
                    getY(),
                    getX() + getWidth(),
                    getY() + getHeight(),
                    0xff000000
            );
        }

        EntityRenderer<FakePlayer, EntityRenderState> renderer = (EntityRenderer<FakePlayer, EntityRenderState>) mc.getEntityRenderDispatcher().getRenderer(player);
        EntityRenderState state = renderer.createRenderState();
        renderer.extractRenderState(player, state, a);
        if (state instanceof AvatarRenderState st) {
            st.elytraRotX = 0.25f;
            st.elytraRotY = -0.01f;
            st.elytraRotZ = -0.275f;
        }

        graphics.entity(
                state,
                scale,
                new Vector3f(0, 0.7f, 0),
                new Quaternionf()
                        .rotateX((float) Math.toRadians(rotationX))
                        .rotateY((float) Math.toRadians(rotationY)),
                new Quaternionf(),
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight()
        );
    }

    @Override
    protected void onDrag(@NotNull MouseButtonEvent event, double dx, double dy) {
        rotationX = Mth.clamp(this.rotationX - (float) -dy * 2.5f, 125.0f, 225.0f);
        rotationY += (float) dx * 2.5f;
        super.onDrag(event, dx, dy);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}
