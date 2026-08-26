package slimeknights.mantle.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;

/**
 * Extension of {@link Recipe} to set some methods that always set.
 * @param <C>  Input type
 */
public interface ICommonRecipe<C extends RecipeInput> extends Recipe<C> {
  @Override
  default ItemStack assemble(C inv) {
    return ItemStack.EMPTY;
  }

  @Override
  default PlacementInfo placementInfo() {
    return PlacementInfo.NOT_PLACEABLE;
  }

  @Override
  default RecipeBookCategory recipeBookCategory() {
    return RecipeBookCategories.CRAFTING_MISC;
  }

  @Override
  default String group() {
    return "";
  }

  @Override
  default boolean showNotification() {
    return false;
  }

  @Override
  default boolean isSpecial() {
    return true;
  }
}