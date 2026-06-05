package hu.jgj52.capey.screen;

import com.google.common.net.HttpHeaders;
import com.mojang.authlib.GameProfile;
import hu.jgj52.capey.Capey;
import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Player;
import hu.jgj52.capey.widget.PlayerWithCapeWidget;
import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ConfigScreen extends BetterScreen {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();

    private final Screen parent;
    public ConfigScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    protected int getScrollSpeed() {
        return 30;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int i, int i1, float v) {

    }

    @Override
    protected void createWidgets(Font font) {
        if (mc.level != null && mc.player != null) {
            AtomicInteger offset = new AtomicInteger();
            int perRow = mc.getWindow().getGuiScaledWidth() / 80 - 2;
            Player local = Player.of(mc.player.getUUID());
            Supplier<Cape> selected = local::getCape;
            List<PlayerWithCapeWidget> all = new ArrayList<>();
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

                int i = offset.getAndIncrement();
                all.add(widget(new PlayerWithCapeWidget(
                        (i % perRow) * 80 + (width - perRow * 80) / 2,
                        (i / perRow) * 100,
                        70,
                        100,
                        p
                ) {
                    @Override
                    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

                        if (selected.get() == cape && !background()) {
                            all.forEach(w -> w.background(false));
                            background(true);
                        } else if (selected.get() == null && background()) {
                            background(false);
                        }
                    }

                    @Override
                    public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
                        String nowUUID = mc.player.getUUID().toString();
                        String capeUUID = cape.getUUID().toString();
                        if (background()) Capey.local.get().remove(nowUUID);
                        else Capey.local.get().addProperty(nowUUID, capeUUID);
                        Capey.local.save();
                        local.reFetch();
                        fetcher.submit(() -> {
                            try {
                                HttpRequest request = HttpRequest.newBuilder()
                                        .uri(new URI("https://capey.jgj52.hu/v1/player"))
                                        .POST(
                                                background()
                                                        ? HttpRequest.BodyPublishers.noBody()
                                                        : HttpRequest.BodyPublishers.ofString(capeUUID)
                                        )
                                        .header(
                                                HttpHeaders.AUTHORIZATION,
                                                Capey.tokens.get().has(nowUUID)
                                                        ? Capey.tokens.get().get(nowUUID).getAsString()
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
                    }
                }));
                widget(new StringWidget(
                        (i % perRow) * 80 + (width - perRow * 80) / 2,
                        (i / perRow) * 100 + 90,
                        70,
                        font.lineHeight,
                        Component.literal(capeO.get("name").getAsString()),
                        font
                ));
            });
        }
    }

    @Override
    public void resize(int width, int height) {
        onClose();
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}