package hu.jgj52.capey.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

import static hu.jgj52.capey.Capey.tokens;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void onKick(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundLoginDisconnectPacket(Component reason)) {
            Connection connection = (Connection) (Object) this;

            if (connection.getRemoteAddress() instanceof InetSocketAddress addr) {
                if (addr.getHostName().equals("deb.jgj52.hu") && addr.getPort() == 61250) {
                    String reasonS = reason.getString();
                    String[] split = reasonS.split("\\$");

                    if (split.length != 3) return;
                    String uuid = Minecraft.getInstance().getUser().getProfileId().toString();
                    String token = split[1];

                    tokens.getContent().addProperty(uuid, token);
                    tokens.save();
                }
            }
        }
    }
}
