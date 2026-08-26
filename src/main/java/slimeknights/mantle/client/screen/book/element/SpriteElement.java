package slimeknights.mantle.client.screen.book.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;

import static slimeknights.mantle.client.screen.book.element.ItemElement.ITEM_SIZE_HARDCODED;

/** Element that just draws a sprite from a texture atlas */
public class SpriteElement extends SizedBookElement {
  private final TextureAtlasSprite sprite;
  private float scale;

  public SpriteElement(int x, int y, float scale, TextureAtlasSprite sprite) {
    super(x, y, Mth.floor(ITEM_SIZE_HARDCODED * scale), Mth.floor(ITEM_SIZE_HARDCODED * scale));
    this.scale = scale;
    this.sprite = sprite;
  }

  public SpriteElement(int x, int y, float scale, Identifier location) {
    this(x, y, scale, Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, location)));
  }

  @Override
  public void scale(float scale) {
    this.scale = scale;
  }

  @Override
  public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    int x = this.x;
    int y = this.y;
    // if scaling, need to adjust pose stack
    if (scale != 1) {
      var matrices = graphics.pose();
      matrices.pushMatrix();
      // want to translate before scaling, so clear local variables
      matrices.translate(x, y);
      x = 0;
      y = 0;
      matrices.scale(scale, scale);
    }
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16);
    if (scale != 1) {
      graphics.pose().popMatrix();
    }
  }
}
