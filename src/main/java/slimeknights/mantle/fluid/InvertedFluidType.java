package slimeknights.mantle.fluid;

import net.neoforged.neoforge.fluids.FluidType;

/** Fluid type for inverted fluids; client texture specialization is installed with the client layer. */
public class InvertedFluidType extends FluidType {
  public InvertedFluidType(Properties properties) {
    super(properties);
  }
}