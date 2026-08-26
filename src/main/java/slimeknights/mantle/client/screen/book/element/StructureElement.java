package slimeknights.mantle.client.screen.book.element;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.joml.Quaternionf;
import slimeknights.mantle.client.book.structure.StructureInfo;
import slimeknights.mantle.client.book.structure.StructurePreviewRenderer;
import slimeknights.mantle.client.book.structure.level.TemplateLevel;
import slimeknights.mantle.client.screen.book.BookScreen;

import java.util.List;
import java.util.stream.IntStream;

public class StructureElement extends SizedBookElement {
  public boolean canTick = false;

  public float scale = 50f;
  public float transX = 0;
  public float transY = 0;
  public Quaternionf additionalTransform = new Quaternionf();
  public final StructureInfo renderInfo;
  public final TemplateLevel structureWorld;

  public long lastStep = -1;

  public StructureElement(int x, int y, int width, int height, StructureTemplate template, List<StructureTemplate.StructureBlockInfo> structure) {
    super(x, y, width, height);

    int[] size = {template.getSize().getX(), template.getSize().getY(), template.getSize().getZ()};
    this.scale = 100f / (float) IntStream.of(size).max().orElse(1);

    float sx = (float) width / (float) BookScreen.PAGE_WIDTH;
    float sy = (float) height / (float) BookScreen.PAGE_HEIGHT;
    this.scale *= Math.min(sx, sy);

    this.renderInfo = new StructureInfo(structure);
    this.structureWorld = new TemplateLevel(structure, renderInfo);
    this.transX = x + width / 2F;
    this.transY = y + height / 2F;
  }

  @Override
  public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    long currentTime = System.currentTimeMillis();
    if (this.lastStep < 0) {
      this.lastStep = currentTime;
    } else if (this.canTick && currentTime - this.lastStep > 200) {
      this.renderInfo.step();
      this.lastStep = currentTime;
    }
    if (!this.canTick) {
      this.renderInfo.reset();
    }

    StructurePreviewRenderer.render(graphics, this.structureWorld, this.renderInfo, this.x, this.y, this.width, this.height);
  }

  @Override
  public void mouseDragged(double clickX, double clickY, double mouseX, double mouseY, double lastX, double lastY, int button) {
    // The NeoForge 26 GUI extractor is 2D-only here; keep drag input consumed for compatibility with old books.
  }
}