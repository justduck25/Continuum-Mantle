package slimeknights.mantle.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** Flowing fluid family used by Mantle's inverted-fluid registrations. */
public abstract class InvertedFluid extends BaseFlowingFluid {
  protected InvertedFluid(Properties properties) {
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

  public static class Flowing extends InvertedFluid {
    public Flowing(Properties properties) {
      super(properties);
      registerDefaultState(stateDefinition.any().setValue(LEVEL, 7));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
      super.createFluidStateDefinition(builder);
      builder.add(LEVEL);
    }

    @Override
    public int getAmount(FluidState state) {
      return state.getValue(LEVEL);
    }

    @Override
    public boolean isSource(FluidState state) {
      return false;
    }
  }

  public static class Source extends InvertedFluid {
    public Source(Properties properties) {
      super(properties);
    }

    @Override
    public int getAmount(FluidState state) {
      return 8;
    }

    @Override
    public boolean isSource(FluidState state) {
      return true;
    }
  }
}