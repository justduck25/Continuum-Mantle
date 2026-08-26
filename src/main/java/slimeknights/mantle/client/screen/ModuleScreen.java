package slimeknights.mantle.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

// a sub-gui. Mostly the same as a separate ContainerScreen, but doesn't do the calls that affect the game as if this were the only gui
public abstract class ModuleScreen<P extends MultiModuleScreen<?>, C extends AbstractContainerMenu> extends AbstractContainerScreen<C> {

  protected final P parent;

  // left or right of the parent
  protected final boolean right;
  // top or bottom of the parent
  protected final boolean bottom;

  public int yOffset = 0;
  public int xOffset = 0;
  protected int realWidth;
  protected int realHeight;

  public ModuleScreen(P parent, C container, Inventory playerInventory, Component title, boolean right, boolean bottom) {
    super(container, playerInventory, title);

    this.parent = parent;
    this.right = right;
    this.bottom = bottom;
  }

  protected int effectiveWidth() {
    return realWidth > 0 ? realWidth : this.imageWidth;
  }
  protected int effectiveHeight() {
    return realHeight > 0 ? realHeight : this.imageHeight;
  }

  public int guiLeft() {
    return this.leftPos;
  }

  public int guiTop() {
    return this.topPos;
  }

  public int guiRight() {
    return this.leftPos + effectiveWidth();
  }

  public int guiBottom() {
    return this.topPos + effectiveHeight();
  }

  public Rect2i getArea() {
    int w = effectiveWidth();
    int h = effectiveHeight();
    return new Rect2i(this.leftPos, this.topPos, w, h);
  }

  @Override
  public void init() {
    int w = effectiveWidth();
    int h = effectiveHeight();
    this.leftPos = (this.width - w) / 2;
    this.topPos = (this.height - h) / 2;
  }

  public void updatePosition(int parentX, int parentY, int parentSizeX, int parentSizeY) {
    int w = effectiveWidth();
    int h = effectiveHeight();
    if (this.right) {
      this.leftPos = parentX + parentSizeX;
    } else {
      this.leftPos = parentX - w;
    }

    if (this.bottom) {
      this.topPos = parentY + parentSizeY - h;
    } else {
      this.topPos = parentY;
    }

    this.leftPos += this.xOffset;
    this.topPos += this.yOffset;
  }

  public boolean shouldDrawSlot(Slot slot) {
    return true;
  }

  public boolean isMouseInModule(int mouseX, int mouseY) {
    return mouseX >= this.leftPos && mouseX < this.guiRight() && mouseY >= this.topPos && mouseY < this.guiBottom();
  }

  public boolean isMouseOverFullSlot(double mouseX, double mouseY) {
    for (Slot slot : this.menu.slots) {
      if (this.parent.isSlotHovering(slot, mouseX, mouseY) && slot.hasItem()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Callback to draw background elements.
   */
  public void handleDrawGuiContainerBackgroundLayer(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
    this.extractBackground(graphics, mouseX, mouseY, partialTicks);
  }

  /**
   * Callback to draw foreground elements.
   */
  public void handleDrawGuiContainerForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    this.extractLabels(graphics, mouseX, mouseY);
  }

  /**
   * Callback to draw hovering tooltips.
   */
  public void handleRenderHoveredTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    this.extractTooltip(graphics, mouseX, mouseY);
  }

  /**
   * Custom mouse click handling.
   *
   * @return True to prevent the main container handling the mouseclick
   */
  public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
    return false;
  }

  /**
   * Custom mouse drag handling.
   *
   * @return True to prevent the main container handling the mouse drag
   */
  public boolean handleMouseClickMove(double mouseX, double mouseY, int clickedMouseButton, double timeSinceLastClick) {
    return false;
  }

  /**
   * Custom mouse click handling.
   *
   * @return True to prevent the main container handling the mouseclick
   */
  public boolean handleMouseReleased(double mouseX, double mouseY, int state) {
    return false;
  }

  /**
   * Custom mouse scrolled handling.
   *
   * @return True to prevent the main container handling the mouseclick
   */
  public boolean handleMouseScrolled(double mouseX, double mouseY, double delta) {
    return false;
  }
}