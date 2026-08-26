package slimeknights.mantle.fluid.texture;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

/** Client logic for {@link slimeknights.mantle.fluid.InvertedFluidType} */
public class ClientInvertedFluidType extends ClientTextureFluidType {
  private Identifier lastFlowing;
  private Identifier invertedFlowing;
  public ClientInvertedFluidType(FluidType type) {
    super(type);
  }

  public Identifier getFlowingTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
    Identifier flowing = getFlowingTexture();
    if (flowing == lastFlowing) {
      return invertedFlowing;
    }
    invertedFlowing = flowing.withSuffix("_inverted");
    lastFlowing = flowing;
    return invertedFlowing;
  }
}
