package hu.jgj52.capey.screen;

import com.google.common.net.HttpHeaders;
import com.mojang.authlib.GameProfile;
import dev.tr7zw.trender.gui.TriState;
import dev.tr7zw.trender.gui.client.AbstractConfigScreen;
import dev.tr7zw.trender.gui.client.CottonClientScreen;
import dev.tr7zw.trender.gui.client.RenderContext;
import dev.tr7zw.trender.gui.widget.*;
import dev.tr7zw.trender.gui.widget.data.InputResult;
import dev.tr7zw.trender.gui.widget.data.Insets;
import hu.jgj52.capey.Capey;
import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ConfigScreen extends AbstractConfigScreen {
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

    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Minecraft mc = Minecraft.getInstance();

    public ConfigScreen(Component title, Screen previous) {
        super(title, previous);

        // i hate rendering
        WGridPanel root = new WGridPanel(0) {
            @Override
            public void paint(RenderContext context, int x, int y, int mouseX, int mouseY) {
                context.getGuiGraphics().enableScissor(
                        x,
                        y,
                        x + getWidth(),
                        y + getHeight()
                );
                super.paint(context, x, y, mouseX, mouseY);
            }
        };
        root.setInsets(Insets.ROOT_PANEL);
        setRootPanel(root);

        WGridPanel capePanel = new WGridPanel(1);

        WScrollPanel scrollPanel = new WScrollPanel(capePanel);
        scrollPanel.setScrollingHorizontally(TriState.FALSE);
        scrollPanel.setScrollingVertically(TriState.DEFAULT);
        root.add(scrollPanel, 10, 25);

        if (mc.level != null && mc.player != null) {
            AtomicInteger offset = new AtomicInteger();
            int perRow = mc.getWindow().getGuiScaledWidth() / 80 - 2;
            Player local = Player.of(mc.player.getUUID());
            Supplier<Cape> selected = local::getCape;
            List<WPlayerPreview> previews = new ArrayList<>();
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
                WPlayerPreview preview = new WPlayerPreview(mcPlayer) {
                    @Override
                    public void paint(RenderContext context, int x, int y, int mouseX, int mouseY) {
                        super.paint(context, x, y, mouseX, mouseY);
                        if (selected.get() == cape && !isShowBackground()) {
                            previews.forEach(w -> w.setShowBackground(false));
                            setShowBackground(true);
                        } else if (selected.get() == null && isShowBackground()) {
                            setShowBackground(false);
                        }
                    }

                    @Override
                    public InputResult onClick(int x, int y, int button) {
                        String nowUUID = mc.player.getUUID().toString();
                        String capeUUID = cape.getUUID().toString();
                        if (isShowBackground()) Capey.local.getContent().remove(nowUUID);
                        else Capey.local.getContent().addProperty(nowUUID, capeUUID);
                        Capey.local.save();
                        local.reFetch();
                        fetcher.submit(() -> {
                            try {
                                HttpRequest request = HttpRequest.newBuilder()
                                        .uri(new URI("https://capey.jgj52.hu/v1/player"))
                                        .POST(
                                                isShowBackground()
                                                ? HttpRequest.BodyPublishers.noBody()
                                                : HttpRequest.BodyPublishers.ofString(capeUUID)
                                        )
                                        .header(
                                                HttpHeaders.AUTHORIZATION,
                                                Capey.tokens.getContent().has(nowUUID)
                                                ? Capey.tokens.getContent().get(nowUUID).getAsString()
                                                : ""
                                        )
                                        .build();

                                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                                if (response.statusCode() != 200) {
                                    throw new RuntimeException(response.body());
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                        return InputResult.PROCESSED;
                    }
                };
                WText text = new WText(Component.literal(capeO.get("name").getAsString()).withColor(0xffffffff));
                previews.add(preview);
                preview.setRotationX(164);
                preview.setRotationY(5);
                preview.setShowBackground(false);
                int i = offset.getAndIncrement();
                capePanel.add(preview, (i % perRow) * 80, (i / perRow) * 100, 70, 140);
                capePanel.add(text, (i % perRow) * 80, (i / perRow) * 100 + 90, 70, mc.font.lineHeight);
            });
            scrollPanel.setSize(perRow * 80, Math.min(offset.get() / perRow * 100, mc.getWindow().getGuiScaledHeight() - 150));
        }

        root.validate(this);
        root.setHost(this);
    }

    @Override
    public Screen createScreen() {
        CottonClientScreen screen = (CottonClientScreen) super.createScreen();
        screen.addRenderableWidget(Button.builder(
                Component.translatable("capey.config.main.reload"),
                button ->
                    CompletableFuture.supplyAsync(() ->
                            Cape.all(true)).thenAccept(capes ->
                                mc.execute(screen::onClose)
                            )
                ).build()
        ).setPosition(10, 10);
        screen.addRenderableWidget(Button.builder(
                Component.translatable("capey.config.main.profile"),
                button ->
                        mc.execute(() ->
                            mc.setScreen(new ProfileScreen(Component.empty(), screen).createScreen())
                        )
                ).build()
        ).setPosition(screen.getWidth() - 10, 10);
        return screen;
    }

    @Override
    public void save() {

    }

    @Override
    public void reset() {

    }
}