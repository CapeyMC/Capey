package hu.jgj52.capey.mixin;

import com.mojang.authlib.GameProfile;
import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Player;
import net.minecraft.client.multiplayer.PlayerInfo;
//? >= 1.21.10 {
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
//? } else {
/*import net.minecraft.client.resources.PlayerSkin;
*///? }
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerSkin original = cir.getReturnValue();
        PlayerInfo info = (PlayerInfo) (Object) this;
        Player player = Player.of(getUUID(info.getProfile()));

        Cape cape = player.getCape();

        if (cape != null && cape.getIdentifier() != null) {

            PlayerSkin skin = new PlayerSkin(
            //? >= 1.21.10 {
                    original.body(),
                    new TextureImpl(cape.getIdentifier()), new TextureImpl(cape.getIdentifier()),
                    original.model(),
                    original.secure()
            //? } else {
                    /*original.texture(), original.textureUrl(),
                    cape.getIdentifier(), cape.getIdentifier(),
                    original.model(),
                    original.secure()
            *///? }
            );

            cir.setReturnValue(skin);
        }
    }

    //? >= 1.21.10 {
    record TextureImpl(Identifier identifier) implements ClientAsset.Texture {
        @Override
        public @NonNull Identifier texturePath() {
            return identifier;
        }

        @Override
        public @NonNull Identifier id() {
            return identifier;
        }
    }
    //? }

    @Unique
    private UUID getUUID(GameProfile profile) {
        return profile.
                //? >= 1.21.10 {
                    id
                //? } else {
                    /*getId
                *///? }
            ();
    }
}
