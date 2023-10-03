package salverz.enhancedminecarts.mixin;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractMinecartEntity.class)
abstract class MinecartModifier {
    @Redirect(method = "getMaxSpeed",
            at = @At(value = "INVOKE", target = ))
    Minecart
}
