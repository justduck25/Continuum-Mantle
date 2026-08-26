package slimeknights.mantle.fluid.texture;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector4f;

import javax.annotation.Nullable;

/** Implementation of {@link IClientFluidTypeExtensions} using {@link FluidTexture} */
@RequiredArgsConstructor
public class ClientTextureFluidType implements IClientFluidTypeExtensions {
  protected final FluidType type;
  private Vector4f fogColor;

  public int getTintColor() {
    return FluidTextureManager.getColor(type);
  }

  public Identifier getStillTexture() {
    return FluidTextureManager.getStillTexture(type);
  }

  public Identifier getFlowingTexture() {
    return FluidTextureManager.getFlowingTexture(type);
  }

  @Nullable
  public Identifier getOverlayTexture() {
    return FluidTextureManager.getOverlayTexture(type);
  }

  @Nullable
  public Identifier getRenderOverlayTexture(Minecraft mc) {
    return FluidTextureManager.getCameraTexture(type);
  }

  @Override
  public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
    int fluidColor = FluidTextureManager.getData(type).fogColor();
    if (fluidColor != -1) {
      if (fogColor == null) {
        fogColor = new Vector4f(ARGB.red(fluidColor) / 255f, ARGB.green(fluidColor) / 255f, ARGB.blue(fluidColor) / 255f, 1);
      }
      fluidFogColor.x *= fogColor.x;
      fluidFogColor.y *= fogColor.y;
      fluidFogColor.z *= fogColor.z;
    }
  }

  @Override
  public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
    FluidTexture data = FluidTextureManager.getData(type);
    if (data.fogStart() < fogData.environmentalStart) {
      fogData.environmentalStart = data.fogStart();
    }
    if (data.fogEnd() < fogData.environmentalEnd) {
      fogData.environmentalEnd = data.fogEnd();
    }
  }
}
