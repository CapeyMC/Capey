package hu.jgj52.capey.screen;

import com.mojang.authlib.GameProfile;
import dev.tr7zw.trender.gui.client.AbstractConfigScreen;
import dev.tr7zw.trender.gui.widget.WGridPanel;
import dev.tr7zw.trender.gui.widget.data.Insets;
import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Player;
import hu.jgj52.capey.widget.PlayerPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ConfigScreen extends AbstractConfigScreen {
    private static final List<FakePlayer> fakes = new ArrayList<>();
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

            fakes.add(this);
        }

        @Override
        public @NonNull PlayerSkin getSkin() {
            return skin.get();
        }
    }

    private static final Minecraft mc = Minecraft.getInstance();

    public ConfigScreen(Component title, Screen previous) {
        super(title, previous);

        WGridPanel root = new WGridPanel(1);
        root.setInsets(Insets.ROOT_PANEL);
        setRootPanel(root);

        WGridPanel buttons = new WGridPanel(0);
        buttons.setInsets(Insets.NONE);

        if (mc.level != null) {
            AtomicInteger offset = new AtomicInteger();
            int perRow = mc.getWindow().getGuiScaledWidth() / 80 - 2;
            Cape.all(false).forEach(capeO -> {
                Player player = Player.of(UUID.fromString(capeO.get("uploader").getAsString()));
                Cape cape = Cape.of(UUID.fromString(capeO.get("uuid").getAsString()));
                GameProfile profile = new GameProfile(player.getUUID(), "player");
                Supplier<PlayerSkin> skinWithCape = cape.fromSkin(player.getSkin());

                FakePlayer mcPlayer = new FakePlayer(
                        mc.level,
                        profile,
                        skinWithCape
                );
                PlayerPreview preview = new PlayerPreview(mcPlayer);
                preview.setRotationX(164);
                preview.setRotationY(5);
                preview.setShowBackground(true);
                int i = offset.getAndIncrement();
                root.add(preview, (i % perRow) * 80, (i / perRow) * 100, 70, 140);
            });
        }

        root.validate(this);
        root.setHost(this);
    }

    @Override
    public void save() {

    }

    @Override
    public void reset() {

    }
}
