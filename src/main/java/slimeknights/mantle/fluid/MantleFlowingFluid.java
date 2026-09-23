package slimeknights.mantle.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** Default Mantle flowing fluid, with a hook for surface and drip particles. */
public abstract class MantleFlowingFluid {
  private MantleFlowingFluid() {}

  public static class Source extends BaseFlowingFluid.Source {
    public Source(Properties properties) {
      super(properties);
    }

    @Override
    public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
      FlowingFluidEffects.animate(this, level, pos, state, random);
    }

    @Override
    protected ParticleOptions getDripParticle() {
      return FlowingFluidEffects.drip(getSource());
    }
  }

  public static class Flowing extends BaseFlowingFluid.Flowing {
    public Flowing(Properties properties) {
      super(properties);
    }

    @Override
    public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
      FlowingFluidEffects.animate(this, level, pos, state, random);
    }

    @Override
    protected ParticleOptions getDripParticle() {
      return FlowingFluidEffects.drip(getSource());
    }
  }
}
