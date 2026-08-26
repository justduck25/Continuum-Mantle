package slimeknights.mantle.client.book.structure;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.client.book.structure.level.TemplateLevel;

public class StructurePreviewRenderer {
  private StructurePreviewRenderer() {}

  /**
   * Renders the structure as an isometric 3D preview using axis-aligned fills.
   * Uses painter's algorithm: renders from bottom layer to top, back to front.
   */
  public static void render(GuiGraphicsExtractor graphics, TemplateLevel world, StructureInfo info,
                            int areaX, int areaY, int areaWidth, int areaHeight) {
    int sL = Math.max(1, info.structureLength);
    int sW = Math.max(1, info.structureWidth);
    int sH = Math.max(1, info.structureHeight);

    float scale = Math.min(
      areaWidth / (float)(sL + sW),
      areaHeight / (float)(sL + sW + sH)
    );
    if (scale < 1) scale = 1;

    int centerX = areaX + areaWidth / 2;
    int centerY = areaY + areaHeight / 2;
    int cell = Math.max(1, (int) Math.ceil(scale));

    graphics.fill(areaX, areaY, areaX + areaWidth, areaY + areaHeight, 0x66000000);

    // Painter's algorithm: bottom layer first (lower Y), then top layers
    // Within each layer: high bx+bz (back) first, then low bx+bz (front)
    for (int by = 0; by < sH; by++) {
      float heightBrightness = 0.8f + 0.2f * (by / (float) Math.max(1, sH - 1));

      for (int sum = sL + sW - 2; sum >= 0; sum--) {
        for (int bx = 0; bx < sL; bx++) {
          int bz = sum - bx;
          if (bz < 0 || bz >= sW) continue;

          BlockPos pos = new BlockPos(bx, by, bz);
          if (!info.test(pos)) continue;
          BlockState state = world.getBlockState(pos);
          if (state.isAir()) continue;

          int color = state.getMapColor(world, new BlockPos(bx, 0, bz)).col;
          if (color == 0) color = 0x777777;

          int sx = centerX + (int) ((bx - bz) * scale);
          int sy = centerY + (int) ((bx + bz) * scale * 0.5f - by * scale);

          int r = Math.min(255, (int) (((color >> 16) & 0xFF) * heightBrightness));
          int g = Math.min(255, (int) (((color >> 8) & 0xFF) * heightBrightness));
          int b = Math.min(255, (int) ((color & 0xFF) * heightBrightness));
          int adjustedColor = 0xFF000000 | (r << 16) | (g << 8) | b;

          graphics.fill(sx, sy, sx + cell, sy + cell, adjustedColor);
          graphics.fill(sx, sy, sx + cell, sy + 1, 0x44000000);
          graphics.fill(sx, sy, sx + 1, sy + cell, 0x44000000);
        }
      }
    }
  }
}
