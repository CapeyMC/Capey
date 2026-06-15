package hu.jgj52.capey.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class PlayerWithCapeWidget extends AbstractWidget {
    public static class FakePlayer {
        private static ItemStack ely = ItemStack.EMPTY;

        public static ItemStack ely() {
            if (!ely.isEmpty()) return ely;
            try {
                ely = new ItemStack(Items.ELYTRA);
            } catch (NullPointerException e) {
                ely = ItemStack.EMPTY;
            }
            return ely;
        }

        private final Supplier<PlayerSkin> skin;
        private boolean elytra = false;

        public FakePlayer(Supplier<PlayerSkin> skin) {
            this.skin = skin;
        }

        public void elytra() {
            elytra = !elytra;
        }

        public AvatarRenderState state() {
            AvatarRenderState state = new AvatarRenderState();
            state.skin = skin.get();
            state.elytraRotX = 0.25f;
            state.elytraRotY = -0.01f;
            state.elytraRotZ = -0.275f;
            if (elytra) {
                state.chestEquipment = ely();
            }
            return state;
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

    public void rotateX(float x) {
        rotationX = x;
    }

    public void rotateY(float y) {
        rotationY = y;
    }

    public void background(boolean background) {
        this.background = background;
    }

    public boolean background() {
        return background;
    }

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

        graphics.entity(
                player.state(),
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
        rotationX = Mth.clamp(this.rotationX - (float) -dy * 2.5f, 140.0f, 220.0f);
        rotationY += (float) dx * 2.5f;
        super.onDrag(event, dx, dy);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}
