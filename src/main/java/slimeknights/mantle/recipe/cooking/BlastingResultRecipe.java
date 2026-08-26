package slimeknights.mantle.recipe.cooking;

import lombok.Getter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

@Getter
public class BlastingResultRecipe extends AbstractResultCookingRecipe {
  public static LoadableField<Integer, AbstractCookingRecipe> COOKING_TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooking_time", 100, true, AbstractCookingRecipe::cookingTime);
  public static final RecordLoadable<BlastingResultRecipe> LOADABLE = RecordLoadable.create(
    ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP, CookingResultRecipe.CATEGORY_FIELD,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", AbstractCookingRecipe::input),
    RESULT_FIELD, EXPERIENCE_FIELD, COOKING_TIME_FIELD,
    BlastingResultRecipe::new);

  public BlastingResultRecipe(Identifier id, String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookingTime) {
    super(group, category, ingredient, result, experience, cookingTime);
  }

  @Override
  public RecipeSerializer<? extends AbstractCookingRecipe> getSerializer() {
    return MantleRecipes.BLASTING.get();
  }

  @Override
  public RecipeType<? extends AbstractCookingRecipe> getType() {
    return RecipeType.BLASTING;
  }

  @Override
  protected Item furnaceIcon() {
    return Items.BLAST_FURNACE;
  }

  @Override
  public RecipeBookCategory recipeBookCategory() {
    return RecipeBookCategories.BLAST_FURNACE_MISC;
  }
}
