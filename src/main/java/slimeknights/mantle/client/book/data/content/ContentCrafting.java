package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import javax.annotation.Nullable;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.BookLoadException;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.IngredientData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ImageElement;
import slimeknights.mantle.client.screen.book.element.ItemElement;
import slimeknights.mantle.client.screen.book.element.TextElement;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;
import slimeknights.mantle.util.html.HtmlElement;
import slimeknights.mantle.util.html.HtmlGroup;
import slimeknights.mantle.util.html.HtmlSerializable;
import static slimeknights.mantle.client.screen.book.Textures.TEX_CRAFTING;

public class ContentCrafting extends PageContent {
  public static final Identifier ID = Mantle.getResource("crafting");

  public static final int TEX_SIZE = 256;
  public static final ImageData IMG_CRAFTING_LARGE = new ImageData(TEX_CRAFTING, 0, 0, 183, 114, TEX_SIZE, TEX_SIZE);
  public static final ImageData IMG_CRAFTING_SMALL = new ImageData(TEX_CRAFTING, 0, 114, 155, 78, TEX_SIZE, TEX_SIZE);

  public static final int X_RESULT_SMALL = 118;
  public static final int Y_RESULT_SMALL = 23;
  public static final int X_RESULT_LARGE = 146;
  public static final int Y_RESULT_LARGE = 41;

  public static final float ITEM_SCALE = 2.0F;
  public static final int SLOT_MARGIN = 5;
  public static final int SLOT_PADDING = 4;

  @Getter
  public String title = "Crafting";
  public String grid_size = "auto";
  public IngredientData[][] grid;
  public IngredientData result;
  @Nullable
  public TextData[] description;
  public String recipe;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int x = 0;
    int y;
    int height = 100;
    int resultX = 100;
    int resultY = 50;

    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
      y = getTitleHeight();
    }

    // Fallback for if grid size is not specified in a manual recipe
    String size = this.grid_size.equalsIgnoreCase("auto") ? "large" : this.grid_size;

    if (size.equalsIgnoreCase("small")) {
      x = BookScreen.PAGE_WIDTH / 2 - IMG_CRAFTING_SMALL.width / 2;
      height = y + IMG_CRAFTING_SMALL.height;
      list.add(new ImageElement(x, y, IMG_CRAFTING_SMALL.width, IMG_CRAFTING_SMALL.height, IMG_CRAFTING_SMALL, book.appearance.slotColor));
      resultX = x + X_RESULT_SMALL;
      resultY = y + Y_RESULT_SMALL;
    } else if (size.equalsIgnoreCase("large")) {
      x = BookScreen.PAGE_WIDTH / 2 - IMG_CRAFTING_LARGE.width / 2;
      height = y + IMG_CRAFTING_LARGE.height;
      list.add(new ImageElement(x, y, IMG_CRAFTING_LARGE.width, IMG_CRAFTING_LARGE.height, IMG_CRAFTING_LARGE, book.appearance.slotColor));
      resultX = x + X_RESULT_LARGE;
      resultY = y + Y_RESULT_LARGE;
    }

    if (this.grid != null) {
      for (int i = 0; i < this.grid.length; i++) {
        for (int j = 0; j < this.grid[i].length; j++) {
          if (this.grid[i][j] == null || this.grid[i][j].getItems().isEmpty()) {
            continue;
          }
          list.add(new ItemElement(x + SLOT_MARGIN + (SLOT_PADDING + Math.round(ItemElement.ITEM_SIZE_HARDCODED * ITEM_SCALE)) * j, y + SLOT_MARGIN + (SLOT_PADDING + Math.round(ItemElement.ITEM_SIZE_HARDCODED * ITEM_SCALE)) * i, ITEM_SCALE, this.grid[i][j].getItems(), this.grid[i][j].action));
        }
      }
    }

    if (this.result != null) {
      list.add(new ItemElement(resultX, resultY, ITEM_SCALE, this.result.getItems(), this.result.action));
    }

    if (this.description != null && this.description.length > 0) {
      list.add(new TextElement(0, height + 5, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - height - 5, this.description));
    }
  }

  @Override
  public void load() {
    super.load();

    if (this.recipe != null && !this.recipe.isEmpty() && Identifier.tryParse(this.recipe) != null) {
      int w = 0, h = 0;

      Minecraft minecraft = Minecraft.getInstance();
      Level level = minecraft.level;
      assert level != null;
      RecipeHolder<?> holder = getRecipeHolder(minecraft, level, Identifier.parse(this.recipe));
      Recipe<?> recipe = holder != null ? holder.value() : null;
      if (recipe == null) {
        return;
      }
      if (recipe instanceof CraftingRecipe) {
        RecipeDisplay display = recipe.display().isEmpty() ? null : recipe.display().get(0);
        ContextMap displayContext = SlotDisplayContext.fromLevel(level);
        boolean canFit2x2 = canFit(recipe, display, 2, 2);
        if(grid_size.equalsIgnoreCase("auto")) {
          if(canFit2x2) {
            grid_size = "small";
          } else {
            grid_size = "large";
          }
        }

        switch (grid_size.toLowerCase()) {
          case "large" -> w = h = 3;
          case "small" -> w = h = 2;
        }

        boolean canFitWH = canFit(recipe, display, w, h);
        if (!canFitWH) {
          throw new BookLoadException("Recipe " + this.recipe + " cannot fit in a " + w + "x" + h + " crafting grid");
        }

        ItemStack resultStack = getRecipeResult((CraftingRecipe)recipe, display, displayContext);
        result = IngredientData.getItemStackData(resultStack);

        if (display instanceof ShapedCraftingRecipeDisplay shapedDisplay) {
          grid = new IngredientData[shapedDisplay.height()][shapedDisplay.width()];
          java.util.List<SlotDisplay> ingredients = shapedDisplay.ingredients();
          for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
              int idx = x + y * grid[y].length;
              grid[y][x] = getDisplayIngredient(idx < ingredients.size() ? ingredients.get(idx) : null, displayContext);
            }
          }
          return;
        }

        if (display instanceof ShapelessCraftingRecipeDisplay shapelessDisplay) {
          grid = new IngredientData[h][w];
          java.util.List<SlotDisplay> ingredients = shapelessDisplay.ingredients();
          for (int i = 0; i < ingredients.size(); i++) {
            grid[i / w][i % w] = getDisplayIngredient(ingredients.get(i), displayContext);
          }
          return;
        }

        java.util.List<Ingredient> ingredients = recipe.placementInfo().ingredients();

        if (recipe instanceof ShapedRecipe shaped) {
          grid = new IngredientData[shaped.getHeight()][shaped.getWidth()];

          for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
              int idx = x + y * grid[y].length;
              NonNullList<ItemStack> stackList = NonNullList.create();
              if (idx < ingredients.size()) {
                stackList.addAll(resolveIngredientStacks(ingredients.get(idx), displayContext));
              }
              grid[y][x] = stackList.isEmpty() ? null : IngredientData.getItemStackData(stackList);
            }
          }

          return;
        }

        grid = new IngredientData[h][w];
        for (int i = 0; i < ingredients.size(); i++) {
          NonNullList<ItemStack> stackList = NonNullList.create();
          stackList.addAll(resolveIngredientStacks(ingredients.get(i), displayContext));
          grid[i / h][i % w] = stackList.isEmpty() ? null : IngredientData.getItemStackData(stackList);
        }
      }
    }
  }

  /** Checks if the recipe display fits a target grid, falling back to placement info for older recipe implementations. */
  private static boolean canFit(Recipe<?> recipe, @Nullable RecipeDisplay display, int width, int height) {
    if (display instanceof ShapedCraftingRecipeDisplay shapedDisplay) {
      return shapedDisplay.width() <= width && shapedDisplay.height() <= height;
    }
    if (display instanceof ShapelessCraftingRecipeDisplay shapelessDisplay) {
      return shapelessDisplay.ingredients().size() <= width * height;
    }
    return (recipe instanceof ShapedRecipe shaped) ? (shaped.getWidth() <= width && shaped.getHeight() <= height) : (recipe.placementInfo().ingredients().size() <= width * height);
  }

  /** Resolves a recipe display slot into book data; empty recipe slots stay empty instead of showing the missing-item barrier. */
  @Nullable
  private static IngredientData getDisplayIngredient(@Nullable SlotDisplay display, ContextMap displayContext) {
    NonNullList<ItemStack> stacks = NonNullList.create();
    if (display != null) {
      try {
        stacks.addAll(display.resolveForStacks(displayContext));
      } catch (UnsupportedOperationException | IllegalStateException ignored) {
      }
    }
    if (stacks.isEmpty()) {
      return null;
    }
    return IngredientData.getItemStackData(stacks);
  }

  /** Gets a stable result stack for book display, falling back when recipe displays cannot resolve without context. */
  private static ItemStack getRecipeResult(CraftingRecipe recipe, @Nullable RecipeDisplay display, ContextMap displayContext) {
    ItemStack resultStack = ItemStack.EMPTY;
    if (display != null) {
      try {
        resultStack = display.result().resolveForFirstStack(displayContext);
      } catch (UnsupportedOperationException | IllegalStateException ignored) {
      }
    }
    if (resultStack.isEmpty()) {
      resultStack = recipe.assemble(CraftingInput.EMPTY);
    }
    return resultStack;
  }

  /** Resolves vanilla ingredients for old recipe paths without letting tag placeholders break the book. */
  private static NonNullList<ItemStack> resolveIngredientStacks(Ingredient ingredient, ContextMap displayContext) {
    NonNullList<ItemStack> stackList = NonNullList.create();
    try {
      stackList.addAll(ingredient.display().resolveForStacks(displayContext));
    } catch (UnsupportedOperationException | IllegalStateException ignored) {
    }
    if (stackList.isEmpty()) {
      stackList.addAll(SizedIngredient.of(ingredient).getMatchingStacks());
    }
    return stackList;
  }

  /** Gets a recipe by ID. Clients in 26.1 only expose limited recipe access, so singleplayer books need the integrated server. */
  @Nullable
  private static RecipeHolder<?> getRecipeHolder(Minecraft minecraft, Level level, Identifier recipe) {
    ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, recipe);
    if (level.recipeAccess() instanceof RecipeManager manager) {
      return manager.byKey(key).orElse(null);
    }
    return minecraft.getSingleplayerServer() == null ? null : minecraft.getSingleplayerServer().getRecipeManager().byKey(key).orElse(null);
  }

  @Override
  public HtmlSerializable toHTML(BookData book) {
    return HtmlGroup.indent().add(
      makeTitleHTML(),
      HtmlElement.div()
        .classes(grid_size.equalsIgnoreCase("small") ? "spacing" : "spacing-lg")
        .add(TextData.toHtml(description, book))
    );
  }
}
