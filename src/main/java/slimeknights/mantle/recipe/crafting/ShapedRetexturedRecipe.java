package slimeknights.mantle.recipe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.recipe.MantleRecipes;
import java.lang.reflect.Field;
import slimeknights.mantle.util.RetexturedHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Recipe which sets the texture for a {@link slimeknights.mantle.block.RetexturedBlock} based on an ingredient input. */
// TODO NeoForge 26.1: restore legacy texture-key JSON shorthand if still needed; current port serializes explicit texture ingredient.
@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  public static final MapCodec<ShapedRetexturedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
    CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
    ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
    ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
    Ingredient.CODEC.fieldOf("texture").forGetter(recipe -> recipe.texture),
    Codec.BOOL.optionalFieldOf("match_all", false).forGetter(recipe -> recipe.matchAll)
  ).apply(instance, ShapedRetexturedRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRetexturedRecipe> STREAM_CODEC = StreamCodec.composite(
    Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
    CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
    ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
    ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
    Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.texture,
    ByteBufCodecs.BOOL, recipe -> recipe.matchAll,
    ShapedRetexturedRecipe::new
  );

  /** Ingredient used to determine the texture on the output */
  @Getter
  private final Ingredient texture;
  private final boolean matchAll;
  private final ItemStackTemplate result;

  /** Creates a new recipe using the passed parameters */
  protected ShapedRetexturedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result, Ingredient texture, boolean matchAll) {
    super(commonInfo, bookInfo, pattern, result);
    this.texture = texture;
    this.matchAll = matchAll;
    this.result = result;
  }

  /**
   * Creates a new recipe using an existing shaped recipe
   * @param orig       Shaped recipe to copy
   * @param texture    Ingredient to use for the texture
   * @param matchAll   If true, all inputs must match for the recipe to match
   */
  protected ShapedRetexturedRecipe(ShapedRecipe orig, Ingredient texture, boolean matchAll) {
    this(new Recipe.CommonInfo(orig.showNotification()), new CraftingRecipe.CraftingBookInfo(orig.category(), orig.group()), orig.pattern, getResultTemplate(orig), texture, matchAll);
  }

  private static ItemStackTemplate getResultTemplate(ShapedRecipe recipe) {
    try {
      Field field = ShapedRecipe.class.getDeclaredField("result");
      field.setAccessible(true);
      return (ItemStackTemplate) field.get(recipe);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to copy shaped recipe result template", e);
    }
  }

  /** Placeholder ingredient used by the builder for legacy texture-key mode. */
  public static Ingredient emptyIngredient() {
    return Ingredient.of(Items.AIR);
  }

  /**
   * Gets the output using the given texture
   * @param texture  Texture to use
   * @return  Output with texture. Will be blank if the input is not a block
   */
  public ItemStack getResultItem(Item texture) {
    return RetexturedHelper.setTexture(result.create(), Block.byItem(texture));
  }

  @Override
  public boolean matches(CraftingInput input, Level level) {
    return super.matches(input, level);
  }

  @Override
  public ItemStack assemble(CraftingInput input) {
    ItemStack result = super.assemble(input);
    Block currentTexture = null;
    for (int i = 0; i < input.size(); i++) {
      ItemStack stack = input.getItem(i);
      if (!stack.isEmpty() && texture.test(stack)) {
        // fetch texture from the block if it has one
        Block block = RetexturedHelper.getTexture(stack);
        // assuming it does not, use the block itself as the texture (provided it is not the result that is)
        if (block == Blocks.AIR && stack.getItem() != result.getItem()) {
          block = Block.byItem(stack.getItem());
        }
        // if no texture, skip
        if (block == Blocks.AIR) {
          continue;
        }

        // if we have not found a texture yet, store the found block
        if (currentTexture == null) {
          currentTexture = block;
          // match all means we must check the rest. If not match all, we can be done
          if (!matchAll) {
            break;
          }

          // if we found a texture before, must match or we do no texture
        } else if (currentTexture != block) {
          currentTexture = null;
          break;
        }
      }
    }

    // set the texture if found. No texture will use the fallback
    if (currentTexture != null) {
      return RetexturedHelper.setTexture(result, currentTexture);
    }
    return result;
  }

  @Override
  protected PlacementInfo createPlacementInfo() {
    return PlacementInfo.createFromOptionals(this.pattern.ingredients());
  }

  @Override
  public List<RecipeDisplay> display() {
    List<SlotDisplay> ingredients = this.pattern.ingredients().stream().map(Ingredient::optionalIngredientToDisplay).toList();
    List<RecipeDisplay> displays = new ArrayList<>();
    for (var holder : this.texture.items().toList()) {
      Block block = Block.byItem(holder.value());
      if (block != Blocks.AIR) {
        displays.add(display(RetexturedHelper.setTexture(this.result.create(), block), ingredients));
      }
    }
    if (displays.isEmpty()) {
      displays.add(display(this.result.create(), ingredients));
    }
    return displays;
  }

  private RecipeDisplay display(ItemStack result, List<SlotDisplay> ingredients) {
    return new ShapedCraftingRecipeDisplay(
      this.pattern.width(),
      this.pattern.height(),
      ingredients,
      new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(result)),
      new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
    );
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer) MantleRecipes.CRAFTING_SHAPED_RETEXTURED.get();
  }
}
