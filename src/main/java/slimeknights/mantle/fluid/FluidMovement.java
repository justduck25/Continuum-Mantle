package slimeknights.mantle.fluid;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * NeoForge 26 only applies vanilla water or lava travel. Custom fluids must move the entity themselves,
 * otherwise standing in a source block freezes movement.
 */
public final class FluidMovement {
  private FluidMovement() {}

  public static boolean move(FluidType type, LivingEntity entity, Vec3 input, double gravity) {
    boolean falling = entity.getDeltaMovement().y <= 0.0;
    double oldY = entity.getY();
    if (type.canSwim(entity)) {
      travelLikeWater(entity, input, gravity, falling, oldY);
    } else {
      travelLikeLava(type, entity, input, gravity, falling, oldY);
    }
    return true;
  }

  private static void travelLikeWater(LivingEntity entity, Vec3 input, double gravity, boolean falling, double oldY) {
    float slowDown = entity.isSprinting() ? 0.9F : 0.8F;
    float speed = 0.02F;
    float waterWalker = (float) entity.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
    if (!entity.onGround()) {
      waterWalker *= 0.5F;
    }
    if (waterWalker > 0.0F) {
      slowDown += (0.54600006F - slowDown) * waterWalker;
      speed += (entity.getSpeed() - speed) * waterWalker;
    }
    if (entity.hasEffect(MobEffects.DOLPHINS_GRACE)) {
      slowDown = 0.96F;
    }
    speed *= (float) entity.getAttributeValue(swimSpeed());
    entity.moveRelative(speed, input);
    entity.move(MoverType.SELF, entity.getDeltaMovement());
    Vec3 movement = entity.getDeltaMovement();
    if (entity.horizontalCollision && entity.onClimbable()) {
      movement = new Vec3(movement.x, 0.2, movement.z);
    }
    movement = movement.multiply(slowDown, 0.8F, slowDown);
    entity.setDeltaMovement(entity.getFluidFallingAdjustedMovement(gravity, falling, movement));
    jumpOut(entity, oldY);
  }

  private static void travelLikeLava(FluidType type, LivingEntity entity, Vec3 input, double gravity, boolean falling, double oldY) {
    entity.moveRelative(0.02F, input);
    entity.move(MoverType.SELF, entity.getDeltaMovement());
    if (entity.getFluidHeight(type) <= entity.getFluidJumpThreshold()) {
      entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5, 0.8F, 0.5));
      entity.setDeltaMovement(entity.getFluidFallingAdjustedMovement(gravity, falling, entity.getDeltaMovement()));
    } else {
      entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5));
    }
    if (gravity != 0.0) {
      entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, -gravity / 4.0, 0.0));
    }
    jumpOut(entity, oldY);
  }

  private static void jumpOut(LivingEntity entity, double oldY) {
    Vec3 movement = entity.getDeltaMovement();
    if (entity.horizontalCollision && entity.isFree(movement.x, movement.y + 0.6F - entity.getY() + oldY, movement.z)) {
      entity.setDeltaMovement(movement.x, 0.3F, movement.z);
    }
  }

  private static Holder<net.minecraft.world.entity.ai.attributes.Attribute> swimSpeed() {
    return NeoForgeMod.SWIM_SPEED;
  }
}
