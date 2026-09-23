package slimeknights.mantle.fluid;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

/** Fluid type whose client extensions are supplied by the active NeoForge client defaults. */
public class TextureFluidType extends FluidType {
  public TextureFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public boolean move(LivingEntity entity, Vec3 movementVector, double gravity) {
    return FluidMovement.move(this, entity, movementVector, gravity);
  }
}