package slimeknights.mantle.fluid;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

/** Fluid type for inverted fluids; client texture specialization is installed with the client layer. */
public class InvertedFluidType extends FluidType {
  public InvertedFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public boolean move(LivingEntity entity, Vec3 movementVector, double gravity) {
    return FluidMovement.move(this, entity, movementVector, gravity);
  }
}