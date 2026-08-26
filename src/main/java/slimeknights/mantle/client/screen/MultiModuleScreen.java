package slimeknights.mantle.client.screen;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.joml.Matrix3x2fStack;
import slimeknights.mantle.inventory.MultiModuleContainerMenu;
import slimeknights.mantle.inventory.WrapperSlot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiModuleScreen<CONTAINER extends MultiModuleContainerMenu<?>> extends AbstractContainerScreen<CONTAINER> {

  protected List<ModuleScreen<?,?>> modules = Lists.newArrayList();

  public int cornerX;
  public int cornerY;
  public int realWidth;
  public int realHeight;

  public MultiModuleScreen(CONTAINER container, Inventory playerInventory, Component title) {
    this(container, playerInventory, title, 176, 166);
  }

  public MultiModuleScreen(CONTAINER container, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
    super(container, playerInventory, title, imageWidth, imageHeight);

    this.realWidth = imageWidth;
    this.realHeight = imageHeight;
  }

  protected void addModule(ModuleScreen<?,?> module) {
    this.modules.add(module);
  }

  public List<Rect2i> getModuleAreas() {
    List<Rect2i> areas = new ArrayList<>(this.modules.size());
    for (ModuleScreen<?,?> module : this.modules) {
      areas.add(module.getArea());
    }
    return areas;
  }

  @Override
  protected void init() {
    this.realWidth = this.realWidth > 0 ? this.realWidth : this.getImageWidth();
    this.realHeight = this.realHeight > 0 ? this.realHeight : this.getImageHeight();

    super.init();

    this.leftPos = (this.width - this.realWidth) / 2;
    this.topPos = (this.height - this.realHeight) / 2;
    this.cornerX = this.leftPos;
    this.cornerY = this.topPos;

    for (ModuleScreen<?,?> module : this.modules) {
      module.resize(width, height);
      module.init();
      this.updateSubmodule(module);
    }
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);

    for (ModuleScreen<?,?> module : this.modules) {
      module.resize(width, height);
      this.updateSubmodule(module);
    }
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
    super.extractBackground(graphics, mouseX, mouseY, partialTicks);
    for (ModuleScreen<?,?> module : this.modules) {
      module.handleDrawGuiContainerBackgroundLayer(graphics, partialTicks, mouseX, mouseY);
    }
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    this.drawContainerName(graphics);
    this.drawPlayerInventoryName(graphics);

    Matrix3x2fStack poses = graphics.pose();
    for (ModuleScreen<?,?> module : this.modules) {
      poses.pushMatrix();
      poses.translate(module.guiLeft() - this.leftPos, module.guiTop() - this.topPos);
      module.handleDrawGuiContainerForegroundLayer(graphics, mouseX, mouseY);
      poses.popMatrix();
    }
  }

  @Override
  protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    super.extractTooltip(graphics, mouseX, mouseY);

    for (ModuleScreen<?,?> module : this.modules) {
      module.handleRenderHoveredTooltip(graphics, mouseX, mouseY);
    }
  }

  protected void drawBackground(GuiGraphicsExtractor graphics, Identifier background) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, background, this.cornerX, this.cornerY, 0, 0, this.realWidth, this.realHeight, 256, 256);
  }

  protected void drawContainerName(GuiGraphicsExtractor graphics) {
    graphics.text(this.font, this.getTitle(), 8, 6, 0x404040, false);
  }

  protected void drawPlayerInventoryName(GuiGraphicsExtractor graphics) {
    graphics.text(this.font, this.playerInventoryTitle, 8, this.realHeight - 96 + 2, 0x404040, false);
  }

  protected void updateSubmodule(ModuleScreen<?,?> module) {
    module.updatePosition(this.cornerX, this.cornerY, this.realWidth, this.realHeight);
  }

  @Override
  protected void extractSlot(GuiGraphicsExtractor graphics, Slot slotIn, int mouseX, int mouseY) {
    ModuleScreen<?,?> module = this.getModuleForSlot(slotIn.index);

    if (module != null) {
      Slot slot = slotIn;
      if (slotIn instanceof WrapperSlot wrapper) {
        slot = wrapper.parent;
      }

      if (!module.shouldDrawSlot(slot)) {
        return;
      }
    }

    super.extractSlot(graphics, slotIn, mouseX, mouseY);
  }

  public boolean isSlotHovering(Slot slotIn, double mouseX, double mouseY) {
    ModuleScreen<?,?> module = this.getModuleForSlot(slotIn.index);

    if (module != null) {
      Slot slot = slotIn;
      if (slotIn instanceof WrapperSlot wrapper) {
        slot = wrapper.parent;
      }

      if (!module.shouldDrawSlot(slot)) {
        return false;
      }
    }

    return this.isHovering(slotIn.x, slotIn.y, 16, 16, mouseX, mouseY);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    ModuleScreen<?,?> module = this.getModuleForPoint(event.x(), event.y());

    if (module != null && module.handleMouseClicked(event.x(), event.y(), event.button())) {
      return false;
    }

    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
    ModuleScreen<?,?> module = this.getModuleForPoint(event.x(), event.y());

    if (module != null && module.handleMouseClickMove(event.x(), event.y(), event.button(), dx)) {
      return false;
    }

    return super.mouseDragged(event, dx, dy);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    ModuleScreen<?,?> module = this.getModuleForPoint(mouseX, mouseY);

    if (module != null && module.handleMouseScrolled(mouseX, mouseY, scrollY)) {
      return false;
    }

    return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    ModuleScreen<?,?> module = this.getModuleForPoint(event.x(), event.y());

    if (module != null && module.handleMouseReleased(event.x(), event.y(), event.button())) {
      return false;
    }

    return super.mouseReleased(event);
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForPoint(double x, double y) {
    for (ModuleScreen<?,?> module : this.modules) {
      if (this.isPointInBounds(module.guiLeft(), module.guiTop(), module.guiRight(), module.guiBottom(), x, y)) {
        return module;
      }
    }

    return null;
  }

  protected boolean isPointInBounds(int left, int top, int right, int bottom, double x, double y) {
    return x >= left - 1 && x < right + 1 && y >= top - 1 && y < bottom + 1;
  }

  @Override
  protected boolean isHovering(int left, int top, int width, int height, double mouseX, double mouseY) {
    int x = this.leftPos;
    int y = this.topPos;
    mouseX -= x;
    mouseY -= y;
    return mouseX >= left - 1 && mouseX < left + width + 1 && mouseY >= top - 1 && mouseY < top + height + 1;
  }

  @Override
  public ScreenRectangle getRectangle() {
    return new ScreenRectangle(this.leftPos, this.topPos, this.realWidth, this.realHeight);
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForSlot(int slotNumber) {
    return this.getModuleForContainer(this.getMenu().getSlotContainer(slotNumber));
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForContainer(AbstractContainerMenu container) {
    for (ModuleScreen<?,?> module : this.modules) {
      if (module.getMenu() == container) {
        return module;
      }
    }

    return null;
  }

  @Override
  public CONTAINER getMenu() {
    return this.menu;
  }
}