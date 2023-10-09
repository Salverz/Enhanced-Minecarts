package salverz.enhancedminecarts.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractMinecartEntity.class)
public interface AbstractMinecartEntityInvoker {
    @Invoker("getMaxSpeed")
    public double invokeGetMaxSpeed();

    @Invoker("applySlowdown")
    public void invokeApplySlowdown();

    @Invoker("willHitBlockAt")
    public boolean invokeWillHitBlockAt(BlockPos pos);

    @Invoker("getAdjacentRailPositionsByShape")
    public static Pair<Vec3i, Vec3i> invokeGetAdjacentRailPositionsByShape(RailShape shape) {
        throw new AssertionError();
    }
}
