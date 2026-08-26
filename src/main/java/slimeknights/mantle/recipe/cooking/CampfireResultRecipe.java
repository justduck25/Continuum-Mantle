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
public class CampfireResultRecipe extends AbstractResultCookingRecipe {
  public static LoadableField<Integer, AbstractCookingRecipe> COOKING_TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooking_time", 600, true, AbstractCookingRecipe::cookingTime);
  public static final RecordLoadable<CampfireResultRecipe> LOADABLE = RecordLoadable.create(
    ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP, CookingResultRecipe.CATEGORY_FIELD,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", AbstractCookingRecipe::input),
    RESULT_FIELD, EXPERIENCE_FIELD, COOKING_TIME_FIELD,
    CampfireResultRecipe::new);

  public CampfireResultRecipe(Identifier id, String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookingTime) {
    super(group, category, ingredient, result, experience, cookingTime);
  }

  @Override
  public RecipeSerializer<? extends AbstractCookingRecipe> getSerializer() {
    return MantleRecipes.CAMPFIRE.get();
  }

  @Override
  public RecipeType<? extends AbstractCookingRecipe> getType() {
    return RecipeType.CAMPFIRE_COOKING;
  }

  @Override
  protected Item furnaceIcon() {
    return Items.CAMPFIRE;
  }

  @Override
  public RecipeBookCategory recipeBookCategory() {
    return RecipeBookCategories.CAMPFIRE;
  }
}
