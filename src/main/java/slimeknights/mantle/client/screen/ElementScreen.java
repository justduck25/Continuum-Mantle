package slimeknights.mantle.client.screen;

import net.minecraft.client.renderer.RenderPipelines;
import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Represents a GUI element INSIDE the graphics file.
 * The coordinates all refer to the coordinates inside the graphics!
 */
@AllArgsConstructor
public class ElementScreen {
  // TODO: can this be final?
  public Identifier texture;
  public final int x;
  public final int y;
  public final int w;
  public final int h;

  public final int texW;
  public final int texH;

  /** Creates a new element from this texture with the X, Y, width, and height */
  public ElementScreen move(int x, int y, int width, int height) {
    return new ElementScreen(this.texture, x, y, width, height, this.texW, this.texH);
  }

  /** Creates a new element by offsetting this element by the given amount */
  public ElementScreen shift(int xd, int yd) {
    return move(x + xd, y + yd, this.w, this.h);
  }

  /**
   * Draws the element at the given x/y coordinates
   *
   * @param xPos X-Coordinate on the screen
   * @param yPos Y-Coordinate on the screen
   */
  public void draw(GuiGraphicsExtractor graphics, int xPos, int yPos) {
    this.drawWithColor(graphics, xPos, yPos, -1);
  }

  /**
   * Draws the element at the given x/y coordinates with alpha transparency.
   *
   * @param xPos  X-Coordinate on the screen
   * @param yPos  Y-Coordinate on the screen
   * @param alpha Alpha value from 0.0F to 1.0F
   */
  public void draw(GuiGraphicsExtractor graphics, int xPos, int yPos, float alpha) {
    int a = Math.clamp((int) (alpha * 255.0F), 0, 255);
    this.drawWithColor(graphics, xPos, yPos, (a << 24) | 0x00FFFFFF);
  }

  /**
   * Draws the element at the given x/y coordinates with an ARGB color tint.
   *
   * @param xPos  X-Coordinate on the screen
   * @param yPos  Y-Coordinate on the screen
   * @param color ARGB color
   */
  public void drawWithColor(GuiGraphicsExtractor graphics, int xPos, int yPos, int color) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, xPos, yPos, this.x, this.y, this.w, this.h, this.texW, this.texH, color);
  }

  /**
   * Legacy method for backwards compatibility.
   */
  @Deprecated
  public void draw(GuiGraphicsExtractor graphics, int xPos, int yPos, int blitOffset) {
    this.draw(graphics, xPos, yPos);
  }
}
