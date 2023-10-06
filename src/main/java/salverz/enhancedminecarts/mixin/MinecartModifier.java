package salverz.enhancedminecarts.mixin;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public abstract class MinecartModifier {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    public void getMaxSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(20.0);
    }
}
