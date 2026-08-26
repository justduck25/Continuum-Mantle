package slimeknights.mantle.block.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

/** Liquid block setting the entity on fire. */
public class BurningLiquidBlock extends LiquidBlock {
  private final int burnTime;
  private final float damage;

  public BurningLiquidBlock(Supplier<? extends FlowingFluid> supplier, Properties properties, int burnTime, float damage) {
    super(supplier.get(), properties);
    this.burnTime = burnTime;
    this.damage = damage;
  }

  public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    if (!entity.fireImmune() && !state.getFluidState().isEmpty()) {
      entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), burnTime * 20));
      entity.hurt(entity.damageSources().lava(), damage);
      entity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + level.getRandom().nextFloat() * 0.4F);
    }
  }

  public static Function<Supplier<? extends FlowingFluid>, LiquidBlock> createBurning(MapColor color, int lightLevel, int burnTime, float damage) {
    return fluid -> new BurningLiquidBlock(fluid, FluidDeferredRegister.createProperties(color, lightLevel), burnTime, damage);
  }
}