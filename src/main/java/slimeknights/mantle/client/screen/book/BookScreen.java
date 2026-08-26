package slimeknights.mantle.client.screen.book;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.util.html.HtmlElement;
import slimeknights.mantle.util.html.HtmlSerializable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/** Book screen ported to Minecraft 26.1's GuiGraphicsExtractor render path. */
public class BookScreen extends Screen {
  public static boolean debug = false;
  public static final int TEX_SIZE = 512;
  public static final int PAGE_MARGIN = 8;
  public static final int PAGE_PADDING_TOP = 4;
  public static final int PAGE_PADDING_BOT = 4;
  public static final int PAGE_PADDING_LEFT = 8;
  public static final int PAGE_PADDING_RIGHT = 0;
  public static final float PAGE_SCALE = 1f;
  public static final int PAGE_WIDTH_UNSCALED = 206;
  public static final int PAGE_HEIGHT_UNSCALED = 200;
  public static final int PAGE_WIDTH = (int)((PAGE_WIDTH_UNSCALED - (PAGE_PADDING_LEFT + PAGE_PADDING_RIGHT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);
  public static final int PAGE_HEIGHT = (int)((PAGE_HEIGHT_UNSCALED - (PAGE_PADDING_TOP + PAGE_PADDING_BOT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);

  public boolean drawArrows = true;
  public boolean mouseInput = true;
  public boolean enableAnimations = true;
  public boolean drawText = true;

  @Nullable
  private ArrowButton previousArrow, nextArrow, backArrow, indexArrow;

  public final BookData book;
  @Nullable
  private final Consumer<String> pageUpdater;
  @Nullable
  private final Consumer<?> bookPickup;
  public final AdvancementCache advancementCache = new AdvancementCache();
  private final ArrayList<BookElement> leftElements = new ArrayList<>();
  private final ArrayList<BookElement> rightElements = new ArrayList<>();
  private int page = -1;
  private int oldPage = -2;
  @Nullable
  private double[] lastClick;
  @Nullable
  private double[] lastDrag;

  private static final ILayerRenderFunction[] LAYERS = {BookElement::draw, BookElement::drawOverlay};

  public BookScreen(Component title, BookData book, String page, @Nullable Consumer<String> pageUpdater, @Nullable Consumer<?> bookPickup) {
    super(title);
    this.book = book;
    this.pageUpdater = pageUpdater;
    this.bookPickup = bookPickup;
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player != null) {
      minecraft.player.connection.getAdvancements().setListener(this.advancementCache);
    }
    this.openPage(book.findPageNumber(page, this.advancementCache));
  }

  public static Font getAltFont() {
    return Minecraft.getInstance().font;
  }

  public static Font getUniformFont() {
    return Minecraft.getInstance().font;
  }

  public Font getFontRenderer() {
    return this.book.fontRenderer == null ? Minecraft.getInstance().font : this.book.fontRenderer;
  }

  private static int opaque(int color) {
    return color | 0xFF000000;
  }

  private static void drawString(GuiGraphicsExtractor graphics, String text, float x, float y, float scale) {
    Matrix3x2fStack pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(x, y);
    pose.scale(scale, scale);
    graphics.textRenderer().accept(0, 0, Component.literal(text));
    pose.popMatrix();
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    Font fontRenderer = getFontRenderer();

    if (this.page == -1) {
      renderCover(graphics, fontRenderer);
    } else {
      boolean renderLeft = shouldRenderPage(false);
      boolean renderRight = shouldRenderPage(true);
      renderUnderLayer(graphics);

      if (renderLeft) {
        renderPageBackground(graphics, false);
      }
      if (renderRight) {
        renderPageBackground(graphics, true);
      }

      if (this.book.appearance.drawPageNumbers) {
        if (renderLeft) {
          String pNum = Integer.toString(this.page * 2);
          Matrix3x2fStack pose = graphics.pose();
          pose.pushMatrix();
          drawerTransform(pose, false);
          graphics.textRenderer().accept((PAGE_WIDTH - fontRenderer.width(pNum)) / 2, PAGE_HEIGHT - 10, Component.literal(pNum));
          pose.popMatrix();
        }
        if (renderRight) {
          String pNum = Integer.toString(this.page * 2 + 1);
          Matrix3x2fStack pose = graphics.pose();
          pose.pushMatrix();
          drawerTransform(pose, true);
          graphics.textRenderer().accept((PAGE_WIDTH - fontRenderer.width(pNum)) / 2, PAGE_HEIGHT - 10, Component.literal(pNum));
          pose.popMatrix();
        }
      }

      int leftMX = this.getMouseX(false);
      int rightMX = this.getMouseX(true);
      int mY = this.getMouseY();
      for (ILayerRenderFunction layer : LAYERS) {
        if (renderLeft) {
          Matrix3x2fStack pose = graphics.pose();
          pose.pushMatrix();
          drawerTransform(pose, false);
          pose.scale(PAGE_SCALE, PAGE_SCALE);
          renderPageLayer(graphics, leftMX, mY, partialTick, leftElements, layer);
          pose.popMatrix();
        }
        if (renderRight) {
          Matrix3x2fStack pose = graphics.pose();
          pose.pushMatrix();
          drawerTransform(pose, true);
          pose.scale(PAGE_SCALE, PAGE_SCALE);
          renderPageLayer(graphics, rightMX, mY, partialTick, rightElements, layer);
          pose.popMatrix();
        }
      }
    }

    if (debug) {
      graphics.fill(0, 0, fontRenderer.width("DEBUG") + 4, fontRenderer.lineHeight + 4, 0x55000000);
      graphics.textRenderer().accept(2, 2, Component.literal("DEBUG"));
    }

    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
  }

  private boolean shouldRenderPage(boolean rightSide) {
    if (!rightSide) {
      return this.page != 0;
    }
    int fullPageCount = this.book.getFullPageCount(this.advancementCache);
    return this.page < fullPageCount - 1 || this.book.getPageCount(this.advancementCache) % 2 != 0;
  }

  private void renderCover(GuiGraphicsExtractor graphics, Font fontRenderer) {
    Identifier cover = book.appearance.getCoverTexture();
    int centerX = this.width / 2 - PAGE_WIDTH_UNSCALED / 2;
    int centerY = this.height / 2 - PAGE_HEIGHT_UNSCALED / 2;
    graphics.blit(RenderPipelines.GUI_TEXTURED, cover, centerX, centerY, 0, 0, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE, opaque(this.book.appearance.coverColor));

    if (!this.book.appearance.title.isEmpty()) {
      graphics.blit(RenderPipelines.GUI_TEXTURED, cover, centerX, centerY, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE, opaque(this.book.appearance.coverColor));
      int width = this.font.width(this.book.appearance.title);
      float scale = Math.max(0.01f, Math.min((float)PAGE_WIDTH / Math.max(1, width), 2.5f));
      drawString(graphics, this.book.appearance.title, (this.width / 2F) / scale + 3 - width / 2F, (this.height / 2F - fontRenderer.lineHeight / 2F) / scale - 4, scale);
    }

    if (!this.book.appearance.subtitle.isEmpty()) {
      int width = this.font.width(this.book.appearance.subtitle);
      float scale = Math.max(0.01f, Math.min((float)PAGE_WIDTH / Math.max(1, width), 1.5f));
      drawString(graphics, this.book.appearance.subtitle, (this.width / 2F) / scale + 7 - width / 2F, (this.height / 2F + 100 - fontRenderer.lineHeight * 2) / scale, scale);
    }
  }

  private void renderUnderLayer(GuiGraphicsExtractor graphics) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, this.book.appearance.getBookTexture(), this.width / 2 - PAGE_WIDTH_UNSCALED, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED * 2, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE, opaque(this.book.appearance.coverColor));
  }

  private void renderPageBackground(GuiGraphicsExtractor graphics, boolean rightSide) {
    int tint = opaque(this.book.appearance.getPageTint());
    if (!rightSide) {
      graphics.blit(RenderPipelines.GUI_TEXTURED, book.appearance.getBookTexture(), this.width / 2 - PAGE_WIDTH_UNSCALED, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE, tint);
    } else {
      graphics.blit(RenderPipelines.GUI_TEXTURED, book.appearance.getBookTexture(), this.width / 2, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE, tint);
    }
  }

  private void renderPageLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, List<BookElement> elements, ILayerRenderFunction layerFunc) {
    Font font = getFontRenderer();
    for (BookElement element : elements) {
      if (!drawText && element.isText()) {
        continue;
      }
      layerFunc.draw(element, graphics, mouseX, mouseY, partialTicks, font);
    }
  }

  @Override
  protected void init() {
    super.init();
    clearWidgets();

    this.previousArrow = this.addRenderableWidget(new ArrowButton(book, 50, -50, ArrowButton.ArrowType.PREV, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, button -> previousPage()));
    this.nextArrow = this.addRenderableWidget(new ArrowButton(book, -50, -50, ArrowButton.ArrowType.NEXT, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, button -> nextPage()));
    this.backArrow = this.addRenderableWidget(new ArrowButton(book, this.width / 2 - ArrowButton.WIDTH / 2, this.height / 2 + ArrowButton.HEIGHT / 2 + PAGE_HEIGHT / 2, ArrowButton.ArrowType.LEFT, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, button -> {
      if (this.oldPage >= -1) {
        this.page = this.oldPage;
      }
      this.oldPage = -2;
      this.buildPages();
    }));
    this.indexArrow = this.addRenderableWidget(new ArrowButton(book, this.width / 2 - PAGE_WIDTH_UNSCALED - ArrowButton.WIDTH / 2, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, ArrowButton.ArrowType.BACK_UP, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, button -> {
      this.openPage(this.book.findPageNumber("index.page1", this.advancementCache));
      this.oldPage = -2;
      this.buildPages();
    }));

    if (this.bookPickup != null) {
      int margin = this.height / 2 + PAGE_HEIGHT_UNSCALED / 2 + 10 + 20 >= this.height ? 0 : 10;
      this.addRenderableWidget(Button.builder(Component.translatable("lectern.take_book"), button -> {
        this.onClose();
        this.bookPickup.accept(null);
      }).pos(this.width / 2 - 196 / 2, this.height / 2 + PAGE_HEIGHT_UNSCALED / 2 + margin).size(196, 20).build());
    }

    this.buildPages();
  }

  @Override
  public void tick() {
    super.tick();
    if (this.previousArrow == null || this.nextArrow == null || this.backArrow == null || this.indexArrow == null) {
      return;
    }
    this.previousArrow.visible = this.page != -1 && drawArrows;
    this.nextArrow.visible = this.page + 1 < this.book.getFullPageCount(this.advancementCache) && drawArrows;
    this.backArrow.visible = this.oldPage >= -1 && drawArrows;
    if (this.page == -1) {
      this.nextArrow.setX(this.width / 2 + 80);
      this.indexArrow.visible = false;
    } else {
      this.previousArrow.setX(this.width / 2 - 184);
      this.nextArrow.setX(this.width / 2 + 165);
      SectionData index = this.book.findSection("index", this.advancementCache);
      this.indexArrow.visible = index != null && (this.page - 1) * 2 + 2 > index.getPageCount() && drawArrows;
    }
    this.previousArrow.setY(this.height / 2 + 75);
    this.nextArrow.setY(this.height / 2 + 75);
  }

  public boolean previousPage() {
    this.page--;
    if (this.page < -1) {
      this.page = -1;
      return false;
    }
    this.oldPage = -2;
    this.buildPages();
    return true;
  }

  public boolean nextPage() {
    this.page++;
    int fullPageCount = this.book.getFullPageCount(this.advancementCache);
    if (this.page >= fullPageCount) {
      this.page = fullPageCount - 1;
      return false;
    }
    this.oldPage = -2;
    this.buildPages();
    return true;
  }

  @Override
  public boolean keyPressed(KeyEvent event) {
    switch (event.key()) {
      case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> {
        return previousPage();
      }
      case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> {
        return nextPage();
      }
      case GLFW.GLFW_KEY_F3 -> {
        debug = !debug;
        return true;
      }
      default -> {
        return super.keyPressed(event);
      }
    }
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (scrollY < 0.0D) {
      return nextPage();
    } else if (scrollY > 0.0D) {
      return previousPage();
    }
    return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    boolean right = false;
    double mouseX = this.getMouseX(false);
    double mouseY = this.getMouseY();
    if (mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING_LEFT) / PAGE_SCALE) {
      mouseX = this.getMouseX(true);
      right = true;
    }
    lastClick = new double[]{mouseX, mouseY};
    int oldPage = this.page;
    List<BookElement> elementList = List.copyOf(right ? this.rightElements : this.leftElements);
    for (BookElement element : elementList) {
      element.mouseClicked(mouseX, mouseY, event.button());
      if (this.page != oldPage) {
        return true;
      }
    }
    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    boolean right = false;
    double mouseX = this.getMouseX(false);
    double mouseY = this.getMouseY();
    if (mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING_LEFT) / PAGE_SCALE) {
      mouseX = this.getMouseX(true);
      right = true;
    }
    List<BookElement> elements = right ? this.rightElements : this.leftElements;
    for (BookElement element : List.copyOf(elements)) {
      element.mouseReleased(mouseX, mouseY, event.button());
    }
    lastClick = null;
    lastDrag = null;
    return super.mouseReleased(event);
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
    boolean right = false;
    double mouseX = this.getMouseX(false);
    double mouseY = this.getMouseY();
    if (mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING_LEFT) / PAGE_SCALE) {
      mouseX = this.getMouseX(true);
      right = true;
    }
    if (lastClick != null) {
      if (lastDrag == null) {
        lastDrag = new double[]{mouseX, mouseY};
      }
      List<BookElement> elements = right ? this.rightElements : this.leftElements;
      for (BookElement element : List.copyOf(elements)) {
        element.mouseDragged(lastClick[0], lastClick[1], mouseX, mouseY, lastDrag[0], lastDrag[1], event.button());
      }
      lastDrag = new double[]{mouseX, mouseY};
    }
    return true;
  }

  @Override
  public void removed() {
    if (Minecraft.getInstance().player == null) {
      return;
    }
    if (pageUpdater != null) {
      String pageStr = "";
      if (this.page >= 0) {
        PageData page = this.page == 0 ? this.book.findPage(0, this.advancementCache) : getLeftPage();
        if (page == null) {
          page = getRightPage();
        }
        if (page != null && page.parent != null) {
          pageStr = page.parent.name + "." + page.name;
        }
      }
      pageUpdater.accept(pageStr);
    }
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  public void drawerTransform(Matrix3x2fStack pose, boolean rightSide) {
    if (rightSide) {
      pose.translate(this.width / 2f + PAGE_PADDING_RIGHT + PAGE_MARGIN, this.height / 2f - PAGE_HEIGHT_UNSCALED / 2f + PAGE_PADDING_TOP + PAGE_MARGIN);
    } else {
      pose.translate(this.width / 2f - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN, this.height / 2f - PAGE_HEIGHT_UNSCALED / 2f + PAGE_PADDING_TOP + PAGE_MARGIN);
    }
  }

  protected float leftOffset(boolean rightSide) {
    return rightSide ? this.width / 2f + PAGE_PADDING_RIGHT + PAGE_MARGIN : this.width / 2f - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN;
  }

  protected float topOffset() {
    return this.height / 2f - PAGE_HEIGHT_UNSCALED / 2f + PAGE_PADDING_TOP + PAGE_MARGIN;
  }

  public int getMouseX(boolean rightSide) {
    if (!mouseInput) {
      return -1;
    }
    Minecraft minecraft = Minecraft.getInstance();
    return (int)((minecraft.mouseHandler.xpos() * this.width / minecraft.getWindow().getScreenWidth() - this.leftOffset(rightSide)) / PAGE_SCALE);
  }

  public int getMouseY() {
    if (!mouseInput) {
      return -1;
    }
    Minecraft minecraft = Minecraft.getInstance();
    return (int)((minecraft.mouseHandler.ypos() * this.height / minecraft.getWindow().getScreenHeight() - 1 - this.topOffset()) / PAGE_SCALE);
  }

  public int openPage(int page) {
    return this.openPage(page, false);
  }

  public int openPage(int page, boolean returner) {
    if (page < 0) {
      this.openCover();
      return -1;
    }
    int bookPage;
    if (page == 1) {
      bookPage = 0;
    } else if (page % 2 == 0) {
      bookPage = (page - 1) / 2 + 1;
    } else {
      bookPage = (page - 2) / 2 + 1;
    }
    if (bookPage >= -1 && bookPage < this.book.getFullPageCount(this.advancementCache)) {
      if (returner) {
        this.oldPage = this.page;
      }
      this._setPage(bookPage);
    }
    return page % 2 == 0 ? 0 : 1;
  }

  public void _setPage(int page) {
    this.page = page;
    this.buildPages();
  }

  public int getPage(int side) {
    if (this.page == 0 && side == 0) {
      return -1;
    } else if (this.page == 0 && side == 1) {
      return 0;
    } else if (side == 0) {
      return (this.page - 1) * 2 + 1;
    } else if (side == 1) {
      return (this.page - 2) * 2 + 2;
    }
    return -1;
  }

  public int getPage_() {
    return this.page;
  }

  public List<BookElement> getElements(int side) {
    return side == 0 ? this.leftElements : side == 1 ? this.rightElements : Collections.emptyList();
  }

  public void openCover() {
    this._setPage(-1);
    this.leftElements.clear();
    this.rightElements.clear();
    this.buildPages();
  }

  public void buildPages() {
    this.leftElements.clear();
    this.rightElements.clear();
    if (this.page == -1) {
      return;
    }
    if (this.page == 0) {
      PageData page = this.book.findPage(0, this.advancementCache);
      if (page != null) {
        page.content.build(this.book, this.rightElements, false);
      }
    } else {
      int leftPageIndex = (this.page - 1) * 2 + 1;
      int rightPageIndex = (this.page - 1) * 2 + 2;
      PageData leftPage = getLeftPage();
      PageData rightPage = getRightPage();
      if (leftPage != null) {
        leftPage.content.build(this.book, this.leftElements, false);
      }
      if (rightPage != null) {
        rightPage.content.build(this.book, this.rightElements, true);
      }
    }
    for (BookElement element : this.leftElements) {
      element.setParent(this);
    }
    for (BookElement element : this.rightElements) {
      element.setParent(this);
    }
  }

  @Nullable
  public PageData getLeftPage() {
    return this.book.findPage((this.page - 1) * 2 + 1, this.advancementCache);
  }

  @Nullable
  public PageData getRightPage() {
    return this.book.findPage((this.page - 1) * 2 + 2, this.advancementCache);
  }

  public String coverToHtml(String bookName, String title, String version, String mod) {
    return "---\n" +
      "layout: book-cover\n" +
      "title: " + title + " (" + version + ")\n" +
      "breadcrumb: " + title + "\n" +
      "description: Interactive " + title + " from " + mod + " in Minecraft " + version + ".\n" +
      "book: " + bookName + "\n" +
      "---\n\n";
  }

  public String pageToHtml(String bookName, String title, String version, String mod) {
    PageData leftData = getLeftPage();
    PageData rightData = getRightPage();
    StringBuilder builder = new StringBuilder();
    if (leftData != null || rightData != null) {
      builder.append("---\n")
        .append("layout: book-page\n")
        .append("title: ").append(title).append(" (").append(version).append(") - page ").append(this.page).append('\n')
        .append("breadcrumb: ").append(this.page).append('\n')
        .append("description: Interactive ").append(title).append(" from ").append(mod).append(" in Minecraft ").append(version).append(".\n")
        .append("book: ").append(bookName).append('\n')
        .append("page_num: ").append(this.page).append('\n')
        .append("---\n\n");
    }
    if (leftData != null) {
      HtmlSerializable left = leftData.content.toHTML(book);
      if (left != null) {
        HtmlElement.div().classes("left").add(left).toHtml(builder, "");
      }
      builder.append('\n');
    }
    if (rightData != null) {
      HtmlSerializable right = rightData.content.toHTML(book);
      if (right != null) {
        HtmlElement.div().classes("right").add(right).toHtml(builder, "");
      }
      builder.append('\n');
    }
    return builder.toString();
  }

  public String toHTML(String title, String mod, String version, String bookName) {
    return this.page == -1 ? coverToHtml(bookName, title, version, mod) : pageToHtml(bookName, title, version, mod);
  }

  public static class AdvancementCache implements ClientAdvancements.Listener {
    private final HashMap<AdvancementNode, AdvancementProgress> progress = new HashMap<>();
    private final HashMap<Identifier, AdvancementNode> nameCache = new HashMap<>();

    @Nullable
    public AdvancementProgress getProgress(String id) {
      return this.getProgress(this.getAdvancement(id));
    }

    @Nullable
    public AdvancementProgress getProgress(@Nullable AdvancementNode advancement) {
      return advancement == null ? null : this.progress.get(advancement);
    }

    @Nullable
    public AdvancementNode getAdvancement(String id) {
      return this.nameCache.get(Identifier.parse(id));
    }

    @Override
    public void onUpdateAdvancementProgress(AdvancementNode advancement, AdvancementProgress advancementProgress) {
      this.progress.put(advancement, advancementProgress);
      this.nameCache.put(advancement.holder().id(), advancement);
    }

    @Override
    public void onSelectedTabChanged(@Nullable AdvancementHolder selectedTab) {}

    @Override
    public void onAddAdvancementRoot(AdvancementNode advancement) {
      this.nameCache.put(advancement.holder().id(), advancement);
    }

    @Override
    public void onRemoveAdvancementRoot(AdvancementNode advancement) {
      this.progress.remove(advancement);
      this.nameCache.remove(advancement.holder().id());
    }

    @Override
    public void onAddAdvancementTask(AdvancementNode advancement) {
      this.nameCache.put(advancement.holder().id(), advancement);
    }

    @Override
    public void onRemoveAdvancementTask(AdvancementNode advancement) {
      this.progress.remove(advancement);
      this.nameCache.remove(advancement.holder().id());
    }

    @Override
    public void onAdvancementsCleared() {
      this.progress.clear();
      this.nameCache.clear();
    }
  }
}
