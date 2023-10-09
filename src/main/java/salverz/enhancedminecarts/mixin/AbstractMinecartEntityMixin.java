package salverz.enhancedminecarts.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AbstractMinecartEntity.class)
public abstract class MinecartModifier {

    // Increases the max speed of minecarts
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    public void modifyMaxSpeed(CallbackInfoReturnable<Double> cir) {
        AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;
        System.out.println(minecart.getVelocity());
//        minecart.setVelocity(new Vec3d());
        cir.setReturnValue(20.0);
    }

    // Invokers for private/protected methods in moveOnRail()

    // Modification of the moveOnRail method to remove speed cap and modify derailment behavior
    @Inject(method = "moveOnRail", at = @At("HEAD"), cancellable = true)
    private void modifyMoveOnRail(BlockPos pos, BlockState state, CallbackInfo ci) {
        AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;
        AbstractMinecartEntityInvoker minecartEntityInvoker = (AbstractMinecartEntityInvoker) minecart;

        System.out.println("HIJACKING moveOnRail()");
        minecart.onLanding();
        double d = minecart.getX();
        double e = minecart.getY();
        double f = minecart.getZ();
        Vec3d vec3d = minecart.snapPositionToRail(d, e, f);
        e = (double)pos.getY();
        boolean bl = false;
        boolean bl2 = false;
        if (state.isOf(Blocks.POWERED_RAIL)) {
            bl = (Boolean)state.get(PoweredRailBlock.POWERED);
            bl2 = !bl;
        }

        double g = 0.0078125;
        if (minecart.isTouchingWater()) {
            g *= 0.2;
        }

        Vec3d vec3d2 = minecart.getVelocity();
        RailShape railShape = (RailShape)state.get(((AbstractRailBlock)state.getBlock()).getShapeProperty());
        switch (railShape) {
            case ASCENDING_EAST:
                minecart.setVelocity(vec3d2.add(-g, 0.0, 0.0));
                ++e;
                break;
            case ASCENDING_WEST:
                minecart.setVelocity(vec3d2.add(g, 0.0, 0.0));
                ++e;
                break;
            case ASCENDING_NORTH:
                minecart.setVelocity(vec3d2.add(0.0, 0.0, g));
                ++e;
                break;
            case ASCENDING_SOUTH:
                minecart.setVelocity(vec3d2.add(0.0, 0.0, -g));
                ++e;
        }

        vec3d2 = minecart.getVelocity();
        Pair<Vec3i, Vec3i> pair = AbstractMinecartEntityInvoker.invokeGetAdjacentRailPositionsByShape(railShape);
        Vec3i vec3i = (Vec3i)pair.getFirst();
        Vec3i vec3i2 = (Vec3i)pair.getSecond();
        double h = (double)(vec3i2.getX() - vec3i.getX());
        double i = (double)(vec3i2.getZ() - vec3i.getZ());
        double j = Math.sqrt(h * h + i * i);
        double k = vec3d2.x * h + vec3d2.z * i;
        if (k < 0.0) {
            h = -h;
            i = -i;
        }

        double l = Math.min(2.0, vec3d2.horizontalLength());
        vec3d2 = new Vec3d(l * h / j, vec3d2.y, l * i / j);
        minecart.setVelocity(vec3d2);
        Entity entity = minecart.getFirstPassenger();
        if (entity instanceof PlayerEntity) {
            Vec3d vec3d3 = entity.getVelocity();
            double m = vec3d3.horizontalLengthSquared();
            double n = minecart.getVelocity().horizontalLengthSquared();
            if (m > 1.0E-4 && n < 0.01) {
                minecart.setVelocity(minecart.getVelocity().add(vec3d3.x * 0.1, 0.0, vec3d3.z * 0.1));
                bl2 = false;
            }
        }

        double o;
        if (bl2) {
            o = minecart.getVelocity().horizontalLength();
            if (o < 0.03) {
                minecart.setVelocity(Vec3d.ZERO);
            } else {
                minecart.setVelocity(minecart.getVelocity().multiply(0.5, 0.0, 0.5));
            }
        }

        o = (double)pos.getX() + 0.5 + (double)vec3i.getX() * 0.5;
        double p = (double)pos.getZ() + 0.5 + (double)vec3i.getZ() * 0.5;
        double q = (double)pos.getX() + 0.5 + (double)vec3i2.getX() * 0.5;
        double r = (double)pos.getZ() + 0.5 + (double)vec3i2.getZ() * 0.5;
        h = q - o;
        i = r - p;
        double s;
        double t;
        double u;
        if (h == 0.0) {
            s = f - (double)pos.getZ();
        } else if (i == 0.0) {
            s = d - (double)pos.getX();
        } else {
            t = d - o;
            u = f - p;
            s = (t * h + u * i) * 2.0;
        }

        d = o + h * s;
        f = p + i * s;
        minecart.setPosition(d, e, f);
        t = minecart.hasPassengers() ? 0.75 : 1.0;
        u = minecartEntityInvoker.invokeGetMaxSpeed();
        vec3d2 = minecart.getVelocity();
        minecart.move(MovementType.SELF, new Vec3d(MathHelper.clamp(t * vec3d2.x, -u, u), 0.0, MathHelper.clamp(t * vec3d2.z, -u, u)));
        if (vec3i.getY() != 0 && MathHelper.floor(minecart.getX()) - pos.getX() == vec3i.getX() && MathHelper.floor(minecart.getZ()) - pos.getZ() == vec3i.getZ()) {
            minecart.setPosition(minecart.getX(), minecart.getY() + (double)vec3i.getY(), minecart.getZ());
        } else if (vec3i2.getY() != 0 && MathHelper.floor(minecart.getX()) - pos.getX() == vec3i2.getX() && MathHelper.floor(minecart.getZ()) - pos.getZ() == vec3i2.getZ()) {
            minecart.setPosition(minecart.getX(), minecart.getY() + (double)vec3i2.getY(), minecart.getZ());
        }

        minecartEntityInvoker.invokeApplySlowdown();
        Vec3d vec3d4 = minecart.snapPositionToRail(minecart.getX(), minecart.getY(), minecart.getZ());
        Vec3d vec3d5;
        double w;
        if (vec3d4 != null && vec3d != null) {
            double v = (vec3d.y - vec3d4.y) * 0.05;
            vec3d5 = minecart.getVelocity();
            w = vec3d5.horizontalLength();
            if (w > 0.0) {
                minecart.setVelocity(vec3d5.multiply((w + v) / w, 1.0, (w + v) / w));
            }

            minecart.setPosition(minecart.getX(), vec3d4.y, minecart.getZ());
        }

        int x = MathHelper.floor(minecart.getX());
        int y = MathHelper.floor(minecart.getZ());
        if (x != pos.getX() || y != pos.getZ()) {
            vec3d5 = minecart.getVelocity();
            w = vec3d5.horizontalLength();
            minecart.setVelocity(w * (double)(x - pos.getX()), vec3d5.y, w * (double)(y - pos.getZ()));
        }

        if (bl) {
            vec3d5 = minecart.getVelocity();
            w = vec3d5.horizontalLength();
            if (w > 0.01) {
                double z = 0.06;
                minecart.setVelocity(vec3d5.add(vec3d5.x / w * 0.06, 0.0, vec3d5.z / w * 0.06));
            } else {
                Vec3d vec3d6 = minecart.getVelocity();
                double aa = vec3d6.x;
                double ab = vec3d6.z;
                if (railShape == RailShape.EAST_WEST) {
                    if (minecartEntityInvoker.invokeWillHitBlockAt(pos.west())) {
                        aa = 0.02;
                    } else if (minecartEntityInvoker.invokeWillHitBlockAt(pos.east())) {
                        aa = -0.02;
                    }
                } else {
                    if (railShape != RailShape.NORTH_SOUTH) {
                        return;
                    }

                    if (minecartEntityInvoker.invokeWillHitBlockAt(pos.north())) {
                        ab = 0.02;
                    } else if (minecartEntityInvoker.invokeWillHitBlockAt(pos.south())) {
                        ab = -0.02;
                    }
                }

                minecart.setVelocity(aa, vec3d6.y, ab);
            }
        }

        ci.cancel();
    }
}
