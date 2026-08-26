package slimeknights.mantle.plugin.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;

import java.util.List;

/** JEI crafting extension for {@link ShapedRetexturedRecipe}. */
public class RetexturableRecipeExtension implements ICraftingCategoryExtension<ShapedRetexturedRecipe> {
  @Override
  public List<SlotDisplay> getIngredients(RecipeHolder<ShapedRetexturedRecipe> recipe) {
    return recipe.value().getIngredients().stream().map(Ingredient::optionalIngredientToDisplay).toList();
  }

  @Override
  public int getWidth(RecipeHolder<ShapedRetexturedRecipe> recipe) {
    return recipe.value().getWidth();
  }

  @Override
  public int getHeight(RecipeHolder<ShapedRetexturedRecipe> recipe) {
    return recipe.value().getHeight();
  }

  @Override
  public void setRecipe(RecipeHolder<ShapedRetexturedRecipe> recipe, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    ICraftingCategoryExtension.super.setRecipe(recipe, builder, craftingGridHelper, focuses);
  }
}