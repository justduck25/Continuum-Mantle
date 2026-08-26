package slimeknights.mantle.client.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;

/**
 * Class for render types defined by Mantle.
 */
public final class MantleRenderTypes {
  private MantleRenderTypes() {}

  /** Render type used for the fluid renderer. */
  public static final RenderType FLUID = RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);

  /** Render type used for the structure renderer. */
  public static final RenderType TRANSLUCENT_FULLBRIGHT = RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
}