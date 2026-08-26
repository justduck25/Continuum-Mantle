package slimeknights.mantle.fluid.texture;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.data.client.DeanimateTextureGenerator;

import java.util.Map.Entry;
import java.util.Set;

/** Generates fluid camera textures using the first frame of the still texture */
public class FluidTextureCameraProvider extends DeanimateTextureGenerator {
  private final AbstractFluidTextureProvider provider;
  /** Fluid types from the provider to ignore */
  private final Set<FluidType> skip;

  public FluidTextureCameraProvider(PackOutput packOutput, ResourceManager resourceManager, AbstractFluidTextureProvider provider, Set<FluidType> skip) {
    super(packOutput, resourceManager);
    this.provider = provider;
    this.skip = skip;
  }

  public FluidTextureCameraProvider(PackOutput packOutput, ResourceManager resourceManager, AbstractFluidTextureProvider provider) {
    this(packOutput, resourceManager, provider, Set.of());
  }

  @Override
  protected void addTextures() {
    for (Entry<FluidType, FluidTexture.Builder> entry : provider.getAllTextures().entrySet()) {
      if (!skip.contains(entry.getKey())) {
        FluidTexture.Builder builder = entry.getValue();
        Identifier camera = builder.getCamera();
        if (camera != null) {
          deanimate(builder.getStill(), camera);
        }
      }
    }
  }

  @Override
  public String getName() {
    return "Fluid texture camera provider";
  }
}
