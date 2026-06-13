package hu.jgj52.capey.mixin;

import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Player;
import net.minecraft.client.multiplayer.PlayerInfo;
//? >= 1.21.9 {
import net.minecraft.world.entity.player.PlayerSkin;
//? } else {
/*import net.minecraft.client.resources.PlayerSkin;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static hu.jgj52.capey.Capey.getUUID;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerSkin original = cir.getReturnValue();
        PlayerInfo info = (PlayerInfo) (Object) this;
        Player player = Player.of(getUUID(info.getProfile()));
        Cape cape = player.getCape();

        if (cape == null) return;

        cir.setReturnValue(cape.fromSkin(() -> original).get());
    }
}
