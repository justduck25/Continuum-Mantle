package slimeknights.mantle.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;

/** Client hook so flowing fluids can drip and pop without Mantle depending on a particle type. */
public final class FlowingFluidEffects {
  private static Handler handler;

  private FlowingFluidEffects() {}

  public static void setHandler(@Nullable Handler handler) {
    FlowingFluidEffects.handler = handler;
  }

  public static void animate(Fluid fluid, Level level, BlockPos pos, FluidState state, RandomSource random) {
    Handler current = handler;
    if (current != null) {
      current.animate(fluid, level, pos, state, random);
    }
  }

  @Nullable
  public static ParticleOptions drip(Fluid source) {
    Handler current = handler;
    return current == null ? null : current.drip(source);
  }

  public interface Handler {
    @Nullable
    ParticleOptions drip(Fluid source);

    void animate(Fluid fluid, Level level, BlockPos pos, FluidState state, RandomSource random);
  }
}
